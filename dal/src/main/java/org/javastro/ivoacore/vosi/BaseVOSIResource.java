package org.javastro.ivoacore.vosi;


import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.javastro.ivoa.entities.vosi.capabilities.Capabilities;

/*
 * Created on 28/08/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

public abstract class BaseVOSIResource implements VOSIResource{
//IMPL - there is not much point in this class if this is all that it does.....
   VOSIProvider provider;

   public BaseVOSIResource(VOSIProvider provider) {
      this.provider = provider;
   }


   @Override
   public Capabilities capabilities() {
      return provider.getCapabilities();
   }

}