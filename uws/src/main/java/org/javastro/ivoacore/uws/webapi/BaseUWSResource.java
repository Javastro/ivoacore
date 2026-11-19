/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.uws.webapi;

/*
 * Created on 05/03/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.hibernate.query.sqm.spi.DelegatingSqmSelectionQueryImplementor;
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
    * @return HTTP redirect response to the relevant job resource.
    */
   protected abstract Response redirectToJob(String jobid);


   @GET
   @Override
   public Jobs listJobs(@QueryParam("PHASE") String phase, @QueryParam("AFTER") ZonedDateTime after, @QueryParam("LAST") Integer last) throws UWSException {
      return getJobManager().listJobs(phase, after, last);
   }

   @GET
   @Path("{jobid}")
   @Override
   public Job jobDetail(@PathParam("jobid") String jobid) throws UWSException {
      final Job job = getJobManager().jobDetail(jobid);
      if(job != null) {
         return job;
      }
      else {
         throw new NotFoundException("Job with id " + jobid + " not found");
      }
   }

   /**
    * Performs a job delete with an FORM action.
    * @param jobid the JobID
    * @param action the action paramters
    * @return a redirect to the job list.
    * @throws UWSException if there is a problem.
    */
   @POST
   @Path("{jobid}")
   @Transactional
   public Response jobActionDelete(@PathParam("jobid") String jobid, @FormParam("ACTION") String action) throws UWSException {
      final Job job = getJobManager().jobDetail(jobid);
      if (job != null) {

         switch(action){
            case "DELETE":
               boolean success = getJobManager().deleteJob(jobid);
               if(success) {
                  return redirectToJob(null);
               } else {
                  throw new InternalServerErrorException("Failed to delete job " + jobid);
               }
            default:
               throw new BadRequestException("Unknown action: " + action);
         }

      } else {
         throw new NotFoundException("Job with id " + jobid + " not found");
      }
   }



   @GET
   @Path("{jobid}/results")
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
   @Path("{jobid}/phase")
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
   @Path("{jobid}/error")
   public String getJobErrorDetail(@PathParam("jobid") String jobid) throws UWSException {
      return getJobManager().jobErrorDetail(jobid);
   }

   @Override
   @POST
   @Path("{jobid}/destruction")
   public Response setDestruction(@PathParam("jobid")String jobId, @FormParam("DESTRUCTION") ZonedDateTime destructionTime) throws UWSException {
      //TODO determine whether to handle potential UWSException from setDestruction, or let it propagate
      getJobManager().setDestruction(jobId, destructionTime);
      return redirectToJob(jobId);
   }

   @POST
   @Path("{jobid}/executionduration")
   @Override
   public Response setExecutionDuration(@PathParam("jobid")String jobId, @FormParam("EXECUTIONDURATION") Long executionDuration) throws UWSException {
      //TODO determine whether to handle potential UWSException from setExecutionDuration, or let it propagate
      getJobManager().setExecutionDuration(jobId, executionDuration);
      return redirectToJob(jobId);
   }

   @Override
   @DELETE
   @Transactional
   @Path("{jobid}")
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


   /**
    * set the parameters for a job. This is a placeholder implementation that only supports setting the RUNID parameter.
    * The actual implementation will depend on the specific UWS JOB implementation and how it handles parameters
    * @param jobid the job identifier.
    * @param formParams the form parameters.
    * @return a redirect if successful.
    * @throws UWSException on errors.
    */
   @POST
   @Path("{jobid}/parameters")
   @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
   public Response setParameters(@PathParam("jobid")String jobid,MultivaluedMap<String, String> formParams) throws UWSException {

      Job detail = getJobManager().jobDetail(jobid);
      if(detail.getPhase() == ExecutionPhase.PENDING) {
         if (formParams == null || formParams.isEmpty()) {
            throw new BadRequestException("No parameters provided to set for job " + jobid);
         }

         if(formParams.keySet().stream().anyMatch(param -> param.equalsIgnoreCase("RUNID"))){
            String runid = formParams.entrySet().stream()
                  .filter(entry -> entry.getKey().equalsIgnoreCase("RUNID"))
                  .map(entry -> entry.getValue().get(0)) // Assuming single value for RUNID
                  .findFirst()
                  .orElse(null);
            getJobManager().setRunId(jobid, runid);
            return redirectToJob(jobid);
         }
         else{
            //TODO - this is a placeholder - the actual implementation will depend on the specific UWS JOB implementation and how it handles parameters - need to think of a general API design
            // For now, we just return a 501 Not Implemented response
            return Response.status(Response.Status.NOT_IMPLEMENTED)
                  .entity("Setting general parameters is not implemented for this UWS implementation.")
                  .build();
         }
      }
      else {
         throw new BadRequestException("Cannot set parameters for job " + jobid + " in phase " + detail.getPhase());
      }

   }


}
