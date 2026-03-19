/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.tap.schema;


/*
 * Created on 02/03/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import jakarta.xml.bind.*;
import net.sf.saxon.s9api.*;
import org.ivoa.dm.tapschema.ColNameKeys;
import org.ivoa.dm.tapschema.Schema;
import org.ivoa.dm.tapschema.TapschemaModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link SchemaProvider} that reads TAP schema definitions from VO-DML model resources
 * and converts them to the VOSI tables representation.
 */
public class VODMLSchemaProvider extends BaseSchemaProvider implements SchemaProvider {

   private static final Logger log = LoggerFactory.getLogger(VODMLSchemaProvider.class);
   private final String tapSchemaResource;


   /**
    * Constructs a VODMLSchemaProvider by loading the named TAP schema resource.
    * The standard TAP_SCHEMA is always loaded in addition to the named resource.
    * @param tapSchemaResource the classpath resource path of the VO-DML TAP schema XML file.
    */
   public VODMLSchemaProvider(String tapSchemaResource) {
      this.tapSchemaResource = tapSchemaResource;

   }

   /**
    * read the schema in.
    *
    * @return The marshalled schemata.
    */
   private List<Schema> readSchema(InputStream is) throws jakarta.xml.bind.JAXBException {
      if (is != null) {
         TapschemaModel model = new TapschemaModel();
         JAXBContext jc = model.management().contextFactory();
         Unmarshaller unmarshaller = jc.createUnmarshaller();
         JAXBElement<TapschemaModel> el = unmarshaller.unmarshal(new StreamSource(is), TapschemaModel.class);
         TapschemaModel model_in = el.getValue();
         if (model_in != null) {
            ColNameKeys.normalize(model_in);
            List<Schema> schemas = model_in.getContent(Schema.class);
            for(var s:schemas){
               log.info("loaded tap schema {} from VO-DML model",s.getSchema_name());
            }
            return schemas;
         }
      }
      return new ArrayList<>();
   }

   @Override
   protected List<Schema> provideSchemas() {
      List<Schema> retval = new ArrayList<>();
      try {
         retval.addAll(readSchema(this.getClass().getResourceAsStream("/"+tapSchemaResource)));
         retval.addAll(readSchema(TapschemaModel.TAPSchema()));
      } catch (JAXBException e) {
         log.error("problem loading schemas",e);
         throw new RuntimeException(e);
      }

      return retval;
   }
}
