package org.javastro.ivoacore.uws;


import org.javastro.ivoacore.uws.description.JobType;

public interface JobFactory extends JobType { //TODO not quite right that JobFactory extends JobType for the JobFactoryAggregator

   BaseUWSJob createJob(JobSpecification jobDescription) throws UWSException;
}
