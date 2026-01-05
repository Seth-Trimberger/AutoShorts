package com.autoshorts.scraper.service;

import org.springframework.stereotype.Service;

@Service
public class StoryProcessor {

    private static final int WORDS_PER_MINUTE = 150;
    private static final int MIN_SECONDS = 45;

    public String processForScript(String title, String rawContent) {
        if (rawContent == null || rawContent.isEmpty()) return "";

        // Remove URLs and Emojis
        String clean = rawContent.replaceAll("https?://\\S+\\s?", "");
        clean = clean.replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}]", "");

        // Format Edits/Updates for narration
        clean = clean.replaceAll("(?i)EDIT:", "\n\nUpdate: ");
        clean = clean.replaceAll("(?i)UPDATE:", "\n\nUpdate: ");

        return clean.trim();
    }

    public boolean meetsMinimumLength(String title, String content) {
        String fullText = title + " " + content;
        String[] words = fullText.split("\\s+");

        // Math: (Word Count / 150) * 60 seconds
        double estimatedSeconds = (words.length / (double) WORDS_PER_MINUTE) * 60;

        System.out.println("Processing Story: " + words.length + " words | Est. Time: " + String.format("%.2f", estimatedSeconds) + "s");

        if (estimatedSeconds < MIN_SECONDS) {
            System.out.println("REJECTED: Too short (" + String.format("%.2f", estimatedSeconds) + "s). Min required: 45s.");
            return false;
        }

        System.out.println("ACCEPTED: Story meets minimum length requirements.");
        return true;
    }
}