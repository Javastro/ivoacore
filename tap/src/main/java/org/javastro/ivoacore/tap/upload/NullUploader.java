package org.javastro.ivoacore.tap.upload;


import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * No-op uploader implementation used when TAP requests do not include uploads.
 */
public class NullUploader implements TAPUploadCacher {

   @Override
   public boolean hasUpload() {
      return false;
   }

   @Override
   public Map<String, Path> storeUploads(Path uploadDirectory) {
      return new HashMap<>();
   }
}
