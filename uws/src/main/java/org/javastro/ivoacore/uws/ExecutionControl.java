package org.javastro.ivoacore.uws;


public interface ExecutionControl extends UWS {

   BaseUWSJob createJob(JobSpecification specification) throws UWSException;


}
