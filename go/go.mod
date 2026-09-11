// The Go reference plugin, and its own module the way a plugin author's is.
//
// It names gocraft-api-go by module path rather than reaching into a sibling
// checkout, so building it asks exactly what an author's build asks: that the
// SDK is usable by someone who has the coordinates and nothing else.
//
// Published feature commits include the custom and native event APIs. The
// example can therefore be built without a sibling checkout or go.work.
module github.com/GoCraft-MC/gocraft-plugin-examples/go

go 1.26.0

require github.com/GoCraft-MC/gocraft-api-go v0.3.1-0.20260909092105-c01a15fc60a3

require (
	github.com/GoCraft-MC/gocraft-abi v0.4.1-0.20260909091520-46f15c995cb4 // indirect
	google.golang.org/protobuf v1.36.11 // indirect
)
