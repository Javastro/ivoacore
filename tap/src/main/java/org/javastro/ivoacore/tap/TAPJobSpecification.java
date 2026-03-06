package org.javastro.ivoacore.tap;


/*
 * Created on 09/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

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
public class TAPJobSpecification extends BaseJobSpecification {


    String adqlQuery;
    Long maxrec;
    String responseFormat;
    String lang;


   List<ParameterValue> parameters = new ArrayList<>();
   /**
    * Create the Job Specification.
    * @param query the ADQL query.
    * @param runId
    */
   public TAPJobSpecification(String query, String lang, String responseformat, Long maxrec, String runId,
                              String upload) {
      super(runId,List.of(new ImmutableStringValue("QUERY", query),
            new ImmutableStringValue("MAXREC",maxrec.toString()),
            new ImmutableStringValue("RESPONSEFORMAT",responseformat),
            new ImmutableStringValue("LANG",lang)));
      this.adqlQuery = query;
      this.maxrec = maxrec;
      this.responseFormat = responseformat;
      this.lang = lang;

   }

   public TAPJobSpecification(String query){
      this(query,"ADQL","votable", 5000L,null,null);
   }


   @Override
   public String jobTypeIdentifier() {
      return TAPJob.JOB_TYPE;
   }

   @Override
   public String getJDL() {
      return "";
   }


}
