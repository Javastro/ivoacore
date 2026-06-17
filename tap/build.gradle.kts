plugins {
    id("ivoacore-conventions")
}
description = "IVOA TAP server library"

dependencies {
    api(project(":uws"))
    api("uk.ac.starlink:stil")
    api("org.javastro.ivoa.dm:tapschema:0.9.8")
    api("net.ivoa:ADQLLib:2.1-SNAPSHOT")
    implementation("net.sf.saxon:Saxon-HE:12.5")


    testImplementation("com.h2database:h2:2.3.232") // for database testing

}

tasks.withType<Test> {
    configure<JacocoTaskExtension> {
        // Excludes the class from instrumentation (on-the-fly)
        excludes = listOf( "adql.parser.grammar.*")
    }
}