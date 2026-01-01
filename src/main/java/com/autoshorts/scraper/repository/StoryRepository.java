package com.autoshorts.scraper.repository;

import com.autoshorts.scraper.Model.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {
    // This tells Spring to automatically create a method to find a story by its Reddit ID
    boolean existsByRedditId(String redditId);
}