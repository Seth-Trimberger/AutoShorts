package com.autoshorts.scraper.repository;

import com.autoshorts.scraper.Model.QueueItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface QueueRepository extends JpaRepository<QueueItem, Long> {

    // This finds the first (oldest) item where status is 0
    Optional<QueueItem> findFirstByStatusOrderByIdAsc(int status);

    boolean existsByUrl(String url);
}