plugins {
    // The version is gocraft-jvm's declaredVersion, resolved from mavenLocal
    // while the event API is untagged. See settings.gradle.kts.
    id("fr.gocraft.plugin") version "0.2.2"
}

gocraft {
    bundleName = "gocraft-example-java"

    // The packer, pinned to a local build on purpose.
    //
    // gocraft-cli reads the manifest with the same strict decoder the server
    // does, and a released one is built against a gocraft-abi that predates
    // [[events.provides]]. Unknown keys are refused, so a downloaded packer
    // would reject a manifest the server accepts — the one failure mode where
    // the error message names nothing useful.
    //
    // -PgocraftCli=<path> overrides it; otherwise this expects the workspace
    // layout, where `make cli` puts the packer in run/. When gocraft-cli and
    // gocraft-abi are tagged together, delete this block and let the build
    // plugin download and verify the release as it does for an author.
    toolPath = (project.findProperty("gocraftCli") as String?)
        ?: rootProject.file(
            if (System.getProperty("os.name").startsWith("Windows")) "../../run/gocraft-cli.exe"
            else "../../run/gocraft-cli"
        ).absolutePath
}