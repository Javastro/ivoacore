/*
 * Copyright (c) 2025. Paul Harrison University of Manchester
 *
 */

package org.javastro.ivoacore.tap;


/*
 * Created on 02/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import adql.db.DBChecker;
import adql.db.DBColumn;
import adql.db.DBTable;
import adql.parser.ADQLParser;
import adql.parser.QueryChecker;
import adql.parser.grammar.ParseException;
import adql.query.ADQLSet;
import adql.translator.ADQLTranslator;
import adql.translator.PgSphereTranslator;
import adql.translator.TranslationException;
import org.ivoa.dm.tapschema.Column;
import org.ivoa.dm.tapschema.Schema;
import org.ivoa.dm.tapschema.Table;
import org.javastro.ivoa.entities.uws.ResultReference;
import org.javastro.ivoa.entities.uws.Results;
import org.javastro.ivoacore.tap.schema.MetadataTransformer;
import org.javastro.ivoacore.tap.schema.SchemaProvider;
import org.javastro.ivoacore.tap.schema.TapADQLColumn;
import org.javastro.ivoacore.tap.schema.TapADQLTable;
import org.javastro.ivoacore.uws.*;
import org.javastro.ivoacore.uws.environment.EnvironmentFactory;
import org.javastro.ivoacore.uws.environment.ExecutionEnvironment;
import org.javastro.ivoacore.uws.environment.execution.ParameterValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.starlink.table.*;
import uk.ac.starlink.table.jdbc.*;
import uk.ac.starlink.votable.ResourceType;
import uk.ac.starlink.votable.VOTableBuilder;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * A UWS {@link Job} that executes a TAP (Table Access Protocol) query.
 */
public class TAPJob extends BaseUWSJob {

   public static final String JOB_TYPE = "TAP";
   private static final Logger log = LoggerFactory.getLogger(TAPJob.class);
   private final DataSource dataSource;
   private final TAPJobSpecification tapJobSpec;
   private final SchemaProvider schemaProvider;

   private record UploadContext(
           String physicalTableName,
           TapADQLTable adqlTable) {
   }

   /**
    * Constructs a new TAPJob with the given identifier, specification and data source.
    *
    * @param id                   the unique identifier for this job.
    * @param spec                 the TAP job specification containing query parameters.
    * @param executionEnvironment the execution environment for this job.
    * @param ds                   the JDBC data source to execute the query against.
    * @param schemaProvider       the schema provider for the TAP job.
    */
   public TAPJob(String id, TAPJobSpecification spec, ExecutionEnvironment executionEnvironment, DataSource ds, SchemaProvider schemaProvider) {
      super(id, spec, executionEnvironment);
      this.dataSource = ds;
      this.tapJobSpec = spec;
      this.schemaProvider = schemaProvider;
   }

   TAPJob(PersistedJobRecord record, ExecutionEnvironment executionEnvironment, DataSource ds, SchemaProvider schemaProvider) {
      super(record, executionEnvironment);
      this.dataSource = ds;
      this.tapJobSpec = (TAPJobSpecification) record.specification();
      this.schemaProvider = schemaProvider;
   }

   @Override
   public List<ParameterValue> performAction() throws UWSException {
      validateRequest();

      File votable = executionEnvironment.getWorkDir()
              .toPath()
              .resolve("results.vot")
              .toFile();

      UploadContext uploadContext = null;

      try {

         List<DBTable> tables =
                 new MetadataTransformer(schemaProvider).transformToADQLLib();

         if (hasUpload()) {
            uploadContext = processUpload();
            tables.add(uploadContext.adqlTable());
         }

         ADQLSet query = parseQuery(tables);

         String sql = translateQuery(query, uploadContext);

         JDBCStarTable table = getResultTable(sql);

         outputResult(query, table, votable);

      } catch (SQLException | ParseException |
               TranslationException | IOException e) {

         log.error("Error while executing TAP query", e);
         throw new UWSException("Error while executing TAP query", e);

      } catch (Exception e) {
          throw new RuntimeException(e);
      } finally {

         cleanupUploadTable(uploadContext);

      }

      return resultParameter(votable);
   }

   UWSException getException()
   {
      return exception;
   }

   @Override
   public Results createExternalJobResult() {
      //FIXME - this is too simplistic at the moment - need to fetch things properly - need to think about refactor of org.javastro.ivoacore.uws.description.parameter
      Results.Builder<Void> resultsBuilder = Results.builder();

      if(!results.isEmpty()) {
         ParameterValue pv = results.get(0); //IMPL assuming the only result....
         final ResultReference.Builder<Void> builder = ResultReference.builder().withId(pv.getId());
         if (pv.isIndirect()) {
            builder.withHref("./results/" + pv.getId());
            builder.withMimeType("application/x-votable+xml");
            builder.withSize(new File(pv.getValue()).length());
         }
         resultsBuilder.addResults(builder.build());
      }

      return resultsBuilder.build();

   }

