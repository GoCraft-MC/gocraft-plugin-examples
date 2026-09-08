// The Go reference plugin, and its own module the way a plugin author's is.
//
// It names gocraft-api-go by module path rather than reaching into a sibling
// checkout, so building it asks exactly what an author's build asks: that the
// SDK is usable by someone who has the coordinates and nothing else.
//
// The pins below are the published ones and stay that way. Building with no
// workspace file in reach is how the claim above gets tested.
module github.com/GoCraft-MC/gocraft-plugin-examples/go

go 1.26.0

require github.com/GoCraft-MC/gocraft-api-go v0.3.0

require (
	github.com/GoCraft-MC/gocraft-abi v0.4.0 // indirect
	google.golang.org/protobuf v1.36.11 // indirect
)
