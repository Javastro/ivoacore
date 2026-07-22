package org.javastro.ivoacore.uws;


/*
 * Created on 09/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.javastro.ivoacore.uws.environment.execution.ParameterValue;

import java.util.List;

/**
 * Base implementation of {@link JobSpecification} storing the run ID and parameter list.
 * Note that for JSON serialization to work a default constructor is required, which is *not* provided here. Subclasses should provide their own constructors for proper initialization.
 */
public abstract class BaseJobSpecification implements JobSpecification {
   /** The run ID associated with this job specification. */
   protected String runId;
   /** The list of parameter values for this job. */
   protected List<ParameterValue> parameters ;

   /**
    * Constructs a new BaseJobSpecification with the given run ID and parameter list.
    * Note that this constructor is protected and should only be used by subclasses.
    * @param runId run identifier for the job.
    * @param parameterValues parameter values for job execution.
    */
   protected BaseJobSpecification(String runId, List<ParameterValue> parameterValues) {
      this.runId = runId;
      this.parameters = parameterValues;
   }

   @Override
   public final String getRunId() {
      return runId;
   }

   @Override
   public final List<ParameterValue> getParameters() {
      return parameters;
   }

   @Override
   public String getJDL() {
      return "";
   }

   //note not public, only for use by mapper
   void setRunId(String runId) {
      this.runId = runId;
   }

   //note not public, only for use by mapper
   void setParameters(List<ParameterValue> parameters) {
      this.parameters = parameters;
   }
}
