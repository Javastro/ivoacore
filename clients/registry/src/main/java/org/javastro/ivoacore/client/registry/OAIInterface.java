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

   /**
    * Represents a metadata format in the OAI-PMH service.
    * @param metadataPrefix the metadata prefix.
    * @param schema the schema.
    * @param metadataNamespace the namespace.
    *
    * @author Paul Harrison (paul.harrison@manchester.ac.uk)    */
   public static record MetadataFormat(String metadataPrefix, String schema, String metadataNamespace){}

   /**
    * Represents a set in the OAI-PMH service.
    * @param setSpec the set id.
    * @param setName the set name.
    * @param description the description of the set.
    *
    * @author Paul Harrison (paul.harrison@manchester.ac.uk)    */
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

   /**
    * Get a record.
    * @param identifier the identifier for the record.
    * @param metadataPrefix the type of matadata to return.
    * @return the returned record.
    * @throws OAIException on any error.
    */
   public Record getRecord(String identifier, String metadataPrefix) throws OAIException;

   /**
    * The Identification of the OAI-PMH service. This is the first call that should be made to any OAI-PMH service.
    * @return The identification.
    * @throws OAIException on any error condition.
    */
   public Element identify()  throws OAIException;

   /**
    * List identifiers for records in the OAI-PMH service.
    * @param metadataPrefix the metadata type.
    * @param from starting date for searching for records.
    * @param until ending date for searching for records.
    * @param set the set identifier fo the records.
    * @param resumptionToken a resumption token in the case of paged return of records.
    * @return The list of header records for the identifiers.
    * @throws OAIException on any error.
    */
   public List<Header> listIdentifiers(String metadataPrefix, ZonedDateTime from, ZonedDateTime until, String set, String resumptionToken) throws OAIException;
   /**
    * List records in the OAI-PMH service.
    * @param metadataPrefix the metadata type.
    * @param from starting date for searching for records.
    * @param until ending date for searching for records.
    * @param set the set identifier fo the records.
    * @param resumptionToken a resumption token in the case of paged return of records.
    * @return The list of header records for the identifiers.
    * @throws OAIException on any error.
    */
   public List<Record> listRecords(String metadataPrefix, ZonedDateTime from, ZonedDateTime until, String set, String resumptionToken) throws OAIException;

   /**
    * list the metadata formats available for a particular record.
    * @param identifier the identifier of the record.
    * @return the list of metadata formats.
    * @throws OAIException on any error.
    */
   public List<MetadataFormat> listMetadataFormats(String identifier) throws OAIException;

   /**
    * list the sets that group any records in the archive.
    * @return the list of sets.
    * @throws OAIException on any error.
    */
   public List<Set> listSets() throws OAIException;
}
