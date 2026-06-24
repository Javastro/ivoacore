package org.javastro.ivoacore.tap.upload;


import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

public abstract class BaseTAPUploadCacher implements TAPUploadCacher {

   protected final String uploadParam;


   protected BaseTAPUploadCacher(String uploadParam) {
      this.uploadParam = uploadParam;
   }

   /**
    * Parses the UPLOAD parameter from a DALI-compliant query and processes any file uploads or URLs present in it.
    * @return A map where the keys are table names and the values are Paths pointing to corresponding data sources (e.g., temporary file URIs, remote URLs).
    */
   @Override
   public Map<String, Path> storeUploads(Path uploadDirectory) {
      Map<String, Path> uploadMap = new java.util.HashMap<>();

      if (uploadParam != null) {
         String[] uploadSpecs = uploadParam.split(";");
         for (String uploadSpec : uploadSpecs) {
            // If there's a "~,param:~~" upload parameter supplied, then upload the file to tmp and create a file: URI to it.
            String[] parts = uploadSpec.split(",");
            String tableName = parts[0];
            String tableLoc = parts[1];
            Path filePath = null;
            //either param:<upload file> or a URL to a remote file (http, https, vos, etc)
            if (TapUploadService.isValidUploadParam(uploadSpec)) {
               if (tableLoc.startsWith("param:")) {
                  try {
                     filePath = storeParam(tableLoc, uploadDirectory);
                  } catch (IOException e) {
                     throw new RuntimeException(e);
                  }
               } else if (tableLoc.startsWith("http") || tableLoc.startsWith("https")) {
                  //store the content of the URL
                  try {
                     URI uri = URI.create(tableLoc);
                     filePath = generateFileName(uploadDirectory, tableName);
                     Files.copy(uri.toURL().openStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                  } catch (IOException e) {
                     throw new RuntimeException("Failed to download file from URL: " + tableLoc, e);
                  }
               } else if (tableLoc.startsWith("vos")) {
                  //Requires a VOSpace client to be configured for testing
                  throw new UnsupportedOperationException("VOSpace uploads are not currently supported");
               }
               else {
                  uploadSpec = tableLoc;
               }
            }
            uploadMap.put(tableName, filePath);
         }
      }
      return uploadMap;
   }

   /**
    * Stores a VOTable in a temporary file and returns the URI of the file.
    * This is expected to be implemented by the specific TAP service implementation,
    * as it may depend on the underlying framework or environment to extract the param values
    * from the Multi-partformdata.
    * @param tableLoc parameter of the DALI UPLOAD query parameter, e.g. "param:t3"
    * @return The Path of the uploaded file, or null if the file was not uploaded.
    * @throws IOException If an I/O error occurs while storing the file.
    */
   protected abstract Path storeParam(String tableLoc, Path uploadDirectory) throws IOException;

   protected Path generateFileName(Path dir, String tableName) {
      UUID uuid = UUID.randomUUID(); //TODO does this really need the extra uuid in name?
      return dir.resolve("tap-upload-" + tableName + "-" + uuid + ".vot");
   }



}
