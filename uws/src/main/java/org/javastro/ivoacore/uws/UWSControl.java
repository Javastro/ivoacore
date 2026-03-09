package org.javastro.ivoacore.uws;


/*
 * Created on 06/03/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.time.ZonedDateTime;

/**
 * Extension of {@link UWSCore} providing job lifecycle control operations (set destruction, execution duration, deletion)
 * that map to HTTP 303 redirect responses in the UWS REST API.
 */
public interface UWSControl extends UWSCore {

//IMPL these are methods for which the UWS standard does a 303 which is not the most "natural" RPC thing to to.
   /**
    * set the destruction time for a job. The destruction time is the time at which a job will be deteted from the UWS system.
    *
    * @param jobId the identifier of the job.
    * @param destructionTime the new destruction time for the job.
    * @param uriInfo JAX-RS URI information used for building redirect responses.
    * @return a redirect response to the job resource.
    * @throws UWSException if the destruction time cannot be set.
    */
   Response setDestruction(String jobId, ZonedDateTime destructionTime, @Context UriInfo uriInfo) throws UWSException;

   /**
    * Set the maximum execution duration for a job.
    * @param jobId the identifier of the job.
    * @param Long A length of time in seconds that the job can run for.
    * @param uriInfo JAX-RS URI information used for building redirect responses.
    * @return a redirect response to the job resource.
    * @throws UWSException if the execution duration cannot be set.
    */
   Response setExecutionDuration(String jobId, Long Long, @Context UriInfo uriInfo) throws UWSException;

   /**
    * Delete a job.
    *
    * @param jobId the identifier of the job to delete.
    * @param uriInfo JAX-RS URI information used for building redirect responses.
    * @return a redirect response after deletion.
    * @throws UWSException if the job cannot be deleted.
    */
   Response deleteJob(String jobId, @Context UriInfo uriInfo) throws UWSException;

}
