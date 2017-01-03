package com.github.kpavlov.akkabox.stream.expensiveProducer;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.stream.actor.ActorPublisherMessage;
import akka.stream.actor.UntypedActorPublisher;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

class SlowServiceActor extends UntypedActorPublisher<String> {

    private static final int DELAY_SEC = 3;
    private final SlowService service;

    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    public SlowServiceActor(SlowService service) {
        this.service = service;
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof ActorPublisherMessage.Request) {

            final int demand = (int) totalDemand();
            if (demand == 0) {
                logger.info("No demand");
                return;
            }

            int fetchedCount = service.list(demand, this::onNext);

            if (fetchedCount == 0) {
                scheduleRequest((ActorPublisherMessage.Request) message);
            }

        } else {
            logger.error("Unsupported message: {}", message);
        }
    }

    private void scheduleRequest(ActorPublisherMessage.Request message) {
        logger.info("Re-scheduling DB request in {} seconds", DELAY_SEC);
        final ActorSystem system = this.context().system();
        system.scheduler().scheduleOnce(FiniteDuration.create(DELAY_SEC, TimeUnit.SECONDS),
                getSelf(),
                message,
                system.dispatcher(), ActorRef.noSender()
        );
    }
}
