package com.autoshorts.scraper.Model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "story_queue")
@Data
public class QueueItem {

    public static final int PENDING = 0;
    public static final int PROCESSED = 1;
    public static final int FAILED = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String url;

    // 0 = Pending, 1 = Processed, 2 = Failed after the retry limit
    @Column(nullable = false)
    private int status = PENDING;

    @Column(nullable = false)
    private int attempts = 0;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    // We add a timestamp so we can be 100% sure we process in order
    private LocalDateTime createdAt = LocalDateTime.now();
}
