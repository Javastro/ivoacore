package org.javastro.ivoacore.uws.tools;

import org.javastro.ivoacore.uws.JobFactoryAggregator;
import org.javastro.ivoacore.uws.JobManager;
import org.javastro.ivoacore.uws.SimpleLambdaJob;
import org.javastro.ivoacore.uws.environment.DefaultEnvironmentFactory;
import org.javastro.ivoacore.uws.environment.DefaultExecutionPolicy;
import org.javastro.ivoacore.uws.persist.MemoryBasedJobStore;

import java.io.File;

public class TestJobManagerFactory {

    public static JobManager create(File tmpdir) {

        JobFactoryAggregator agg = new JobFactoryAggregator();

        agg.addFactory(new SimpleLambdaJob.JobFactory(
                s -> {
                    try {
                        Thread.sleep(2300);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                    return "hello " + s;
                },
                new DefaultEnvironmentFactory(tmpdir)
        ));

        return new JobManager(
                agg,
                new MemoryBasedJobStore(),
                new DefaultExecutionPolicy()
        );
    }
}