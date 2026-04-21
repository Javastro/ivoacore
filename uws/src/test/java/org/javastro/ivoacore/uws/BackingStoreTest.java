package org.javastro.ivoacore.uws;

import org.javastro.ivoacore.uws.environment.DefaultEnvironmentFactory;
import org.javastro.ivoacore.uws.persist.DatabaseJobStore;
import org.javastro.ivoacore.uws.tools.JpaTestSupport;
import org.javastro.ivoacore.uws.tools.TestJobManagerFactory;
import org.javastro.ivoacore.uws.tools.TestPersistenceFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The BackingStoreTest class contains unit tests for verifying the persistence and retrieval of {@link BaseUWSJob}
 * instances within a database-backed job store. It tests the functionality of the {@link JobManager} and
 * {@link DatabaseJobStore}.
 * <p>
 * The class ensures that the job storage mechanism is functioning correctly in conjunction with the database
 * configurations provided by {@link JpaTestSupport}. The tests confirm that job data is stored persistently, can be
 * retrieved accurately, and aligns with expected database mappings.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BackingStoreTest {

    private JobManager jobManager;
    private DatabaseJobStore store;

    private JpaTestSupport jpa;

    @BeforeAll
    void setup() throws IOException {
        jpa = new JpaTestSupport();
        jpa.start();

        // Create a shared aggregator ONCE
        JobFactoryAggregator sharedAgg = new JobFactoryAggregator();
        File tmpdir = Files.createTempDirectory("managerTest").toFile();

        // Register factories to shared aggregator
        sharedAgg.addFactory(new SimpleLambdaJob.JobFactory(
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

        // Pass shared aggregator to both
        store = TestPersistenceFactory.createStore(jpa.em(), sharedAgg);
        jobManager = TestJobManagerFactory.create(tmpdir, sharedAgg);
    }

    @AfterAll
    void tearDown() {
        jpa.stop();
    }

    /**
     * Verifies the correct functionality of storing a {@link BaseUWSJob} instance in the database and
     * ensures that it is persisted and retrievable.
     * <p>
     * The test adds details of a job to the database using the expecting mapping approach and verifies that the
     * job is retrievable from the database.
     */
    @Test
    public void testBackingStore() {
        BaseUWSJob job = createJob();

        assertNotNull(job);
        store.store(job);

        jpa.em().clear();

        //Check that the job is in the database
        List<?> rows = jpa.em().createNativeQuery("SELECT job_id, executionPhase, creationTime, job_spec FROM uws.uws_jobs WHERE job_id = ?")
                .setParameter(1, job.getID())
                .getResultList();

        assertNotNull(rows);
        assertFalse(rows.isEmpty());

        Object[] row = (Object[]) rows.get(0);
        assertEquals(job.getID(), row[0]);

        assertEquals(job.getExecutionPhase().name(), row[1]);
    }

    @Test
    public void testBackingStoreParse() {
        BaseUWSJob job = createJob();

        assertNotNull(job);
        store.store(job);

        jpa.em().clear();

        BaseUWSJob retrieved = store.retrieve(job.getID());
        assertNotNull(retrieved);
        assertEquals(job.getID(), retrieved.getID());
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
