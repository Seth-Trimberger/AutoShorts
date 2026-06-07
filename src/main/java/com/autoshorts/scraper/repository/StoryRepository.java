package com.autoshorts.scraper.repository;

import com.autoshorts.scraper.Model.AssetStatus;
import com.autoshorts.scraper.Model.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {
    boolean existsByRedditId(String redditId);

    Optional<Story> findFirstByAssetStatusOrderByIdAsc(AssetStatus assetStatus);
}