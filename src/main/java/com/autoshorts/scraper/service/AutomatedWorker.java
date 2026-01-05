package com.autoshorts.scraper.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AutomatedWorker {

    private final QueueProcessor queueProcessor;
    private final Random random = new Random();

    private int dailyRunCount = 0;
    private int lastRunDay = -1;

    // The 12-hour active window (e.g., 9 AM to 9 PM)
    private static final LocalTime START_TIME = LocalTime.of(9, 0);
    private static final LocalTime END_TIME = LocalTime.of(21, 0);

    public AutomatedWorker(QueueProcessor queueProcessor) {
        this.queueProcessor = queueProcessor;
    }

    // Check every 5 minutes to see if the window is open
    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void scheduleManager() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();

        // Reset the daily counter when the day changes
        if (now.getDayOfMonth() != lastRunDay) {
            dailyRunCount = 0;
            lastRunDay = now.getDayOfMonth();
        }

        // Only run if within the 12-hour window and under the 9-run limit
        if (currentTime.isAfter(START_TIME) && currentTime.isBefore(END_TIME)) {
            if (dailyRunCount < 9) {
                executeWorkWithProductionJitter();
            }
        }
    }

    private void executeWorkWithProductionJitter() {
        try {
            // PRODUCTION RANGE: 5 mins to 90 mins (1.5 hours)
            long minDelay = TimeUnit.MINUTES.toMillis(5);
            long maxDelay = TimeUnit.MINUTES.toMillis(90);

            long jitter = minDelay + (long)(random.nextDouble() * (maxDelay - minDelay));

            System.out.println("AutomatedWorker: Production delay of " + (jitter / 60000) + " minutes starting...");

            Thread.sleep(jitter);

            System.out.println("AutomatedWorker: Executing scrape " + (dailyRunCount + 1) + "/9...");
            queueProcessor.processNextInQueue();

            dailyRunCount++;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}