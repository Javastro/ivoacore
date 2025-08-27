package org.javastro.ivoacore.client.registry;


/*
 * Created on 21/08/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A client for interacting with an OAI-PMH (Open Archives Initiative
 * Protocol for Metadata Harvesting) repository.
 *
 * This client is self-contained and uses the modern java.net.http.HttpClient.
 */
public class BasicOAIClient implements OAIInterface {

   private final String baseUrl;
   private final DocumentBuilder docBuilder;
   private final HttpClient httpClient;

   private static final Logger log = LoggerFactory.getLogger(BasicOAIClient.class.getName());

   /**
       * Represents a response from a ListRecords or ListIdentifiers request,
       * which may include a resumption token for pagination.
       */
      public record ListRecordsResponse(List<Record> records, String resumptionToken) {

      public boolean hasResumptionToken() {
            return resumptionToken != null && !resumptionToken.isEmpty();
         }
      }

   /**
    * Main constructor for the client.
    * @param baseUrl The base URL of the OAI-PMH repository.
    * @throws Exception if the XML document builder cannot be initialized.
    */
   public BasicOAIClient(String baseUrl) throws Exception {
      this.baseUrl = baseUrl;
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      this.docBuilder = factory.newDocumentBuilder();
      // Initialize a single HttpClient to be reused for all requests.
      this.httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .build();
   }

   /**
    * Performs the 'ListRecords' OAI-PMH verb.
    * @param metadataPrefix The metadata format to retrieve (e.g., "oai_dc").
    * @return A ListRecordsResponse containing the first batch of records and a possible resumption token.
    * @throws Exception if the request fails or the response is invalid.
    */
   private ListRecordsResponse listRecords(String metadataPrefix) throws Exception {
      return listRecordsInternal(metadataPrefix, null,null,null,null);
   }

   private ListRecordsResponse listRecordsInternal(String metadataPrefix, String resumptionToken, ZonedDateTime from, ZonedDateTime until, String set) throws Exception {
      StringBuffer query = new StringBuffer( "?verb=ListRecords" );
      if (resumptionToken != null && !resumptionToken.isEmpty()) {
         query.append("&resumptionToken=").append(URLEncoder.encode(resumptionToken, StandardCharsets.UTF_8));
      } else {
         formQuery(metadataPrefix, from, until, set, query);
      }
      log.info("ListRecords: query={}", query.toString());

      Document doc = performRequest(query.toString());

      checkForOaiError(doc);

      List<Record> records = new ArrayList<>();
      NodeList recordNodes = doc.getElementsByTagNameNS("http://www.openarchives.org/OAI/2.0/","record");
      for (int i = 0; i < recordNodes.getLength(); i++) {
         Element recordElement = (Element) recordNodes.item(i);
         records.add(parseRecord(recordElement));
      }

      String newResumptionToken = getElementTextContent(doc.getDocumentElement(), "resumptionToken");


      return new ListRecordsResponse(records, newResumptionToken);
   }

