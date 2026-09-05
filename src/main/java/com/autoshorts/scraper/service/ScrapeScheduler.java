package com.autoshorts.scraper.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
public class ScrapeScheduler {

    private final QueueProcessor queueProcessor;
    private final Random random = new Random();

    public ScrapeScheduler(QueueProcessor queueProcessor) {
        this.queueProcessor = queueProcessor;
    }

    @Async
    public void runScrapeWithJitter(int runNumber, Runnable onComplete) {
        try {
            long minDelay = TimeUnit.MINUTES.toMillis(5);
            long maxDelay = TimeUnit.MINUTES.toMillis(90);
            long jitter = minDelay + (long) (random.nextDouble() * (maxDelay - minDelay));

            System.out.println("AutomatedWorker: Production delay of " + (jitter / 60000) + " minutes starting...");
            Thread.sleep(jitter);

            System.out.println("AutomatedWorker: Executing scrape " + (runNumber + 1) + "/9...");
            queueProcessor.processNextInQueue();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            onComplete.run();
        }
    }
}
