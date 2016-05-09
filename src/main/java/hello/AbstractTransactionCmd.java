package hello;

import java.util.UUID;

public abstract class AbstractTransactionCmd extends AbstractCmd {
    private final long transactionId;



    protected AbstractTransactionCmd(long transactionId) {
        this.transactionId = transactionId;
    }

    public long getTransactionId() {
        return transactionId;
    }

}
