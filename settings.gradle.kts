
rootProject.name = "ivoacore"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        maven {
            url= uri("https://repo.dev.uksrc.org/repository/maven-snapshots/")
        }
    }
}


include("common")
include("dal")
include("uws")
include("tap")
include("pgsphere")
include(":clients:registry")
include(":clients:vospace")
include(":clients:tap")

