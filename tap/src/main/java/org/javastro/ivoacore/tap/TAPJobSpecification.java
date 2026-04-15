package org.javastro.ivoacore.tap;


/*
 * Created on 09/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.javastro.ivoacore.uws.BaseJobSpecification;
import org.javastro.ivoacore.uws.environment.execution.ParameterValue;
import org.javastro.ivoacore.uws.environment.parameter.ImmutableStringValue;

import java.util.ArrayList;
import java.util.List;

/**
 * A "default" simple TAP Job specification.
 * i.e.
 * <ul>
 * <li>no upload</li>
 *</ul>
 */
@JsonTypeName("TAP")
public class TAPJobSpecification extends BaseJobSpecification {




   String adqlQuery;
    Long maxrec;
    String responseFormat;
    String lang;


   List<ParameterValue> parameters = new ArrayList<>();
   /**
    * Create the Job Specification.
    * @param query the ADQL query.
    * @param lang the query language (e.g. "ADQL").
    * @param responseformat the desired response format (e.g. "votable").
    * @param maxrec the maximum number of records to return.
    * @param runId the run identifier for this job.
    * @param upload the upload parameter value, or {@code null} if not used.
    */
   public TAPJobSpecification(String query, String lang, String responseformat, Long maxrec, String runId,
                              String upload) {
      super(runId,buildParameters(query,maxrec,responseformat,upload));
      this.adqlQuery = query;
      this.maxrec = maxrec;
      this.responseFormat = responseformat;
      this.lang = lang;

   }

   private static List<ParameterValue> buildParameters(String query, Long maxrec, String responseformat, String lang) {
      List<ParameterValue> parameters = new ArrayList<>();
      if (query != null) {
         parameters.add(new ImmutableStringValue("QUERY", query));
      }
      if (maxrec != null) {
         parameters.add(new ImmutableStringValue("MAXREC", maxrec.toString()));
      }
      if (responseformat != null) {
         parameters.add(new ImmutableStringValue("RESPONSEFORMAT", responseformat));
      }
      if (lang != null) {
         parameters.add(new ImmutableStringValue("LANG", lang));
      }
     return parameters;
   }

   /**
    * Convenience constructor for a simple TAP query using default settings.
    * @param query the ADQL query string.
    */
   public TAPJobSpecification(String query){
      this(query,"ADQL","votable", 5000L,null,null);
   }


   @Override
   public String jobTypeIdentifier() {
      return TAPJob.JOB_TYPE;
   }

   @Override
   public String getJDL() {
      return ""; //IMPL
   }


   public String getAdqlQuery() {
      return adqlQuery;
   }
}
