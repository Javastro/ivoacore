package org.javastro.ivoacore.uws.persist;


import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoacore.uws.BaseUWSJob;
import org.javastro.ivoacore.uws.UWSException;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

/**
 * Persistent store for UWS job instances.
 */
public interface JobStore {

   /**
    * Stores a job in this store.
    * @param job the job to store.
    */
   void store(BaseUWSJob job);

   /**
    * Retrieves a job by its identifier.
    * @param id the job identifier.
    * @return the stored job, or {@code null} if not found.
    */
   BaseUWSJob retrieve(String id);

   /**
    * Deletes a job from this store.
    * @param id the job identifier.
    * @return {@code true} if the job was deleted, {@code false} if not found.
    */
   boolean delete(String id);

   /**
    * Returns the set of all job identifiers in this store.
    * @return the set of job ID strings.
    */
   Set<String> getAllIds();
   /**
    * List of jobs known to the UWS system.
    *
    * @param phase filter jobs by execution phase; may be {@code null} for no filtering.
    * @param after filter jobs created after this time; may be {@code null} for no filtering.
    * @param last return only the last N jobs; may be {@code null} for no limit.
    * @return the list of jobs matching the filter criteria.
    *
    */
   List<BaseUWSJob> getJobs(ExecutionPhase phase, ZonedDateTime after, Integer last);
}
