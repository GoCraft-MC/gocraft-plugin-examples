plugins {
    // The gocraft-jvm release tag, served verbatim by JitPack. See
    // settings.gradle.kts for how the id resolves to it.
    id("fr.gocraft.plugin") version "v0.3.0"
}

gocraft {
    bundleName = "gocraft-example-java"

    // The packer release the build downloads and verifies against the
    // release's checksums.txt. Named here because the plugin's own default
    // still says v0.1.1, whose decoder predates [[events.provides]] and would
    // refuse this manifest — the one failure mode where the error message
    // names nothing useful.
    toolVersion = "v0.2.1"
}
