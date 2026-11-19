plugins {
    id("org.javastro.build.module.lib")
}

group = "org.javastro.ivoa.core.clients"
description  = "IVOA Registry client"

dependencies {
    implementation(project(":common"))
    implementation("org.javastro.ivoa:ivoa-entities")
    implementation("net.sf.saxon:Saxon-HE") // for xslt 3.0
}


