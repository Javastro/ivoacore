/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.tap;


/*
 * Created on 17/03/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltExecutable;
import org.ivoa.dm.tapschema.*;
import org.javastro.ivoacore.tap.schema.BaseSchemaProvider;
import org.javastro.ivoacore.tap.schema.SchemaProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class TestSchemaProvider extends BaseSchemaProvider implements SchemaProvider {


   private static final Logger log = LoggerFactory.getLogger(TestSchemaProvider.class);
   private final XsltExecutable stylesheet;

   public TestSchemaProvider() {
      super(false);
      try {
         stylesheet = compiler.compile(new StreamSource(TapschemaModel.class.getResourceAsStream("/tap2schemaDDL.xsl")));
      } catch (SaxonApiException e) {
         log.error("cannot compile the TAP_SCHEMA stylesheet");
         
         throw new RuntimeException(e);
      }
   }
   
   public static final List<Schema> SCHEMA_LIST = List.of(Schema.createSchema(s -> {
      s.schema_name = "test_schema";
      s.description = "A test schema for unit testing";
      s.tables = List.of(Table.createTable(t -> {
         t.table_name = "test_table";
         t.columns = List.of(Column.createColumn(c -> {
                  c.column_name = "id";
                  c.datatype = TAPType.INTEGER;
                  c.description = "The unique identifier for the record";
                  c.indexed = true; //IMPL need this as primary key heuristic if no joins
               }), Column.createColumn(c -> {
                  c.column_name = "avalue";
                  c.datatype = TAPType.VARCHAR;
                  c.description = "A string value associated with the record";
               })
         );
      }));
   }));

   String[] TESTDATA_DDL = {
         "insert into test_schema.test_table (id, avalue) values (1, 'A1');"
   };




   public void writeDataBaseDDL(Connection connection) {
      try {
         String ddl = transformSchemasToDdl(SCHEMA_LIST);
         executeDdl(connection, ddl);
      } catch (Exception e) {
         throw new IllegalStateException("Failed to create database DDL from TAP schema metadata", e);
      }
   }

   public void populateDataBase(Connection connection) {

      try {
         for (String ddlLine : TESTDATA_DDL) {
            executeDdl(connection, ddlLine);
         }
      } catch (Exception e) {
         throw new IllegalStateException("Failed to populate database DDL from TAP schema metadata", e);
      }
   }

   /**
    *
    * @param schemas
    * @return
    * @throws Exception
    */
   private String transformSchemasToDdl(List<Schema> schemas) throws JAXBException, SaxonApiException {
      TapschemaModel model = new TapschemaModel();
      for (Schema schema : schemas) {
         model.addContent(schema);
      }
      JAXBContext jc = TapschemaModel.contextFactory();
      Marshaller mar = jc.createMarshaller();
      StringWriter sw = new StringWriter();
      mar.marshal(model, sw);
      StringWriter swo = new StringWriter();
      Serializer out = processor.newSerializer(swo);
      out.setOutputProperty(Serializer.Property.METHOD, "text");
      Xslt30Transformer transformer = stylesheet.load30();
      transformer.transform(new StreamSource(new StringReader(sw.toString())), out);
      return swo.toString();
   }

  

   private void executeDdl(Connection connection, String ddl) throws SQLException {
      try (Statement statement = connection.createStatement()) {
         for (String sql : ddl.split(";")) {
            String trimmed = sql.trim();
            if (!trimmed.isEmpty()) {
               log.info("Executing DDL: " + trimmed);
               statement.execute(trimmed);
            }
         }
      }
   }


   @Override
   protected List<Schema> provideSchemas() {
      return SCHEMA_LIST;
   }


}
