package gocraft.example;

import fr.gocraft.api.CommandContext;
import fr.gocraft.api.Host;
import fr.gocraft.api.command.Cmd;
import fr.gocraft.api.command.Range;
import fr.gocraft.api.command.Sub;

import java.util.List;

/// Where the cross-runtime pair starts: a line typed in chat becomes an event
/// another plugin, in another language, in another process, gets to change.
///
/// The command is what makes it observable. An event fired by the tick would
/// prove the same thing and nobody would see it happen.
///
/// Annotations rather than the builder, and not by taste: the host parses a
/// typed line against the tree the bundle carries, before the plugin is asked
/// anything. gocraft-apt is what puts a tree there. A builder declares one at
/// runtime, which is too late for a command an admin can type.
@Cmd("shop")
public final class ShopCommands {

    private final Host host;

    public ShopCommands(Host host) {
        this.host = host;
    }

    /// Announces a purchase and reports what came back.
    ///
    /// emit blocks until every subscriber has run, and the event object is
    /// updated before it returns — so the price read on the line after is the
    /// price a discount plugin set, across a process boundary and two
    /// languages. False means somebody refused the sale.
    @Sub("buy <price>")
    void buy(CommandContext context, @Range(min = 0.01, max = 1_000_000) double price) {
        if (!context.sender().isPlayer()) {
            context.reply("Only a player can buy something.");
            return;
        }
        PurchaseEvent purchase = new PurchaseEvent(context.sender().player(),
                List.of(new Tier("gold", price * 0.75), new Tier("iron", price * 0.25)),
                price,
                java.util.Map.of("gold_ingot", 12, "iron_ingot", 240));

        if (!host.emit(purchase)) {
            context.reply("The sale was refused.");
            return;
        }
        if (purchase.price() < price) {
            context.reply(String.format("Sold for %.2f instead of %.2f.",
                    purchase.price(), price));
        } else {
            context.reply(String.format("Sold for %.2f. Nobody discounted it.", price));
        }
        for (Tier tier : purchase.tiers()) {
            context.reply(String.format("  %s: %.2f", tier.label(), tier.price()));
        }
    }
}