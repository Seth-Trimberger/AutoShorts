package com.autoshorts.scraper;

import com.autoshorts.scraper.Model.QueueItem;
import com.autoshorts.scraper.config.AutoShortsProperties;
import com.autoshorts.scraper.repository.QueueRepository;
import com.autoshorts.scraper.service.QueueProcessor;
import com.autoshorts.scraper.service.VideoPipelineOrchestrator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Scanner;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class ScraperApplication implements CommandLineRunner {

    private final QueueRepository queueRepository;
    private final QueueProcessor queueProcessor;
    private final VideoPipelineOrchestrator videoPipelineOrchestrator;
    private final AutoShortsProperties properties;

    public ScraperApplication(QueueRepository queueRepository,
                              QueueProcessor queueProcessor,
                              VideoPipelineOrchestrator videoPipelineOrchestrator,
                              AutoShortsProperties properties) {
        this.queueRepository = queueRepository;
        this.queueProcessor = queueProcessor;
        this.videoPipelineOrchestrator = videoPipelineOrchestrator;
        this.properties = properties;
    }

    public static void main(String[] args) {
        SpringApplication.run(ScraperApplication.class, args);
    }

    @Override
    public void run(String... args) {
        if (!properties.getCli().isEnabled()) {
            System.out.println("CLI disabled (autoshorts.cli.enabled=false). Scheduling and automation continue.");
            return;
        }

        if (System.console() == null) {
            System.out.println("No interactive console detected. CLI skipped; scheduling and automation continue.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        if (!scanner.hasNextLine()) {
            System.out.println("No stdin available. CLI skipped; scheduling and automation continue.");
            return;
        }

        System.out.println("\n--- REDDIT QUEUE MANAGER STARTING ---");
        System.out.println("Commands: [URL] to add, 'process' to scrape top 1, 'render' to video next pending,");
        System.out.println("          'render <id>' for specific story, 'render <id> voice <voice>' to override voice,");
        System.out.println("          'voices' to list default voice pool, 'exit' to quit.");

        while (scanner.hasNextLine()) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Exiting application...");
                System.exit(0);
            }

            if (input.equalsIgnoreCase("process")) {
                queueProcessor.processNextInQueue();
                continue;
            }

            if (input.equalsIgnoreCase("render")) {
                videoPipelineOrchestrator.processNextStoryForVideo();
                continue;
            }

            if (input.equalsIgnoreCase("voices")) {
                printVoicePool();
                continue;
            }

            if (input.toLowerCase().startsWith("render ")) {
                handleRenderCommand(input.substring(7).trim());
                continue;
            }

            if (input.startsWith("http")) {
                if (!queueRepository.existsByUrl(input)) {
                    QueueItem item = new QueueItem();
                    item.setUrl(input);
                    item.setStatus(0);
                    queueRepository.save(item);
                    System.out.println("ADDED: " + input);
                } else {
                    System.out.println("SKIPPED: URL already in queue.");
                }
            } else if (!input.isEmpty()) {
                System.out.println("INVALID: Please enter a valid URL, 'process', 'render', 'voices', or 'exit'.");
            }
        }

        System.out.println("stdin closed. CLI exiting; scheduling and automation continue.");
    }

    private void printVoicePool() {
        AutoShortsProperties.Tts tts = properties.getTts();
        System.out.println("Default voice pool (mode=" + tts.getVoiceMode() + "):");
        for (String voice : tts.getVoices()) {
            System.out.println("  - " + voice);
        }
        System.out.println("Fallback voice: " + tts.getVoice());
        System.out.println("Override: render <id> voice <name>");
        System.out.println("Preview:  edge-tts --voice en-GB-SoniaNeural --text \"test\" --write-media /tmp/test.mp3");
    }

    private void handleRenderCommand(String args) {
        String[] parts = args.split("\\s+");
        if (parts.length == 0 || parts[0].isBlank()) {
            System.out.println("INVALID: render id must be a number, e.g. 'render 1'.");
            return;
        }

        try {
            long storyId = Long.parseLong(parts[0]);
            String voiceOverride = null;
            if (parts.length >= 3 && parts[1].equalsIgnoreCase("voice")) {
                voiceOverride = parts[2];
            } else if (parts.length != 1) {
                System.out.println("INVALID: use 'render <id>' or 'render <id> voice <voiceName>'.");
                return;
            }

            videoPipelineOrchestrator.renderStory(storyId, voiceOverride);
        } catch (NumberFormatException e) {
            System.out.println("INVALID: render id must be a number, e.g. 'render 1'.");
        }
    }
}
