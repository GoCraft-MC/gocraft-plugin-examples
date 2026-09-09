# gocraft-plugin-examples

The reference plugins for [GoCraft](https://github.com/GoCraft-MC/GoCraft), one
per runtime. They are the only examples that exist: nothing in the server
repository and nothing in the SDKs duplicates what is here.

One plugin per runtime rather than a folder of small samples, and each one
exercises every part of the API its runtime can reach. A folder of samples
answers "how do I do X" and leaves "does all of it still work together"
unanswered — which is the question a reference plugin is for. When something in
the API stops working, one of these stops building or stops behaving, and the
test server loads them on every run.

| Directory | Runtime | Plugin id |
| --- | --- | --- |
| `java/` | `jvm` | `gocraft.example.java` |
| `go/` | `go` | `gocraft.example.go` |
| `lua/` | `lua` | not written — `runtime/lua` does not exist yet |

They are also a pair. The Java plugin publishes a plugin-defined event and the
Go plugin subscribes to it, and the other way round — which is the only thing
that proves an event crosses two languages and a process boundary with its
mutations intact. Neither half is worth much alone.

## Building

Each plugin builds into a `.gcpkg` bundle, which is what a server loads.

```sh
cd java && ./gradlew gocraftBundle     # -> java/build/gocraft/gocraft-example-java.gcpkg
cd go   && ./build.sh                  # -> go/gocraft-example-go.gcpkg
```

For a fresh checkout, generate the Java subscriber from the Go provider first
(from this repository's root, using the custom-events-capable packer):

```sh
gocraft-cli gen -lang java -package gocraft.example.greeting \
  -o java/src/main/java/gocraft/example/greeting go
```

Both builds can download and verify the released packer. For a local override,
pass `-PgocraftCli=/absolute/path/to/gocraft-cli` to Java or set `GOCRAFT_CLI`
for Go. Set `SHOP_BUNDLE` to the Java bundle when first building Go. No
untracked workspace Makefile is required; see the native API prerequisite below.

## Native cancellation and mutation

Both examples subscribe to `player.chat` in addition to the original events:

- `hello-go` becomes `Hello from the typed Go event API.` before broadcast.
- `hello-java` becomes `Hello from the typed Java event API.` before broadcast.
- `hide-go` / `hide-java` are cancelled and never broadcast.

Go assigns `event.Message` and uses `control.Cancel()`. Java uses
`event.setMessage(...)` and `control.cancel()`. Each uses the same verdict
round trip. Other fields are immutable snapshots, not additional write APIs.
The custom purchase/greeting examples and their layout locks remain unchanged.

Each half carries its own build, and both do the same two things beyond
compiling: they generate the types the other plugin declares, from that plugin's
own manifest, and they hand the packer `events.lock.json` — which refuses a
reordered or removed event field before a bundle exists. Appending one is fine.
That record is committed, and it is what protects a subscriber compiled last
month against a layout that moved under it.

The Java bundle comes first. Its manifest is derived from its annotated classes
and exists only once built, so `build.sh` needs `SHOP_BUNDLE` pointing at it the
first time — the Go manifest is written by hand and needs nothing in return,
which is what unties the knot. Neither build needs a packer installed: the
Gradle build downloads the `gocraft-cli` release and verifies it against the
release's `checksums.txt` before running it, and `build.sh` does the same when
`GOCRAFT_CLI` does not name one.

From the workspace, `make examples` orders the two and drops the bundles in the
test server's `plugins/`.

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
