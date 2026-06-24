package org.javastro.ivoacore.tap.upload;


import java.nio.file.Path;
import java.util.Map;

/**
 * The operations required to cache uploaded VOTables for a TAP service.
 * The implementation of this interface is provided by the TAP service implementation and is used by the TAP service
 * to store uploaded files Job Work directory for later storage in database.
 *
 * @author Paul Harrison (paul.harrison@manchester.ac.uk) */
public interface TAPUploadCacher {

   /**
    * Are there any uploads to cache?
    * @return if there were any uploads
    */
   boolean hasUpload();

   /**
    * Store the referenced VOTables as local files in the specified directory.
    * @param uploadDirectory the directory in which to cache the uploads.
    * @return m
    */
   Map<String, Path> storeUploads(Path uploadDirectory);

}
