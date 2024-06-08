import dev.greenhouseteam.orchestrate.gradle.Properties
import dev.greenhouseteam.orchestrate.gradle.Versions

plugins {
    id("orchestrate.common")
    id("org.spongepowered.gradle.vanilla") version "0.2.1-SNAPSHOT"
}

sourceSets {
    create("generated") {
        resources {
            srcDir("src/generated/resources")
        }
    }
}

minecraft {
    version(Versions.INTERNAL_MINECRAFT)
    val aw = file("src/main/resources/${Properties.MOD_ID}.accesswidener")
    if (aw.exists())
        accessWideners(aw)
}

repositories {
    mavenLocal()
}

dependencies {
    compileOnly("io.github.llamalad7:mixinextras-common:${Versions.MIXIN_EXTRAS}")
    annotationProcessor("io.github.llamalad7:mixinextras-common:${Versions.MIXIN_EXTRAS}")
    compileOnly("net.fabricmc:sponge-mixin:${Versions.FABRIC_MIXIN}")
    compileOnly("dev.greenhouseteam:mib-common:${Versions.MIB}+${Versions.MINECRAFT}")
}

configurations {
    register("commonJava") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
    register("commonResources") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
    register("commonTestResources") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
}

artifacts {
    add("commonJava", sourceSets["main"].java.sourceDirectories.singleFile)
    add("commonResources", sourceSets["main"].resources.sourceDirectories.singleFile)
    add("commonResources", sourceSets["generated"].resources.sourceDirectories.singleFile)
    add("commonTestResources", sourceSets["test"].resources.sourceDirectories.singleFile)
}