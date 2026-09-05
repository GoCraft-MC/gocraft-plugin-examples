# The Go reference plugin

Logs joins and block breaks, and provides `/greet`. Its callbacks run in a
separate process, and the same event API answers whether the player arrived on
Java or on Bedrock.

## Building

```sh
go run . -gocraft-dump-commands .gocraft/commands.json
go build -o bin/gocraft-example-go .
```

The first line asks the plugin what commands it has. It declares them once, in
`Commands()`, and that same declaration is what the loader binds handlers from —
so the shape in the bundle and the functions that answer cannot disagree. The
dump lands in a dot directory because the packer skips those.

Then package it:

```sh
gocraft-cli build -commands .gocraft/commands.json -o gocraft-example-go.gcpkg .
```

It reads the directory, it does not compile it. `gocraft-cli` turns that neutral
file into the `commands.pb` the bundle ships — the same program that does it for
a Java plugin, from the same kind of file its annotation processor writes.
Executor ids are minted there and nowhere else, which is why handlers bind to
paths rather than to numbers.

While the plugin-defined event API is untagged, use the packer the workspace
builds rather than a released one: a release is built against a `gocraft-abi`
that predates `[[events.provides]]`, and its strict decoder refuses a manifest
the server accepts. `make cli` puts one in the test server's directory.

Copy the `.gcpkg` into the server's `plugins/` directory and restart it. GoCraft
creates `plugins/gocraft.example.go/` for configuration and plugin data.

## Platforms

The executable is platform-specific, so build the bundle for the operating
system and architecture the server runs. Cross-compilation works:

```sh
GOOS=linux GOARCH=amd64 go build -o bin/gocraft-example-go .
```

There is no hot reload and no unloading independently of the process. Rebuild
whenever GoCraft's Go version or the plugin API version changes.