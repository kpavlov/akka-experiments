package com.github.kpavlov.akkabox.transactions;

import java.util.Optional;
import java.util.UUID;

public abstract class AbstractEvent {

    private final Optional<UUID> commandId;

    public AbstractEvent(Optional<UUID> commandId) {
        this.commandId = commandId;
    }

    public Optional<UUID> getCommandId() {
        return commandId;
    }
}
