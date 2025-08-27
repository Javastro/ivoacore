package org.javastro.ivoacore.client.registry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import javax.xml.transform.TransformerException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Created on 21/08/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

class BasicOAIClientTest {

    BasicOAIClient client;
   @BeforeEach
    void prepare() throws Exception {
       client = new BasicOAIClient("http://localhost:8080/oai");
   }

   @Test
   void getRecord() throws OAIException {
       Element id = client.identify();
      // this bit depends on it being an IVOA registry
       String identifier = id.getElementsByTagNameNS("*", "identifier").item(0).getTextContent();
      OAIInterface.Record record = client.getRecord(identifier, "ivo_vor");
      assertNotNull(record);
      assertNotNull(record.header());
      assertNotNull(record.metadata());
   }

   @Test
   void identify() throws OAIException, TransformerException {

      Element id = client.identify();
      assertNotNull(id);
      System.out.println(id.getTagName());
      // this bit depends on it being an IVOA registry
      String identifier = id.getElementsByTagNameNS("*", "identifier").item(0).getTextContent();
      assertNotNull(identifier);
      System.out.println(identifier);
      System.out.println(client.elementToString(id));

   }

   @Test
   void listIdentifiers() throws OAIException {

      List<OAIInterface.Header> ids = client.listIdentifiers("ivo_vor", null, null, "ivo_managed", null);
      assertNotNull(ids);
      assertFalse(ids.isEmpty());
      System.out.println(ids.size());
   }

   @Test
   void listRecords() throws OAIException {

         List<OAIInterface.Record> rec = client.listRecords("ivo_vor", null, null, "ivo_managed", null);
         assertNotNull(rec);
         assertFalse(rec.isEmpty());
         System.out.println(rec.size());


   }

   @Test
   void listMetadataFormats() throws OAIException {
      List<OAIInterface.MetadataFormat> mf = client.listMetadataFormats(null);
      assertNotNull(mf);
      assertFalse(mf.isEmpty());
      mf.forEach(System.out::println);
   }

   @Test
   void listSets() throws OAIException {
      List<OAIInterface.Set> sets = client.listSets();
      assertNotNull(sets);
      assertFalse(sets.isEmpty());
      sets.forEach(System.out::println);
   }
}