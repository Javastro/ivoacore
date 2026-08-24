plugins {
    `kotlin-dsl`
}
repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies{
    dependencies {
        implementation("pl.allegro.tech.build.axion-release:pl.allegro.tech.build.axion-release.gradle.plugin:1.21.3")
      //  implementation("com.github.jmongard.git-semver-plugin:com.github.jmongard.git-semver-plugin.gradle.plugin:0.19.5")
    }
}
