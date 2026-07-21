package org.javastro.ivoacore.uws.persist;

import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoacore.uws.BaseUWSJob;

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
public final class RecordingJobStore  extends MemoryBasedJobStore{
    public final List<String> calls = new ArrayList<>();

    public boolean failOnStore;
    public boolean deleteResult = true;

    @Override
    public void store(BaseUWSJob job) {
        calls.add("store:" + job.getID());
        if (failOnStore) {
            throw new RuntimeException("store failed");
        }
        super.store(job);
    }

    @Override
    public BaseUWSJob retrieve(String id) {
        calls.add("retrieve:" + id);
        return super.retrieve(id);
    }

    @Override
    public boolean delete(String id) {
        calls.add("delete:" + id);
        return super.delete(id);
    }

    @Override
    public Set<String> getAllIds() {
        calls.add("getAllIds");
        return super.getAllIds();
    }

    @Override
    public List<BaseUWSJob> getJobs(ExecutionPhase phase, ZonedDateTime after, Integer last) {
        calls.add("getJobs");
        return super.getJobs(phase, after, last);
    }
}
