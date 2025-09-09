package org.javastro.ivoacore.uws.environment;


/*
 * Created on 04/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.javastro.ivoacore.common.security.SecurityGuard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class DefaultExecutionEnvironment implements ExecutionEnvironment {

    File baseDir;
   SecurityGuard securityGuard;
   private static final Logger logger = LoggerFactory.getLogger(DefaultExecutionEnvironment.class.getName());
   public DefaultExecutionEnvironment(File baseDir) {
      this.baseDir = baseDir;
      logger.info("Execution Environment baseDir: {} ", baseDir.getAbsolutePath());
   }

   @Override
   public SecurityGuard getSecGuard() {
      return null;
   }

   @Override
   public File getWorkDir(String id) {
      File retval = new File(this.baseDir, id);
      if(!retval.mkdirs())
      {
         throw new RuntimeException("Unable to create work dir "+retval.getAbsolutePath());
      }
      return retval;
   }

   @Override
   public File getTempFile(String id) {
      return null;
   }
}
