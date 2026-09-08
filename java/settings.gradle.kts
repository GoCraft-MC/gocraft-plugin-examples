// JitPack, the way an author resolves it.
//
// JitPack publishes under a group derived from the repository path and serves
// a tag verbatim, so the module is com.github.GoCraft-MC.gocraft-jvm:* and the
// version keeps its leading v. The resolution strategy is the one piece Gradle
// needs beyond the repository: JitPack serves no plugin marker artefact, so
// the plugin id is mapped onto the module that carries it.
pluginManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
        gradlePluginPortal()
    }
    resolutionStrategy.eachPlugin {
        if (requested.id.id == "fr.gocraft.plugin") {
            useModule("com.github.GoCraft-MC.gocraft-jvm:gocraft-gradle-plugin:${requested.version}")
        }
    }
}

rootProject.name = "gocraft-example-java"
