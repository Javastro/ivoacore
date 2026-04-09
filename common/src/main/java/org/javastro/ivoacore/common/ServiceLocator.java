/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.common;

import java.net.URI;

/**
 * functional interface to supply the base URI for a service. This is especially needed for
 * the scenario where the service is running behind a reverse proxy, and the base URL is not the same as the one the service is running on.
 */
@FunctionalInterface
public interface ServiceLocator {
   /**
    * Return the base url for a service.
    * @return the base URL for the service as seen by the client.
    */
   URI serviceURI();
}
