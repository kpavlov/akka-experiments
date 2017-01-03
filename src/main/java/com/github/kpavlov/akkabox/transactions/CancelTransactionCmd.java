package com.github.kpavlov.akkabox.transactions;

public class CancelTransactionCmd extends AbstractTransactionCmd {
    protected CancelTransactionCmd(long id) {
        super(id);
    }
}
