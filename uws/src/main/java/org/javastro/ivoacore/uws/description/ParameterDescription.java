package org.javastro.ivoacore.uws.description;


/*
 * Created on 14/08/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

/**
 * Describes a parameter associated with a UWS job type, providing metadata for UI and processing purposes.
 */
public interface ParameterDescription {

   /** identifier of the parameter - used as key
    * @return the unique identifier string for this parameter.
    */
   String getId();
   /** name to use in UI
    * @return the display name for this parameter.
    */
   String getName();
   /** description to use in UI
    * @return the human-readable description of this parameter.
    */
   String getDescription();
   /** UCD of parameter
    * @return the Unified Content Descriptor (UCD) string for this parameter.
    */
   String getUcd();
   /** units of parameter
    * @return the unit string for this parameter.
    */
   String getUnit();
   /** type of parameter
    * @return the {@link ParameterType} of this parameter.
    */
    ParameterType getType();
   /** mime type of the parameter
    * @return the MIME type string for this parameter.
    */
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
