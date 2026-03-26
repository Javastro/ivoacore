/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.tap.schema;


/*
 * Created on 17/03/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import net.sf.saxon.s9api.*;
import org.ivoa.dm.tapschema.Schema;
import org.ivoa.dm.tapschema.TapschemaModel;
import org.javastro.ivoa.entities.IvoaJAXBContextFactory;
import org.javastro.ivoa.entities.vosi.tables.Tableset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 *  A base implementation of {@link SchemaProvider} that provides common functionality for schema providers that
 * load TAP schema definitions from various sources  and then supply them with {@link #getSchemas()},as well as
 * convert them to the VOSI tables representation.
 * <p>
 * Subclasses must implement the {@link #provideSchemas()} method to load the TAP schema definitions from their specific sources.
 * The base class will then handle the conversion to VOSI tables and provide the resulting {@link Tableset} through the {@link #asVOSI()} method.
 *
 * @author Paul Harrison (paul.harrison@manchester.ac.uk)
 */
public abstract class BaseSchemaProvider implements SchemaProvider {
   private static final Logger log = LoggerFactory.getLogger(BaseSchemaProvider.class);
   protected final Processor processor;
   protected final XsltCompiler compiler;
   final List<Schema> schemas = new ArrayList<>();
   protected final Tableset tableSet;
   protected final boolean dbCaseSensitive;

   public BaseSchemaProvider(boolean dbCaseSensitive) {
      this.dbCaseSensitive = dbCaseSensitive;
      processor = new Processor(false);
      compiler = processor.newXsltCompiler();
      schemas.addAll(provideSchemas());
      try {
         this.tableSet = writeVOSI();
      } catch (SaxonApiException | JAXBException e) {
         log.error("problem writing as VOSI",e);
         throw new RuntimeException(e);
      }
   }

   protected abstract List<Schema> provideSchemas();

   @Override
   public List<Schema> getSchemas() {
      return schemas;
   }

   @Override
   public Tableset asVOSI() {
    return tableSet;
   }

   @Override
   public boolean isDBCaseSensitive() {
      return dbCaseSensitive;
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
