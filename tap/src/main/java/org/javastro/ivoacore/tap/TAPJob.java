/*
 * Copyright (c) 2025. Paul Harrison University of Manchester
 *
 */

package org.javastro.ivoacore.tap;


/*
 * Created on 02/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.javastro.ivoacore.uws.BaseJobFactory;
import org.javastro.ivoacore.uws.BaseUWSJob;
import org.javastro.ivoacore.uws.UWSException;
import org.javastro.ivoacore.uws.JobSpecification;
import org.javastro.ivoacore.uws.environment.execution.ParameterValue;

import javax.sql.DataSource;
import java.util.List;

/**
 * A UWS {@link org.javastro.ivoacore.uws.Job} that executes a TAP (Table Access Protocol) query.
 */
public class TAPJob extends BaseUWSJob {

   public static final String JOB_TYPE = "TAP";
   private final DataSource dataSource;
   private final TAPJobSpecification tapJobSpec;

   /**
    * Constructs a new TAPJob with the given identifier, specification and data source.
    * @param id the unique identifier for this job.
    * @param spec the TAP job specification containing query parameters.
    * @param ds the JDBC data source to execute the query against.
    */
   public TAPJob(String id, TAPJobSpecification spec, DataSource ds) {
      super(id,spec);
      this.dataSource = ds;
      this.tapJobSpec = spec;
   }


   @Override
   public List<ParameterValue> performAction() {
      //FIXME - really query
      return List.of(new ParameterValue() {
         @Override
         public String getValue() {
            return "file://blah";//TODO really make this work
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

   /**
    * Factory for creating {@link TAPJob} instances.
    */
   public static class JobFactory extends BaseJobFactory {
      private final DataSource ds;

      /**
       * Constructs a JobFactory using the given data source.
       * @param ds the JDBC data source used to execute TAP queries.
       */
      public JobFactory(DataSource ds) {
         super(JOB_TYPE, "Runs TAP jobs", true);
         this.ds = ds;
      }

      @Override
      public BaseUWSJob createJob(JobSpecification jobDescription) throws UWSException {
         if(jobDescription.jobTypeIdentifier().equals("TAP")) {
            return new TAPJob(idProvider.generateId(), (TAPJobSpecification) jobDescription, ds);
         }
         else throw new UWSException("Invalid job type");

      }

      /**
       * Creates a TAP job from individual query parameters.
       * @param ds the JDBC data source to execute the query against.
       * @param query the ADQL query string.
       * @param lang the query language (e.g. "ADQL").
       * @param responseformat the desired response format (e.g. "votable").
       * @param maxrec the maximum number of records to return.
       * @param runid the run identifier for this job.
       * @param upload the upload parameter value, or {@code null} if not used.
       * @return a new {@link TAPJob} with the specified parameters.
       */
      public TAPJob createJob(DataSource ds, String query, String lang,  String responseformat,  Long maxrec, String runid,
                               String upload)  {
          return new TAPJob(idProvider.generateId(),new TAPJobSpecification(query,lang,responseformat,maxrec,runid,upload),ds);
      }

   }
}
