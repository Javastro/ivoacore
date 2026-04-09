/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.uws.webapi;

/*
 * Created on 05/03/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.javastro.ivoa.entities.uws.*;
import org.javastro.ivoacore.uws.JobManager;
import org.javastro.ivoacore.uws.UWSException;

import java.time.ZonedDateTime;

/**
 * Base JAX-RS resource implementation of the {@link UWS} interface, providing default
 * HTTP endpoint implementations delegating to a {@link JobManager}.
 */
@Produces(MediaType.APPLICATION_XML)
public abstract class BaseUWSResource implements UWS {

   /**
    * get the JobManager.
    * @return the JobManager.
    */
   protected abstract JobManager getJobManager();

   /**
    * return the correct job redirection. Abstract as that depended on the implementation.
    * @param jobid the job identifier. Note if this is null the redirect will be to the job list.
    * @return
    */
   protected abstract Response redirectToJob(String jobid);


   @GET
   @Override
   public Jobs listJobs(@QueryParam("PHASE") String phase, @QueryParam("AFTER") ZonedDateTime after, @QueryParam("LAST") Integer last) throws UWSException {
      return getJobManager().listJobs(phase, after, last);
   }

   @GET
   @Path("/{jobid}")
   @Override
   public Job jobDetail(@PathParam("jobid") String jobid) throws UWSException {
      final Job job = getJobManager().jobDetail(jobid);

      return job; //FIXME need 404 for not found...when job is null - perhaps do with exception handler...
   }

   @GET
   @Path("/{jobid}/results")
   @Override
   public Results getResults(@PathParam("jobid") String jobid) throws UWSException {
      //IMPL getting the results might require more than just the below - which is why it is implemented here
      return  getJobManager().jobDetail(jobid).getResults();
   }


   /**
    * Creates a {@link ShortJobDescription} from the given {@link Job}.
    * @param job the job to shorten.
    * @return a short description of the job.
    */
   private ShortJobDescription shorten(Job job)
   {
      return new ShortJobDescription(job.getPhase(), job.getRunId(), job.getOwnerId(),job.getCreationTime(), job.getJobId(), "type", "href");
   }

   /**
    * Sets the execution phase of a job (e.g., to "RUN" or "ABORT") and redirects to the job resource.
    * @param jobid the identifier of the job.
    * @param phase the new phase string.
    * @return a 303 redirect response to the job resource.
    * @throws UWSException if the phase transition fails.
    */
   @POST
   @Path("/{jobid}/phase")
   public Response setPhase(@PathParam("jobid") String jobid, @FormParam("PHASE") String phase) throws UWSException {
      ExecutionPhase newphase = getJobManager().setPhase(jobid, phase);
      return redirectToJob(jobid);
   }

   /**
    * Get the error detail message for a job.
    * @param jobid the identifier of the job.
    * @return the error message string, or {@code null} if no error.
    * @throws UWSException if the job cannot be found or accessed.
    */
   @GET
   @Path("/{jobid}/error")
   public String getJobErrorDetail(@PathParam("jobid") String jobid) throws UWSException
   {
      return getJobManager().jobErrorDetail(jobid);
   }



   @Override
   @POST
   @Path("/{jobid}/destruction")
   public Response setDestruction(@PathParam("jobid")String jobId, @FormParam("DESTRUCTION") ZonedDateTime destructionTime) throws UWSException {
      throw new UWSException("Not implemented");
   }

   @POST
   @Path("/{jobid}/executionduration")
   @Override
   public Response setExecutionDuration(@PathParam("jobid")String jobId, @FormParam("EXECUTIONDURATION") Long executionDuration) throws UWSException {
      throw new UWSException("Not implemented");
   }

   @Override
   @DELETE
   @Path("/{jobid}")
   public Response deleteJob(@PathParam("jobid")String jobid) throws UWSException {
      boolean success = getJobManager().deleteJob(jobid);
      if (success) {
         return redirectToJob(null);
      } else {

         return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
               .entity("Failed to delete job " + jobid)
               .build();
      }
   }

}
