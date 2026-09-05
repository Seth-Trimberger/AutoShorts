package com.autoshorts.scraper.repository;

import com.autoshorts.scraper.Model.AssetStatus;
import com.autoshorts.scraper.Model.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {
    boolean existsByRedditId(String redditId);

    Optional<Story> findFirstByAssetStatusOrderByIdAsc(AssetStatus assetStatus);

    @Modifying
    @Transactional
    @Query("update Story s set s.assetStatus = :claimedStatus "
            + "where s.id = :storyId and (s.assetStatus = :pendingStatus or s.assetStatus = :failedStatus)")
    int claimForRendering(@Param("storyId") Long storyId,
                          @Param("claimedStatus") AssetStatus claimedStatus,
                          @Param("pendingStatus") AssetStatus pendingStatus,
                          @Param("failedStatus") AssetStatus failedStatus);
}
