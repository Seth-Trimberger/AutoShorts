package com.autoshorts.scraper.service;

import com.autoshorts.scraper.Model.QueueItem; // Changed 'Model' to 'model'
import com.autoshorts.scraper.Model.Story;     // Changed 'Model' to 'model'
import com.autoshorts.scraper.repository.QueueRepository;
import com.autoshorts.scraper.repository.StoryRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class QueueProcessor {

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
        Optional<QueueItem> nextItem = queueRepository.findFirstByStatusOrderByIdAsc(0);

        if (nextItem.isPresent()) {
            QueueItem item = nextItem.get();
            System.out.println("Processing from Queue: " + item.getUrl());

            Story scraped = redditService.scrapeStoryFromUrl(item.getUrl());

            if (scraped != null) {
                // 3. Clean the text using our new processor
                String cleanContent = storyProcessor.processForScript(scraped.getTitle(), scraped.getContent());

                // 4. Check if it's at least 45 seconds long
                if (storyProcessor.meetsMinimumLength(scraped.getTitle(), cleanContent)) {
                    scraped.setContent(cleanContent);
                    storyRepository.save(scraped);
                    System.out.println("DATABASE: Story archived successfully.");
                } else {
                    System.out.println("DATABASE: URL skipped (Below 45s minimum).");
                }

                // 5. Mark as processed (1) so it's removed from the 'To-Do' list
                item.setStatus(1);
                queueRepository.save(item);
            }
        } else {
            System.out.println("Queue is currently empty.");
        }
    }
}