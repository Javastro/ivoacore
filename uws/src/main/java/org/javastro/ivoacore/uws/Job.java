package org.javastro.ivoacore.uws;


import org.javastro.ivoacore.uws.environment.execution.ParameterValue;

import java.util.List;

/**
 * Represents the executable part of a UWS job that performs the actual work.
 */
public interface Job {

   /**
    * What the job actually does. This is where the business logic of each job type is
    * implemented.
    * @return A list of parameter Values.
    */
    List<ParameterValue> performAction() throws UWSException;

}
