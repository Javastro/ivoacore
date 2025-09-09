package org.javastro.ivoacore.uws;


import org.javastro.ivoacore.uws.environment.execution.ParameterValue;

import java.util.List;

public interface Job {


   /**
    * What the job actually does.
    * @return A list of parameter Values.
    */
    List<ParameterValue> runJob();

}
