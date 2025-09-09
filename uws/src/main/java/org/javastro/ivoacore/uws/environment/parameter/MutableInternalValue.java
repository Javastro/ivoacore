

package org.javastro.ivoacore.uws.environment.parameter;

import java.io.InputStream;

/**
 * An internal value that can be changed.
 * @author Paul Harrison (paul.harrison@manchester.ac.uk) 13 Jul 2009
 * @version $Name:  $
 * @since AIDA Stage 1
 */
public interface MutableInternalValue extends InternalValue {
    
      void setValue(String val) throws ParameterStorageException;
      void setValue(byte[] resultData) throws ParameterStorageException;
      void setValue(InputStream is) throws ParameterStorageException;
}

