package gocraft.example;

import fr.gocraft.api.EventValue;

/// One line of a purchase: what was bought and what it costs.
///
/// A record rather than three parallel lists, which is the shape a flat layout
/// could not express. The label is final and the price is not, so a subscriber
/// may discount a line without being able to rename it — and the manifest the
/// build derives says exactly that, one level down.
@EventValue
public final class Tier {

    private final String label;
    private double price;

    public Tier(String label, double price) {
        this.label = label;
        this.price = price;
    }

    public String label() {
        return label;
    }

    public double price() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}