/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.vosi;


/*
 * Created on 04/03/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.javastro.ivoa.entities.resource.AccessURL;
import org.javastro.ivoa.entities.resource.Capability;
import org.javastro.ivoa.entities.resource.dataservice.ParamHTTP;
import org.javastro.ivoa.entities.resource.tap.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for building IVOA VOSI capability lists.
 */
public class CapabilityBuilder {

   private CapabilityBuilder(){}

   /**
    * Creates a list of standard VOSI capabilities for the given service URL.
    * @param url the base URL of the service.
    * @return a list of {@link Capability} objects for the VOSI capabilities and availability endpoints.
    */
   static public List<Capability> createVOSICapabilities(URL url) throws MalformedURLException {
      List<Capability> capabilities = new ArrayList<>();
      ParamHTTP intf = ParamHTTP.builder().withVersion("1.1").build();
      URL capURL = new URL(url, "capabilities");
      URL availURL = new URL(url, "availability");
      capabilities.add(Capability.builder().withStandardID("ivo://ivoa.net/std/VOSI#capabilities")
                  .withInterfaces(List.of(intf.newCopyBuilder().addAccessURLs(new AccessURL(capURL.toString(),"full")).build()))
            .build());//FIXE add sufficient detail

      capabilities.add(Capability.builder().withStandardID("ivo://ivoa.net/std/VOSI#availability")
                  .withInterfaces(List.of(intf.newCopyBuilder().addAccessURLs(new AccessURL(availURL.toString(),"full")).build()))
            .build());
      return capabilities;
   }

   static public List<Capability> createTAPCapabilities(URL url) throws MalformedURLException {
      List<Capability> capabilities = new ArrayList<>();
      ParamHTTP intf = ParamHTTP.builder().withVersion("1.1").build();//TODO add all the parameters

      capabilities.add(TableAccess.builder().withStandardID("ivo://ivoa.net/std/TAP")
            .withInterfaces(
                  List.of(intf.newCopyBuilder().addAccessURLs(new AccessURL(url.toString(), "base"))
                              .withRole("std")
                        .build()
                  )
            )
            .addLanguages(Language.builder().withName("ADQL")
                  .withDescription("The Astronomical Data Query Language is the standard IVOA dialect of SQL; " +
                        "it contains a very general SELECT statement as well as some extensions for spherical geometry" +
                        " and higher mathematics.")
                  .withVersions(new Version("2.1", "ivo://ivoa.net/std/ADQL#v2.1"))
                  .withLanguageFeatures(new LanguageFeatureList(List.of(
                        new LanguageFeature("POINT", "ADQL geometry POINT")
                     //TODO Add other features
                        ),"ivo://ivoa.net/std/TAPRegExt#features-adqlgeo"),
                        new LanguageFeatureList(List.of( //TODO add all the UDFs - would want have definitions in common with the hibernate dialect org.javastro.ivoacore.pgsphere.PgSphereDialect
                              new LanguageFeature("gavo_getauthority(ivoid TEXT) -> TEXT", "returns the authority part of an ivoid (or, more generally a URI).\n" +
                                    "So, ivo://org.gavo.dc/foo/bar#baz becomes org.gavo.dc.\n" +
                                    "\n" +
                                    "The behaviour for anything that's not a full URI is undefined.") //FIXME this is just an example
                              //TODO Add other features
                        ),"ivo://ivoa.net/std/TAPRegExt#features-udf")

                        )

                  .build())
                  .withOutputFormats(List.of(
                        new OutputFormat("application/x-votable+xml;serialization=TABLEDATA", List.of("votable"), "ivo://ivoa.net/std/TAPRegExt#output-votable-td"),
                        new OutputFormat("text/csv", List.of("csv"), "ivo://ivoa.net/std/TAPRegExt#output-csv")
                  )
                  )

            .build()
      );
      URL tablesURL = new URL(url, "tables");
      capabilities.add(Capability.builder().withStandardID("ivo://ivoa.net/std/VOSI#tables")
            .withInterfaces(List.of(
                  intf.newCopyBuilder().addAccessURLs(new AccessURL(tablesURL.toString(), "full")).build()
            ))
            .build()
      );
      return capabilities;
   }
}
