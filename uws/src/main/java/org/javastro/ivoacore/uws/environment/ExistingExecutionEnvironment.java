package org.javastro.ivoacore.uws.environment;


import org.javastro.ivoacore.common.security.SecurityGuard;

import java.io.File;
/**
 * An implementation of {@link ExecutionEnvironment} that uses an existing working directory and security guard.
 * Probably retrieved from persistent store.
 */
public class ExistingExecutionEnvironment implements ExecutionEnvironment {
    private final SecurityGuard secGuard;
    private final File workDir;

    /**
     * Creates an execution environment backed by an existing working directory.
     *
     * @param secGuard security guard associated with the environment.
     * @param workDir existing working directory for the job.
     */
    public ExistingExecutionEnvironment(SecurityGuard secGuard, File workDir) {
        this.secGuard = secGuard;
        this.workDir = workDir;
    }

    @Override
    public SecurityGuard getSecGuard() {
        return secGuard;
    }

    @Override
    public File getWorkDir() {
        return workDir;
    }

    @Override
    public File getTempFile(String id) {
        // This implementation does not support temporary files.
        return null;
    }
}
