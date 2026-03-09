/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.vosi;


/*
 * Created on 02/03/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.javastro.ivoa.entities.vosi.availability.Availability;
import org.javastro.ivoa.entities.vosi.capabilities.Capabilities;

/**
 * JAX-RS resource interface exposing the IVOA VOSI (Virtual Observatory Support Interface) endpoints.
 */
public interface VOSIResource {
   /**
    * Returns the capabilities of this service.
    * @return the VOSI {@link Capabilities} document.
    */
   @GET
   @Path("capabilities")
   @Produces(MediaType.APPLICATION_XML)
   Capabilities capabilities();

   /**
    * Returns the availability of this service.
    * @return the VOSI {@link Availability} document.
    */
   @GET
   @Path("availability")
   @Produces(MediaType.APPLICATION_XML)
   Availability availability();


}
