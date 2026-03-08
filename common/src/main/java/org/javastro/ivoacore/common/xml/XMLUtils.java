package org.javastro.ivoacore.common.xml;


/*
 * Created on 06/08/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import jakarta.xml.bind.*;
import net.sf.saxon.s9api.*;
import org.javastro.ivoa.entities.IvoaJAXBContextFactory;
import org.javastro.ivoa.entities.resource.Resource;
import org.javastro.ivoa.entities.resource.registry.iface.ResourceInstance;
import org.javastro.ivoa.entities.resource.registry.iface.VOResources;
import org.javastro.ivoa.entities.resource.registry.oaipmh.OAIPMH;
import org.javastro.ivoa.schema.Namespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for marshalling and unmarshalling IVOA resource XML using JAXB and Saxon.
 */
public class XMLUtils {

   private final static Namespaces ns = Namespaces.RI;
   private final Marshaller marshaller;
   private final Processor processor = new Processor(false);
   private final XsltExecutable cleaningStylesheet;
   private final DocumentBuilder docBuilder;
   private final JAXBContext context;
   private final Unmarshaller unmarshaller;

   /**
    * Constructs an XMLUtils instance, initializing the JAXB context, marshaller/unmarshaller,
    * and the namespace-normalizing XSLT stylesheet.
    */
   public XMLUtils() {
      try {
         context = IvoaJAXBContextFactory.newInstance();
         marshaller = context.createMarshaller();
         marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
         marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
         XsltCompiler compiler = processor.newXsltCompiler();
         cleaningStylesheet = compiler.compile(new StreamSource(XMLUtils.class.getResourceAsStream("/xslt/normalizeNamespaces.xslt")));
         DocumentBuilderFactory dbf = DocumentBuilderFactory
               .newInstance();
         dbf.setNamespaceAware(true);
         dbf.setValidating(false);
         docBuilder = dbf.newDocumentBuilder();
         unmarshaller = context.createUnmarshaller();
      } catch (JAXBException | SaxonApiException e) {
         throw new RuntimeException(e); // should not happen in practice
      } catch (ParserConfigurationException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Marshals an IVOA resource object to an XML string.
    * @param <T> the type of the object to marshal.
    * @param a the object to marshal.
    * @return the XML string representation of the object.
    */
   @SuppressWarnings("unchecked")
   public <T> String marshall(T a) {
      return marshallElement(new JAXBElement<T>(
            new QName(ns.getNamespace(), "Resource", ns.getPrefix()),
            (Class<T>) a.getClass(), a));
   }

   /**
    * Unmarshals a list of IVOA {@link Resource} objects from an XML string.
    * @param xml the XML string to unmarshal.
    * @return a list of {@link Resource} objects found in the XML.
    * @throws JAXBException if the XML cannot be parsed.
    */
   public List<Resource> unmarshal(String xml) throws JAXBException {
      Object rv = unmarshaller.unmarshal(new StringReader(xml));
      List<Resource> retval = new ArrayList<Resource>();
      if (rv instanceof Resource r) {
            retval = List.of(r);
      }
      else if (rv instanceof ResourceInstance r) {
         retval = List.of(r.getValue());
      }
      else if (rv instanceof VOResources j) {
         retval = j.getResources();
      }

      return retval;

   }

   /**
    * Unmarshals an object from a DOM {@link Element}.
    * @param <T> the type to unmarshal into.
    * @param xml the DOM element to unmarshal.
    * @param clazz the class of the target type.
    * @return the unmarshalled object.
    * @throws JAXBException if the element cannot be unmarshalled.
    */
   public <T> T unmarshal(Element xml, Class<T> clazz) throws JAXBException {
      JAXBElement<T> retval = unmarshaller.unmarshal(xml, clazz);
      return retval.getValue();
   }



   /**
    * Marshals a {@link JAXBElement} to an XML string, applying namespace normalization.
    * @param element the JAXB element to marshal.
    * @return the XML string representation of the element.
    */
   public String marshallElement(JAXBElement<?> element) {


      try {
         StringWriter sw = new StringWriter();
         Serializer out = processor.newSerializer(sw);
         out.setOutputProperty(Serializer.Property.METHOD, "xml");
         out.setOutputProperty(Serializer.Property.INDENT, "yes");
         Xslt30Transformer transformer = cleaningStylesheet.load30();
         Document doc = docBuilder.newDocument(); // IMPL not sure is this is the most efficient intermediate form?
         marshaller.marshal(element, doc);
         transformer.transform( new DOMSource(doc), out);
         return sw.toString();
      } catch (JAXBException | SaxonApiException e) {
         throw new RuntimeException(e);
      }
   }


   /**
    * Marshals an OAI-PMH element to an XML string.
    * @param element the OAI-PMH element to marshal.
    * @return the XML string representation of the OAI-PMH element.
    */
   public String marshallOAI(OAIPMH element) {
      StringWriter sw = new StringWriter();
      try {
         marshaller.marshal(element, sw);
      } catch (JAXBException e) {
         throw new RuntimeException(e);
      }
      return sw.toString();
   }
}
