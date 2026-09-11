plugins {
    // Matching native-event feature artifacts, published to Maven local first.
    // The released v0.3.0 API does not yet contain these native events.
    id("fr.gocraft.plugin") version "0.3.0"
}

repositories {
    mavenLocal()
}

gocraft {
    bundleName = "gocraft-example-java"

    // The packer release the build downloads and verifies against the
    // release's checksums.txt, preserving main's published-tool workflow.
    toolVersion = "v0.2.1"
    (project.findProperty("gocraftCli") as String?)?.let { toolPath = it }
}
