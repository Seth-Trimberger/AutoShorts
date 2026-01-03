package com.autoshorts.scraper.service;

import com.autoshorts.scraper.Model.QueueItem;
import com.autoshorts.scraper.Model.Story;
import com.autoshorts.scraper.repository.QueueRepository;
import com.autoshorts.scraper.repository.StoryRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class QueueProcessor {

    private final QueueRepository queueRepository;
    private final RedditService redditService;
    private final StoryRepository storyRepository;

    public QueueProcessor(QueueRepository queueRepository, RedditService redditService, StoryRepository storyRepository) {
        this.queueRepository = queueRepository;
        this.redditService = redditService;
        this.storyRepository = storyRepository;
    }

    public void processNextInQueue() {
        // 1. Get the top 1 item with status 0
        Optional<QueueItem> nextItem = queueRepository.findFirstByStatusOrderByIdAsc(0);

        if (nextItem.isPresent()) {
            QueueItem item = nextItem.get();
            System.out.println("Processing URL: " + item.getUrl());

            // 2. Run your existing Scraper logic
            Story scrapedStory = redditService.scrapeStoryFromUrl(item.getUrl());

            if (scrapedStory != null) {
                // 3. Save to the 'stories' table
                storyRepository.save(scrapedStory);

                // 4. Update the Queue status to 1 (So we don't scrape it again)
                item.setStatus(1);
                queueRepository.save(item);

                System.out.println("Success! Scraped: " + scrapedStory.getTitle());
            }
        } else {
            System.out.println("No pending items in the queue.");
        }
    }
}