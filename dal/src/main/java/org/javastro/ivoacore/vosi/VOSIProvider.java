package org.javastro.ivoacore.vosi;


import org.javastro.ivoa.entities.vosi.capabilities.Capabilities;

/*
 * Created on 28/08/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */
/*
provides the basic VOSI interface
 */
public interface VOSIProvider {
      public Capabilities getCapabilities();
}
