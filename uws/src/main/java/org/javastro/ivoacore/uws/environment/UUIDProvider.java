package org.javastro.ivoacore.uws.environment;


/*
 * Created on 04/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import java.util.UUID;

public class UUIDProvider implements IdProvider {

   @Override
   public String generateId() {
      return UUID.randomUUID().toString();
   }
}
