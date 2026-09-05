package com.autoshorts.scraper.service.video;

import com.autoshorts.scraper.config.AutoShortsProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

@Service
public class LocalBackgroundService implements BackgroundAssetService {

    private final AutoShortsProperties properties;
    private final Random random = new Random();

    public LocalBackgroundService(AutoShortsProperties properties) {
        this.properties = properties;
    }

    @Override
    public Path selectBackground() throws IOException {
        Path backgroundsDir = Path.of(properties.getAssets().getBackgroundsDir());

        if (!Files.isDirectory(backgroundsDir)) {
            throw new IOException("Backgrounds directory not found: " + backgroundsDir);
        }

        List<Path> backgrounds;
        try (Stream<Path> stream = Files.list(backgroundsDir)) {
            backgrounds = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> hasVideoExtension(path.getFileName().toString()))
                    .toList();
        }

        if (backgrounds.isEmpty()) {
            throw new IOException("No background videos found in " + backgroundsDir
                    + ". Add .mp4 or .mov files from Pexels/Pixabay.");
        }

        Path selected = backgrounds.get(random.nextInt(backgrounds.size()));
        System.out.println("BACKGROUND: Selected " + selected.getFileName());
        return selected;
    }

    private boolean hasVideoExtension(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".mp4") || lower.endsWith(".mov") || lower.endsWith(".webm");
    }
}
