

package org.javastro.ivoacore.uws.environment.parameter;

import java.io.InputStream;

/**
 * An internal value that can be changed.
 * @author Paul Harrison (paul.harrison@manchester.ac.uk) 13 Jul 2009
 * @version $Name:  $
 * @since AIDA Stage 1
 */
public interface MutableInternalValue extends InternalValue {
    
      /**
       * Sets the internal value from a string.
       * @param val the string value to set.
       * @throws ParameterStorageException if the value cannot be stored.
       */
      void setValue(String val) throws ParameterStorageException;

      /**
       * Sets the internal value from a byte array.
       * @param resultData the byte array to set as the value.
       * @throws ParameterStorageException if the value cannot be stored.
       */
      void setValue(byte[] resultData) throws ParameterStorageException;

      /**
       * Sets the internal value from an input stream.
       * @param is the input stream to read the value from.
       * @throws ParameterStorageException if the value cannot be stored.
       */
      void setValue(InputStream is) throws ParameterStorageException;
}

