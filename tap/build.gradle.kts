plugins {
    id("ivoacore-conventions")
}
description = "IVOA TAP server library"

dependencies {
    api(project(":uws"))
    api("uk.ac.starlink:stil")
    api("org.javastro.ivoa.dm:tapschema:0.9.6")
    api("fr.unistra.cds:ADQLlib:2.0-SNAPSHOT")
    testImplementation("com.h2database:h2:2.3.232") // for database testing

}