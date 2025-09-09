package org.javastro.ivoacore.uws.environment;


/*
 * Created on 04/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

public class DefaultExecutionPolicy implements ExecutionPolicy {
   @Override
   public int getMaxRunTime() {
      return 86400;
   }

   @Override
   public int getKillPeriod() {
      return 60;
   }

   @Override
   public int getDefaultLifetime() {
      return 86400*14;
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
