package io.unitycatalog.server.base.table;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.unitycatalog.client.ApiException;
import io.unitycatalog.client.model.*;
import io.unitycatalog.server.base.BaseCRUDTest;
import io.unitycatalog.server.base.ServerConfig;
import io.unitycatalog.server.base.schema.SchemaOperations;
import io.unitycatalog.server.persist.dao.ColumnInfoDAO;
import io.unitycatalog.server.persist.dao.TableInfoDAO;
import io.unitycatalog.server.persist.utils.FileUtils;
import io.unitycatalog.server.persist.utils.HibernateUtils;
import io.unitycatalog.server.utils.TestUtils;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class BaseTableCRUDTest extends BaseCRUDTest {

  protected SchemaOperations schemaOperations;
  protected TableOperations tableOperations;
  private String schemaId;

  protected abstract SchemaOperations createSchemaOperations(ServerConfig serverConfig);

  protected abstract TableOperations createTableOperations(ServerConfig serverConfig);

  @BeforeEach
  @Override
  public void setUp() {
    super.setUp();
    schemaOperations = createSchemaOperations(serverConfig);
    tableOperations = createTableOperations(serverConfig);
  }

  protected void createCommonResources() throws ApiException {
    CreateCatalog createCatalog =
        new CreateCatalog().name(TestUtils.CATALOG_NAME).comment(TestUtils.COMMENT);
    catalogOperations.createCatalog(createCatalog);

    SchemaInfo schemaInfo =
        schemaOperations.createSchema(
            new CreateSchema().name(TestUtils.SCHEMA_NAME).catalogName(TestUtils.CATALOG_NAME));
    schemaId = schemaInfo.getSchemaId();
  }

  @Test
  public void testTableCRUD() throws IOException, ApiException {
    assertThrows(Exception.class, () -> tableOperations.getTable(TestUtils.TABLE_FULL_NAME));
    createCommonResources();

    // Create and verify a table
    TableInfo createdTable = createAndVerifyTable();

    // Get table and verify columns
    TableInfo retrievedTable = tableOperations.getTable(TestUtils.TABLE_FULL_NAME);
    verifyTableInfo(retrievedTable, createdTable);

    // Create multiple tables and verify pagination
    List<TableInfo> createdTables = createMultipleTestingTables(111);
    verifyTablePagination();
    // Sort and verify tables
    verifyTableSorting();
    // Clean up list tables
    cleanUpTables(createdTables);

    // Test delete table functionality
    testDeleteTable();

    // Test managed table retrieval
    testManagedTableRetrieval();

    // Test schema update and deletion scenarios
    testTableAfterSchemaUpdateAndDeletion();
  }

  private TableInfo createAndVerifyTable() throws IOException, ApiException {
    TableInfo tableInfo = createTestingTable(TestUtils.TABLE_NAME, TestUtils.STORAGE_LOCATION);
    assertEquals(TestUtils.TABLE_NAME, tableInfo.getName());
    assertEquals(TestUtils.CATALOG_NAME, tableInfo.getCatalogName());
    assertEquals(TestUtils.SCHEMA_NAME, tableInfo.getSchemaName());
    assertNotNull(tableInfo.getTableId());
    return tableInfo;
  }

  private void verifyTableInfo(TableInfo retrievedTable, TableInfo expectedTable) {
    assertEquals(expectedTable, retrievedTable);
    Collection<ColumnInfo> columns = retrievedTable.getColumns();
    assertEquals(2, columns.size());
    assertTrue(columns.stream().anyMatch(c -> c.getName().equals("as_int")));
    assertTrue(columns.stream().anyMatch(c -> c.getName().equals("as_string")));
  }

  private void verifyTablePagination() throws ApiException {
    Iterable<TableInfo> tables =
        tableOperations.listTables(TestUtils.CATALOG_NAME, TestUtils.SCHEMA_NAME);
    assertEquals(100, TestUtils.getSize(tables));
  }

  private void verifyTableSorting() throws ApiException {
    Iterable<TableInfo> tables =
        tableOperations.listTables(TestUtils.CATALOG_NAME, TestUtils.SCHEMA_NAME);
    List<TableInfo> sortedTables = new ArrayList<>();
    tables.forEach(sortedTables::add);

    List<String> tableNames =
        sortedTables.stream().map(TableInfo::getName).collect(Collectors.toList());

    List<String> sortedTableNames = new ArrayList<>(tableNames);
    Collections.sort(sortedTableNames);

    assertEquals(sortedTableNames, tableNames);
  }

  private void cleanUpTables(List<TableInfo> tables) {
    tables.forEach(
        table -> {
          try {
            tableOperations.deleteTable(
                TestUtils.CATALOG_NAME + "." + TestUtils.SCHEMA_NAME + "." + table.getName());
          } catch (ApiException e) {
            fail("Failed to delete table: " + e.getMessage());
          }
        });
  }

  private void testDeleteTable() throws ApiException {
    tableOperations.deleteTable(TestUtils.TABLE_FULL_NAME);
    assertThrows(Exception.class, () -> tableOperations.getTable(TestUtils.TABLE_FULL_NAME));
  }

  private void testManagedTableRetrieval() throws ApiException {
    try (Session session = HibernateUtils.getSessionFactory().openSession()) {
      Transaction tx = session.beginTransaction();
      UUID tableId = UUID.randomUUID();

      TableInfoDAO tableInfoDAO = createManagedTableDAO(tableId);
      session.persist(tableInfoDAO);
      session.flush();
      tx.commit();
    } catch (Exception e) {
      fail("Failed to set up managed table: " + e.getMessage());
    }

    TableInfo managedTable = tableOperations.getTable(TestUtils.TABLE_FULL_NAME);
    assertEquals(TestUtils.TABLE_NAME, managedTable.getName());
    assertEquals(TestUtils.CATALOG_NAME, managedTable.getCatalogName());
    assertEquals(TestUtils.SCHEMA_NAME, managedTable.getSchemaName());
    assertEquals(
        FileUtils.convertRelativePathToURI("/tmp/managedStagingLocation"),
        managedTable.getStorageLocation());
    assertEquals(TableType.MANAGED, managedTable.getTableType());
    assertEquals(DataSourceFormat.DELTA, managedTable.getDataSourceFormat());
    assertNotNull(managedTable.getCreatedAt());
    assertNotNull(managedTable.getTableId());
  }

  private TableInfoDAO createManagedTableDAO(UUID tableId) {
    TableInfoDAO tableInfoDAO =
        TableInfoDAO.builder()
            .name(TestUtils.TABLE_NAME)
            .schemaId(UUID.fromString(schemaId))
            .comment(TestUtils.COMMENT)
            .url("/tmp/managedStagingLocation")
            .type(TableType.MANAGED.name())
            .dataSourceFormat(DataSourceFormat.DELTA.name())
            .id(tableId)
            .createdAt(new Date())
            .updatedAt(new Date())
            .build();

    ColumnInfoDAO columnInfoDAO1 =
        ColumnInfoDAO.builder()
            .id(UUID.randomUUID())
            .name("as_int")
            .typeText("INTEGER")
            .typeJson("{\"type\": \"integer\"}")
            .typeName(ColumnTypeName.INT.name())
            .typePrecision(10)
            .typeScale(0)
            .ordinalPosition((short) 0)
            .comment("Integer column")
            .nullable(true)
            .table(tableInfoDAO)
            .build();

    ColumnInfoDAO columnInfoDAO2 =
        ColumnInfoDAO.builder()
            .id(UUID.randomUUID())
            .name("as_string")
            .typeText("VARCHAR(255)")
            .typeJson("{\"type\": \"string\", \"length\": \"255\"}")
            .typeName(ColumnTypeName.STRING.name())
            .ordinalPosition((short) 1)
            .comment("String column")
            .nullable(true)
            .table(tableInfoDAO)
            .build();

    tableInfoDAO.setColumns(List.of(columnInfoDAO1, columnInfoDAO2));
    return tableInfoDAO;
  }

  private void testTableAfterSchemaUpdateAndDeletion() throws ApiException {
    schemaOperations.updateSchema(
        TestUtils.SCHEMA_FULL_NAME,
        new UpdateSchema().newName(TestUtils.SCHEMA_NEW_NAME).comment(TestUtils.SCHEMA_COMMENT));

    TableInfo tableAfterSchemaUpdate =
        tableOperations.getTable(
            TestUtils.CATALOG_NAME + "." + TestUtils.SCHEMA_NEW_NAME + "." + TestUtils.TABLE_NAME);
    assertEquals(tableAfterSchemaUpdate.getTableId(), tableAfterSchemaUpdate.getTableId());

    assertThrows(
        Exception.class,
        () ->
            schemaOperations.deleteSchema(
                TestUtils.CATALOG_NAME + "." + TestUtils.SCHEMA_NEW_NAME, Optional.of(false)));

    String newTableFullName =
        TestUtils.CATALOG_NAME + "." + TestUtils.SCHEMA_NEW_NAME + "." + TestUtils.TABLE_NAME;
    schemaOperations.deleteSchema(
        TestUtils.CATALOG_NAME + "." + TestUtils.SCHEMA_NEW_NAME, Optional.of(true));
    assertThrows(Exception.class, () -> tableOperations.getTable(newTableFullName));
    assertThrows(
        Exception.class,
        () -> schemaOperations.getSchema(TestUtils.CATALOG_NAME + "." + TestUtils.SCHEMA_NEW_NAME));
  }

  protected TableInfo createTestingTable(String tableName, String storageLocation)
      throws IOException, ApiException {
    ColumnInfo columnInfo1 =
        new ColumnInfo()
            .name("as_int")
            .typeText("INTEGER")
            .typeJson("{\"type\": \"integer\"}")
            .typeName(ColumnTypeName.INT)
            .typePrecision(10)
            .typeScale(0)
            .position(0)
            .comment("Integer column")
            .nullable(true);

    ColumnInfo columnInfo2 =
        new ColumnInfo()
            .name("as_string")
            .typeText("VARCHAR(255)")
            .typeJson("{\"type\": \"string\", \"length\": \"255\"}")
            .typeName(ColumnTypeName.STRING)
            .position(1)
            .comment("String column")
            .nullable(true);

    CreateTable createTableRequest =
        new CreateTable()
            .name(tableName)
            .catalogName(TestUtils.CATALOG_NAME)
            .schemaName(TestUtils.SCHEMA_NAME)
            .columns(List.of(columnInfo1, columnInfo2))
            .properties(TestUtils.PROPERTIES)
            .comment(TestUtils.COMMENT)
            .storageLocation(storageLocation)
            .tableType(TableType.EXTERNAL)
            .dataSourceFormat(DataSourceFormat.DELTA);

    return tableOperations.createTable(createTableRequest);
  }

  protected List<TableInfo> createMultipleTestingTables(int numberOfTables)
      throws IOException, ApiException {
    List<TableInfo> createdTables = new ArrayList<>();
    for (int i = numberOfTables; i > 0; i--) {
      String tableName = TestUtils.TABLE_NAME + "_" + i;
      String storageLocation = TestUtils.STORAGE_LOCATION + "/" + tableName;
      createdTables.add(createTestingTable(tableName, storageLocation));
    }
    return createdTables;
  }
}
