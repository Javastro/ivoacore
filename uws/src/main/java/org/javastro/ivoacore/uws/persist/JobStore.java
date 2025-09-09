package org.javastro.ivoacore.uws.persist;


import org.javastro.ivoacore.uws.BaseUWSJob;

import java.util.Set;

public interface JobStore {

   void store(BaseUWSJob job);
   BaseUWSJob retrieve(String id);
   boolean delete(String id);
   Set<String> getAllIds();
}
