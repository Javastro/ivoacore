package org.javastro.ivoacore.uws;

import org.javastro.ivoacore.uws.environment.DefaultEnvironmentFactory;
import org.javastro.ivoacore.uws.environment.DefaultExecutionPolicy;
import org.javastro.ivoacore.uws.persist.CachedJobStore;
import org.javastro.ivoacore.uws.persist.MemoryBasedJobStore;
import org.javastro.ivoacore.uws.store.RecordingJobStore;
import org.javastro.ivoacore.uws.tools.JpaTestSupport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CachedStoreTest {

    private static JpaTestSupport jpa;
    private static JobManager jobManager;

    @BeforeAll
    static void setup() throws IOException {
        jpa = new JpaTestSupport();

        File tmpDir = Files.createTempDirectory("managerTest").toFile();

        JobFactoryAggregator agg = new JobFactoryAggregator();
        agg.addFactory(new SimpleLambdaJob.JobFactory(
                CachedStoreTest::runLambdaJob,
                new DefaultEnvironmentFactory(tmpDir)
        ));

        jobManager = new JobManager(agg, new MemoryBasedJobStore(), new DefaultExecutionPolicy());
    }

    @AfterAll
    static void teardown() {
        if (jpa != null) {
            jpa.close();
        }
    }

    @Test
    @DisplayName("Test that the CachedJobStore correctly stores and retrieves jobs from the backing store")
    void storeWritesToBackingStoreAndCache() {
        RecordingJobStore cache = new RecordingJobStore();
        RecordingJobStore backing = new RecordingJobStore();
        CachedJobStore store = new CachedJobStore(cache, backing);

        BaseUWSJob job = createJob();
        store.store(job);

        assertSame(job, backing.jobs.get(job.getID()));
        assertSame(job, cache.jobs.get(job.getID()));

        assertEquals(List.of("store:" + job.getID()), backing.calls);
        assertEquals(List.of("store:" + job.getID()), cache.calls);
    }

    @Test
    @DisplayName("Test that the CachedJobStore does not cache jobs if the backing store fails")
    void storeDoesNotCacheIfBackingStoreFails() {
        RecordingJobStore cache = new RecordingJobStore();
        RecordingJobStore backing = new RecordingJobStore();
        backing.failOnStore = true;

        CachedJobStore store = new CachedJobStore(cache, backing);
        BaseUWSJob job = createJob();

        assertThrows(RuntimeException.class, () -> store.store(job));

        assertFalse(cache.jobs.containsKey(job.getID()));
    }

    @Test
    @DisplayName("Test that the CachedJobStore correctly retrieves jobs from the cache and only the cache")
    void retrieveReturnsCachedJobWithoutCallingBackingStore() {
        RecordingJobStore cache = new RecordingJobStore();
        RecordingJobStore backing = new RecordingJobStore();
        CachedJobStore store = new CachedJobStore(cache, backing);

        BaseUWSJob job = createJob();
        cache.jobs.put(job.getID(), job);   //force job just into the cache to backing store stays empty

        BaseUWSJob retrieved = store.retrieve(job.getID());

        assertSame(job, retrieved);
        assertEquals(List.of("retrieve:" + job.getID()), cache.calls);
        assertTrue(backing.calls.isEmpty());
    }

    @Test
    @DisplayName("Test that the CachedJobStore correctly retrieves jobs from the backing store and caches them")
    void retrieveFallsBackToBackingStoreAndCachesResult() {
        RecordingJobStore cache = new RecordingJobStore();
        RecordingJobStore backing = new RecordingJobStore();
        CachedJobStore store = new CachedJobStore(cache, backing);

        BaseUWSJob job = createJob();
        backing.jobs.put(job.getID(), job);

        BaseUWSJob retrieved = store.retrieve(job.getID());

        assertSame(job, retrieved);
        assertSame(job, cache.jobs.get(job.getID()));

        assertEquals(List.of(
                "retrieve:" + job.getID(),
                "store:" + job.getID()
        ), cache.calls);

        assertEquals(List.of("retrieve:" + job.getID()), backing.calls);
    }

    @Test
    @DisplayName("Test that the CachedJobStore correctly handles the case where a job is not found in either store")
    void retrieveReturnsNullWhenMissingFromBothStores() {
        RecordingJobStore cache = new RecordingJobStore();
        RecordingJobStore backing = new RecordingJobStore();
        CachedJobStore store = new CachedJobStore(cache, backing);

        BaseUWSJob retrieved = store.retrieve("missing-job");

        assertNull(retrieved);
        assertFalse(cache.jobs.containsKey("missing-job"));

        assertEquals(List.of("retrieve:missing-job"), cache.calls);
        assertEquals(List.of("retrieve:missing-job"), backing.calls);
    }

    @Test
    @DisplayName("Test that the CachedJobStore correctly deletes jobs from the cache and backing store")
    void deleteRemovesFromCacheEvenWhenBackingStoreReturnsFalse() {
        RecordingJobStore cache = new RecordingJobStore();
        RecordingJobStore backing = new RecordingJobStore();
        backing.deleteResult = false;

        CachedJobStore store = new CachedJobStore(cache, backing);

        BaseUWSJob job = createJob();
        cache.jobs.put(job.getID(), job);

        boolean deleted = store.delete(job.getID());

        assertFalse(deleted);
        assertFalse(cache.jobs.containsKey(job.getID()));

        assertEquals(List.of("delete:" + job.getID()), backing.calls);
        assertEquals(List.of("delete:" + job.getID()), cache.calls);
    }

    @Test
    @DisplayName("Test that the CachedJobStore correctly retrieves all job IDs from the backing store")
    void getAllIdsDelegatesToBackingStoreOnly() {
        RecordingJobStore cache = new RecordingJobStore();
        RecordingJobStore backing = new RecordingJobStore();
        CachedJobStore store = new CachedJobStore(cache, backing);

        BaseUWSJob job = createJob();
        backing.jobs.put(job.getID(), job);

        Set<String> ids = store.getAllIds();

        assertEquals(Set.of(job.getID()), ids);
        assertTrue(cache.calls.isEmpty());
        assertEquals(List.of("getAllIds"), backing.calls);
    }

    @Test
    @DisplayName("Test that the CachedJobStore correctly retrieves all jobs from the backing store")
    void getJobsDelegatesToBackingStoreAndCachesReturnedJobs() {
        RecordingJobStore cache = new RecordingJobStore();
        RecordingJobStore backing = new RecordingJobStore();
        CachedJobStore store = new CachedJobStore(cache, backing);

        BaseUWSJob job1 = createJob();
        BaseUWSJob job2 = createJob();

        backing.jobs.put(job1.getID(), job1);
        backing.jobs.put(job2.getID(), job2);

        List<BaseUWSJob> jobs = store.getJobs(null, null, null);

        assertEquals(List.of(job1, job2), jobs);
        assertSame(job1, cache.jobs.get(job1.getID()));
        assertSame(job2, cache.jobs.get(job2.getID()));

        assertEquals(List.of("getJobs"), backing.calls);
        assertEquals(List.of(
                "store:" + job1.getID(),
                "store:" + job2.getID()
        ), cache.calls);
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

    /**
     * Simple lambda job that sleeps for 2.3 seconds and returns a string.
     * @param s the string to return
     * @return the string "hello " + the supplied string
     */
    private static String runLambdaJob(String s) {
        try {
            Thread.sleep(2300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        return "hello " + s;
    }
}
