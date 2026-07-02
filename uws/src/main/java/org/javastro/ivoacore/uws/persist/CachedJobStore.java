package org.javastro.ivoacore.uws.persist;

import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoacore.uws.BaseUWSJob;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A {@link JobStore} decorator that keeps recently accessed jobs in memory while
 * delegating persistence to another {@link JobStore}.
 * <p>
 * The backing store remains the source of truth. Writes are performed against the
 * backing store first, then reflected in the memory cache.
 */
public class CachedJobStore implements JobStore {

    private final JobStore cache;
    private final JobStore backingStore;

    /**
     * Constructs a CachedJobStore with the specified backing store.
     * The backing store is used for persistent storage of jobs, while
     * a memory-based cache is utilised for more efficient access to recently
     * retrieved or stored jobs.
     *
     * @param backingStore the backing {@link JobStore} that serves as the
     *                     persistent data source. This store acts as the
     *                     source of truth for all operations.
     */
    public CachedJobStore(JobStore backingStore) {
        this(new MemoryBasedJobStore(), backingStore);
    }

    /**
     * Constructs a CachedJobStore with a specified cache and a backing store.
     * The cache is used to store recently accessed jobs in memory for faster access,
     * while the backing store serves as the persistent data source and source of truth.
     *
     * @param cache the memory-based {@link JobStore} serving as the cache for recent jobs.
     *              Must not be null.
     * @param backingStore the backing {@link JobStore} that serves as the persistent
     *                     storage. This store is responsible for all durable data operations.
     *                     Must not be null.
     */
    public CachedJobStore(JobStore cache, JobStore backingStore) {
        this.cache = Objects.requireNonNull(cache, "cache");
        this.backingStore = Objects.requireNonNull(backingStore, "backingStore");
    }

    /**
     * Stores the specified job in both the backing store and the memory cache.
     * The job is first stored in the backing store to ensure durability, and then
     * a copy is stored in the memory cache for faster subsequent access.
     *
     * @param job the {@link BaseUWSJob} to be stored. Must not be null.
     */
    @Override
    public void store(BaseUWSJob job) {
        backingStore.store(job);
        cache.store(job);
    }

    /**
     * Retrieves the specified job from the memory cache, falling back to the backing store if not found.
     *
     * @param id the ID of the job to retrieve. Must not be null.
     * @return the {@link BaseUWSJob} if found, or null if not found.
     */
    @Override
    public BaseUWSJob retrieve(String id) {
        BaseUWSJob job = cache.retrieve(id);
        if (job != null) {
            return job;
        }

        job = backingStore.retrieve(id);
        if (job != null) {
            cache.store(job);
        }

        return job;
    }

    /**
     * Deletes the specified job from both the backing store and the memory cache.
     *
     * @param id the ID of the job to delete. Must not be null.
     * @return true if the job was deleted, false otherwise.
     */
    @Override
    public boolean delete(String id) {
        boolean deleted = backingStore.delete(id);
        cache.delete(id);
        return deleted;
    }

    /**
     * Returns a set of all job IDs from the backing store.
     *
     * @return a set of all job IDs.
     */
    @Override
    public Set<String> getAllIds() {
        return backingStore.getAllIds();
    }

    /**
     * Retrieves a list of jobs from the backing store based on the specified execution phase,
     * creation or modification time, and optionally limits the number of jobs returned.
     * The retrieved jobs are also cached for faster future retrieval.
     *
     * @param phase the execution phase of the jobs to retrieve. May be null to retrieve jobs of any phase.
     * @param after a timestamp specifying that only jobs created or modified after this time
     *              should be retrieved. May be null to ignore this filter.
     * @param last an optional limit on the number of jobs to retrieve. May be null to retrieve all matching jobs.
     * @return a list of {@link BaseUWSJob} instances matching the specified criteria.
     *         The list may be empty if no jobs match.
     */
    @Override
    public List<BaseUWSJob> getJobs(ExecutionPhase phase, ZonedDateTime after, Integer last) {
        List<BaseUWSJob> jobs = backingStore.getJobs(phase, after, last);
        jobs.forEach(cache::store);
        return jobs;
    }
}
