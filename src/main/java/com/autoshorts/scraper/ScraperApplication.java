package com.autoshorts.scraper;

import com.autoshorts.scraper.Model.QueueItem; // Check if your 'model' folder is lowercase
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
@EnableScheduling   // Allows @Scheduled tasks
@EnableAsync        // Allows tasks to run on a background thread
public class ScraperApplication implements CommandLineRunner {

    private final QueueRepository queueRepository;
    private final QueueProcessor queueProcessor;
    private final VideoPipelineOrchestrator videoPipelineOrchestrator;

    public ScraperApplication(QueueRepository queueRepository,
                              QueueProcessor queueProcessor,
                              VideoPipelineOrchestrator videoPipelineOrchestrator) {
        this.queueRepository = queueRepository;
        this.queueProcessor = queueProcessor;
        this.videoPipelineOrchestrator = videoPipelineOrchestrator;
    }

    public static void main(String[] args) {
        SpringApplication.run(ScraperApplication.class, args);
    }

    @Override
    public void run(String... args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n--- REDDIT QUEUE MANAGER STARTING ---");
        System.out.println("Commands: [URL] to add, 'process' to scrape top 1, 'render' to video next pending, 'render <id>' for specific story, 'exit' to quit.");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Exiting application...");
                System.exit(0);
            }

            // 3. Trigger the processor
            if (input.equalsIgnoreCase("process")) {
                queueProcessor.processNextInQueue();
                continue;
            }

            if (input.equalsIgnoreCase("render")) {
                videoPipelineOrchestrator.processNextStoryForVideo();
                continue;
            }

            if (input.toLowerCase().startsWith("render ")) {
                try {
                    long storyId = Long.parseLong(input.substring(7).trim());
                    videoPipelineOrchestrator.renderStory(storyId);
                } catch (NumberFormatException e) {
                    System.out.println("INVALID: render id must be a number, e.g. 'render 1'.");
                }
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
                //checks
            } else if (!input.isEmpty()) {
                System.out.println("INVALID: Please enter a valid URL, 'process', 'render', or 'exit'.");
            }
        }
    }
}