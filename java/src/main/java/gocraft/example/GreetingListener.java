package gocraft.example;

import fr.gocraft.api.Subscribe;

import gocraft.example.greeting.Greeting;

/// The receiving half of the pair, in this direction: an event the Go plugin
/// defines, handled here through the class gocraft-cli generated from its
/// manifest.
///
/// Nothing in this plugin describes that event. There is no annotated copy to
/// keep in step, because the description lives in the provider's manifest and
/// the type is derived from it — so an event that changes shape is a build that
/// stops compiling, rather than a handler that quietly reads the wrong field.
public final class GreetingListener {

    /// Rewrites the line before the Go plugin sends it.
    ///
    /// The message field is mutable and the player is not, which the provider
    /// declared and the generated class carries: there is a setter for one and
    /// none for the other, so the rule is a compile error here rather than a
    /// mutation the host refuses at dispatch.
    ///
    /// No EventControl: the provider did not declare the greeting cancellable,
    /// and asking for one on an event nobody may refuse is turned down when the
    /// listener is registered.
    @Subscribe
    public void onGreeting(Greeting greeting) {
        greeting.setMessage(greeting.message() + " Java says hello too.");
    }
}