   private static void formQuery(String metadataPrefix, ZonedDateTime from, ZonedDateTime until, String set, StringBuffer query) {
      if (metadataPrefix != null && !metadataPrefix.isEmpty()) {
         query.append("&metadataPrefix=").append(URLEncoder.encode(metadataPrefix, StandardCharsets.UTF_8));
      }
      if (set != null && !set.isEmpty()) {
         query.append("&set=").append(URLEncoder.encode(set, StandardCharsets.UTF_8));
      }
      if (from != null) {
         query.append("&from=").append(from.withZoneSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME));
      }
      if (until != null) {
         query.append("&until=").append(until.withZoneSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME));
      }
   }

   @Override
   public Record getRecord(String identifier, String metadataPrefix) throws OAIException {

      String query = "?verb=GetRecord&identifier=" + URLEncoder.encode(identifier, StandardCharsets.UTF_8)
            + "&metadataPrefix=" + URLEncoder.encode(metadataPrefix, StandardCharsets.UTF_8);

      Document doc = performRequest(query);
      System.err.println(doc.toString());
      checkForOaiError(doc);
      Element record = (Element) doc.getElementsByTagNameNS("http://www.openarchives.org/OAI/2.0/", "record").item(0);
      return parseRecord(record);
   }

   /**
    * Performs the 'Identify' OAI-PMH verb.
    *
    * @return A Document containing the XML of the Identify response.
    * @throws OAIException if the request fails.
    */
   @Override
   public Element identify() throws OAIException {
      String query = "?verb=Identify";
      Document doc = performRequest(query);
      checkForOaiError(doc);
      return (Element)doc.getElementsByTagNameNS("http://www.openarchives.org/OAI/2.0/","Identify").item(0);
   }

   @Override
   public List<Header> listIdentifiers(String metadataPrefix, ZonedDateTime from, ZonedDateTime until, String set, String resumptionToken) throws OAIException {
      StringBuffer query = new StringBuffer( "?verb=ListIdentifiers" );
      if (resumptionToken != null && !resumptionToken.isEmpty()) {
         query.append("&resumptionToken=").append(URLEncoder.encode(resumptionToken, StandardCharsets.UTF_8));
      } else {
         formQuery(metadataPrefix, from, until, set, query);

      }
      Document doc = performRequest(query.toString());
      checkForOaiError(doc);
      List<Header> retVal = new ArrayList<>();
      Element idElementWrapper = (Element) doc.getElementsByTagNameNS("http://www.openarchives.org/OAI/2.0/", "ListIdentifiers").item(0);
      NodeList ids = idElementWrapper.getElementsByTagNameNS("http://www.openarchives.org/OAI/2.0/", "header");
      for(int i = 0; i < ids.getLength(); i++) {
         Header h = parseHeader((Element) ids.item(i));
         retVal.add(h);
      }
      return retVal;

   }

   @Override
   public List<Record> listRecords(String metadataPrefix, ZonedDateTime from, ZonedDateTime until, String set, String resumptionToken) throws OAIException {
      List<Record> retval= new ArrayList<>();
      try {
        do {
           ListRecordsResponse resp = listRecordsInternal(metadataPrefix,resumptionToken,from, until,set);
           retval.addAll(resp.records());
           resumptionToken = resp.resumptionToken();
        } while (resumptionToken != null && !resumptionToken.isEmpty());

      } catch (Exception e) {
         throw new RuntimeException(e);
      }

      return retval;
   }

   @Override
   public List<MetadataFormat> listMetadataFormats(String identifier) throws OAIException {
      String query = "?verb=ListMetadataFormats";
      if(identifier != null && !identifier.isEmpty()) {
         query += "&identifier=" + URLEncoder.encode(identifier, StandardCharsets.UTF_8);
      }
      Document doc = performRequest(query);
      checkForOaiError(doc);
      List<MetadataFormat> metadataList = new ArrayList<>();
      NodeList recordNodes = doc.getElementsByTagNameNS("*","metadataFormat");
      for (int i = 0; i < recordNodes.getLength(); i++) {
         Element recordElement = (Element) recordNodes.item(i);
         String metadataPrefix = getElementTextContent(recordElement,"metadataPrefix");
         String metadataNamespace = getElementTextContent(recordElement,"metadataNamespace");
         String schema = getElementTextContent(recordElement,"schema");
         metadataList.add(new MetadataFormat(metadataPrefix, metadataNamespace, schema));
      }

      return metadataList;

   }

   @Override
   public List<Set> listSets() throws OAIException {
      String query = "?verb=ListSets";
      Document doc = performRequest(query);
      checkForOaiError(doc);
      List<Set> sets = new ArrayList<>();
      NodeList setNodes = doc.getElementsByTagNameNS("*","set");
      for (int i = 0; i < setNodes.getLength(); i++) {
         Element setElement = (Element) setNodes.item(i);
         String setSpec = getElementTextContent(setElement,"setSpec");
         String setName = getElementTextContent(setElement,"setName");
         String setDescription = getElementTextContent(setElement,"setDescription");
         sets.add(new Set(setSpec, setName, setDescription));
      }
      return sets;
   }




   /**
    * Helper method to parse a <record> element from the XML response.
    */
   private Record parseRecord(Element recordElement) {
      Header header = parseHeader((Element) recordElement.getElementsByTagNameNS("http://www.openarchives.org/OAI/2.0/","header").item(0));
      Element metadata = (Element) recordElement.getElementsByTagNameNS("http://www.openarchives.org/OAI/2.0/","metadata").item(0);
      // The actual metadata content is the first child element of the <metadata> tag
      NodeList metadataChildren = metadata.getChildNodes();
      Element metadataContent = null;
      for (int i = 0; i < metadataChildren.getLength(); i++) {
         if (metadataChildren.item(i).getNodeType() == Node.ELEMENT_NODE) {
            metadataContent = (Element) metadataChildren.item(i);
            break;
         }
      }
      return new Record(header, metadataContent);
   }

   /**
    * Helper method to parse a <header> element from the XML response.
    */
   private Header parseHeader(Element headerElement) {
      String identifier = getElementTextContent(headerElement, "identifier");
      String datestamp = getElementTextContent(headerElement, "datestamp");

      boolean isDeleted = headerElement.hasAttribute("status") && "deleted".equals(headerElement.getAttribute("status"));

      List<String> setSpecs = new ArrayList<>();
      NodeList setSpecNodes = headerElement.getElementsByTagName("setSpec");
      for (int i = 0; i < setSpecNodes.getLength(); i++) {
         setSpecs.add(setSpecNodes.item(i).getTextContent());
      }
      return new Header(identifier, datestamp, setSpecs, isDeleted);
   }

   /**
    * Performs the actual HTTP GET request using HttpClient and parses the response into a DOM Document.
    */
   private Document performRequest(String query) throws OAIException {
      log.debug("OAI query: {}", query);
      HttpRequest request = null;
      try {
         request = HttpRequest.newBuilder()
               .uri(new URI(this.baseUrl + query))
               .header("Accept", "text/xml")
               .GET()
               .build();


      HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

      if (response.statusCode() != 200) {
         throw new OAIException("HTTP GET Request Failed with Error code : " + response.statusCode());
      }

      try (InputStream in = response.body()) {
         return docBuilder.parse(in);
      } catch (SAXException e) {
         throw new OAIException("Problem decoding response",e);
      }
      } catch (URISyntaxException | IOException | InterruptedException e) {
         throw new OAIException("Problem communicating with Server ",e);
      }
   }

   /**
    * Checks the parsed document for an OAI-PMH <error> element.
    * @param doc The parsed XML document.
    * @throws OAIException if an error element is found.
    */
   private void checkForOaiError(Document doc) throws OAIException {
      NodeList errorNodes = doc.getElementsByTagName("error");
      if (errorNodes.getLength() > 0) {
         Element errorElement = (Element) errorNodes.item(0);
         String code = errorElement.getAttribute("code");
         String message = errorElement.getTextContent();
         throw new OAIException("OAI-PMH Error: code='" + code + "', message='" + message.trim() + "'");
      }
   }

   /**
    * Utility method to get the text content of a direct child element by its tag name.
    */
   private String getElementTextContent(Element parent, String tagName) {
      NodeList nodeList = parent.getElementsByTagNameNS("*",tagName);
      if (nodeList.getLength() > 0) {
         return nodeList.item(0).getTextContent();
      }
      return null;
   }

   /**
    * Utility method to convert a DOM Document to a string.
    * @throws TransformerException if the result cannot be created.
    */
   public String elementToString(Element doc) throws TransformerException {
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer transformer = tf.newTransformer();
      StringWriter writer = new StringWriter();
      transformer.transform(new DOMSource(doc), new StreamResult(writer));
      return writer.toString();
   }


}
