

package org.javastro.ivoacore.uws.webapi;


import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoa.entities.uws.Parameters;
import org.javastro.ivoa.entities.uws.Results;
import org.javastro.ivoacore.uws.UWSControl;
import org.javastro.ivoacore.uws.UWSException;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


/**
 * Universal Worker service (UWS) interface REST operations and external representations. Note that this excludes operations
 * that create, run and abort jobs.
 */
public interface UWS extends UWSControl {


    /**
     * Get the execution phase of a job.
     * @param jobid the identifier of the job.
     * @return the current {@link ExecutionPhase} of the job.
     * @throws UWSException if the job cannot be found or accessed.
     */
    @GET
    @Path("{jobid}/phase")
    default String getPhase(@PathParam("jobid") String jobid) throws UWSException {
        return  jobDetail(jobid).getPhase().value();
    }

    /**
     * Get the maximum execution duration of a job in seconds.
     * @param jobid the identifier of the job.
     * @return the maximum execution duration in seconds.
     * @throws UWSException if the job cannot be found or accessed.
     */
    @GET
    @Path("{jobid}/executionduration")
    @Produces(MediaType.TEXT_PLAIN)
    default Long getExecutionDuration(@PathParam("jobid")String jobid) throws UWSException
    {
        return (long) jobDetail(jobid).getExecutionDuration();
    }

    /**
     * Get the destruction time for a job.
     * @param jobid the identifier of the job.
     * @return the destruction time as a strubg.
     * @throws UWSException if the job cannot be found or accessed.
     */
    @GET
    @Path("{jobid}/destruction")
    @Produces(MediaType.TEXT_PLAIN)
    default String getJobDestruction(@PathParam("jobid") String jobid) throws UWSException
    {
        //IMPL made the return a string as that was the easiest way to get the ZonedDateTime to be returned in the correct format.
        return jobDetail(jobid).getDestruction().truncatedTo(java.time.temporal.ChronoUnit.SECONDS)
              .withZoneSameInstant(java.time.ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
    }


    /**
     * Get the owner identifier of a job.
     * @param jobid the identifier of the job.
     * @return the owner ID string.
     * @throws UWSException if the job cannot be found or accessed.
     */
    @GET
    @Path("{jobid}/owner")
    default String  getJobOwner(@PathParam("jobid") String jobid) throws UWSException
    {
        return jobDetail(jobid).getOwnerId();
    }

    /**
     * Get the quote (estimated completion time) for a job.
     * @param jobid the identifier of the job.
     * @return the quoted completion time as a {@link ZonedDateTime}.
     * @throws UWSException if the job cannot be found or accessed.
     */
    @GET
    @Path("{jobid}/quote")
    default ZonedDateTime getJobQuote(@PathParam("jobid") String jobid) throws UWSException
    {
        return jobDetail(jobid).getQuote();
    }

    /**
     * Get the parameters of a job.
     * @param jobid the identifier of the job.
     * @return the {@link Parameters} of the job.
     * @throws UWSException if the job cannot be found or accessed.
     */
    @GET
    @Path("{jobid}/parameters")
    default Parameters getJobParameters(@PathParam("jobid") String jobid) throws UWSException
    {
        return jobDetail(jobid).getParameters();
    }

    /**
     * Get the result list for a job. This will not return until the results have been created.
     * @param jobId the identifier of the job.
     * @return the {@link Results} of the job.
     * @throws UWSException if the job cannot be found or accessed.
     */
    @GET
    @Path("{jobid}/results")
    default Results getResults(@PathParam("jobid") String jobId) throws UWSException
    {
        return jobDetail(jobId).getResults();
    }
}


