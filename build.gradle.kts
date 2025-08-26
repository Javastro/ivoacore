plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

//publishing
nexusPublishing {
    repositories {
        //TODO this is a rather unsatisfactory kludge, but still seems better than the suggested JReleaser which is not really gradle friendly
        // see https://central.sonatype.org/publish/publish-portal-ossrh-staging-api/#configuration
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
        }
    }
}
