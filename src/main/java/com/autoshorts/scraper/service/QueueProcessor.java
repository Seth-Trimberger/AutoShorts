package com.autoshorts.scraper.service;

import com.autoshorts.scraper.Model.QueueItem; // Changed 'Model' to 'model'
import com.autoshorts.scraper.Model.Story;     // Changed 'Model' to 'model'
import com.autoshorts.scraper.repository.QueueRepository;
import com.autoshorts.scraper.repository.StoryRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class QueueProcessor {

    private static final int MAX_ATTEMPTS = 3;

    private final QueueRepository queueRepository;
    private final RedditService redditService;
    private final StoryRepository storyRepository;
    private final StoryProcessor storyProcessor; // 1. Added this field

    // 2. Added storyProcessor to the constructor
    public QueueProcessor(QueueRepository queueRepository,
                          RedditService redditService,
                          StoryRepository storyRepository,
                          StoryProcessor storyProcessor) {
        this.queueRepository = queueRepository;
        this.redditService = redditService;
        this.storyRepository = storyRepository;
        this.storyProcessor = storyProcessor;
    }

    public void processNextInQueue() {
        Optional<QueueItem> nextItem = queueRepository.findFirstByStatusOrderByIdAsc(QueueItem.PENDING);

        if (nextItem.isPresent()) {
            QueueItem item = nextItem.get();
            System.out.println("Processing from Queue: " + item.getUrl());

            try {
                Story scraped = redditService.scrapeStoryFromUrl(item.getUrl());
                if (scraped == null) {
                    throw new IllegalStateException("Reddit story could not be scraped");
                }

                if (storyRepository.existsByRedditId(scraped.getRedditId())) {
                    System.out.println("DATABASE: Story already exists; marking URL processed.");
                    markProcessed(item);
                    return;
                }

                String cleanContent = storyProcessor.processForScript(scraped.getTitle(), scraped.getContent());
                if (storyProcessor.meetsMinimumLength(scraped.getTitle(), cleanContent)) {
                    scraped.setContent(cleanContent);
                    storyRepository.save(scraped);
                    System.out.println("DATABASE: Story archived successfully.");
                } else {
                    System.out.println("DATABASE: URL skipped (Below 45s minimum).");
                }

                markProcessed(item);
            } catch (Exception e) {
                markFailedOrRetry(item, e);
            }
        } else {
            System.out.println("Queue is currently empty.");
        }
    }

    private void markProcessed(QueueItem item) {
        item.setStatus(QueueItem.PROCESSED);
        item.setErrorMessage(null);
        queueRepository.save(item);
    }

    private void markFailedOrRetry(QueueItem item, Exception exception) {
        int attempts = item.getAttempts() + 1;
        item.setAttempts(attempts);
        item.setErrorMessage(messageFor(exception));
        item.setStatus(attempts >= MAX_ATTEMPTS ? QueueItem.FAILED : QueueItem.PENDING);
        queueRepository.save(item);
        System.err.println("QUEUE: Attempt " + attempts + "/" + MAX_ATTEMPTS + " failed for "
                + item.getUrl() + ": " + item.getErrorMessage());
    }

    private String messageFor(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
    }
}
