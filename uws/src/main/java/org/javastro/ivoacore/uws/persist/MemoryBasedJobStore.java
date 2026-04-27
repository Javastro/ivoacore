package org.javastro.ivoacore.uws.persist;


/*
 * Created on 04/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.javastro.ivoacore.uws.BaseUWSJob;
import org.javastro.ivoa.entities.uws.ExecutionPhase;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An in-memory implementation of {@link JobStore} using a {@link java.util.concurrent.ConcurrentHashMap}.
 */
public class MemoryBasedJobStore implements JobStore {

   final Map<String, BaseUWSJob> jobs = new ConcurrentHashMap<>();
   @Override
   public void store(BaseUWSJob job) {
      jobs.put(job.getID(), job);
   }

   @Override
   public BaseUWSJob retrieve(String id) {
     return jobs.get(id);
   }

   @Override
   public boolean delete(String id) {
      return jobs.remove(id) != null;
   }

   @Override
   public Set<String> getAllIds() {
      return Set.copyOf(jobs.keySet());// make unmodifiable
   }

   @Override
   public List<BaseUWSJob> getJobs(ExecutionPhase phase, ZonedDateTime after, Integer last) {
      List<BaseUWSJob> filtered = jobs.values().stream()
            .filter(job -> phase == null || job.getExecutionPhase() == phase)
            .filter(job -> after == null || !job.asJob().getCreationTime().isBefore(after))
            .sorted(Comparator.comparing(job -> job.asJob().getCreationTime()))
            .toList();

      if (last == null || last < 0 || last >= filtered.size()) {
         return filtered;
      }

      return filtered.subList(filtered.size() - last, filtered.size());
   }
}
