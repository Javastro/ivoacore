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
import org.javastro.ivoa.entities.IvoaJAXBContextFactory;
import org.javastro.ivoa.entities.vosi.tables.Tableset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class VODMLSchemaProvider implements SchemaProvider {

   private static final Logger log = LoggerFactory.getLogger(VODMLSchemaProvider.class);

   final List<Schema> schemas = new ArrayList<>();
   final private Processor processor;
   final private XsltCompiler compiler;
   private final Tableset tableSet;

   public VODMLSchemaProvider(String tapSchemaResource) {
      processor = new Processor(false);
      compiler = processor.newXsltCompiler();
      try {
         readSchema(this.getClass().getResourceAsStream("/"+tapSchemaResource));
         readSchema(TapschemaModel.TAPSchema());
      } catch (JAXBException e) {
         log.error("cannot load the tap schema  {} {}", tapSchemaResource, e.getMessage());
         throw new RuntimeException(e);
      }
      try {
         tableSet = writeVOSI();
      } catch (SaxonApiException | JAXBException e) {
         log.error("problem writing as VOSI",e);
         throw new RuntimeException(e);
      }
   }

   @Override
   public List<Schema> getSchemas() {
      return schemas;
   }

   @Override
   public Tableset asVOSI() {
    return tableSet;
   }

   /**
    * read the schema in.
    */
   private void readSchema( InputStream is) throws jakarta.xml.bind.JAXBException {
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
            this.schemas.addAll(schemas);
         }
      }
   }
/*
IMPL it might be more efficient to try to do this, but actually as only fairly small does not really matter.
https://stackoverflow.com/questions/56590224/is-it-possible-to-create-an-xsl-transformer-output-stream
so go via string representations.
 */
   private Tableset writeVOSI() throws SaxonApiException, JAXBException {
      XsltExecutable stylesheet = null;
      try {
         stylesheet = compiler.compile(new StreamSource(TapschemaModel.class.getResourceAsStream("/tap2VOSI.xsl")));
      } catch (SaxonApiException e) {
         log.error("cannot compile the VOSI stylesheet");
         throw new RuntimeException(e);
      }
      StringWriter swo = new StringWriter();
      Serializer out = processor.newSerializer(swo);
      out.setOutputProperty(Serializer.Property.METHOD, "xml");
      Xslt30Transformer transformer = stylesheet.load30();
      TapschemaModel model = new TapschemaModel();
      for(var s:this.schemas){
         model.addContent(s);
      }
      JAXBContext jc = TapschemaModel.contextFactory();
      Marshaller mar = jc.createMarshaller();
      StringWriter sw = new StringWriter();
      mar.marshal(model, sw);
      transformer.transform(new StreamSource(new StringReader(sw.toString())), out);
      JAXBContext context = IvoaJAXBContextFactory.newInstance();
      Unmarshaller unmarshaller = context.createUnmarshaller();
      Object rv = unmarshaller.unmarshal(new StringReader(swo.toString()));
      if (rv instanceof Tableset) {
         return (Tableset) rv;
      }
      else throw new JAXBException("The returned object is not a Tableset");

   }
}
