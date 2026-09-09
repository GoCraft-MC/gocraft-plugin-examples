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
# GOCRAFT_CLI names the packer. Without it, the gocraft-cli release is
# downloaded once into .gocraft/tool and verified against the release's
# checksums.txt before it ever runs — the same discipline the Gradle plugin
# applies for the Java half. The workspace Makefile sets GOCRAFT_CLI at the
# local build; nothing here knows the workspace exists.
#
# Never committed, and that was considered. A binary in the repository is the
# mistake this project already made once and cannot undo: an 11 MB
# protoc-gen-gocraft is still in GoCraft's history, untracked since, and every
# fresh clone pays for it — and here it would be one per platform, in the
# repository an author is told to copy from.
set -e

cd "$(dirname "$0")"
output=${1:-gocraft-example-go.gcpkg}

cli=$GOCRAFT_CLI
if [ -z "$cli" ]; then
	# The release download. Refused unless its sha256 matches the checksums.txt
	# published beside it: a build that runs an unverified binary it just pulled
	# off the network is a supply chain with a hole in it.
	cli_version=v0.2.1
	case "$(uname -s)" in
		Linux) os=linux ;;
		Darwin) os=darwin ;;
		MINGW* | MSYS* | CYGWIN*) os=windows ;;
		*) echo "no gocraft-cli build for $(uname -s): set GOCRAFT_CLI" >&2; exit 1 ;;
	esac
	case "$(uname -m)" in
		x86_64 | amd64) arch=amd64 ;;
		aarch64 | arm64) arch=arm64 ;;
		*) echo "no gocraft-cli build for $(uname -m): set GOCRAFT_CLI" >&2; exit 1 ;;
	esac
	asset="gocraft-cli_${cli_version}_${os}_${arch}"
	[ "$os" = windows ] && asset="$asset.exe"
	cli=.gocraft/tool/$asset
	if [ ! -x "$cli" ]; then
		base="https://github.com/GoCraft-MC/gocraft-cli/releases/download/$cli_version"
		mkdir -p .gocraft/tool
		curl -fsSL -o "$cli.tmp" "$base/$asset"
		curl -fsSL -o .gocraft/tool/checksums.txt "$base/checksums.txt"
		want=$(awk -v a="$asset" '$2 == a || $2 == "*"a { print $1 }' .gocraft/tool/checksums.txt)
		got=$(sha256sum "$cli.tmp" | awk '{ print $1 }')
		if [ -z "$want" ] || [ "$want" != "$got" ]; then
			rm -f "$cli.tmp"
			echo "$asset does not match the release's checksums.txt: refused" >&2
			exit 1
		fi
		mv "$cli.tmp" "$cli"
		chmod +x "$cli"
	fi
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

# Outside any workspace, deliberately. This module pins a published SDK commit:
# building it proves the SDK is usable by someone who only has the coordinates;
# a go.work up the tree — the development workspace is one — would resolve the
# checkouts beside it instead and hide exactly that.
export GOWORK=off

# The commands, asked of the plugin itself: it declares them once in Commands(),
# and that declaration is what the loader binds, so the shape in the bundle and
# the functions answering it cannot disagree.
go run . -gocraft-dump-commands .gocraft/commands.json

# The binary the manifest's entry names. No extension, on any platform: the
# host extracts under "plugin" plus its own.
go build -o bin/gocraft-example-go .

# The stale output first: the packer lists the directory before creating the
# archive, so a bundle left by the previous run would be packed into the new
# one — reading the very file being written, which never reaches EOF.
rm -f "$output"

# -layout-lock is the point of this script. Appending a field to an event is
# allowed; reordering or removing one is refused here, before the bundle exists,
# because the index is what the wire carries and a subscriber compiled against
# the old order would read one field for another in silence.
"$cli" build -commands .gocraft/commands.json -layout-lock events.lock.json \
	-o "$output" .
