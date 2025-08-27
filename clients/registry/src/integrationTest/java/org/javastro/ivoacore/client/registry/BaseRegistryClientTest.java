package org.javastro.ivoacore.client.registry;

import org.javastro.ivoa.entities.Ivoid;
import org.javastro.ivoa.entities.resource.Resource;
import org.javastro.ivoa.entities.resource.registry.Registry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Created on 22/08/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

class BaseRegistryClientTest {

   private BaseRegistryClient client;

   @BeforeEach
   void prepare() throws Exception {
      client = new BaseRegistryClient(new BasicOAIClient("http://localhost:8080/oai"));
   }
   @Test
   void identify() {
      Registry reg = client.identify();
      assertNotNull(reg);
      assertNotNull(reg.getIdentifier());
      System.out.println("identity: " + reg.getIdentifier());
      System.out.println("authorities: " + reg.getManagedAuthorities());

   }

   @Test
   void allIds() {

      List<Ivoid> ids = client.allIds();
      assertNotNull(ids);
      assertFalse(ids.isEmpty());

   }

   @Test
   void getResource() throws URISyntaxException {
      Registry reg = client.identify();
      assertNotNull(reg);
      String id = reg.getIdentifier();
      assertNotNull(id);
      System.out.println("getResource: " + id);
      Resource res = client.getResource(new Ivoid(id));
      assertNotNull(res);
      assertInstanceOf(Registry.class, res);

   }
   }