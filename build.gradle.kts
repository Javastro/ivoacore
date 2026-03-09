plugins {
//    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
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
