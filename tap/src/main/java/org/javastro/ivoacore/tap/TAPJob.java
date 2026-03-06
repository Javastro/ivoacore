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

public class TAPJob extends BaseUWSJob {

   public static final String JOB_TYPE = "TAP";
   private final DataSource dataSource;
   private final TAPJobSpecification tapJobSpec;


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

   public static class JobFactory extends BaseJobFactory {
      private final DataSource ds;
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

      public TAPJob createJob(DataSource ds, String query, String lang,  String responseformat,  Long maxrec, String runid,
                               String upload)  {
          return new TAPJob(idProvider.generateId(),new TAPJobSpecification(query,lang,responseformat,maxrec,runid,upload),ds);
      }

   }
}
