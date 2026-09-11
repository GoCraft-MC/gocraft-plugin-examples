# gocraft-plugin-examples

Reference plugins for [GoCraft](https://github.com/GoCraft-MC/GoCraft), showing
commands, typed native events, and plugin-defined events across Go and Java.

| Directory | Runtime | Plugin id |
| --- | --- | --- |
| `java/` | `jvm` | `gocraft.example.java` |
| `go/` | `go` | `gocraft.example.go` |

The examples work as a pair: Java publishes a purchase event that Go can modify
or cancel, and Go publishes a greeting that Java can modify before it is sent.
Build and install both to try these cross-runtime interactions.

## Building

Each plugin builds into a `.gcpkg` bundle, which is what a server loads.

Prerequisites for this branch:

- Go 1.26.0 and JDK 25. Use the checked-in Gradle wrapper.
- `gocraft-cli` v0.2.1 on `PATH` for the initial subscriber generation. Release
  binaries and `checksums.txt` are available in the
  [CLI release](https://github.com/GoCraft-MC/gocraft-cli/releases/tag/v0.2.1).
  Verify the downloaded binary against its checksum before running it.
- The matching [`gocraft-jvm` feature branch](https://github.com/GoCraft-MC/gocraft-jvm/tree/feat/go-events-api)
  published locally: run `./gradlew publishToMavenLocal` in that checkout first.
  The released v0.3.0 API does not contain `PlayerChatEvent`; see [Versions](#versions).

Run these commands in order from this repository's root, in a POSIX shell
(Git Bash on Windows). The subshells keep each path relative to the root:

```sh
# Bootstrap the Java subscriber from the Go plugin's hand-written manifest.
gocraft-cli gen -lang java -package gocraft.example.greeting \
  -o java/src/main/java/gocraft/example/greeting go

# Java's annotated provider becomes a manifest inside this bundle.
(cd java && sh ./gradlew gocraftBundle)

# Generate the Go subscriber from that bundle, then build and package Go.
(cd go && SHOP_BUNDLE=../java/build/gocraft/gocraft-example-java.gcpkg ./build.sh)
```

Use `sh` for the example's wrapper because it is not committed as executable.
On Windows, use `./gradlew.bat` instead of `sh ./gradlew` (or `./gradlew` when
publishing JVM artifacts). Keep these shell commands in Git Bash, not PowerShell.

The outputs are `java/build/gocraft/gocraft-example-java.gcpkg` and
`go/gocraft-example-go.gcpkg`. Copy both into the server's `plugins/` directory
and restart a GoCraft build containing the matching native event API.

The build steps can download and verify their own CLI v0.2.1; the bootstrap
command still needs the CLI installed above. To reuse a local binary, pass
`-PgocraftCli=/absolute/path/to/gocraft-cli` to Gradle and set `GOCRAFT_CLI` to
its absolute path for `build.sh`. No workspace Makefile is required.

Both builds pass the committed `events.lock.json` to the packer. It checks
custom-event field layouts: appending a field is allowed, but reordering or
removing one is refused. Keep `SHOP_BUNDLE` set when rebuilding Go after changes
to the Java provider so its subscriber types are regenerated.

## Native cancellation and mutation

After both plugins are enabled, connect to a test server and send these as chat
messages, without a leading slash. With no other chat-modifying plugins:

- `hello-go` becomes `Hello from the typed Go event API.` before broadcast.
- `hello-java` becomes `Hello from the typed Java event API.` before broadcast.
- `hide-go` / `hide-java` are cancelled and never broadcast.

Go assigns `event.Message` and uses `control.Cancel()`. Java uses
`event.setMessage(...)` and `control.cancel()`. Each uses the same verdict
round trip. Other fields are immutable snapshots, not additional write APIs.
The custom purchase/greeting examples and their layout locks remain unchanged.

See the registrations and handlers in [Go](go/main.go) and
[Java](java/src/main/java/gocraft/example/ExampleListener.java). Go registers a
typed callback with `context.Events().OnPlayerChat(...)`; Java uses `@Subscribe`
on a method receiving `PlayerChatEvent` and `EventControl`.

## Cross-runtime smoke checks

With both examples enabled and no other custom-event subscribers, try these
manual checks from a player account:

| Action | Expected result |
| --- | --- |
| Join the server | The Go greeting ends with `Java says hello too.` after Java modifies it. |
| `/shop buy 100` | Java reports a sale for 90.00; the gold tier is 37.50 after Go modifies both prices. |
| `/shop buy 100001` | Java replies `The sale was refused.` after Go cancels the purchase. |

These are expected in-game results, not checks performed by building the bundles.
The shop command demonstrates event round trips; it does not implement an economy.

## Versions

The Go module pins published feature commits containing the native event API,
on top of the released ABI v0.4.0 and SDK v0.3.0 baselines. `build.sh` keeps
`GOWORK=off`: a development workspace must not hide incorrect dependency pins.

The Java native events are not in the v0.3.0 release. Before building this
branch's Java example, run `./gradlew publishToMavenLocal` on the matching
`gocraft-jvm/feat/go-events-api` branch. Its declared version is 0.3.0 and
`gocraft.artefactGroup=fr.gocraft` selects those local feature artifacts.
Do not substitute the released tag, which lacks PlayerChatEvent.

Once the native API is released, remove the local artifact-group override and
use its new leading-`v` JitPack version. The settings retain that release
resolution path. Both build scripts retain the verified CLI download behavior.

## Licence

Apache-2.0, like the SDKs. A plugin author is expected to copy from these.
