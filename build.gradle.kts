import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.21" apply false
}


allprojects {
    group = "io.simplyservers"
    version = "1.0-SNAPSHOT"
    repositories {
        jcenter()
        mavenCentral()
    }

    // config JVM target to 1.8 for kotlin compilation tasks
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "1.8"
    }
}
