package org.javastro.ivoacore.uws.store;

import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoacore.uws.BaseUWSJob;
import org.javastro.ivoacore.uws.persist.JobStore;

import java.time.ZonedDateTime;
import java.util.*;

/**
 * A specialized {@code JobStore} implementation that records method calls
 * and keeps jobs in memory for testing purposes.
 *
 * The {@code RecordingJobStore} is primarily intended for tracking interactions
 * with the store and verifying its behaviour during test scenarios. It offers
 * optional failure modes and custom behaviours for flexibility in testing.
 */
public final class RecordingJobStore implements JobStore {
    public final Map<String, BaseUWSJob> jobs = new LinkedHashMap<>();
    public final List<String> calls = new ArrayList<>();

    public boolean failOnStore;
    public boolean deleteResult = true;

    @Override
    public void store(BaseUWSJob job) {
        calls.add("store:" + job.getID());
        if (failOnStore) {
            throw new RuntimeException("store failed");
        }
        jobs.put(job.getID(), job);
    }

    @Override
    public BaseUWSJob retrieve(String id) {
        calls.add("retrieve:" + id);
        return jobs.get(id);
    }

    @Override
    public boolean delete(String id) {
        calls.add("delete:" + id);
        jobs.remove(id);
        return deleteResult;
    }

    @Override
    public Set<String> getAllIds() {
        calls.add("getAllIds");
        return jobs.keySet();
    }

    @Override
    public List<BaseUWSJob> getJobs(ExecutionPhase phase, ZonedDateTime after, Integer last) {
        calls.add("getJobs");
        return new ArrayList<>(jobs.values());
    }
}
