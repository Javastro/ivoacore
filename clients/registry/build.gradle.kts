plugins {
    id("ivoacore-conventions")
}

group = "org.javastro.ivoa.core.clients"

dependencies {
    implementation(project(":common"))
    implementation("org.javastro.ivoa:ivoa-entities")
    implementation("net.sf.saxon:Saxon-HE") // for xslt 3.0
}


