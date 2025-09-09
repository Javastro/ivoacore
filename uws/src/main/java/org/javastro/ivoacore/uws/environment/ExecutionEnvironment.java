package org.javastro.ivoacore.uws.environment;


import org.javastro.ivoacore.common.security.SecurityGuard;

import java.io.File;

/**
The environment that the Job runs in.
 */
public interface ExecutionEnvironment {
   SecurityGuard getSecGuard();
   File getWorkDir(String id);
   File getTempFile(String id);
}
