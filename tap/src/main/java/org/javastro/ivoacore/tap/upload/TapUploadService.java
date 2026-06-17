package org.javastro.ivoacore.tap.upload;

import org.ivoa.dm.tapschema.Column;
import org.ivoa.dm.tapschema.Schema;
import org.ivoa.dm.tapschema.Table;
import org.javastro.ivoacore.tap.schema.MetadataTransformer;
import org.javastro.ivoacore.tap.schema.TapADQLTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StarTableFactory;
import uk.ac.starlink.table.jdbc.JDBCFormatter;
import uk.ac.starlink.table.jdbc.WriteMode;
import uk.ac.starlink.votable.VOTableBuilder;

import javax.sql.DataSource;
import java.io.InputStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Pattern;

/**
 * A service class responsible for handling TAP (Table Access Protocol) upload operations.
 * This class provides methods for processing uploads, managing upload tables, and
 * ensuring proper metadata and schema creation in a TAP-compatible database.
 */
public class TapUploadService {

    /**
     * Encapsulates metadata and context information relevant to a specific upload operation.
     * This record is utilised to track details of a table upload, particularly within the
     * context of a TAP (Table Access Protocol) service.
     * <p>
     * The UploadContext holds:
     * - The logical name of the table, typically representing the name as referred to in the context
     *   of a job or user interaction.
     * - The physical name of the table, representing how the table is stored in the database.
     * - The associated {@link TapADQLTable}, which contains metadata about the table, including
     *   its schema, columns, and other attributes, in the context of ADQL-based queries.
     */
    public record UploadContext(
            String logicalTableName,
            String physicalTableName,
            TapADQLTable adqlTable) {
    }

    private final DataSource dataSource;
    private final Logger log = LoggerFactory.getLogger(TapUploadService.class);
    //Table names must conform to the pattern.
    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    /**
     * Constructs a new TapUploadService instance with the specified data source.
     *
     * @param dataSource the JDBC data source to be used by this service for managing
     *                   upload-related operations.
     */
    public TapUploadService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Processes an upload operation by creating the necessary database structures
     * and metadata and returns the context of the upload.
     * <p>
     * The method performs the following steps:
     * 1. Opens an input stream to the provided upload URI.
     * 2. Parses the input stream into a {@code StarTable}.
     * 3. Ensures that the necessary destination database schema exists.
     * 4. Creates and populates a database table for the uploaded data.
     * 5. Generates metadata for ADQL (Astronomical Data Query Language) queries.
     *
     * @param uploads The location of the VOTable file to be uploaded and its name.
     * @param jobId the unique identifier for the job associated with this upload.
     * @param schemaName the name of the database schema where the upload table will be created.
     * @return an {@code UploadContext} object containing metadata and contextual information
     *         for the created upload table.
     * @throws RuntimeException if an error occurs during the upload process, such as issues with
     *                   the upload URI, database connection, schema creation, or table population.
     */
    public List<UploadContext> processUpload(Map<String, URI> uploads, String jobId, String schemaName) throws RuntimeException{
        if (uploads == null || uploads.isEmpty()) {
            throw new IllegalArgumentException("Upload values must be provided");
        }

        List<UploadContext> uploadContexts = new ArrayList<>();

        uploads.forEach((name, uri) -> {
            //Defined against SQL injection as the table name will be added to the query
            validateTableName(name);

            try (InputStream in = uri.toURL().openStream();
                 Connection conn = dataSource.getConnection()) {

                StarTable table = new StarTableFactory().makeStarTable(in, new VOTableBuilder());
                table.setName(name);    //overwrite the VOTable name with the name from the param.

                ensureUploadSchemaExists(conn, schemaName);

                String physicalTableName = createAndPopulateUploadTable(conn, table, jobId, schemaName);

                TapADQLTable adqlTable = createUploadMetadata(table, schemaName);

                uploadContexts.add(new UploadContext(table.getName(), physicalTableName, adqlTable));
            } catch (Exception e) {
                //Split exception handling?
                throw new RuntimeException(e);
            }
        });

        return uploadContexts;
    }

