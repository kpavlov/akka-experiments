package com.github.kpavlov.akkabox.transactions;

import java.util.UUID;

public class AbstractCmd {

    private final UUID commandId;

    public AbstractCmd() {
        this.commandId = UUID.randomUUID();
    }

    public UUID getCommandId() {
        return commandId;
    }
}
