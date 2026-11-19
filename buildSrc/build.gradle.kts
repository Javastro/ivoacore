plugins {
    `kotlin-dsl`
}
repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies{
    dependencies {
        implementation("pl.allegro.tech.build.axion-release:pl.allegro.tech.build.axion-release.gradle.plugin:1.21.4")
        implementation("org.semver4j:semver4j:6.0.0")
        testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
      //  implementation("com.github.jmongard.git-semver-plugin:com.github.jmongard.git-semver-plugin.gradle.plugin:0.19.5")
    }
}

tasks.test {
    useJUnitPlatform()
}
