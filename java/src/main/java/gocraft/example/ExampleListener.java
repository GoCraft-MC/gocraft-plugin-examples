package gocraft.example;

import fr.gocraft.api.Host;
import fr.gocraft.api.Priority;
import fr.gocraft.api.Subscribe;
import fr.gocraft.api.event.BlockBreakEvent;
import fr.gocraft.api.event.PlayerJoinEvent;

/// Every part of the event API, in as few lines as will show it.
///
/// No IPC anywhere in here: the position, the block and the permission answers
/// all arrived with the event, and the message goes back inside the verdict. One
/// event, one round trip, whatever the handler does.
public final class ExampleListener {

    private final Host host;

    public ExampleListener(Host host) {
        this.host = host;
    }

    /// Greets an arrival.
    ///
    /// Observational: the server does not wait for this, and nothing said here
    /// can prevent a connection that already happened — which is why the event
    /// offers no cancel().
    @Subscribe
    public void onPlayerJoin(PlayerJoinEvent event) {
        host.log(event.player().username() + " joined from " + event.player().edition());
        event.sendMessage("Hello from a Java plugin, " + event.player().username() + ".");
    }

    /// Refuses to let anyone mine bedrock, and says why.
    ///
    /// Cancellable, so the server is holding its tick waiting for this answer —
    /// under a budget shared with every other subscriber. Quick work only.
    ///
    /// HIGH rather than the default, because a protection decision should be
    /// made after plugins that merely observe have had their look.
    @Subscribe(priority = Priority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!"minecraft:bedrock".equals(event.block().id())) {
            return;
        }
        if (event.can("gocraft.example.notify")) {
            // The permission arrived resolved inside the event; asking the
            // server would have cost a round trip while it waits.
            event.sendMessage("Bedrock stays put, but you would have been allowed.");
            return;
        }
        event.cancel();
        event.sendMessage("Bedrock is not yours to break at "
                + event.pos() + " with " + event.tool() + ".");
    }
}
