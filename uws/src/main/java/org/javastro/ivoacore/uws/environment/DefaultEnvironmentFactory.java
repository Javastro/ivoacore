/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.uws.environment;


import java.io.File;

/**
 * Default {@link EnvironmentFactory} creating per-job filesystem work directories.
 */
public class DefaultEnvironmentFactory implements EnvironmentFactory {
   private final File baseDir;

   /**
    * Creates a factory rooted at the given base directory.
    *
    * @param baseDir base directory under which job work directories are created.
    */
   public DefaultEnvironmentFactory(File baseDir) {
      this.baseDir = baseDir;
   }

   @Override
   public ExecutionEnvironment create(String jobId) {
      return new DefaultExecutionEnvironment(baseDir,jobId);
   }
}
