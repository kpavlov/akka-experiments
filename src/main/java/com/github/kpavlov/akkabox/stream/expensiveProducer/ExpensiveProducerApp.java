package com.github.kpavlov.akkabox.stream.expensiveProducer;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.ThrottleMode;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.typesafe.config.ConfigFactory;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class ExpensiveProducerApp {

    public static void main(String[] args) {

        SlowService service = new SlowService();

        ActorSystem system = ActorSystem.create("jas", ConfigFactory.load());
        final Materializer materializer = ActorMaterializer.create(system);

        Source source = Source.actorPublisher(Props
                .create(SlowServiceActor.class, service)
                .withDispatcher("akka.stream.default-blocking-io-dispatcher")
        ).log("source");

        source
                .throttle(1, Duration.create(200, TimeUnit.MILLISECONDS), 1, ThrottleMode.shaping())
                .runWith(Sink.foreach(r -> system.log().info("Result: {}", r)), materializer);
    }
}
