package com.github.kpavlov.akkabox.transactions;

public class StartTransactionCmd extends AbstractTransactionCmd {
    protected StartTransactionCmd(long id) {
        super(id);
    }
}
