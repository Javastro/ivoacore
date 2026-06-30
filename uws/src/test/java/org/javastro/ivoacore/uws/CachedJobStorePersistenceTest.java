package org.javastro.ivoacore.uws;

import jakarta.persistence.EntityTransaction;
import org.javastro.ivoacore.uws.environment.DefaultEnvironmentFactory;
import org.javastro.ivoacore.uws.environment.DefaultExecutionPolicy;
import org.javastro.ivoacore.uws.persist.CachedJobStore;
import org.javastro.ivoacore.uws.persist.DatabaseJobStore;
import org.javastro.ivoacore.uws.persist.MemoryBasedJobStore;
import org.javastro.ivoacore.uws.tools.JpaTestSupport;
import org.javastro.ivoacore.uws.tools.TestPersistenceFactory;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CachedJobStorePersistenceTest {

    private JpaTestSupport jpa;
    private JobManager jobManager;
    private DatabaseJobStore store;

    @BeforeAll
    void setup() throws IOException {
        jpa = new JpaTestSupport();

        File tmpDir = Files.createTempDirectory("managerTest").toFile();

        JobFactoryAggregator agg = new JobFactoryAggregator();
        agg.addFactory(new SimpleLambdaJob.JobFactory(
                this::runLambdaJob,
                new DefaultEnvironmentFactory(tmpDir)
        ));

        store = TestPersistenceFactory.create(jpa.entityManager(), agg);

        jobManager = new JobManager(agg, new MemoryBasedJobStore(), new DefaultExecutionPolicy());
    }

    @AfterAll
    void teardown() {
        if (jpa != null) {
            jpa.close();
        }
    }

    @Test
    @DisplayName("CachedJobStore can retrieve a job from database backing store and cache it")
    void cachedStoreCanRetrieveFromDatabaseBackingStore() {
        BaseUWSJob job = createJob();

        EntityTransaction tx = jpa.entityManager().getTransaction();
        tx.begin();
        store.store(job);
        tx.commit();

        MemoryBasedJobStore cache = new MemoryBasedJobStore();
        CachedJobStore cachedStore = new CachedJobStore(cache, store);  //store is the database backing store

        BaseUWSJob retrieved = cachedStore.retrieve(job.getID());

        assertNotNull(retrieved);
        assertEquals(job.getID(), retrieved.getID());

        BaseUWSJob cached = cache.retrieve(job.getID());
        assertNotNull(cached);
        assertEquals(job.getID(), cached.getID());
    }

    /**
     * Simple lambda job that sleeps for 2.3 seconds and returns a string.
     * @param s the string to return
     * @return the string "hello " + the supplied string
     */
    private String runLambdaJob(String s) {
        try {
            Thread.sleep(2300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        return "hello " + s;
    }

    /**
     * Creates and returns a new instance of {@link BaseUWSJob} using the predefined specification.
     *
     * @return A new instance of {@link BaseUWSJob}.
     * @throws RuntimeException if a {@link UWSException} occurs during job creation.
     */
    private BaseUWSJob createJob(){
        SimpleLambdaJob.Specification spec = new SimpleLambdaJob.Specification("world", "myrefID");
        BaseUWSJob job;
        try {
            job = jobManager.createJob(spec);
        } catch (UWSException e) {
            throw new RuntimeException(e);
        }
        return job;
    }
}
