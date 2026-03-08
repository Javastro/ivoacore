package org.javastro.ivoacore.uws.environment;


import org.javastro.ivoacore.common.security.SecurityGuard;

import java.io.File;

/**
The environment that the Job runs in.
 */
public interface ExecutionEnvironment {
   /**
    * Returns the security guard for the current execution context.
    * @return the {@link SecurityGuard} for this environment.
    */
   SecurityGuard getSecGuard();

   /**
    * Returns the working directory for the job with the given identifier.
    * @param id the job identifier.
    * @return the working directory {@link File} for the job.
    */
   File getWorkDir(String id);

   /**
    * Returns a temporary file for the given identifier.
    * @param id an identifier used to name the temporary file.
    * @return the temporary {@link File}, or {@code null} if not supported.
    */
   File getTempFile(String id);
}
