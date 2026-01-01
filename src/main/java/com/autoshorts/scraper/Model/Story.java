package com.autoshorts.scraper.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "stories")
@Data // Lombok: Generates Getters, Setters, toString, etc.
@NoArgsConstructor
@AllArgsConstructor
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // We use unique = true so we don't save the same Reddit post twice
    @Column(unique = true, nullable = false)
    private String redditId;

    private String title;

    private String author;

    // "TEXT" allows for long stories, standard String only allows 255 characters
    @Column(columnDefinition = "TEXT")
    private String content;
}