package com.autoshorts.scraper.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.autoshorts.scraper.Model.AutomationState;
import com.autoshorts.scraper.repository.AutomationStateRepository;

@Service
public class AutomatedWorker {

    private final VideoPipelineOrchestrator videoPipelineOrchestrator;
    private final ScrapeScheduler scrapeScheduler;
    private final AutomationStateRepository automationStateRepository;
    private final AtomicBoolean scrapeInProgress = new AtomicBoolean(false);

    private static final LocalTime START_TIME = LocalTime.of(9, 0);
    private static final LocalTime END_TIME = LocalTime.of(21, 0);
    private static final String SCRAPE_STATE_KEY = "reddit-scrape";
    private static final int MAX_DAILY_RUNS = 9;

    public AutomatedWorker(VideoPipelineOrchestrator videoPipelineOrchestrator,
                           ScrapeScheduler scrapeScheduler,
                           AutomationStateRepository automationStateRepository) {
        this.videoPipelineOrchestrator = videoPipelineOrchestrator;
        this.scrapeScheduler = scrapeScheduler;
        this.automationStateRepository = automationStateRepository;
    }

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public synchronized void scheduleManager() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();

        AutomationState state = automationStateRepository.findById(SCRAPE_STATE_KEY)
                .orElseGet(() -> new AutomationState(SCRAPE_STATE_KEY, now.toLocalDate(), 0));
        if (!now.toLocalDate().equals(state.getRunDate())) {
            state.setRunDate(now.toLocalDate());
            state.setRunCount(0);
        }

        if (!currentTime.isBefore(START_TIME) && currentTime.isBefore(END_TIME)
                && state.getRunCount() < MAX_DAILY_RUNS
                && scrapeInProgress.compareAndSet(false, true)) {
                int runNumber = state.getRunCount();
                state.setRunCount(runNumber + 1);
                automationStateRepository.save(state);
                scrapeScheduler.runScrapeWithJitter(runNumber, () -> {
                    scrapeInProgress.set(false);
                });
        } else {
            automationStateRepository.save(state);
        }
    }

    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.MINUTES)
    public void scheduledVideoRenderer() {
        LocalTime currentTime = LocalTime.now();
        if (currentTime.isAfter(START_TIME) && currentTime.isBefore(END_TIME)) {
            System.out.println("AutomatedWorker: Checking for pending videos to render...");
            videoPipelineOrchestrator.processNextStoryForVideo();
        }
    }
}