    /**
     * Cleans up the upload table by dropping it from the database if it exists.
     * This method ensures that resources used for the specific upload are released.
     *
     * @param uploads the context of the upload operation. Contains metadata about
     *               the upload, including the logical and physical table names.
     *               Passing a {@code null} value will result in no action.
     */
    @SuppressWarnings("SqlSourceToSinkFlow")
    public void cleanupUploadTable(List<UploadContext> uploads) {
        if (uploads == null) {
            return;
        }

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            for (UploadContext upload : uploads) {
                try {
                    //Warning suppressed as physicalTableName currently generated internally.
                    stmt.execute("DROP TABLE IF EXISTS " + upload.physicalTableName());
                } catch (SQLException e) {
                    log.warn("Failed to drop upload table {}", upload.physicalTableName(), e);
                }
            }
        } catch (SQLException e) {
            log.warn("Failed to obtain connection or statement for cleanup", e);
        }
    }

    /**
     * Ensures that the "TAP_UPLOAD" schema exists in the database, creating it if necessary.
     * This schema is used to store tables and metadata related to upload operations.
     *
     * @param conn the active database connection used to execute the schema creation statement
     * @throws SQLException if a database access error occurs while attempting to create the schema
     */
    private void ensureUploadSchemaExists(Connection conn, String schemaName)
            throws SQLException {

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(
                    String.format("CREATE SCHEMA IF NOT EXISTS \"%s\"", schemaName)
            );
        }
    }

    /**
     * Creates an upload table in the database and populates it with the data
     * from the specified {@code StarTable} for the given job identifier.
     * <p>
     * This method constructs a logical and corresponding physical name for the upload table,
     * creates the table in the database, and formats it to match the database schema.
     * The table is populated with the provided data using a JDBC formatter.
     *
     * @param conn the active database connection used to execute table creation and population.
     * @param uploadTable the {@code StarTable} object containing the data to populate in the table.
     * @param jobId a unique identifier for the job, which is used as part of the constructed table name.
     * @return the physical table name created in the database.
     * @throws Exception if any error occurs during table creation, formatting, or population.
     */
    private String createAndPopulateUploadTable(Connection conn, StarTable uploadTable, String jobId, String schemaName) throws Exception {

        //Table naming format = tap_upload_<jobId>_<table name>
        String tableName = "tap_upload_" + jobId.replace("-", "_") + "_" + uploadTable.getName();

        //Quoted due to postgres issues with capitalised schema names (TAP_UPLOAD)
        String physicalTableName = "\"" + schemaName + "\".\"" + tableName + "\"";

        createUploadTable(conn, uploadTable, physicalTableName);

        JDBCFormatter formatter = new JDBCFormatter(conn, uploadTable);

        formatter.createJDBCTable(physicalTableName, WriteMode.APPEND);

        return physicalTableName;
    }

    /**
     * Creates metadata for an upload table by transforming the input {@code StarTable}
     * into a {@code TapADQLTable} with the appropriate schema, table, and column metadata.
     *
     * @param uploadTable the {@code StarTable} containing data and metadata about the
     *                    upload table, including its columns and their types.
     * @return a {@code TapADQLTable} representing the metadata for the specified
     *         upload table, including schema and column definitions.
     */
    private TapADQLTable createUploadMetadata(StarTable uploadTable, String schemaName) {

        Schema schema = new Schema();
        schema.setSchema_name(schemaName.toUpperCase());

        Table table = new Table();
        table.setTable_name(uploadTable.getName());

        TapADQLTable result = new TapADQLTable(schema, table,false);

        for (int i = 0; i < uploadTable.getColumnCount(); i++) {
            ColumnInfo info = uploadTable.getColumnInfo(i);

            Column column = new Column();
            column.setColumn_name(info.getName().toUpperCase());

            column.setDatatype(
                    MetadataTransformer.mapContentClassToTAPType(info.getContentClass()));

            result.createColumn(column);
        }

        return result;
    }

    /**
     * Creates an upload table in the database using the specified {@code StarTable} structure
     * and assigns it the provided physical name. This method constructs the SQL statement
     * necessary for table creation and executes it on the given database connection.
     *
     * @param conn the active database connection used to execute the SQL statement.
     * @param table the {@code StarTable} object containing the structure and metadata
     *              for the table to be created.
     * @param physicalName the fully qualified physical name of the table to be created
     *                     in the database, including schema and table name.
     * @throws SQLException if a database access error or SQL execution error occurs
     *                      during table creation.
     */
    private void createUploadTable(Connection conn, StarTable table, String physicalName) throws SQLException {

        String sql = buildCreateTableSql(table, physicalName);

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * Builds a SQL statement for creating a database table based on the structure
     * of the provided {@code StarTable} and assigns it the specified physical name.
     * This method constructs the CREATE TABLE SQL statement by iterating through
     * the table's columns, determining their SQL data types, and formatting them
     * appropriately.
     *
     * @param table the {@code StarTable} object containing metadata about the table
     *              structure, including column names and data types.
     * @param physicalName the fully qualified name for the physical database table,
     *                     including schema and table name as applicable.
     * @return a SQL string representing the CREATE TABLE statement for the
     *         specified {@code StarTable} structure and physical table name.
     */
    private String buildCreateTableSql(StarTable table, String physicalName) {

        StringJoiner columns = new StringJoiner(", ");

        for (int i = 0; i < table.getColumnCount(); i++) {
            ColumnInfo info = table.getColumnInfo(i);

            columns.add(
                    "\"" + info.getName().toUpperCase() + "\" "
                            + mapContentClassToSqlType(
                            info.getContentClass()));
        }

        return "CREATE TABLE IF NOT EXISTS "
                + physicalName
                + " ("
                + columns
                + ")";
    }

    /**
     * Maps a Java class type (from STIL ColumnInfo.getContentClass()) to a PostgreSQL SQL type string.
     * Delegates to MetadataTransformer to ensure a single source of truth for type mappings.
     *
     * @param contentClass the Java class representing the column data type
     * @return a PostgreSQL SQL type string (e.g., "VARCHAR", "BIGINT", "DOUBLE PRECISION")
     */
    private String mapContentClassToSqlType(Class<?> contentClass) {
        // Delegate: first map the Java class to a TAPType, then map TAPType to SQL type.
        var tapType = MetadataTransformer.mapContentClassToTAPType(contentClass);
        return MetadataTransformer.mapTAPTypeToSqlType(tapType);
    }

    /**
     * Validates the format of the given table name to ensure it adheres to the expected pattern.
     * Throws an {@link IllegalStateException} if the table name is null or does not match
     * the predefined regular expression pattern.
     *
     * @param name the table name to validate. Must not be null and must conform
     *             to the pattern defined by {@code TABLE_NAME_PATTERN}.
     * @throws IllegalStateException if the table name is null or invalid.
     */
    private void validateTableName(String name) {
        if (name == null || !TABLE_NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalStateException("Invalid table name format: " + name);
        }
    }
}
