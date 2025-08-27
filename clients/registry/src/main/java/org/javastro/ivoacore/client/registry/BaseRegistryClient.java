package org.javastro.ivoacore.client.registry;


/*
 * Created on 22/08/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import jakarta.xml.bind.JAXBException;
import org.javastro.ivoa.entities.Ivoid;
import org.javastro.ivoa.entities.resource.Resource;
import org.javastro.ivoa.entities.resource.registry.Registry;
import org.javastro.ivoa.schema.Namespaces;
import org.javastro.ivoacore.common.xml.XMLUtils;
import org.w3c.dom.Element;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class BaseRegistryClient implements MinimalRegistryInterface {


   OAIInterface oaiInterface;
   XMLUtils xmlutils = new XMLUtils();

   BaseRegistryClient(OAIInterface oaiInterface) {
      this.oaiInterface = oaiInterface;
   }

   @Override
   public Registry identify() {
      try {
         Element id = oaiInterface.identify();
         return xmlutils.unmarshal((Element) id.getElementsByTagNameNS(Namespaces.RI.getNamespace(),"Resource").item(0),Registry.class);

      } catch (OAIException | JAXBException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public List<Ivoid> allIds() {
      try {
         return new ArrayList<>(oaiInterface.listIdentifiers("ivo_vor",null,null,null,null).stream().map(i -> {//FIXME really need to support resumption tokens
            try {
               return new Ivoid( i.identifier());
            } catch (URISyntaxException e) {
               throw new RuntimeException(e);
            }

         }).toList());
      } catch (OAIException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public Resource getResource(Ivoid id) {
      try {
         Resource res = xmlutils.unmarshal(oaiInterface.getRecord(id.toString(), "ivo_vor").metadata(), Resource.class);
         return res;
      } catch (JAXBException | OAIException e) {
         throw new RuntimeException(e);
      }
   }
}
