/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.uws.webapi;

/*
 * Created on 05/03/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import jakarta.ws.rs.*;
import org.javastro.ivoa.entities.uws.*;
import org.javastro.ivoacore.uws.JobManager;
import org.javastro.ivoacore.uws.UWSException;

import java.time.ZonedDateTime;

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


   private ShortJobDescription shorten(Job job)
   {
      return new ShortJobDescription(job.getPhase(), job.getRunId(), job.getOwnerId(),job.getCreationTime(), job.getJobId(), "type", "href");
   }

}
