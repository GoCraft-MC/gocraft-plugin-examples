package main

import (
	"fmt"
	"log/slog"
	"os"

	gocraft "github.com/GoCraft-MC/gocraft-api-go"

	"github.com/GoCraft-MC/gocraft-plugin-examples/go/internal/mine"
	"github.com/GoCraft-MC/gocraft-plugin-examples/go/internal/shop"
)

var metadata = gocraft.Metadata{
	ID: "gocraft.example.go", Version: "0.1.0", APIVersion: gocraft.CurrentVersion,
}

type goExamplePlugin struct {
	context gocraft.Context
}

func (p *goExamplePlugin) OnLoad(context gocraft.Context) error {
	p.context = context
	context.Logger().Info("loaded", "data", context.DataDirectory())
	// The handle arrives bound to this dispatch, so answering the player is a
	// method on the player rather than a call on the channel with their id.
	// The other half of the pair, in this direction: this plugin defines the
	// greeting, publishes it, and the Java plugin gets to rewrite the line
	// before anybody reads it.
	if err := context.Events().OnPlayerJoin(func(event *gocraft.PlayerJoinEvent, control gocraft.EventControl) {
		context.Logger().Info("player joined", "player", event.Player.Username,
			"edition", event.Player.Edition)
		greeting := &mine.Greeting{
			Player:  event.Player,
			Message: "Hello from a Go plugin, " + event.Player.Username + ".",
		}
		// Blocks until every subscriber has run, and the struct is updated
		// before it returns: the message sent below is the one they left.
		allowed, err := context.Events().Emit(greeting)
		if err != nil {
			context.Logger().Error("greeting not published", "err", err)
			return
		}
		if !allowed {
			context.Logger().Info("a subscriber refused the greeting")
			return
		}
		if err := event.Player.SendMessage(greeting.Message); err != nil {
			context.Logger().Error("greeting not queued", "err", err)
		}
	}); err != nil {
		return err
	}
	// And in the other direction: an event the Java plugin defines, received
	// here through types gocraft-cli generated from its manifest. Nothing in
	// this file describes that event — regenerating after it changes is what
	// makes the compiler name whatever moved.
	if err := shop.OnPurchase(context.Events(), func(purchase *shop.Purchase,
		control gocraft.EventControl) {
		context.Logger().Info("purchase seen", "price", purchase.Price,
			"lines", len(purchase.Tiers))
		if purchase.Price > 100_000 {
			// Cancellable, and the emitter is expected to abandon the sale.
			control.Cancel()
			return
		}
		purchase.Price *= 0.9
		// And one line of the purchase, which is the case a flat layout could
		// not express: the list is final — nobody swaps it — while the records
		// in it are not. What travels back is this record's price, not the list
		// it sits in.
		if len(purchase.Tiers) > 0 {
			purchase.Tiers[0].Price *= 0.5
		}
		if purchase.Buyer != nil {
			// A handle carried by a plugin-defined event, bound to this
			// dispatch: somebody to answer, not an id to look up.
			_ = purchase.Buyer.SendMessage("10% off, from a Go plugin.")
		}
	}); err != nil {
		return err
	}
	// Observational handlers take the control and ignore it; one that refuses
	// uses it. Same signature either way, and the same one a plugin-defined
	// event's handler has.
	if err := context.Events().OnBlockBreak(func(event *gocraft.BlockBreakEvent, control gocraft.EventControl) {
		context.Logger().Info("block broken", "player", event.Player.Username,
			"block", event.Block.ID, "position", event.Pos)
		if event.Block.ID == "minecraft:bedrock" && !event.Can("gocraft.example.mine") {
			control.Cancel()
			_ = event.Player.SendMessage("Bedrock is not yours to break.")
		}
	}); err != nil {
		return err
	}
	return nil
}

// Commands is asked twice and never by this file: once by the build, to put the
// shape in the bundle, and once by the loader, to bind what runs. Declaring it
// here is what makes the two agree.
func (p *goExamplePlugin) Commands() *gocraft.CommandSet {
	set := gocraft.NewCommandSet()
	set.Command("greet").Permission("gocraft.example.greet").Runs(func(call *gocraft.CommandContext) error {
		call.Reply(fmt.Sprintf("Hello, %s!", call.SenderName))
		return nil
	})
	return set
}

func (p *goExamplePlugin) OnEnable() error {
	p.context.Logger().Info("enabled")
	return nil
}

func (p *goExamplePlugin) OnDisable() error {
	p.context.Logger().Info("disabled")
	return nil
}

func main() {
	if err := gocraft.Run(metadata, &goExamplePlugin{}); err != nil {
		slog.Error("example plugin stopped", "err", err)
		os.Exit(1)
	}
}
