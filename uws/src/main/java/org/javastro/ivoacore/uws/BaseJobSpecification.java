package org.javastro.ivoacore.uws;


/*
 * Created on 09/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

public abstract class BaseJobSpecification implements JobSpecification {
   protected final String runId;

   protected BaseJobSpecification(String runId) {
      this.runId = runId;
   }

   @Override
   public String getRunId() {
      return runId;
   }
}
