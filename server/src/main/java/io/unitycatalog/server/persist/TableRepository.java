package io.unitycatalog.server.persist;

import io.unitycatalog.server.exception.BaseException;
import io.unitycatalog.server.exception.ErrorCode;
import io.unitycatalog.server.model.CreateTable;
import io.unitycatalog.server.model.ListTablesResponse;
import io.unitycatalog.server.model.TableInfo;
import io.unitycatalog.server.model.TableType;
import io.unitycatalog.server.persist.dao.SchemaInfoDAO;
import io.unitycatalog.server.persist.dao.TableInfoDAO;
import io.unitycatalog.server.utils.ValidationUtils;
import lombok.Getter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class TableRepository {
    @Getter
    private static final TableRepository instance = new TableRepository();
    private static final Logger LOGGER = LoggerFactory.getLogger(TableRepository.class);
    private static final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
    private static final CatalogRepository catalogOperations = CatalogRepository.getInstance();
    private static final SchemaRepository schemaOperations = SchemaRepository.getInstance();

    private TableRepository() {}

    public TableInfo getTableById(String tableId) {
        LOGGER.debug("Getting table by id: " + tableId);
        try (Session session = sessionFactory.openSession()) {
            session.setDefaultReadOnly(true);
            Transaction tx = session.beginTransaction();
            try {
                TableInfoDAO tableInfoDAO = session.get(TableInfoDAO.class, UUID.fromString(tableId));
                if (tableInfoDAO == null) {
                    throw new BaseException(ErrorCode.NOT_FOUND, "Table not found: " + tableId);
                }
                TableInfo tableInfo = tableInfoDAO.toTableInfo();
                tx.commit();
                return tableInfo;
            } catch (Exception e) {
                tx.rollback();
                throw e;
            }
        }
    }

    public TableInfo getTable(String fullName) {
        LOGGER.debug("Getting table: " + fullName);
        TableInfo tableInfo = null;
        try (Session session = sessionFactory.openSession()) {
            session.setDefaultReadOnly(true);
            Transaction tx = session.beginTransaction();
            try {
                String[] parts = fullName.split("\\.");
                if (parts.length != 3) {
                    throw new BaseException(ErrorCode.INVALID_ARGUMENT, "Invalid table name: " + fullName);
                }
                String catalogName = parts[0];
                String schemaName = parts[1];
                String tableName = parts[2];
                TableInfoDAO tableInfoDAO = findTable(session, catalogName, schemaName, tableName);
                if (tableInfoDAO == null) {
                    throw new BaseException(ErrorCode.NOT_FOUND, "Table not found: " + fullName);
                }
                tableInfo = tableInfoDAO.toTableInfo();
                tableInfo.setCatalogName(catalogName);
                tableInfo.setSchemaName(schemaName);
                tx.commit();
            } catch (Exception e) {
                if (tx != null && tx.getStatus().canRollback()) {
                    tx.rollback();
                }
                throw e;
            }
        }
        return tableInfo;
    }

    public String getTableUniformMetadataLocation(Session session,  String catalogName, String schemaName, String tableName) {
        TableInfoDAO dao = findTable(session, catalogName, schemaName, tableName);
        return dao.getUniformIcebergMetadataLocation();
    }

    private TableInfoDAO findTable(Session session, String catalogName, String schemaName, String tableName) {
        String schemaId = getSchemaId(session, catalogName, schemaName);
        return findBySchemaIdAndName(session, schemaId, tableName);
    }


    public TableInfo createTable(CreateTable createTable) {
        ValidationUtils.validateSqlObjectName(createTable.getName());
        TableInfo tableInfo = new TableInfo()
                .tableId(UUID.randomUUID().toString())
                .name(createTable.getName())
                .catalogName(createTable.getCatalogName())
                .schemaName(createTable.getSchemaName())
                .tableType(createTable.getTableType())
                .dataSourceFormat(createTable.getDataSourceFormat())
                .columns(createTable.getColumns())
                .storageLocation(FileUtils.convertRelativePathToURI(createTable.getStorageLocation()))
                .comment(createTable.getComment())
                .properties(createTable.getProperties())
                .createdAt(System.currentTimeMillis());
        String fullName = getTableFullName(tableInfo);
        LOGGER.debug("Creating table: " + fullName);

        Transaction tx;
        try (Session session = sessionFactory.openSession()) {
            String catalogName = tableInfo.getCatalogName();
            String schemaName = tableInfo.getSchemaName();
            String schemaId = getSchemaId(session, catalogName, schemaName);
            tx = session.beginTransaction();

            try {
                // Check if table already exists
                TableInfoDAO existingTable = findBySchemaIdAndName(session, schemaId, tableInfo.getName());
                if (existingTable != null) {
                    throw new BaseException(ErrorCode.ALREADY_EXISTS, "Table already exists: " + fullName);
                }
                if (TableType.MANAGED.equals(tableInfo.getTableType())) {
                    throw new BaseException(ErrorCode.INVALID_ARGUMENT, "MANAGED table creation is not supported yet.");
                }
                // assuming external table
                if (tableInfo.getStorageLocation() == null) {
                    throw new BaseException(ErrorCode.INVALID_ARGUMENT,
                            "Storage location is required for external table");
                }
                TableInfoDAO tableInfoDAO = TableInfoDAO.from(tableInfo);
                tableInfoDAO.setSchemaId(UUID.fromString(schemaId));
                // create columns
                tableInfoDAO.getColumns().forEach(c -> c.setTable(tableInfoDAO));
                // create properties
                tableInfoDAO.getProperties().forEach(p -> p.setTable(tableInfoDAO));
                session.persist(tableInfoDAO);
                tx.commit();
            } catch (RuntimeException e) {
                if (tx != null && tx.getStatus().canRollback()) {
                    tx.rollback();
                }
                throw e;
            }
        } catch (RuntimeException e) {
            if (e instanceof BaseException) {
                throw e;
            }
            throw new BaseException(ErrorCode.INTERNAL, "Error creating table: " + fullName, e);
        }
        return tableInfo;
    }

    public TableInfoDAO findBySchemaIdAndName(Session session, String schemaId, String name) {
        String hql = "FROM TableInfoDAO t WHERE t.schemaId = :schemaId AND t.name = :name";
        Query<TableInfoDAO> query = session.createQuery(hql, TableInfoDAO.class);
        query.setParameter("schemaId", UUID.fromString(schemaId));
        query.setParameter("name", name);
        LOGGER.debug("Finding table by schemaId: " + schemaId + " and name: " + name);
        return query.uniqueResult();  // Returns null if no result is found
    }

    private String getTableFullName(TableInfo tableInfo) {
        return tableInfo.getCatalogName() + "." + tableInfo.getSchemaName() + "." + tableInfo.getName();
    }

    public String getSchemaId(Session session, String catalogName, String schemaName) {
        SchemaInfoDAO schemaInfo = schemaOperations.getSchemaDAO(session, catalogName, schemaName);
        if (schemaInfo == null) {
            throw new BaseException(ErrorCode.NOT_FOUND, "Schema not found: " + schemaName);
        }
        return schemaInfo.getId().toString();
    }

    public static Date convertMillisToDate(String millisString) {
        if (millisString == null || millisString.isEmpty()) {
            return null;
        }
        try {
            long millis = Long.parseLong(millisString);
            return new Date(millis);
        } catch (NumberFormatException e) {
            throw new BaseException(ErrorCode.INVALID_ARGUMENT, "Unable to interpret page token: " + millisString);
        }
    }

    public static String getNextPageToken(List<TableInfoDAO> tables) {
        if (tables == null || tables.isEmpty()) {
            return "";
        }
        // Assuming the last item in the list is the least recent based on the query
        long time = tables.get(tables.size() - 1).getCreatedAt().getTime();
        if (tables.get(tables.size() - 1).getUpdatedAt() != null)
            time = tables.get(tables.size() - 1).getUpdatedAt().getTime();
        return String.valueOf(time);
    }

    /**
     * Return the most recently updated tables first in descending order of updated time
     * @param catalogName
     * @param schemaName
     * @param maxResults
     * @param nextPageToken
     * @param omitProperties
     * @param omitColumns
     * @return
     */
    public ListTablesResponse listTables(String catalogName,
                                         String schemaName,
                                         Integer maxResults,
                                         String nextPageToken,
                                         Boolean omitProperties,
                                         Boolean omitColumns) {
        List<TableInfo> result = new ArrayList<>();
        String returnNextPageToken = null;
        String hql = "FROM TableInfoDAO t WHERE t.schemaId = :schemaId and " +
                "(t.updatedAt < :pageToken OR :pageToken is null) order by t.updatedAt desc";
        try (Session session = sessionFactory.openSession()) {
            session.setDefaultReadOnly(true);
            Transaction tx = session.beginTransaction();
            try {
                String schemaId = getSchemaId(session, catalogName, schemaName);
                Query<TableInfoDAO> query = session.createQuery(hql, TableInfoDAO.class);
                query.setParameter("schemaId", UUID.fromString(schemaId));
                query.setParameter("pageToken", convertMillisToDate(nextPageToken));
                query.setMaxResults(maxResults);
                List<TableInfoDAO> tableInfoDAOList = query.list();
                returnNextPageToken = getNextPageToken(tableInfoDAOList);
                for (TableInfoDAO tableInfoDAO : tableInfoDAOList) {
                    TableInfo tableInfo = tableInfoDAO.toTableInfo();
                    if (omitColumns) {
                        tableInfo.setColumns(null);
                    }
                    if (omitProperties) {
                        tableInfo.setProperties(null);
                    }
                    tableInfo.setCatalogName(catalogName);
                    tableInfo.setSchemaName(schemaName);
                    result.add(tableInfo);
                }
                tx.commit();
            } catch (Exception e) {
                if (tx != null && tx.getStatus().canRollback()) {
                    tx.rollback();
                }
                throw e;
            }
        }
        return new ListTablesResponse().tables(result).nextPageToken(returnNextPageToken);
    }

    public void deleteTable(String fullName) {

        try (Session session = sessionFactory.openSession()) {
            String[] parts = fullName.split("\\.");
            if (parts.length != 3) {
                throw new BaseException(ErrorCode.INVALID_ARGUMENT, "Invalid table name: " + fullName);
            }
            String catalogName = parts[0];
            String schemaName = parts[1];
            String tableName = parts[2];
            Transaction tx = session.beginTransaction();
            try {
                String schemaId = getSchemaId(session, catalogName, schemaName);
                TableInfoDAO tableInfoDAO = findBySchemaIdAndName(session, schemaId, tableName);
                if (tableInfoDAO == null) {
                    throw new BaseException(ErrorCode.NOT_FOUND, "Table not found: " + fullName);
                }
                if (TableType.MANAGED.getValue().equals(tableInfoDAO.getType())) {
                    try {
                        FileUtils.deleteDirectory(tableInfoDAO.getUrl());
                    } catch (Throwable e) {
                        LOGGER.error("Error deleting table directory: " + tableInfoDAO.getUrl());
                    }
                }
                session.remove(tableInfoDAO);
                tx.commit();
            } catch (RuntimeException e) {
                if (tx != null && tx.getStatus().canRollback()) {
                    tx.rollback();
                }
                throw e;
            }
        }

    }
}