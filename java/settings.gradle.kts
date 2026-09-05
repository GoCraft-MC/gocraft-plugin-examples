// mavenLocal, and only until the API is tagged.
//
// This plugin uses the plugin-defined event API, which is written and not
// released, so `./gradlew publishToMavenLocal` in the gocraft-jvm checkout is a
// prerequisite here.
//
// The coordinates differ from an author's twice over, and both differences are
// the same fact: JitPack publishes under a group derived from the repository
// path and serves a tag verbatim, while a local publish writes the group the
// project declares and the version as declared. So this asks for
// fr.gocraft:gocraft-gradle-plugin:0.2.2 where an author writes
// com.github.GoCraft-MC.gocraft-jvm:…:v0.2.2. gradle.properties tells the
// plugin to resolve the API the same way.
//
// When it is tagged, this whole block becomes the four lines the README hands
// an author: jitpack.io, and a version with a leading v.
pluginManagement {
    repositories {
        mavenLocal()
        maven { url = uri("https://jitpack.io") }
        gradlePluginPortal()
    }
    resolutionStrategy.eachPlugin {
        if (requested.id.id == "fr.gocraft.plugin") {
            useModule("fr.gocraft:gocraft-gradle-plugin:${requested.version}")
        }
    }
}

rootProject.name = "gocraft-example-java"