#!/bin/sh
# Builds this plugin into a .gcpkg, with the checks its layout depends on.
#
# It exists because the Java half carries its own build and this half did not.
# There, `gocraft { }` in build.gradle.kts hands the packer the layout lock and
# the build refuses a reordered event before anything is written. Here the same
# steps lived only in the workspace Makefile — which is on one machine and in no
# repository — so a clone of this repository shipped events.lock.json and
# nothing that ever read it. A lock nobody passes to the packer is a file that
# looks like a guarantee.
#
#   ./build.sh [output.gcpkg]
#
# GOCRAFT_CLI names the packer; otherwise the workspace layout is assumed, where
# `make cli` puts it in run/.
#
# Neither committed nor downloaded, and both were considered. A binary in the
# repository is the mistake this project already made once and cannot undo: an
# 11 MB protoc-gen-gocraft is still in GoCraft's history, untracked since, and
# every fresh clone pays for it — and here it would be one per platform, in the
# repository an author is told to copy from. Downloading it is the right answer
# and is already built: the Gradle plugin fetches gocraft-cli for the author's
# machine and verifies it against the release's checksums.txt before running it.
# It is not used yet, for the reason java/build.gradle.kts gives at length — a
# released packer is compiled against a gocraft-abi that predates
# [[events.provides]], and its strict decoder refuses a manifest the server
# accepts. When gocraft-cli and gocraft-abi are tagged together, both halves of
# this repository drop their local path and take the verified download.
set -e

cd "$(dirname "$0")"
output=${1:-gocraft-example-go.gcpkg}

cli=$GOCRAFT_CLI
if [ -z "$cli" ]; then
	for candidate in ../../run/gocraft-cli.exe ../../run/gocraft-cli; do
		if [ -x "$candidate" ]; then cli=$candidate; break; fi
	done
fi
if [ -z "$cli" ]; then
	echo "no packer: set GOCRAFT_CLI, or run 'make cli' from the workspace" >&2
	exit 1
fi

# The types this plugin emits, from its own manifest. Generated rather than
# written, so the struct and the [[events.provides]] block cannot drift.
"$cli" gen -lang go -package mine -o internal/mine .

# The types it subscribes to, from the other plugin's manifest — so they are
# that plugin's description and not a copy kept in step here. Its bundle has to
# exist first, which is the ordering the workspace Makefile spells out: the
# Java manifest is derived from annotated classes and exists only once built.
if [ -n "$SHOP_BUNDLE" ]; then
	"$cli" gen -lang go -package shop -o internal/shop "$SHOP_BUNDLE"
elif [ ! -d internal/shop ]; then
	echo "internal/shop is missing: build the Java plugin, then point" >&2
	echo "SHOP_BUNDLE at its .gcpkg — or run 'make examples' from the workspace" >&2
	exit 1
fi

# The commands, asked of the plugin itself: it declares them once in Commands(),
# and that declaration is what the loader binds, so the shape in the bundle and
# the functions answering it cannot disagree.
go run . -gocraft-dump-commands .gocraft/commands.json

# The binary the manifest's entry names. No extension, on any platform: the
# host extracts under "plugin" plus its own.
go build -o bin/gocraft-example-go .

# -layout-lock is the point of this script. Appending a field to an event is
# allowed; reordering or removing one is refused here, before the bundle exists,
# because the index is what the wire carries and a subscriber compiled against
# the old order would read one field for another in silence.
"$cli" build -commands .gocraft/commands.json -layout-lock events.lock.json \
	-o "$output" .