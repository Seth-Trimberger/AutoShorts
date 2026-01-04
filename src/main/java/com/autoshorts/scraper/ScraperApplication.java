package com.autoshorts.scraper;

import com.autoshorts.scraper.Model.QueueItem; // Check if your 'model' folder is lowercase
import com.autoshorts.scraper.repository.QueueRepository;
import com.autoshorts.scraper.service.QueueProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
public class ScraperApplication implements CommandLineRunner {

    private final QueueRepository queueRepository;
    private final QueueProcessor queueProcessor; // 1. Add this field

    // 2. Inject BOTH into the constructor
    public ScraperApplication(QueueRepository queueRepository, QueueProcessor queueProcessor) {
        this.queueRepository = queueRepository;
        this.queueProcessor = queueProcessor;
    }

    public static void main(String[] args) {
        SpringApplication.run(ScraperApplication.class, args);
    }

    @Override
    public void run(String... args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n--- REDDIT QUEUE MANAGER STARTING ---");
        System.out.println("Commands: [URL] to add, 'process' to scrape top 1, 'exit' to quit.");

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
                continue; // Skip the URL check below
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
                System.out.println("INVALID: Please enter a valid URL, 'process', or 'exit'.");
            }
        }
    }
}