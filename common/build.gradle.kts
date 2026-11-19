plugins {
    id("org.javastro.build.module.lib")
    `java-test-fixtures`
}

description = "common code to ivoacore libraries"
dependencies {
    implementation("org.javastro.ivoa:ivoa-entities")
    implementation("net.sf.saxon:Saxon-HE") // for xslt 3.0
    testFixturesImplementation("org.hibernate.orm:hibernate-core:7.2.6.Final")
    testFixturesImplementation("com.h2database:h2:2.2.220")
}

tasks.register<Jar>("testJar") {
    archiveClassifier.set("tests")
    from(sourceSets.test.get().output)
}

