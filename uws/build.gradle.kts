plugins {
    id("ivoacore-conventions")
}
description = "IVOA UWS server library"
dependencies {
    implementation(project(":common"))
    implementation("org.javastro.ivoa:ivoa-entities")
}