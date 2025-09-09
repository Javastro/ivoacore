package org.javastro.ivoacore.uws.persist;


/*
 * Created on 04/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.javastro.ivoacore.uws.BaseUWSJob;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
      return jobs.keySet();
   }
}
