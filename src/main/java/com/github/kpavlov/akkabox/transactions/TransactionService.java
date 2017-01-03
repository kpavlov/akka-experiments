package com.github.kpavlov.akkabox.transactions;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class TransactionService {

    private final ActorSystem actorSystem;

    private final AtomicLong idGenerator = new AtomicLong(0);

    public TransactionService(ActorSystem actorSystem, Consumer<? extends AbstractEvent> listener) {
        this.actorSystem = actorSystem;
        final ActorRef eventListener = actorSystem.actorOf(Props.create(EventListenerActor.class, listener));
        actorSystem.eventStream().subscribe(eventListener, AbstractEvent.class);
    }

    public void submit(CreateTransactionCmd cmd) throws TimeoutException {
        final long id = idGenerator.incrementAndGet();
        final Props props = Props.create(TransactionActor.class, id);
        final ActorRef actorRef = actorSystem.actorOf(props, "transaction-" + id);

        actorRef.tell(cmd, ActorRef.noSender());
    }

    public void cancel(CancelTransactionCmd cmd) {
        sendTo(cmd.getTransactionId(), cmd);
    }

    public void start(StartTransactionCmd cmd) {
             sendTo(cmd.getTransactionId(), cmd);
    }

    private void sendTo(long transactionId, AbstractCmd cmd) {
        final ActorSelection actorSelection = actorSystem.actorSelection("user/transaction-" + transactionId);
        actorSelection.tell(cmd, ActorRef.noSender());
    }
}
