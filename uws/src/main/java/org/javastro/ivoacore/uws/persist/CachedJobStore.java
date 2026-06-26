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

    public CachedJobStore(JobStore backingStore) {
        this(new MemoryBasedJobStore(), backingStore);
    }

    public CachedJobStore(JobStore cache, JobStore backingStore) {
        this.cache = Objects.requireNonNull(cache, "cache");
        this.backingStore = Objects.requireNonNull(backingStore, "backingStore");
    }

    @Override
    public void store(BaseUWSJob job) {
        backingStore.store(job);
        cache.store(job);
    }

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

    @Override
    public boolean delete(String id) {
        boolean deleted = backingStore.delete(id);
        cache.delete(id);
        return deleted;
    }

    @Override
    public Set<String> getAllIds() {
        return backingStore.getAllIds();
    }

    @Override
    public List<BaseUWSJob> getJobs(ExecutionPhase phase, ZonedDateTime after, Integer last) {
        List<BaseUWSJob> jobs = backingStore.getJobs(phase, after, last);
        jobs.forEach(cache::store);
        return jobs;
    }
}
