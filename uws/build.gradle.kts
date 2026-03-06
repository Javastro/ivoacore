plugins {
    id("ivoacore-conventions")
}
description = "IVOA UWS server library"
dependencies {
    implementation(project(":common"))
    api("org.javastro.ivoa:ivoa-entities")
    api("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")
}