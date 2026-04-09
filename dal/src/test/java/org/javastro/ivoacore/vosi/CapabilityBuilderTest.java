/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.vosi;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.javastro.ivoa.entities.IvoaJAXBContextFactory;
import org.javastro.ivoa.entities.resource.Capability;
import org.javastro.ivoa.entities.vosi.capabilities.Capabilities;
import org.javastro.ivoa.schema.SchemaMap;
import org.javastro.ivoa.schema.XMLValidator;
import org.junit.jupiter.api.Test;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CapabilityBuilderTest {
    @Test
    public void testCapabilities() throws MalformedURLException, JAXBException, ParserConfigurationException {

        URL baseUrl = new URL("http://localhost:8080/");
        final List<Capability> caps = CapabilityBuilder.createVOSICapabilities(baseUrl);
        doValidation(caps);

    }
    @Test
    public void testTapCapabilities() throws MalformedURLException, JAXBException, ParserConfigurationException {

        URL baseUrl = new URL("http://localhost:8080/");
        final List<Capability> caps = CapabilityBuilder.createTAPCapabilities(baseUrl);
        doValidation(caps);

    }


    private static  void  doValidation(List<Capability> caps) throws JAXBException, ParserConfigurationException {
        Capabilities cap = Capabilities.builder().addCapabilities(caps).build();
        assertNotNull(cap);

        XMLValidator  validator = new XMLValidator(SchemaMap.getAllSchemaAsSources());
        //Create a DOM source from the JAXB serialisation of the capabilities document and validate against the VOSI capabilities schema.
        JAXBContext context = IvoaJAXBContextFactory.newInstance();
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        // Marshal to a DOM Document
        StringWriter stringWriter = new StringWriter();

        marshaller.marshal(cap, stringWriter);
        String xmlString = stringWriter.toString();
        System.out.println(xmlString);
        Source capSource = new StreamSource(new StringReader(xmlString));
        // Validate

        boolean isvalid = validator.validate(capSource);
        if (!isvalid) {
            validator.printErrors(System.err);
        }
        assertTrue(isvalid);
    }

}