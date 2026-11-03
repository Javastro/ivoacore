package org.javastro.ivoacore.uws.environment;


/**
 * Default implementation of {@link ExecutionPolicy} providing sensible default values for UWS execution constraints.
 */
public class DefaultExecutionPolicy implements ExecutionPolicy {
   @Override
   public long getMaxRunTime() {
      return 86400;
   }

   @Override
   public long getDefaultRunTime() {
      return 3600;
   }

   @Override
   public int getKillPeriod() {
      return 60;
   }

   @Override
   public long getDefaultLifetime() {
      return 86400*14;
   }

   @Override
   public long getMaxLifetime() {
      return 86400*180;
   }

   @Override
   public int getDestroyPeriod() {
      return 86400/4;
   }

   @Override
   public int getMaxConcurrent() {
      return 10;
   }
}
