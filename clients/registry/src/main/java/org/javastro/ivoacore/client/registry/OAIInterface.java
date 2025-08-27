package org.javastro.ivoacore.client.registry;


/*
 * Created on 21/08/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.List;


/**
 * Defines the basics of what the OAI interface can do.
 */
public interface OAIInterface {

   public static record MetadataFormat(String metadataPrefix, String schema, String metadataNamespace){}
   public static record Set(String setSpec, String setName, String description){}
   /**
    * Represents the header of an OAI-PMH record.
    */
   public record Header(
         String identifier,
         String datestamp,
         List<String> setSpecs,
         boolean isDeleted
   ){}
   /**
    * Represents a single OAI-PMH record, containing a header and metadata.
    *
    * @param metadata Metadata is kept as a DOM Element
    */
   public record Record(Header header, Element metadata) {

      /**
       * Gets the metadata section as a formatted XML string.
       *
       * @return A string containing the XML of the metadata, or an error message.
       */
      public String getMetadataXmlString() {
         try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(metadata), new StreamResult(writer));
            return writer.toString();
         } catch (Exception e) {
            return "Error converting metadata to string: " + e.getMessage();
         }
      }

      @Override
      public String toString() {
         return "Record{\n  header=" + header + ",\n  metadata=" + getMetadataXmlString() + "\n}";
      }
   }


   public Record getRecord(String identifier, String metadataPrefix) throws OAIException;
   public Element identify()  throws OAIException;
   public List<Header> listIdentifiers(String metadataPrefix, ZonedDateTime from, ZonedDateTime until, String set, String resumptionToken) throws OAIException;
   public List<Record> listRecords(String metadataPrefix, ZonedDateTime from, ZonedDateTime until, String set, String resumptionToken) throws OAIException;
   public List<MetadataFormat> listMetadataFormats(String identifier) throws OAIException;
   public List<Set> listSets() throws OAIException;
}
