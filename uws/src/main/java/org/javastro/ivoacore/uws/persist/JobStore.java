package org.javastro.ivoacore.uws.persist;


import org.javastro.ivoacore.uws.BaseUWSJob;

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
}
