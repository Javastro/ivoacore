package org.javastro.ivoacore.uws.description;


/*
 * Created on 14/08/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

public interface ParameterDescription {

   /** identifier of the parameter - used as key */
   String getId();
   /** name to use in UI */
   String getName();
   /** description to use in UI */
   String getDescription();
   /** UCD of parameter */
   String getUcd();
   /** units of parameter */
   String getUnit();
   /** type of parameter */
    ParameterType getType();
   /** mime type of the parameter*/
   String getMimeType();

//    /** subtype / contraint on possible value of parameter */
//    String getSubType();
//    /** data encodings accepted for this parameter */
//    String getAcceptEncodings();
//   /** default value for this parameter */
//   List<String> getDefaultValue();
//   /** Allowed values for this parameter */
//  OptionList getOptionList();


}
