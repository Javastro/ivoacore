package org.javastro.ivoacore.client.registry;


/*
 * Created on 21/08/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.javastro.ivoa.entities.Ivoid;
import org.javastro.ivoa.entities.resource.Resource;
import org.javastro.ivoa.entities.resource.registry.Registry;

import java.util.List;

/**
 * The minimal interface to an IVOA registry.
 * It is formed in terms of parsed entities.
 * It does not include the OAI ListRecords on purpose as this might be a very "heavy" operation.
 */
public interface MinimalRegistryInterface {

   /**
    * Get the registry record for itself.
    * @return The resource record for itself.
    */
   Registry identify();
   /**
    * list the identifiers that the registry publishes.
    * @return the list of identifiers.
    */
   List<Ivoid> allIds();

   /**
    * Return a resource from its ID.
    * @param id
    * @return the resource
    */
   Resource getResource(Ivoid id);
}
