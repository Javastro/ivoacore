package org.javastro.ivoacore.uws.persist;


import org.javastro.ivoa.entities.uws.Results;
import org.javastro.ivoacore.uws.BaseUWSJob;
import org.javastro.ivoacore.uws.JobSpecification;
import org.javastro.ivoacore.uws.environment.ExistingExecutionEnvironment;


/**
 * A Job that is restored from long term storage. It cannot be directly executed,
 * but it can be used to access the results of a previously executed job.
 *
 * @author Paul Harrison (paul.harrison@manchester.ac.uk) */
class RestoredUWSJob extends BaseUWSJob {
   protected RestoredUWSJob(UWSJobEntity entity, JobSpecification jobSpecification) {
      super(entity.jobId, jobSpecification, new ExistingExecutionEnvironment(null,entity.workdir));
      executionPhase = entity.executionPhase;
      creationTime = entity.creationTime;
      startTime = entity.startTime;
      endTime = entity.endTime;
   }

   @Override
   public Results createExternalJobResult() {
      logger.warn("RestoredUWSJob.createExternalJobResult() called - this is not implemented yet.");
      return null; //FIXME need to implement this - probably still need factory pattern to do this, or store representation in the database.
   }
}
