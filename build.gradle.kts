plugins {
    kotlin("jvm") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    idea
    java
}

group = "liray"
version = "1.0"

repositories {
    mavenCentral()

    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")

    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://maven.sk89q.com/repo/")
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    implementation(kotlin("stdlib"))

    compileOnly("com.sk89q.worldguard:worldguard-legacy:6.2")
    compileOnly("org.spigotmc:spigot-api:1.8-R0.1-SNAPSHOT")

    implementation("org.xerial:sqlite-jdbc:3.36.0.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk9:1.6.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:1.5.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:1.5.0")
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

tasks {
    shadowJar {
        archiveBaseName.set("CaptureTheFlagPlugin")
        archiveVersion.set("2.0.1")
        archiveClassifier.set("")
        destinationDirectory.set(file("C:\\Users\\david\\Desktop\\Kleydor\\plugins"))
        relocate("com.github.shynixn.mccoroutine", "liray.mccoroutine")
    }
}
