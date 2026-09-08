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

require github.com/GoCraft-MC/gocraft-api-go v0.2.1-0.20260908121317-56ca886c28e2

require (
	github.com/GoCraft-MC/gocraft-abi v0.3.1-0.20260908120207-f5440be234ac // indirect
	google.golang.org/protobuf v1.36.11 // indirect
)
