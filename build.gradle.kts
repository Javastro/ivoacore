plugins {
//    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("kr.motd.sphinx") version "2.10.1"
}

//TODO when finished with SNAPSHOT phase revert to publishing to maven-central

//publishing
//nexusPublishing {
//    repositories {
//        //TODO this is a rather unsatisfactory kludge, but still seems better than the suggested JReleaser which is not really gradle friendly
//        // see https://central.sonatype.org/publish/publish-portal-ossrh-staging-api/#configuration
//        sonatype {
//            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
//            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
//        }
//    }
//}

// ---------------------------------------------------------------------------
// Aggregated Javadoc across all Java submodules
// ---------------------------------------------------------------------------

val aggregateJavadoc by tasks.registering(Javadoc::class) {
    description = "Generates aggregated Javadoc API documentation for all modules."
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    setDestinationDir(layout.buildDirectory.dir("docs/javadoc").get().asFile)
    title = "${rootProject.name} ${rootProject.version} API"
}

subprojects {
    afterEvaluate {
        if (plugins.hasPlugin("java") || plugins.hasPlugin("java-library")) {
            val mainSourceSet = extensions
                .getByType<JavaPluginExtension>()
                .sourceSets
                .getByName(SourceSet.MAIN_SOURCE_SET_NAME)
            rootProject.tasks.named<Javadoc>("aggregateJavadoc") {
                source(mainSourceSet.allJava)
                classpath += mainSourceSet.compileClasspath
                dependsOn(tasks.named("compileJava"))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Sphinx documentation configuration (kr.motd.sphinx plugin)
// ---------------------------------------------------------------------------

tasks.named("sphinx", kr.motd.gradle.sphinx.gradle.SphinxTask::class.java) {
    setSourceDirectory("${projectDir}/doc")
    setOutputDirectory("${layout.buildDirectory.get().asFile}/site")
    dependsOn(aggregateJavadoc)
    doLast {
        copy {
            from(layout.buildDirectory.dir("docs/javadoc"))
            into(layout.buildDirectory.dir("site/javadoc"))
        }
    }
}

// Convenience task to build all documentation
tasks.register("docs") {
    description = "Builds the complete documentation (Sphinx + aggregated Javadoc)."
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    dependsOn("sphinx")
}
