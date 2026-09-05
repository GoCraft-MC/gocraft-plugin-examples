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

    public PurchaseEvent(PlayerRef buyer, List<Tier> tiers, double price) {
        this.buyer = buyer;
        this.tiers = tiers;
        this.price = price;
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
}