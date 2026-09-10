package io.github.mcengine.pluginmanager.api.manager;

import java.util.List;
import java.util.Objects;

/**
 * The answer to one poll: everything to do, and when to ask again.
 *
 * @param changes         the work, in no particular order
 * @param pollAfterSeconds how long to wait before asking again
 */
public record DesiredState(List<DesiredChange> changes, int pollAfterSeconds) {

    /** The floor for a poll interval, whatever the server asks for. */
    public static final int MINIMUM_POLL_SECONDS = 30;

    public DesiredState {
        changes = List.copyOf(Objects.requireNonNull(changes, "changes"));

        // The server sets the interval, because it is the one that knows how
        // loaded it is -- but a value of zero from a misconfigured or hostile
        // server would turn this plugin into a tight loop against it.
        if (pollAfterSeconds < MINIMUM_POLL_SECONDS) {
            pollAfterSeconds = MINIMUM_POLL_SECONDS;
        }
    }

    public boolean isEmpty() {
        return changes.isEmpty();
    }

    public static DesiredState empty() {
        return new DesiredState(List.of(), MINIMUM_POLL_SECONDS);
    }
}
