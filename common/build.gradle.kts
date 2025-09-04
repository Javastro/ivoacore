plugins {
    id("ivoacore-conventions")
}

description = "common code to ivoacore libraries"
dependencies {
    implementation("org.javastro.ivoa:ivoa-entities")
    implementation("net.sf.saxon:Saxon-HE") // for xslt 3.0
}


