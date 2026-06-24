package org.javastro.ivoacore.tap;


/*
 * Created on 09/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.javastro.ivoacore.tap.upload.TAPUploadCacher;
import org.javastro.ivoacore.uws.BaseJobSpecification;
import org.javastro.ivoacore.uws.environment.execution.ParameterValue;
import org.javastro.ivoacore.uws.environment.parameter.ImmutableStringValue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A "default" simple TAP Job specification.
 */
@JsonTypeName("TAP")
public class TAPJobSpecification extends BaseJobSpecification {

   String adqlQuery;
   Long maxrec;
   String responseFormat;
   String lang;
   TAPUploadCacher uploader;
   Map<String, Path> uploads;   /** table name -> URL to the file */

   List<ParameterValue> parameters = new ArrayList<>();

   /**
    * Create the Job Specification, where uploads have been resolved.
    * @param query the ADQL query.
    * @param lang the query language (e.g. "ADQL").
    * @param responseformat the desired response format (e.g. "votable").
    * @param maxrec the maximum number of records to return.
    * @param runId the run identifier for this job.
    * @param uploads Map of {@code String} to {@code Path}  table name and location of VOTable, or {@code null} if not used.
    */
   public TAPJobSpecification(String query, String lang, String responseformat, Long maxrec, String runId,
                              Map<String, Path> uploads) {
      super(runId,buildParameters(query,maxrec,responseformat,lang));
      this.adqlQuery = query;
      this.maxrec = maxrec;
      this.responseFormat = responseformat;
      this.lang = lang;
      this.uploads = uploads;
   }


   /**
    * Create the TAP Job Specification.
    * @param query the ADQL query.
    * @param lang the query language (e.g. "ADQL").
    * @param responseformat the desired response format (e.g. "votable").
    * @param maxrec the maximum number of records to return.
    * @param runId the run identifier for this job.
    * @param uploader an uploader to handle any uploads associated with this job.
    */
   public TAPJobSpecification(String query, String lang, String responseformat, Long maxrec, String runId,
                              TAPUploadCacher uploader) {

      this(query, lang, responseformat, maxrec, runId, new HashMap<>());
      this.uploader = uploader;
   }

   @JsonCreator
   public TAPJobSpecification(
           @JsonProperty("runId") String runId,
           @JsonProperty("parameters")
           List<ParameterValue> parameters) {

      super(runId, parameters);
      this.parameters = parameters;
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
      this(query,"ADQL","votable", 5000L,null,new HashMap<>());
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

   public Map<String, Path> getUploads() { return uploads; }
}
