package hello;

public class CancelTransactionCmd extends AbstractTransactionCmd {
    protected CancelTransactionCmd(long id) {
        super(id);
    }
}
