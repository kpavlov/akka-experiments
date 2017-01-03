package com.github.kpavlov.akkabox.transactions;

public class GetTransactionStatusCmd extends AbstractTransactionCmd {
    protected GetTransactionStatusCmd(long id) {
        super(id);
    }
}
