package io.github.mcengine.pluginmanager.common.manager;

import io.github.mcengine.pluginmanager.api.manager.DesiredChange;
import java.util.Objects;

/**
 * What happened to one change.
 *
 * @param change  the change that was attempted
 * @param staged  whether it was applied, or staged for the next restart
 * @param message a line worth showing a person, whether or not it succeeded
 */
public record ManagerOutcome(DesiredChange change, boolean staged, String message) {

    public ManagerOutcome {
        Objects.requireNonNull(change, "change");
        Objects.requireNonNull(message, "message");
    }

    public static ManagerOutcome staged(DesiredChange change, String message) {
        return new ManagerOutcome(change, true, message);
    }

    public static ManagerOutcome failed(DesiredChange change, String message) {
        return new ManagerOutcome(change, false, message);
    }
}
