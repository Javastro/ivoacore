

package org.javastro.ivoacore.uws.webapi;


import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoa.entities.uws.Parameters;
import org.javastro.ivoa.entities.uws.Results;
import org.javastro.ivoacore.uws.UWSControl;
import org.javastro.ivoacore.uws.UWSException;

import java.time.ZonedDateTime;


/**
 * Universal Worker service (UWS) interface REST operations and external representations. Note that this excludes operations
 * that create, run and abort jobs.
 */
public interface UWS extends UWSControl {


    /**
     * Get the execution phase of a job.
     * @param jobid
     * @return
     * @throws UWSException
     */
    @GET
    @Path("/{jobid}/phase")
    default ExecutionPhase getPhase(@PathParam("jobid") String jobid) throws UWSException {
        return  jobDetail(jobid).getPhase();
    }

    /**
     * @param jobid
     * @return
     * @throws UWSException
     */
    @GET
    @Path("/{jobid}/executionduration")
    default Long getExecutionDuration(@PathParam("jobid")String jobid) throws UWSException
    {
        return (long) jobDetail(jobid).getExecutionDuration();
    }

    @GET
    @Path("/{jobid}/destruction")
    default ZonedDateTime getJobDestruction(@PathParam("jobid") String jobid) throws UWSException
    {
        return jobDetail(jobid).getDestruction();
    }

    @GET
    @Path("/{jobid}/error")
    default String getJobDError(@PathParam("jobid") String jobid) throws UWSException
    {
        return jobDetail(jobid).getErrorSummary().getMessage();
    }

    @GET
    @Path("/{jobid}/owner")
    default String  getJobOwner(@PathParam("jobid") String jobid) throws UWSException
    {
        return jobDetail(jobid).getOwnerId();
    }

    @GET
    @Path("/{jobid}/quote")
    default ZonedDateTime getJobQuote(@PathParam("jobid") String jobid) throws UWSException
    {
        return jobDetail(jobid).getQuote();
    }

    @GET
    @Path("/{jobid}/parameters")
    default Parameters getJobParameters(@PathParam("jobid") String jobid) throws UWSException
    {
        return jobDetail(jobid).getParameters();
    }

    /**
     * Get the result list for a job. This will not return until the results have been created.
     * @param jobId
     * @return
     * @throws UWSException
     */
    @GET
    @Path("/{jobid}/results")
    default Results getResults(@PathParam("jobid") String jobId) throws UWSException
    {
        return jobDetail(jobId).getResults();
    }
}


