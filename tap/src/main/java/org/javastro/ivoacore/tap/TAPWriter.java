/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.tap;


import uk.ac.starlink.votable.DataFormat;
import uk.ac.starlink.votable.VOSerializer;
import uk.ac.starlink.votable.VOTableVersion;
import uk.ac.starlink.votable.VOTableWriter;

import java.net.URI;

/**
 * TAP-specific VOTable writer. It implements writing Resource level INFO.
 */
public class TAPWriter extends VOTableWriter {

   final private TAPJob job;
   private boolean timeout = false;
   private URI jobUri = null;

   public TAPWriter(TAPJob job) {
      this.job = job;
   }

   public TAPWriter(DataFormat dataFormat, boolean inline, TAPJob job) {
      super(dataFormat, inline);
      this.job = job;
   }



   public TAPWriter(DataFormat dataFormat, boolean inline, VOTableVersion version, TAPJob job) {
      super(dataFormat, inline, version);
      this.job = job;
   }

   public void setTimeoutInfo(URI jobUri) {
      this.timeout = true;
      this.jobUri = jobUri;
   }
   @Override
   protected String createResourceStartTag() {
      StringBuffer sbuf = new StringBuffer( super.createResourceStartTag());
      sbuf.append("\n<INFO name=\"QUERY\" ").append(VOSerializer
            .formatAttribute("value", ((TAPJobSpecification)job.getJobSpecification()).getAdqlQuery() )).append("/>\n");
      if(!timeout) {
         if (job.getExecutionPhase() == org.javastro.ivoa.entities.uws.ExecutionPhase.ERROR) {
            sbuf.append("<INFO name=\"QUERY_STATUS\" value=\"ERROR\">\n");
            sbuf.append(job.getException().getMessage()); //TODO - do we want stack trace here?
            sbuf.append("</INFO>\n");
         } else {
            sbuf.append("<INFO name=\"QUERY_STATUS\" value=\"OK\"/>\n");
         }
      } else {
         sbuf.append("<INFO name=\"QUERY_STATUS\" value=\"TIMEOUT\"/>\n");
         sbuf.append("\n<INFO name=\"UWSJOBURI\" ").append(VOSerializer
               .formatAttribute("value",jobUri.toString())).append("/>\n");

      }
      if(job.getJobSpecification().getRunId() != null) {
         sbuf.append("\n<INFO name=\"RUNID\" ");
         sbuf.append(VOSerializer
               .formatAttribute("value", job.getJobSpecification().getRunId()));
         sbuf.append("/>\n");
      }
      return sbuf.toString();
   }
}
