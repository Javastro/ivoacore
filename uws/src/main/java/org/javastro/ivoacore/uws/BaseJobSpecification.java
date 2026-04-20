package org.javastro.ivoacore.uws;


/*
 * Created on 09/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.javastro.ivoacore.uws.environment.execution.ParameterValue;

import java.util.List;

/**
 * Base implementation of {@link JobSpecification} storing the run ID and parameter list.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class BaseJobSpecification implements JobSpecification {
   /** The run ID associated with this job specification. */
   protected final String runId;
   /** The list of parameter values for this job. */
   protected final List<ParameterValue> parameters;

   /**
    * Constructs a BaseJobSpecification with the given run ID and parameters.
    * @param runId the run identifier for this job.
    * @param parameters the list of parameter values.
    */
   @JsonCreator
   protected BaseJobSpecification(@JsonProperty String runId, @JsonProperty List<ParameterValue> parameters) {
      this.runId = runId;
      this.parameters = parameters;
   }

   @Override
   public final String getRunId() {
      return runId;
   }

   @Override
   public final List<ParameterValue> getParameters() {
      return parameters;
   }
}
