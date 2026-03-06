package org.javastro.ivoacore.uws;


/*
 * Created on 09/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.javastro.ivoacore.uws.environment.execution.ParameterValue;

import java.util.List;

public abstract class BaseJobSpecification implements JobSpecification {
   protected final String runId;
   protected final List<ParameterValue> parameters;

   protected BaseJobSpecification(String runId, List<ParameterValue> parameters) {
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
