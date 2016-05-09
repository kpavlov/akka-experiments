package hello;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

class TransactionActor extends AbstractActor {

    LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private final long id;
    private final AtomicReference<TransactionState> state = new AtomicReference<>();

    private static final Random random = new Random();
    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public TransactionActor(final long id) {
        logger.info("Created TransactionActor: #{}", id);
        this.id = id;

        receive(ReceiveBuilder.
                match(Double.class, d -> {
                    sender().tell(d.isNaN() ? 0 : d, self());
                }).
                match(Integer.class, i -> {
                    sender().tell(i * 10, self());
                }).
                match(String.class, s -> s.startsWith("foo"), s -> {
                    sender().tell(s.toUpperCase(), self());
                })
                .match(
                        CreateTransactionCmd.class,
                        cmd -> {
                            create(id, cmd);
                        }
                )
                .match(
                        CancelTransactionCmd.class,
                        cmd -> {
                            cancel(id, cmd);
                        }
                )
                .match(
                        StartTransactionCmd.class,
                        cmd -> {
                            start(id, cmd);
                        }
                )
                .build()
        );
    }

    private void start(long id, StartTransactionCmd cmd) {
        TransactionState currentState = state.get();
        final TransactionState newState = TransactionStateBuilder.newInstance()
                .message(currentState.message)
                .status(TransactionStatus.STARTED)
                .build();
        if (currentState != null && currentState.status == TransactionStatus.CREATED) {
            setState(id, currentState, cmd.getCommandId(), newState);
            int delay = 1 + random.nextInt(2);
            executorService.schedule(() -> complete(cmd.getCommandId(), id), delay, TimeUnit.SECONDS);
        } else {
            sendErrorEvent(cmd.getCommandId(), "Can't start transaction: Unexpected transaction state:" + currentState);
        }
    }

    private void complete(UUID startCommandId, long id) {
        TransactionState currentState = state.get();
        final TransactionState newState = TransactionStateBuilder.newInstance()
                .message(currentState.message)
                .status(TransactionStatus.COMPLETED)
                .build();
        if (currentState != null && currentState.status == TransactionStatus.STARTED) {
            setState(id, currentState, startCommandId, newState);
        } else {
            sendErrorEvent(startCommandId, "Can't complete transaction: Unexpected transaction state:" + currentState);
        }
    }

    private void sendErrorEvent(UUID commandId, String message) {
        final ErrorEvent event = new ErrorEvent(
                Optional.ofNullable(commandId),
                message
        );
        context().system().eventStream().publish(event);
    }

    private void cancel(long id, CancelTransactionCmd cmd) {
        TransactionState currentState = state.get();
        final TransactionState newState = TransactionStateBuilder.newInstance()
                .message(currentState.message)
                .status(TransactionStatus.CANCELLED)
                .build();
        if (currentState != null && !currentState.status.isFinite()) {
            setState(id, currentState, cmd.getCommandId(), newState);
        } else {
            sendErrorEvent(cmd.getCommandId(), "Can't cancel transaction: Unexpected transaction state:" + currentState);
        }
    }

    private void create(long id, CreateTransactionCmd cmd) {
        final TransactionState newState = TransactionStateBuilder.newInstance()
                .message(cmd.getMessage())
                .status(TransactionStatus.CREATED)
                .build();
        setState(id, null, cmd.getCommandId(), newState);
    }

    private void setState(long id, TransactionState expectedState, UUID commandId, TransactionState newState) {
        boolean result = state.compareAndSet(expectedState, newState);

        if (result) {
            logger.info("Changed state to: {}", this);
            publishStatusEvent(id, commandId, newState);
        } else {
            logger.warning("Concurrent Modification. Command failed. cmdId={}, actor={}", commandId, this);
        }
    }

    private void publishStatusEvent(long id, UUID commandId, TransactionState state) {
        final TransactionStatusEvent event = new TransactionStatusEvent(
                Optional.ofNullable(commandId),
                id,
                state.status
        );
        context().system().eventStream().publish(event);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TransactionActor{");
        sb.append("id=").append(id);
        sb.append("ref=").append(self());
        sb.append(", state=").append(state);
        sb.append('}');
        return sb.toString();
    }

    static class TransactionState {
        private final String message;
        private final TransactionStatus status;

        public TransactionState(TransactionStateBuilder builder) {
            this.message = builder.message;
            this.status = builder.status;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("TransactionState{");
            sb.append("message='").append(message).append('\'');
            sb.append(", status=").append(status);
            sb.append('}');
            return sb.toString();
        }
    }

    static class TransactionStateBuilder {
        private String message;
        private TransactionStatus status;

        static TransactionStateBuilder newInstance() {
            return new TransactionStateBuilder();
        }

        static TransactionStateBuilder from(TransactionState prototype) {
            final TransactionStateBuilder builder = new TransactionStateBuilder();
            builder.message = prototype.message;
            builder.status = prototype.status;
            return builder;
        }


        public TransactionStateBuilder message(String message) {
            this.message = message;
            return this;
        }

        public TransactionStateBuilder status(TransactionStatus status) {
            this.status = status;
            return this;
        }

        TransactionState build() {
            return new TransactionState(this);
        }

    }
}
