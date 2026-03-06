package org.javastro.ivoacore.uws;


/*
 * Created on 06/03/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import jakarta.ws.rs.core.Response;

import java.time.ZonedDateTime;

public interface UWSControl extends UWSCore {

//IMPL these are methods for which the UWS standard does a 303 which is not the most "natural" RPC thing to to.
   /**
    * set the destruction time for a job. The destruction time is the time at which a job will be deteted from the UWS system.
    *
    * @param jobId
    * @return
    * @throws UWSException
    */
   Response setDestruction(String jobId, ZonedDateTime destructionTime) throws UWSException;

   /**
    * @param jobId
    * @param Long  A length of time that the job can run for.
    * @return
    * @throws UWSException
    */
   Response setExecutionDuration(String jobId, Long Long) throws UWSException;

   /**
    * Delete a job.
    *
    * @param jobId
    * @return
    * @throws UWSException
    */
   Response deleteJob(String jobId) throws UWSException;

}
