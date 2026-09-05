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
cd go   && go build -o bin/ ./...      # then gocraft-cli build
```

From the workspace, `make examples` does both and drops the bundles in the test
server's `plugins/`.

## Versions, and why they are not tags yet

A released plugin depends on published artefacts: `gocraft-api-go` by module
tag, `gocraft-api-jvm` from JitPack. That is the property worth having — it
proves the SDKs are usable by someone who only has the coordinates, not a
checkout beside them.

These two do not, yet. They use parts of the API that are written but untagged,
so the Java build resolves `gocraft-jvm` from `mavenLocal()` and the Go build
resolves `gocraft-api-go` through the workspace `go.work`. Both are marked at
the place they are configured. When the API is tagged, both flip back to
coordinates and this section goes away — until then, `./gradlew
publishToMavenLocal` in `gocraft-jvm` is a prerequisite for the Java build.

## Licence

Apache-2.0, like the SDKs. A plugin author is expected to copy from these.