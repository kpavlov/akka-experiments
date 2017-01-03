package com.github.kpavlov.akkabox.transactions;

import akka.actor.ActorSystem;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class HelloTest {

    private TransactionService transactionService;
    private volatile CountDownLatch countDownLatch;
    private final List<AbstractEvent> events = Collections.synchronizedList(new ArrayList<>(100));
    private final Logger logger = LoggerFactory.getLogger(HelloTest.class);

    @Before
    public void setUp() throws Exception {
        ActorSystem system = ActorSystem.create();
        transactionService = new TransactionService(system,
                e -> {
                    logger.info("Received Event: {}", e);
                    events.add(e);
                    countDownLatch.countDown();
                });

    }

    @Test
    public void shouldCreate3Transactions() throws Exception {
        // given
        countDownLatch = new CountDownLatch(3);

        final CreateTransactionCmd create1 = new CreateTransactionCmd("hello");
        final CreateTransactionCmd create2 = new CreateTransactionCmd("hi");
        final CreateTransactionCmd create3 = new CreateTransactionCmd("good morning");

        // when
        transactionService.submit(create1);
        transactionService.submit(create2);
        transactionService.submit(create3);

        // then
        countDownLatch.await();

        assertThat(events, everyItem(instanceOf(TransactionStatusEvent.class)));
        final TransactionStatusEvent event1 = assertAndGetEvent(events, create1.getCommandId(), TransactionStatus.CREATED);
        final TransactionStatusEvent event2 = assertAndGetEvent(events, create2.getCommandId(), TransactionStatus.CREATED);
        final TransactionStatusEvent event3 = assertAndGetEvent(events, create3.getCommandId(), TransactionStatus.CREATED);

        events.clear();
        countDownLatch = new CountDownLatch(6);

        final StartTransactionCmd start1 = new StartTransactionCmd(event1.getTransactionId());
        final StartTransactionCmd start2 = new StartTransactionCmd(event2.getTransactionId());
        final StartTransactionCmd start3 = new StartTransactionCmd(event3.getTransactionId());
        final CancelTransactionCmd cancel3 = new CancelTransactionCmd(event3.getTransactionId());

        transactionService.cancel(cancel3);
        transactionService.start(start2);
        transactionService.start(start1);
        transactionService.start(start3);

        countDownLatch.await();
        assertAndGetEvent(events, cancel3.getCommandId(), TransactionStatus.CANCELLED);
        assertAndGetEvent(events, start1.getCommandId(), TransactionStatus.STARTED);
        assertAndGetEvent(events, start2.getCommandId(), TransactionStatus.STARTED);
        assertErrorEvent(events, start3.getCommandId());
        assertAndGetEvent(events, start1.getCommandId(), TransactionStatus.COMPLETED);
        assertAndGetEvent(events, start2.getCommandId(), TransactionStatus.COMPLETED);
    }

    private static TransactionStatusEvent assertAndGetEvent(List<AbstractEvent> events, UUID cmdId, TransactionStatus expectedStatus) {
        final Optional<TransactionStatusEvent> eventOptional = events.stream()
                .filter(e -> e instanceof TransactionStatusEvent)
                .map(e->(TransactionStatusEvent)e)
                .filter(e -> Optional.of(cmdId).equals(e.getCommandId()))
                .filter(e -> e.getStatus() == expectedStatus)
                .findFirst();

        assertThat(eventOptional.isPresent(), is(true));


        return eventOptional.get();
    }

    private static ErrorEvent assertErrorEvent(List<AbstractEvent> events, UUID cmdId) {
        final Optional<AbstractEvent> eventOptional = events.stream().filter(e -> Optional.of(cmdId).equals(e.getCommandId())).findFirst();

        assertThat(eventOptional.isPresent(), is(true));

        ErrorEvent errorEvent = (ErrorEvent) eventOptional.get();
        final Optional<UUID> commandId = errorEvent.getCommandId();
        assertThat("commandId", commandId.isPresent(), is(true));
        assertThat("commandId", commandId.get(), is(cmdId));
//        assertThat("message", errorEvent.getStatus(), is(expectedStatus));

        return errorEvent;
    }

}