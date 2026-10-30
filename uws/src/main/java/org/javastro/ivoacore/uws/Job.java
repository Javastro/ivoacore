package org.javastro.ivoacore.uws;


import org.javastro.ivoacore.uws.environment.ExecutionPolicy;
import org.javastro.ivoacore.uws.environment.execution.ParameterValue;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Represents the executable part of a UWS job that performs the actual work.
 */
public interface Job {

   /**
    * Returns the execution policy governing this job.
    * @return the job's execution policy.
    */
   ExecutionPolicy getExecutionPolicy();

   /**
    * Returns when this job should be destroyed.
    * @return the destruction time.
    */
   ZonedDateTime getDestructionTime();

   /**
    * Returns the maximum allowed execution duration in seconds.
    * @return the execution duration.
    */
   int getExecutionDuration();

   /**
    * What the job actually does. This is where the business logic of each job type is
    * implemented.
    * @return A list of parameter Values.
    * @throws UWSException if the job fails while executing.
    */
    List<ParameterValue> performAction() throws UWSException;

}
