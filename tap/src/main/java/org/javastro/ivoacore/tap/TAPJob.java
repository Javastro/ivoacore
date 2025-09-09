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

   private String adqlQuery;
   private Long maxrec;
   private String responseFormat;

   public TAPJob(String id, JobSpecification spec, DataSource ds) {
      super(id,spec);
      this.dataSource = ds;
      for(ParameterValue p : spec.getParameters()) {
         switch (p.getId().toUpperCase()) {
            case "QUERY":
               adqlQuery = p.getValue();
               break;
            case "MAXREC":
               maxrec = Long.parseLong(p.getValue());
               break;
            case "LANG":
               //TODO must be ADQL - should throw here?
               break;
            case "RESPONSEFORMAT":
               responseFormat = p.getValue();
               break;
            //FIXME need to support upload - think about parameterValue more.

         }
      }
   }


   @Override
   public List<ParameterValue> runJob() {
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
         return new TAPJob(idProvider.generateId(),  jobDescription, ds);
      }
   }
}
