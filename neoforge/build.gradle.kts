import dev.greenhouseteam.mib.gradle.Properties
import dev.greenhouseteam.mib.gradle.Versions
import net.neoforged.gradle.dsl.common.runs.ide.extensions.IdeaRunExtension
import org.apache.tools.ant.filters.LineContains

plugins {
    id("mib.loader")
    id("net.neoforged.gradle.userdev") version "7.0.138"
}

val at = file("src/main/resources/${Properties.MOD_ID}.cfg")
if (at.exists())
    minecraft.accessTransformers.file(at)

runs {
    configureEach {
        systemProperty("forge.logging.markers", "REGISTRIES")
        systemProperty("forge.logging.console.level", "debug")
        systemProperty("neoforge.enabledGameTestNamespaces", Properties.MOD_ID)
        jvmArguments("-Dmixin.debug.verbose=true", "-Dmixin.debug.export=true")
        extensions.configure<IdeaRunExtension>("idea") {
            primarySourceSet = sourceSets["test"]
        }
        modSource(sourceSets["main"])
        modSource(sourceSets["test"])
    }
    create("client") {}
    create("server") {
        programArgument("--nogui")
    }
}

dependencies {
    implementation("net.neoforged:neoforge:${Versions.NEOFORGE}")
}

tasks {
    named<ProcessResources>("processResources").configure {
        filesMatching("*.mixins.json") {
            filter<LineContains>("negate" to true, "contains" to setOf("refmap"))
        }
    }
}