package com.autoshorts.scraper.service;

import com.autoshorts.scraper.Model.QueueItem;
import com.autoshorts.scraper.repository.QueueRepository;
import com.autoshorts.scraper.repository.StoryRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QueueProcessorTest {

    @Test
    void scrapeFailuresAreRetriedThenMovedToFailedState() {
        QueueRepository queueRepository = mock(QueueRepository.class);
        RedditService redditService = mock(RedditService.class);
        StoryRepository storyRepository = mock(StoryRepository.class);
        StoryProcessor storyProcessor = mock(StoryProcessor.class);

        QueueItem item = new QueueItem();
        item.setUrl("https://www.reddit.com/r/test/comments/example/story");
        when(queueRepository.findFirstByStatusOrderByIdAsc(QueueItem.PENDING))
                .thenReturn(Optional.of(item));
        when(redditService.scrapeStoryFromUrl(item.getUrl())).thenReturn(null);

        QueueProcessor processor = new QueueProcessor(
                queueRepository, redditService, storyRepository, storyProcessor);

        processor.processNextInQueue();
        assertEquals(QueueItem.PENDING, item.getStatus());
        assertEquals(1, item.getAttempts());

        processor.processNextInQueue();
        assertEquals(QueueItem.PENDING, item.getStatus());
        assertEquals(2, item.getAttempts());

        processor.processNextInQueue();
        assertEquals(QueueItem.FAILED, item.getStatus());
        assertEquals(3, item.getAttempts());
    }
}
