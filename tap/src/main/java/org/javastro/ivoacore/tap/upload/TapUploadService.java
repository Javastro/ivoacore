package org.javastro.ivoacore.tap.upload;

import org.ivoa.dm.tapschema.Column;
import org.ivoa.dm.tapschema.Schema;
import org.ivoa.dm.tapschema.Table;
import org.javastro.ivoacore.tap.schema.MetadataTransformer;
import org.javastro.ivoacore.tap.schema.TapADQLTable;
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
import java.util.StringJoiner;

public class TapUploadService {

    public record UploadContext(
            String logicalTableName,
            String physicalTableName,
            TapADQLTable adqlTable) {
    }

    private final DataSource dataSource;

    public TapUploadService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UploadContext processUpload(String upload, String jobId) throws Exception {
        URI uploadUri = URI.create(upload);

        try (InputStream in = uploadUri.toURL().openStream();
             Connection conn = dataSource.getConnection()) {

            StarTable table = new StarTableFactory().makeStarTable(in, new VOTableBuilder());

            ensureUploadSchemaExists(conn);

            String physicalTableName = createAndPopulateUploadTable(conn, table, jobId);
            // table.setName(table.getName() + getID().replace("-", "_"));

            TapADQLTable adqlTable = createUploadMetadata(table);

            return new UploadContext(table.getName(), physicalTableName, adqlTable);
        }
    }

    public void cleanupUploadTable(UploadContext uploadContext) {
        // drop temp table
    }

    public String replaceUploadTableReferences(String sql, String physicalTableName) {
        // temporary solution until upload name handling is more robust
        return null;
    }

    private void ensureUploadSchemaExists(Connection conn)
            throws SQLException {

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
            CREATE SCHEMA IF NOT EXISTS "TAP_UPLOAD"
            """);
        }
    }

    private String createAndPopulateUploadTable(Connection conn, StarTable uploadTable, String jobId) throws Exception {

        String tableName = "tap_upload_" + jobId.replace("-", "_");

        String physicalTableName = "\"TAP_UPLOAD\".\"" + tableName + "\"";

        createUploadTable(conn, uploadTable, physicalTableName);

        JDBCFormatter formatter = new JDBCFormatter(conn, uploadTable);

        formatter.createJDBCTable(physicalTableName, WriteMode.APPEND);

        return physicalTableName;
    }

    private TapADQLTable createUploadMetadata(StarTable uploadTable) {

        Schema schema = new Schema();
        schema.setSchema_name("TAP_UPLOAD");

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

    private void createUploadTable(Connection conn, StarTable table, String physicalName) throws SQLException {

        String sql = buildCreateTableSql(table, physicalName);

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

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
}
