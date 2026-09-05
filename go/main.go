package main

import (
	"fmt"
	"log/slog"
	"os"

	gocraft "github.com/GoCraft-MC/gocraft-api-go"
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
	if err := context.Events().OnPlayerJoin(func(event *gocraft.PlayerJoinEvent, control gocraft.EventControl) {
		context.Logger().Info("player joined", "player", event.Player.Username,
			"edition", event.Player.Edition)
		if err := event.Player.SendMessage("Hello from a Go plugin, " +
			event.Player.Username + "."); err != nil {
			context.Logger().Error("greeting not queued", "err", err)
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
