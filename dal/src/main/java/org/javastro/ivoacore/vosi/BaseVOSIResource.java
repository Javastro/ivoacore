package org.javastro.ivoacore.vosi;


import org.javastro.ivoa.entities.vosi.availability.Availability;
import org.javastro.ivoa.entities.vosi.capabilities.Capabilities;

/*
 * Created on 28/08/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

/**
 * Base implementation of {@link VOSIResource} that delegates to a {@link VOSIProvider}.
 */
public abstract class BaseVOSIResource implements VOSIResource{
   VOSIProvider provider;

   /**
    * Constructs a BaseVOSIResource with the given VOSI provider.
    * @param provider the provider supplying VOSI capabilities.
    */
   public BaseVOSIResource(VOSIProvider provider) {
      this.provider = provider;
   }


   @Override
   public Capabilities capabilities() {
      return provider.getCapabilities();
   }


   @Override
   public Availability availability() {
      return Availability.builder().withAvailable(true).build(); //TODO really want to have easy mechanism for overriding - however this VOSI end point is not that useful in reality if it is attached to the
   }
}