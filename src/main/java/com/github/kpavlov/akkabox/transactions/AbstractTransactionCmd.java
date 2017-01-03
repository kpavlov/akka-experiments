package com.github.kpavlov.akkabox.transactions;

public abstract class AbstractTransactionCmd extends AbstractCmd {

    private final long transactionId;

    protected AbstractTransactionCmd(long transactionId) {
        this.transactionId = transactionId;
    }

    public long getTransactionId() {
        return transactionId;
    }

}
