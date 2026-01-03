package com.autoshorts.scraper.Model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "story_queue")
@Data
public class QueueItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String url;

    // 0 = Pending, 1 = Processed
    private int status = 0;

    // We add a timestamp so we can be 100% sure we process in order
    private LocalDateTime createdAt = LocalDateTime.now();
}