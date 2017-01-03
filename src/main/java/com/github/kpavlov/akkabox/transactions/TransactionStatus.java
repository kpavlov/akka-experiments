package com.github.kpavlov.akkabox.transactions;

public enum TransactionStatus {

    CREATED(false),
    STARTED(false),
    CANCELLED(true),
    COMPLETED(true),
    ERROR(true);

    private final boolean finite;

    TransactionStatus(boolean finite) {
        this.finite = finite;
    }

    public boolean isFinite() {
        return finite;
    }
}
