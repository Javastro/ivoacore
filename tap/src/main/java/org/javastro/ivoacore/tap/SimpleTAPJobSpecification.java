package org.javastro.ivoacore.tap;


/*
 * Created on 09/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.javastro.ivoacore.uws.BaseJobSpecification;
import org.javastro.ivoacore.uws.environment.execution.ParameterValue;

import java.util.ArrayList;
import java.util.List;

/**
 * A "default" simple TAP Job specification.
 * i.e.
 * <li>output VOTable</li>
 * <li>no upload</li>
 *
 */
public class SimpleTAPJobSpecification extends BaseJobSpecification {

   List<ParameterValue> parameters = new ArrayList<>();
   /**
    * Create the Job Specification.
    * @param query the ADQL query.
    * @param runId
    */
   public SimpleTAPJobSpecification(String query, String runId) {
      super(runId);
   }

   /**
    * Create the job specification with just the query.
    * @param query
    */
   public SimpleTAPJobSpecification(String query) {
      super("");
   }

   @Override
   public String jobTypeIdentifier() {
      return TAPJob.JOB_TYPE;
   }

   @Override
   public String getJDL() {
      return "";
   }

   @Override
   public List<ParameterValue> getParameters() {
      return List.of();//TODO create parameters.
   }
}
