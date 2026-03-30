/*
 * Copyright (c) 2025. Paul Harrison University of Manchester
 *
 */

package org.javastro.ivoacore.tap;


/*
 * Created on 02/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import adql.db.DBChecker;
import adql.db.DBTable;
import adql.parser.ADQLParser;
import adql.parser.QueryChecker;
import adql.parser.grammar.ParseException;
import adql.query.ADQLSet;
import adql.translator.ADQLTranslator;
import adql.translator.PgSphereTranslator;
import adql.translator.TranslationException;
import org.javastro.ivoa.entities.uws.ResultReference;
import org.javastro.ivoa.entities.uws.Results;
import org.javastro.ivoacore.tap.schema.MetadataTransformer;
import org.javastro.ivoacore.tap.schema.SchemaProvider;
import org.javastro.ivoacore.tap.schema.TapADQLColumn;
import org.javastro.ivoacore.uws.*;
import org.javastro.ivoacore.uws.environment.EnvironmentFactory;
import org.javastro.ivoacore.uws.environment.ExecutionEnvironment;
import org.javastro.ivoacore.uws.environment.execution.ParameterValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.starlink.table.*;
import uk.ac.starlink.table.jdbc.Connector;
import uk.ac.starlink.table.jdbc.JDBCStarTable;
import uk.ac.starlink.votable.VOTableWriter;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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


   @Override
   public List<ParameterValue> performAction() throws UWSException {
      final File votable = executionEnvironment.getWorkDir().toPath().resolve("results.vot").toFile();
      try {
         List<DBTable> tables = new MetadataTransformer(schemaProvider).transformToADQLLib();
         ADQLParser parser = new ADQLParser();
         QueryChecker checker = new DBChecker(tables);
         // Set the DBChecker to the parser:
         parser.setQueryChecker(checker);
         // Parse ADQL:
         ADQLSet query = parser.parseQuery(tapJobSpec.adqlQuery);

         ADQLTranslator translator = new PgSphereTranslator();
         String sql = translator.translate(query);
         log.info("Translated ADQL query to SQL: {}", sql);


         Connector connector = () -> dataSource.getConnection(); //IMPL new connection each time requested - STIL documentation implies that is what is desired.
         JDBCStarTable table = new JDBCStarTable(connector, sql, false);

         table.setName("tap_result");
         table.setParameter(new DescribedValue(
               new DefaultValueInfo("QUERY", String.class, "Original ADQL query"),
               tapJobSpec.adqlQuery
         ));
         table.setParameter(new DescribedValue(
               new DefaultValueInfo("RUNID", String.class, "TAP run identifier"),
               tapJobSpec.getRunId()
         ));

         Arrays.stream(query.getResultingColumns())
               .forEach(col -> {
                  log.debug( "ADQL column: {} table {}", col.getADQLName(), col.getTable().getADQLName());
               });
         Map<String, TapADQLColumn> columnMap = Arrays.stream(query.getResultingColumns())
               .collect(Collectors.toMap(
                     col -> col.getADQLName(),
                     col -> (TapADQLColumn) col

               ));

         for (int i = 0; i < table.getColumnCount(); i++) {
            ColumnInfo colInfo = table.getColumnInfo(i);
            log.debug(" Stil Column {}: name={}, class={}, description={}", i, colInfo.getName(), colInfo.getContentClass(), colInfo.getDescription());
            TapADQLColumn adqlColInfo = columnMap.get(colInfo.getName());
            colInfo.setDescription(adqlColInfo.getDescription());
            colInfo.setUnitString(adqlColInfo.getUnitString());
            colInfo.setUCD(adqlColInfo.getUcd());
            colInfo.setUtype(adqlColInfo.getUtype());
         }

         final OutputStream outputStream = Files.newOutputStream(votable.toPath());
         StarTableWriter tablewriter = new VOTableWriter();
         new StarTableOutput().writeStarTable(table, outputStream, tablewriter);


      } catch (SQLException e) { //FIXME actually need to get the error into the result VOTable...
         //TODO remove the logging here  - it is just duplicating other logging I think
         log.error("Database error while executing TAP query", e);
         throw new UWSException("Database error while executing TAP query",e); //FIXME need to decide how to handle fail properly - should we fail the job? retry? etc.
      } catch (ParseException e) {
         log.error("Parse error while executing TAP query", e);
         throw new UWSException("Parse error while executing TAP query",e);
      } catch (TranslationException e) {
         log.error("Translation error while executing TAP query", e);
         throw new UWSException("Translation error while executing TAP query",e);
      } catch (IOException e) {
         log.error("IO error while executing TAP query", e);
         throw new UWSException("IO error while executing TAP query",e);
      }
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