   private void validateRequest() throws UWSException {
      if (tapJobSpec.adqlQuery == null || tapJobSpec.adqlQuery.isBlank()) {
         throw new UWSException("ADQL query is missing or empty");
      }
   }

   private boolean hasUpload() {
      return tapJobSpec.upload != null &&
              !tapJobSpec.upload.isBlank();
   }

   private UploadContext processUpload()
           throws Exception {

      URI uploadUri = URI.create(tapJobSpec.upload);

      try (InputStream in = uploadUri.toURL().openStream();
           Connection conn = dataSource.getConnection()) {

         StarTable table =
                 new StarTableFactory().makeStarTable(
                         in,
                         new VOTableBuilder());

         ensureUploadSchemaExists(conn);

         String physicalTableName =
                 createAndPopulateUploadTable(conn, table);

         TapADQLTable adqlTable =
                 createUploadMetadata(table);

         return new UploadContext(
                 physicalTableName,
                 adqlTable);
      }
   }

   private void ensureUploadSchemaExists(Connection conn)
           throws SQLException {

      try (Statement stmt = conn.createStatement()) {
         stmt.execute("""
            CREATE SCHEMA IF NOT EXISTS "TAP_UPLOAD"
            """);
      }
   }

   private String createAndPopulateUploadTable(Connection conn, StarTable uploadTable) throws Exception {

      String tableName = "tap_upload_" + getID().replace("-", "_");

      String physicalTableName = "\"TAP_UPLOAD\".\"" + tableName + "\"";

      createUploadTable(conn, uploadTable, physicalTableName);

      JDBCFormatter formatter = new JDBCFormatter(conn, uploadTable);

      formatter.createJDBCTable(physicalTableName, WriteMode.APPEND);

      return physicalTableName;
   }

   private void createUploadTable(
           Connection conn,
           StarTable table,
           String physicalName)
           throws SQLException {

      String sql =
              buildCreateTableSql(table, physicalName);

      try (Statement stmt = conn.createStatement()) {
         stmt.execute(sql);
      }
   }

