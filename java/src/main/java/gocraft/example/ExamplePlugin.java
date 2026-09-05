package gocraft.example;

import fr.gocraft.api.Host;
import fr.gocraft.api.Plugin;

/// The Java reference plugin: every part of the JVM API, in one plugin.
///
/// One plugin rather than a sample per feature, because the question worth
/// answering is not "how do I do X" but "does all of it still work together".
/// A sample proves its own line; this proves the chain — a Java class compiled
/// against the API, packed into a .gcpkg, loaded into an isolated classloader
/// inside a JVM the Go server spawned, receiving events the server built and
/// answering with things the server then does.
///
/// The handlers live on their own listeners rather than here, which is what §05
/// recommends — a listener can be unit-tested with `new` and no server at all,
/// and this class exists only to hand them to the host.
public final class ExamplePlugin implements Plugin {

    private final Host host;

    public ExamplePlugin(Host host) {
        this.host = host;
    }

    @Override
    public void enable() {
        host.registerListener(new ExampleListener(host));
        host.log("loaded, watching for arrivals and mining");
    }

    @Override
    public void disable() {
        // Nothing of our own is open. The host revokes the listener itself,
        // which is what removes the class of leak Bukkit suffers on /reload.
        host.log("stopping");
    }
}
