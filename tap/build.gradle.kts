plugins {
    id("ivoacore-conventions")
}
description = "IVOA TAP server library"

dependencies {
    implementation(project(":uws"))
    implementation("uk.ac.starlink:stil")
    implementation("org.javastro.ivoa.dm:tapschema:0.9.5")
    implementation("fr.unistra.cds:ADQLlib:2.0-SNAPSHOT")
    testImplementation("com.h2database:h2:2.3.232") // for database testing

}