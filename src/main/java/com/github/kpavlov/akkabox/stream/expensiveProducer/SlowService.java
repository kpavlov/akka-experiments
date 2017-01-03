package com.github.kpavlov.akkabox.stream.expensiveProducer;

import org.slf4j.Logger;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

class SlowService {

    private static final Random random = new Random();

    private final Logger logger = getLogger(SlowService.class);
    private final AtomicLong counter = new AtomicLong();

    public int list(int limit, Consumer<String> callback) {
        try {
            logger.info("Requested {}", limit);
            logger.info("Requesting Data from DB....");
            if (random.nextBoolean()) {
                Thread.sleep(20);
                logger.info("Nothing");
                return 0;
            } else {
                Thread.sleep(1000);
                logger.info("Fetching Data from DB....");
            }
            for (int i = 0; i < limit; i++) {
                callback.accept("[" + counter.incrementAndGet() + "]: " + UUID.randomUUID());
            }

            logger.info("Returned: {}", limit);
            return limit;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
