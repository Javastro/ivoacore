/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.vosi;


/*
 * Created on 04/03/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.javastro.ivoa.entities.resource.Capability;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CapabilityBuilder {

   private CapabilityBuilder(){}

   static public List<Capability> createCapabilities(URL url) {
      List<Capability> capabilities = new ArrayList<>();
      capabilities.add(Capability.builder().withStandardID("ivo://ivoa.net/std/VOSI#capabilities").build());//FIXE add sufficient detail

      capabilities.add(Capability.builder().withStandardID("ivo://ivoa.net/std/VOSI#availability").build());
      return capabilities;
   }
}
