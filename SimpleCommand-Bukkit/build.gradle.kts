plugins {
    kotlin("jvm")
}

repositories {
    maven {
        url = uri("http://oss.sonatype.org/content/repositories/snapshots")
    }
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
    }
}

dependencies {
    compile(project(":SimpleCommand-Core"))
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    implementation(kotlin("stdlib"))
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")
}
