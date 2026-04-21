package org.javastro.ivoacore.uws.tools;

import org.javastro.ivoacore.uws.JobFactoryAggregator;
import org.javastro.ivoacore.uws.JobManager;
import org.javastro.ivoacore.uws.environment.DefaultExecutionPolicy;
import org.javastro.ivoacore.uws.persist.MemoryBasedJobStore;

import java.io.File;

public class TestJobManagerFactory {

    public static JobManager create(File tmpdir, JobFactoryAggregator agg) {

        return new JobManager(
                agg,
                new MemoryBasedJobStore(),
                new DefaultExecutionPolicy()
        );
    }
}