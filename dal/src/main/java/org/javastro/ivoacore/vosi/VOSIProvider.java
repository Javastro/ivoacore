package org.javastro.ivoacore.vosi;


import org.javastro.ivoa.entities.vosi.capabilities.Capabilities;

/*
 * Created on 28/08/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */
/**
provides the basic VOSI interface
 */
public interface VOSIProvider {
      /**
       * Returns the VOSI capabilities for this service.
       * @return the {@link Capabilities} document describing the service's capabilities.
       */
      public Capabilities getCapabilities();
}
