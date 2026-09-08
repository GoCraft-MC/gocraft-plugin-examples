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

Both plugins depend on published artefacts: `gocraft-api-go` by module tag,
`gocraft-jvm` from JitPack — with a leading `v`, because JitPack serves a tag
verbatim. That is the property worth having: it proves the SDKs are usable by
someone who only has the coordinates, not a checkout beside them, and building
these two is how it stays proven. `build.sh` sets `GOWORK=off` for the same
reason — a `go.work` up the tree, and the development workspace is one, would
resolve the checkouts beside this repository instead and hide exactly that.

## Licence

Apache-2.0, like the SDKs. A plugin author is expected to copy from these.