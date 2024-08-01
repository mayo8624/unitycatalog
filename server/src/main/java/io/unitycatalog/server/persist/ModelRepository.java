package io.unitycatalog.server.persist;

import io.unitycatalog.server.exception.BaseException;
import io.unitycatalog.server.exception.ErrorCode;
import io.unitycatalog.server.model.*;
import io.unitycatalog.server.persist.dao.PropertyDAO;
import io.unitycatalog.server.persist.dao.SchemaInfoDAO;
import io.unitycatalog.server.persist.dao.RegisteredModelInfoDAO;
import io.unitycatalog.server.persist.utils.FileUtils;
import io.unitycatalog.server.persist.utils.HibernateUtils;
import io.unitycatalog.server.persist.utils.PagedListingHelper;
import io.unitycatalog.server.persist.utils.RepositoryUtils;
import io.unitycatalog.server.utils.Constants;
import io.unitycatalog.server.utils.ValidationUtils;
import java.util.*;
import java.util.stream.Collectors;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelRepository {
    private static final ModelRepository INSTANCE = new ModelRepository();
    private static final Logger LOGGER = LoggerFactory.getLogger(TableRepository.class);
    private static final SessionFactory SESSION_FACTORY = HibernateUtils.getSessionFactory();
    private static final SchemaRepository SCHEMA_REPOSITORY = SchemaRepository.getInstance();
    private static final PagedListingHelper<RegisteredModelInfoDAO> LISTING_HELPER =
            new PagedListingHelper<>(RegisteredModelInfoDAO.class);

    private ModelRepository() {}

    public static ModelRepository getInstance() {
        return INSTANCE;
    }

    public RegisteredModelInfo getRegisteredModelById(String registeredModelId) {
        LOGGER.debug("Getting registered model by id: " + registeredModelId);
        try (Session session = SESSION_FACTORY.openSession()) {
            session.setDefaultReadOnly(true);
            Transaction tx = session.beginTransaction();
            try {
                RegisteredModelInfoDAO registeredModelInfoDAO = session.get(RegisteredModelInfoDAO.class, UUID.fromString(registeredModelId));
                if (registeredModelInfoDAO == null) {
                    throw new BaseException(ErrorCode.NOT_FOUND, "Registered model not found: " + registeredModelId);
                }
                RegisteredModelInfo registeredModelInfo = registeredModelInfoDAO.toRegisteredModelInfo();
                tx.commit();
                return registeredModelInfo;
            } catch (Exception e) {
                if (tx != null && tx.getStatus().canRollback()) {
                    tx.rollback();
                }
                throw e;
            }
        }
    }

    public RegisteredModelInfo getRegisteredModel(String fullName) {
        LOGGER.debug("Getting registered model: " + fullName);
        RegisteredModelInfo registeredModelInfo = null;
        try (Session session = SESSION_FACTORY.openSession()) {
            session.setDefaultReadOnly(true);
            Transaction tx = session.beginTransaction();
            try {
                String[] parts = fullName.split("\\.");
                if (parts.length != 3) {
                    throw new BaseException(ErrorCode.INVALID_ARGUMENT, "Invalid registered model name: " + fullName);
                }
                String catalogName = parts[0];
                String schemaName = parts[1];
                String registeredModelName = parts[2];
                RegisteredModelInfoDAO registeredModelInfoDAO = findRegisteredModel(session, catalogName, schemaName, registeredModelName);
                if (registeredModelInfoDAO == null) {
                    throw new BaseException(ErrorCode.NOT_FOUND, "Registered model not found: " + fullName);
                }
                registeredModelInfo = registeredModelInfoDAO.toRegisteredModelInfo();
                registeredModelInfo.setCatalogName(catalogName);
                registeredModelInfo.setSchemaName(schemaName);
                tx.commit();
                return registeredModelInfo;
            } catch (Exception e) {
                if (tx != null && tx.getStatus().canRollback()) {
                    tx.rollback();
                }
                throw e;
            }
        }
    }

    private RegisteredModelInfoDAO findRegisteredModel(
            Session session, String catalogName, String schemaName, String registeredModelName) {
        UUID schemaId = getSchemaId(session, catalogName, schemaName);
        return findBySchemaIdAndName(session, schemaId, registeredModelName);
    }

    public RegisteredModelInfo createRegisteredModel(CreateRegisteredModel createRegisteredModel) {
        ValidationUtils.validateSqlObjectName(createRegisteredModel.getModelName());
        RegisteredModelInfo registeredModelInfo =
                new RegisteredModelInfo()
                        .modelId(UUID.randomUUID().toString())
                        .name(createRegisteredModel.getModelName())
                        .catalogName(createRegisteredModel.getCatalogName())
                        .schemaName(createRegisteredModel.getSchemaName())
                        .storageLocation(FileUtils.convertRelativePathToURI(createRegisteredModel.getStorageLocation()))
                        .comment(createRegisteredModel.getComment())
                        .createdAt(System.currentTimeMillis());
        String fullName = getRegisteredModelFullName(registeredModelInfo);
        LOGGER.debug("Creating Registered Model: " + fullName);

        Transaction tx;
        try (Session session = SESSION_FACTORY.openSession()) {
            String catalogName = registeredModelInfo.getCatalogName();
            String schemaName = registeredModelInfo.getSchemaName();
            UUID schemaId = getSchemaId(session, catalogName, schemaName);
            tx = session.beginTransaction();

            try {
                // Check if registered model already exists
                RegisteredModelInfoDAO existingRegisteredModel = findBySchemaIdAndName(session, schemaId, registeredModelInfo.getName());
                if (existingRegisteredModel != null) {
                    throw new BaseException(ErrorCode.ALREADY_EXISTS, "Registered model already exists: " + fullName);
                }
                RegisteredModelInfoDAO registeredModelInfoDAO = RegisteredModelInfoDAO.from(registeredModelInfo);
                registeredModelInfoDAO.setSchemaId(schemaId);
                session.persist(registeredModelInfoDAO);
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
            throw new BaseException(ErrorCode.INTERNAL, "Error creating registered model: " + fullName, e);
        }
        return registeredModelInfo;
    }

    public RegisteredModelInfoDAO findBySchemaIdAndName(Session session, UUID schemaId, String name) {
        String hql = "FROM RegisteredModelInfoDAO t WHERE t.schemaId = :schemaId AND t.name = :name";
        Query<RegisteredModelInfoDAO> query = session.createQuery(hql, RegisteredModelInfoDAO.class);
        query.setParameter("schemaId", schemaId);
        query.setParameter("name", name);
        LOGGER.debug("Finding registered model by schemaId: " + schemaId + " and name: " + name);
        return query.uniqueResult(); // Returns null if no result is found
    }

    private String getRegisteredModelFullName(RegisteredModelInfo registeredModelInfo) {
        return registeredModelInfo.getCatalogName() + "." + registeredModelInfo.getSchemaName() + "." + registeredModelInfo.getName();
    }

    public UUID getSchemaId(Session session, String catalogName, String schemaName) {
        SchemaInfoDAO schemaInfo = SCHEMA_REPOSITORY.getSchemaDAO(session, catalogName, schemaName);
        if (schemaInfo == null) {
            throw new BaseException(ErrorCode.NOT_FOUND, "Schema not found: " + schemaName);
        }
        return schemaInfo.getId();
    }

    /**
     * Return the list of tables in ascending order of table name.
     *
     * @param catalogName
     * @param schemaName
     * @param maxResults
     * @param pageToken
     * @return
     */
    public ListRegisteredModelsResponse listRegisteredModels(
            String catalogName,
            String schemaName,
            Optional<Integer> maxResults,
            Optional<String> pageToken) {
        try (Session session = SESSION_FACTORY.openSession()) {
            session.setDefaultReadOnly(true);
            Transaction tx = session.beginTransaction();
            try {
                UUID schemaId = getSchemaId(session, catalogName, schemaName);
                ListRegisteredModelsResponse response =
                        listRegisteredModels(
                                session,
                                schemaId,
                                catalogName,
                                schemaName,
                                maxResults,
                                pageToken);
                tx.commit();
                return response;
            } catch (Exception e) {
                if (tx != null && tx.getStatus().canRollback()) {
                    tx.rollback();
                }
                throw e;
            }
        }
    }

    public ListRegisteredModelsResponse listRegisteredModels(
            Session session,
            UUID schemaId,
            String catalogName,
            String schemaName,
            Optional<Integer> maxResults,
            Optional<String> pageToken) {
        List<RegisteredModelInfoDAO> registeredModelInfoDAOList =
                LISTING_HELPER.listEntity(session, maxResults, pageToken, schemaId);
        String nextPageToken = LISTING_HELPER.getNextPageToken(registeredModelInfoDAOList, maxResults);
        List<RegisteredModelInfo> result = new ArrayList<>();
        for (RegisteredModelInfoDAO registeredModelInfoDAO : registeredModelInfoDAOList) {
            RegisteredModelInfo registeredModelInfo = registeredModelInfoDAO.toRegisteredModelInfo();
            registeredModelInfo.setCatalogName(catalogName);
            registeredModelInfo.setSchemaName(schemaName);
            result.add(registeredModelInfo);
        }
        return new ListRegisteredModelsResponse().registeredModels(result).nextPageToken(nextPageToken);
    }

    public void deleteRegisteredModel(String fullName) {
        try (Session session = SESSION_FACTORY.openSession()) {
            Transaction tx = session.beginTransaction();
            String[] parts = fullName.split("\\.");
            if (parts.length != 3) {
                throw new BaseException(ErrorCode.INVALID_ARGUMENT, "Invalid registered model name: " + fullName);
            }
            String catalogName = parts[0];
            String schemaName = parts[1];
            String registeredModelName = parts[2];
            try {
                UUID schemaId = getSchemaId(session, catalogName, schemaName);
                deleteRegisteredModel(session, schemaId, registeredModelName);
                tx.commit();
            } catch (RuntimeException e) {
                if (tx != null && tx.getStatus().canRollback()) {
                    tx.rollback();
                }
                throw e;
            }
        }
    }

    public void deleteRegisteredModel(Session session, UUID schemaId, String registeredModelName) {
        RegisteredModelInfoDAO registeredModelInfoDAO = findBySchemaIdAndName(session, schemaId, registeredModelName);
        if (registeredModelInfoDAO == null) {
            throw new BaseException(ErrorCode.NOT_FOUND, "Registered model not found: " + registeredModelName);
        }
        session.remove(registeredModelInfoDAO);
    }
}
