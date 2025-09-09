

package org.javastro.ivoacore.uws.environment.parameter;

import org.javastro.ivoacore.uws.environment.execution.BinaryEncodings;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A {@link MutableInternalValue} that stores the value in a file.
 * @author Paul Harrison (paul.harrison@manchester.ac.uk) 14 Jul 2009
 * @version $Name:  $
 * @TODO need to get the string encoding working.
 * @since AIDA Stage 1
 */
public class FileBasedInternalValue implements MutableInternalValue {

    private File file;

    public FileBasedInternalValue(File storeLocation) {
        file = storeLocation;
    }

    public String asString() throws ParameterAdapterException {
       //FIXME - this needs to be done properly - needs knowledge of the type of the parameter. -
       // look at http://java.sun.com/products/javamail/javadocs/index.html?javax/mail/internet/MimeUtility.html
       try {
        BufferedReader fr = new BufferedReader(new FileReader(file));
        StringBuilder sb = new StringBuilder();
          
        String line;
        while ((line = fr.readLine())!= null) {
           sb.append(line);
        }
        return sb.toString();
    } catch (IOException e) {
       throw new ParameterAdapterException("cannot convert to string",e);
    }
       
    }

    public BinaryEncodings getStringEncoding() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(
                "InternalValue.getStringEncoding() not implemented");
    }

 

    public URL locationOf() {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            logger.error("file storage location bad", e);
            return null;
        }
    }

    public long size() {
        return file.length();
    }

    public void setValue(String value) throws ParameterStorageException {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(file));
            pw.println(value);
        } catch (IOException e) {
            logger.error("problem writing to file", e);
            throw new ParameterStorageException("problem writing to file", e);

        } finally {
            if (pw != null) {
                pw.close();
            }

        }

    }

    /** logger for this class */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
            .getLogger(FileBasedInternalValue.class);

    public void setValue(InputStream is) throws ParameterStorageException {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            is.transferTo(os);
        } catch (IOException e) {
            logger.error("problem writing to file", e);
            throw new ParameterStorageException("problem writing to file", e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    logger.error("problem closing file", e);
                }
                try {
                    is.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                   logger.error("problem closing input stream", e);
                }

            }
        }
    }

    public void writeToStream(OutputStream os) throws IOException {
        FileInputStream is = new FileInputStream(file);
        is.transferTo(os);
    }

    public InputStream getStreamFrom() {
        InputStream retval = null;

        try {
            retval = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            logger.error("cannot get stream", e);
        }
        return retval;

    }

    public void setValue(byte[] resultData) throws ParameterStorageException {
       try {
        FileOutputStream fo = new FileOutputStream(file);
           fo.write(resultData);
    } catch (IOException e) {
       throw new ParameterStorageException("Cannot set the value " , e);
    }
    }

    public OutputStream getStreamTo() throws IOException {
        return new FileOutputStream(file);
    }
}


