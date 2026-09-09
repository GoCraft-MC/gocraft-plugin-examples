# The Go reference plugin

Logs joins and block-break attempts, provides `/greet`, and demonstrates typed
chat cancellation and mutation. It also publishes a custom greeting for Java
to modify and subscribes to Java's purchase event to discount or cancel it.
Callbacks run in a separate process; the player snapshot identifies the edition.

## Building

Follow the [repository build prerequisites and Java-first sequence](../README.md#building)
first. Then, from this `go/` directory in a POSIX shell (Git Bash on Windows):

```sh
SHOP_BUNDLE=../java/build/gocraft/gocraft-example-java.gcpkg ./build.sh
```

The script generates provider and subscriber types, dumps command metadata from
`Commands()`, compiles the executable, and packages `gocraft-example-go.gcpkg`.
It passes `events.lock.json` to the packer to reject incompatible field-layout
changes and removes the previous output bundle before packing to avoid including
it in itself. Use the script instead of bypassing these steps with a bare build.

Without `GOCRAFT_CLI`, the script downloads CLI v0.2.1 and verifies its release
checksum. To use an existing binary, set `GOCRAFT_CLI` to its absolute path.
No workspace Makefile or unpublished packer is required. `GOWORK=off` is set by
the script so builds use the feature dependency versions pinned in `go.mod`.

Copy the `.gcpkg` into the server's `plugins/` directory and restart it. GoCraft
creates `plugins/gocraft.example.go/` for configuration and plugin data.
Install the Java bundle too for the custom-event interactions.

See [native cancellation and mutation](../README.md#native-cancellation-and-mutation)
for chat inputs and expected results. `/greet` requires `gocraft.example.greet`.

## Platforms

The executable is platform-specific. Run `build.sh` on the operating system and
architecture used by the server. Do not cross-target the whole script with
`GOOS`/`GOARCH`: its command-metadata step must execute `go run` on the build host.
Rebuild when changing the target platform or updating the plugin API dependency.
