/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.uws.environment;


import java.io.File;

public class DefaultEnvironmentFactory implements EnvironmentFactory {
   private final File baseDir;

   public DefaultEnvironmentFactory(File baseDir) {
      this.baseDir = baseDir;
   }

   @Override
   public ExecutionEnvironment create(String jobId) {
      return new DefaultExecutionEnvironment(baseDir,jobId);
   }
}
