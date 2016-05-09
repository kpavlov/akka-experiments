package hello;

public class CreateTransactionCmd extends AbstractCmd{
   private final String message;

    public CreateTransactionCmd(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
