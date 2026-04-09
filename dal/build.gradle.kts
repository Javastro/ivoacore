plugins {
    id("ivoacore-conventions")
}
description = "Library that implements some core IVOA DAI conventions"
dependencies   {
    api(project(":common"))
    api("org.javastro.ivoa:ivoa-schema:0.9.13")
    api("org.javastro.ivoa:ivoa-entities")
    api("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")
}