package com.github.kpavlov.akkabox.transactions;

import java.util.Optional;
import java.util.UUID;

public class ErrorEvent extends AbstractEvent {

    private final String message;

    public ErrorEvent(Optional<UUID> commandId, String message) {
        super(commandId);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ErrorEvent{");
        sb.append("message='").append(message).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
