package com.github.kpavlov.akkabox.transactions;

import java.util.Optional;
import java.util.UUID;

public class TransactionStatusEvent extends AbstractEvent {
    private final long transactionId;
    private final TransactionStatus status;

    public TransactionStatusEvent(Optional<UUID> commandId, long transactionId, TransactionStatus status) {
        super(commandId);
        this.transactionId = transactionId;
        this.status = status;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TransactionStatusEvent{");
        sb.append("transactionId=").append(transactionId);
        sb.append(", status=").append(status);
        sb.append('}');
        return sb.toString();
    }
}
