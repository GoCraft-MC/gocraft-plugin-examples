package gocraft.example;

import fr.gocraft.api.PlayerRef;
import fr.gocraft.api.PluginEvent;

import java.util.List;

/// An event this plugin defines, and the half of the pair that starts in Java.
///
/// Nothing describes it twice. The annotation and the fields are the whole
/// declaration: `gocraft-apt` derives the layout from declaration order and
/// `final`, `gocraft-cli` writes it into the manifest the bundle carries, and
/// `gocraft-cli gen` turns that manifest into the Go types the subscriber
/// compiles against. One description, four places it is read.
///
/// Cancellable, so a subscriber can refuse the sale outright. The buyer is
/// final because who is buying is not a subscriber's to change; the price is
/// not, because discounting it is the entire point.
@PluginEvent(value = "gocraft.example/purchase", cancellable = true)
public final class PurchaseEvent {

    private final PlayerRef buyer;
    private final List<Tier> tiers;
    private double price;
    private final java.util.Map<String, Integer> stock;

    public PurchaseEvent(PlayerRef buyer, List<Tier> tiers, double price,
            java.util.Map<String, Integer> stock) {
        this.buyer = buyer;
        this.tiers = tiers;
        this.price = price;
        this.stock = stock;
    }

    public PlayerRef buyer() {
        return buyer;
    }

    /// The lines of the purchase. The list is final — nobody swaps it — while
    /// the records in it are not, which is the case §10 is written around.
    public List<Tier> tiers() {
        return tiers;
    }

    public double price() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    /// What is left on the shelf, by item id. §10 allows "List/Map of those";
    /// the wire has no map kind, so this travels as a list of key/value pairs
    /// sorted by key — the shape a block's properties already take.
    ///
    /// The values are boxed because a Java Map cannot hold a primitive. That is
    /// the one place a boxed type is allowed: a bare `Integer` field is refused,
    /// because the wire has no null and choosing silently between a zero and a
    /// refusal is how a subscriber reads a count nobody set. Inside a map the
    /// codec refuses the null instead, here, where the author can act on it.
    ///
    /// Appended after `price` on purpose: the index is the contract, and
    /// `events.lock.json` refuses anything but an append.
    public java.util.Map<String, Integer> stock() {
        return stock;
    }
}