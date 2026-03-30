/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.uws.webapi;

/*
 * Created on 05/03/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.javastro.ivoa.entities.uws.*;
import org.javastro.ivoacore.uws.JobManager;
import org.javastro.ivoacore.uws.UWSException;

import java.time.ZonedDateTime;

/**
 * Base JAX-RS resource implementation of the {@link UWS} interface, providing default
 * HTTP endpoint implementations delegating to a {@link JobManager}.
 */
public abstract class BaseUWSResource implements UWS {

   /**
    * get the JobManager.
    * @return the JobManager.
    */
   protected abstract JobManager getJobManager();


   @GET
   @Override
   public Jobs listJobs(@QueryParam("PHASE") String phase, @QueryParam("AFTER") ZonedDateTime after, @QueryParam("LAST") Integer last) throws UWSException {
      return getJobManager().listJobs(phase, after, last);
   }

   @GET
   @Path("/{jobid}")
   @Override
   public Job jobDetail(@PathParam("jobid") String jobid) throws UWSException {
      return getJobManager().jobDetail(jobid);
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
    * @param uriInfo JAX-RS URI information for building the redirect response.
    * @return a 303 redirect response to the job resource.
    * @throws UWSException if the phase transition fails.
    */
   @POST
   @Path("/{jobid}/phase")
   public Response setPhase(@PathParam("jobid") String jobid, @FormParam("PHASE") String phase, @Context UriInfo uriInfo) throws UWSException {
      ExecutionPhase newphase = getJobManager().setPhase(jobid, phase);
      Response retval = Response.seeOther(uriInfo.getAbsolutePathBuilder()
            .path(jobid).build()).build();

      return retval;
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
   public Response setDestruction(@PathParam("jobid")String jobId, @FormParam("DESTRUCTION") ZonedDateTime destructionTime, @Context UriInfo uriInfo) throws UWSException {
      throw new UWSException("Not implemented");
   }

   @POST
   @Path("/{jobid}/executionduration")
   @Override
   public Response setExecutionDuration(@PathParam("jobid")String jobId, @FormParam("EXECUTIONDURATION") Long executionDuration, @Context UriInfo uriInfo) throws UWSException {
      throw new UWSException("Not implemented");
   }

   @Override
   @DELETE
   @Path("/{jobid}")
   public Response deleteJob(@PathParam("jobid")String jobid, @Context UriInfo uriInfo) throws UWSException {
      throw new UWSException("Not supported yet.");
   }
}