   private String buildCreateTableSql(
           StarTable table,
           String physicalName) {

      StringJoiner columns =
              new StringJoiner(", ");

      for (int i = 0; i < table.getColumnCount(); i++) {

         ColumnInfo info =
                 table.getColumnInfo(i);

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

   private TapADQLTable createUploadMetadata(
           StarTable uploadTable) {

      Schema schema = new Schema();
      schema.setSchema_name("TAP_UPLOAD");

      Table table = new Table();
      table.setTable_name("targets");

      TapADQLTable result =
              new TapADQLTable(
                      schema,
                      table,
                      false);

      for (int i = 0; i < uploadTable.getColumnCount(); i++) {

         ColumnInfo info =
                 uploadTable.getColumnInfo(i);

         Column column = new Column();
         column.setColumn_name(
                 info.getName().toUpperCase());

         column.setDatatype(
                 MetadataTransformer
                         .mapContentClassToTAPType(
                                 info.getContentClass()));

         result.createColumn(column);
      }

      return result;
   }

   private ADQLSet parseQuery(List<DBTable> tables)
           throws ParseException {

      QueryChecker checker =
              new DBChecker(tables);

      ADQLParser parser =
              new ADQLParser();

      parser.setQueryChecker(checker);

      return parser.parseQuery(
              tapJobSpec.adqlQuery);
   }

   private String translateQuery(
           ADQLSet query,
           UploadContext upload)
           throws TranslationException {

      String sql =
              translateADQLToSQL(query);

      if (upload != null) {
         sql = replaceUploadTableReferences(
                 sql,
                 upload.physicalTableName());
      }

      return sql;
   }

   private void cleanupUploadTable(
           UploadContext upload) {

      if (upload == null) {
         return;
      }

      try (Connection conn = dataSource.getConnection();
           Statement stmt = conn.createStatement()) {

         stmt.execute(
                 "DROP TABLE IF EXISTS "
                         + upload.physicalTableName());

      } catch (SQLException e) {

         log.warn(
                 "Failed to drop upload table {}",
                 upload.physicalTableName(),
                 e);
      }
   }

   private List<ParameterValue> resultParameter(File votable) {
      return List.of(new ParameterValue() {
         @Override
         public String getValue() {
            return votable.getAbsolutePath();
         }

         @Override
         public boolean isIndirect() {
            return true;
         }

         @Override
         public String getId() {
            return "result";
         }
      });
   }

   private String replaceUploadTableReferences(
           String sql,
           String physicalTempTableName) {

      log.debug("Original SQL: {}", sql);

      sql = sql.replaceAll(
              "(?i)\"TAP_UPLOAD\"\\s*\\.\\s*\"TARGETS\"",
              physicalTempTableName);

      sql = sql.replaceAll(
              "(?i)TAP_UPLOAD\\s*\\.\\s*TARGETS",
              physicalTempTableName);

      sql = sql.replaceAll(
              "(?i)\\bTARGETS\\b",
              physicalTempTableName);

      log.debug("Modified SQL with table replacement: {}", sql);

      return sql;
   }



   /**
    * Translates an ADQL (Astronomical Data Query Language) query into an equivalent SQL query.
    * This method enforces a maximum row retrieval limit (MAXREC) at the SQL level if defined in the TAP job specification.
    * If the query already has a limit but exceeds MAXREC, the limit is adjusted. If no limit exists,
    * MAXREC is applied to the query. It utilizes a specialized ADQL translator to perform the conversion.
    *
    * @param query The ADQL query set to be translated into SQL. This includes information
    *              about columns, tables, and constraints defined in ADQL.
    * @return A {@code String} representing the translated SQL query equivalent to the provided ADQL query.
    * @throws TranslationException If an error occurs during the translation process, such as invalid syntax
    *                              or unsupported operations in the ADQL query.
    */
   private String translateADQLToSQL(ADQLSet query) throws TranslationException {
      if (tapJobSpec.maxrec != null && tapJobSpec.maxrec >= 0) {
         // Apply MAXREC at SQL level to avoid fetching unnecessary rows.
         //TODO need to add system wide MAXREC - needs to get from ExecutionPolicy / environment
         if(query.hasLimit() && query.getLimit() > tapJobSpec.maxrec) {
            log.info("Applying MAXREC limit of {} to query with existing limit of {}", tapJobSpec.maxrec, query.getLimit());
            query.setLimit(Math.toIntExact(tapJobSpec.maxrec));
         }
         else if (!query.hasLimit()) {
            log.info("Applying MAXREC limit of {} to query with no existing limit", tapJobSpec.maxrec);
            query.setLimit(Math.toIntExact(tapJobSpec.maxrec));
         }
      }
      logQuery(query);     // TODO - IF log enabled
      ADQLTranslator translator = new PgSphereTranslator();
      return translator.translate(query);
   }

   /**
    * Executes a SQL query and returns the result as a {@code JDBCStarTable}.
    * This method establishes a new database connection, performs the query,
    * and wraps the results in a {@code JDBCStarTable} instance.
    * The connection is closed after the table is created.
    *
    * @param sql       The SQL query string to be executed against the database.
    * @return A {@code JDBCStarTable} instance containing the query results.
    * @throws SQLException If an error occurs while creating the database connection or executing the query.
    */
   private JDBCStarTable getResultTable(String sql) throws SQLException{
      final AtomicReference<Connection> thisConnection = new AtomicReference<>();
      Connector connector = () -> {
         thisConnection.set(dataSource.getConnection());//IMPL new connection each time requested - STIL documentation implies that is what is desired.
         return thisConnection.get();
      };

      // JDBCFormatter formatter = new JDBCFormatter(conn, uploadTable);
      // formatter.createJDBCTable();
    /*  try (InputStream uploadStream = uploadURL.toURL().openStream()) {
         StarTable uploadTable = new StarTableFactory().makeStarTable(uploadStream, new VOTableBuilder());
         uploadTable.getName();     //TODO - make uploaded table name unique in some way? if so, the sql will need to be adjusted too
         JDBCFormatter formatter = new JDBCFormatter(connector.getConnection(), uploadTable);
         formatter.createJDBCTable("targets", WriteMode.DROP_CREATE);
      } catch (IOException e) {
         log.error("Failed to read upload data from URL: {}", uploadURL, e);
         throw new SQLException("Failed to read upload data from URL: " + uploadURL, e);
      }*/


      //SQL executed here
      JDBCStarTable table =  new JDBCStarTable(connector, sql, false);
      table.setName("result");

      try {
         if (thisConnection.get() != null) {
            thisConnection.get().close();  //IMPL close the connection - STIL documentation implies that this is what is desired - but we should check whether this causes any issues with connection pooling etc.
         }
      } catch (SQLException e) {

         log.warn("Failed to close database connection", e);
      }

      return table;
   }

   /**
    * Logs details about the ADQL query, specifically the resulting columns and their associated tables.
    * Each column's ADQL name and the name of its originating table (if available) are included in the log output.
    * If no table is associated with a column, "table null" is logged for that column.
    *
    * @param query the ADQL query set containing the resulting columns to be logged.
    */
   private void logQuery(ADQLSet query) {
      Arrays.stream(query.getResultingColumns())
              .forEach(col -> {
                 if(col.getTable() != null) {
                    log.debug( "ADQL column: {} table {}", col.getADQLName(), col.getTable().getADQLName());
                 } else {
                    log.debug( "ADQL column: {} table null", col.getADQLName());
                 }
              });
   }

   /**
    * Outputs the query result to a VOTable file. This method updates the metadata of
    * the columns in the result table based on the corresponding ADQL query columns,
    * and writes the result table in VOTable format to the specified output file.
    *
    * @param query  The ADQL query set containing the resulting columns and metadata.
    * @param table  The JDBCStarTable containing the tabular data of the query result.
    * @param votable The output file where the VOTable data will be written.
    * @throws IOException If an I/O error occurs while writing the VOTable file.
    */
   private void outputResult(ADQLSet query, JDBCStarTable table, File votable) throws IOException {
      Map<String, ? extends DBColumn> columnMap = Arrays.stream(query.getResultingColumns())
              .collect(Collectors.toMap(
                      col -> col.getADQLName(),
                      col ->  col
              ));

      for (int i = 0; i < table.getColumnCount(); i++) {
         ColumnInfo colInfo = table.getColumnInfo(i);
         log.debug(" Stil Column {}: name={}, class={}, description={}", i, colInfo.getName(), colInfo.getContentClass(), colInfo.getDescription());
         DBColumn dbcolInfo = columnMap.get(colInfo.getName());
         if (dbcolInfo instanceof TapADQLColumn adqlColInfo) { // if the column is a normal column reference (rather than synthetic one like count(*)
            colInfo.setDescription(adqlColInfo.getDescription());
            colInfo.setUnitString(adqlColInfo.getUnitString());
            colInfo.setUCD(adqlColInfo.getUcd());
            colInfo.setUtype(adqlColInfo.getUtype());
         }
         else {
            log.warn(" Column {} is not a TapADQLColumn", colInfo.getName());
         }
      }

       final OutputStream outputStream = Files.newOutputStream(votable.toPath());
       TAPWriter tableWriter = new TAPWriter(this);
       tableWriter.setResourceType(ResourceType.RESULTS);
       new StarTableOutput().writeStarTable(table, outputStream, tableWriter);
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
     * Factory for creating {@link TAPJob} instances.
     */
    public static class JobFactory extends BaseJobFactory {
      private final DataSource ds;
      private final SchemaProvider schemaProvider;

      /**
       * Constructs a JobFactory using the given data source.
       *
       * @param ds the JDBC data source used to execute TAP queries.
       */
      public JobFactory(DataSource ds, SchemaProvider schemaProvider, EnvironmentFactory environmentFactory)  {
         super(JOB_TYPE, "Runs TAP jobs", true, environmentFactory);
         this.ds = ds;
         this.schemaProvider = schemaProvider;
      }

      @Override
      public BaseUWSJob createJob(JobSpecification jobDescription) throws UWSException {
         if (jobDescription.jobTypeIdentifier().equals("TAP")) {
            final String id = idProvider.generateId();
            return new TAPJob(id, (TAPJobSpecification) jobDescription, environmentFactory.create(id), ds, schemaProvider );
         } else throw new UWSException("Invalid job type");

      }

      @Override
      public BaseUWSJob createJob(PersistedJobRecord record) throws UWSException {
         final JobSpecification spec = record.specification();
         if (spec.jobTypeIdentifier().equals("TAP")) {
            return new TAPJob(record, environmentFactory.create(record.jobId()), ds, schemaProvider);
         } else throw new UWSException("Invalid job type");
      }

      /**
       * Creates a TAP job from individual query parameters.
       *
       * @param query          the ADQL query string.
       * @param lang           the query language (e.g. "ADQL").
       * @param responseformat the desired response format (e.g. "votable").
       * @param maxrec         the maximum number of records to return.
       * @param runid          the run identifier for this job.
       * @param upload         the upload parameter value, or {@code null} if not used.
       * @return a new {@link TAPJob} with the specified parameters.
       */
      public TAPJob createJob(String query, String lang, String responseformat, Long maxrec, String runid,
                              String upload) {
         String id = idProvider.generateId();
         return new TAPJob(
               id,
               new TAPJobSpecification(query, lang, responseformat, maxrec, runid, upload),
               environmentFactory.create(id),
               this.ds,
               this.schemaProvider
         );
       }
    }
}
