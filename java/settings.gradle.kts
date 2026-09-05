// mavenLocal() first, and only until the API is tagged.
//
// This plugin uses the plugin-defined event API, which is written but not
// released. So `./gradlew publishToMavenLocal` in the gocraft-jvm checkout is a
// prerequisite here, and the version below is that repository's
// declaredVersion rather than a published tag.
//
// When it is tagged this becomes what the README hands an author: jitpack.io
// alone, and a version with a leading v. Nothing else in this file changes,
// which is the point of writing it this way rather than with a composite build.
pluginManagement {
    repositories {
        mavenLocal()
        maven { url = uri("https://jitpack.io") }
        gradlePluginPortal()
    }
    resolutionStrategy.eachPlugin {
        if (requested.id.id == "fr.gocraft.plugin") {
            useModule("com.github.GoCraft-MC.gocraft-jvm:gocraft-gradle-plugin:${requested.version}")
        }
    }
}

// The build plugin declares the API and the processor for us, at versions
// matching its own — but it cannot declare where they come from, so the same
// order is repeated for them.
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        maven { url = uri("https://jitpack.io") }
        mavenCentral()
    }
}

rootProject.name = "gocraft-example-java"