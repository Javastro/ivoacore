package org.javastro.ivoacore.uws;

import jakarta.persistence.EntityTransaction;
import jakarta.transaction.Transactional;
import jdk.jfr.Description;
import org.javastro.ivoacore.uws.environment.DefaultEnvironmentFactory;
import org.javastro.ivoacore.uws.environment.DefaultExecutionPolicy;
import org.javastro.ivoacore.uws.persist.DatabaseJobStore;
import org.javastro.ivoacore.uws.persist.MemoryBasedJobStore;
import org.javastro.ivoacore.uws.tools.JpaTestSupport;
import org.javastro.ivoacore.uws.tools.TestPersistenceFactory;
import org.junit.jupiter.api.*;

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
class BackingStoreTest {

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

    @BeforeEach
    @Transactional
    void beforeEach() {
        //Clear the database before each test
        EntityTransaction tx = jpa.entityManager().getTransaction();
        tx.begin();

        jpa.entityManager().createNativeQuery("DELETE FROM uws.uws_jobs").executeUpdate();

        tx.commit();
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

    @AfterAll
    void tearDown() {
        jpa.close();
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

        jpa.entityManager().clear();

        //Check that the job is in the database
        List<?> rows = jpa.entityManager().createNativeQuery("SELECT job_id, executionPhase, creationTime, job_spec FROM uws.uws_jobs WHERE job_id = ?")
                .setParameter(1, job.getID())
                .getResultList();

        assertNotNull(rows);
        assertFalse(rows.isEmpty());

        Object[] row = (Object[]) rows.get(0);
        assertEquals(job.getID(), row[0]);

        assertEquals(job.getExecutionPhase().name(), row[1]);
    }

    @Test
    @Description("Test that the job can be parsed from the database")
    public void testBackingStoreParse() {
        BaseUWSJob job = createJob();

        assertNotNull(job);
        store.store(job);

        jpa.entityManager().clear();

        BaseUWSJob retrieved = store.retrieve(job.getID());
        assertNotNull(retrieved);
        assertEquals(job.getID(), retrieved.getID());
    }

    @Test
    @Description("Test that the backing store contains the job ID after storing a job")
    public void testBackingStoreContainsID(){
        BaseUWSJob job = createJob();

        assertNotNull(job);
        store.store(job);

        jpa.entityManager().clear();

        assertTrue(store.getAllIds().contains(job.getID()));
    }

    @Test
    @Description("Test that the backing store contains all job IDs after storing multiple jobs")
    public void testBackingStoreIDs(){
        BaseUWSJob job = createJob();

        assertNotNull(job);
        store.store(job);

        jpa.entityManager().clear();

        assertEquals(1, store.getAllIds().size());

        BaseUWSJob job2 = createJob();

        assertNotNull(job2);
        store.store(job2);

        jpa.entityManager().clear();

        assertEquals(2, store.getAllIds().size());
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
