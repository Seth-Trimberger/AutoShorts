package com.autoshorts.scraper.service.video;

import com.autoshorts.scraper.config.AutoShortsProperties;
import com.autoshorts.scraper.util.ProcessRunner;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class WhisperCaptionService implements CaptionService {

    private final AutoShortsProperties properties;

    public WhisperCaptionService(AutoShortsProperties properties) {
        this.properties = properties;
    }

    @Override
    public Path generateCaptions(String redditId, Path audioPath) throws Exception {
        Path captionsDir = Path.of(properties.getAssets().getCaptionsDir());
        Files.createDirectories(captionsDir);

        Path expectedSrt = properties.getAssets().captionPath(redditId);

        List<String> command = List.of(
                properties.getWhisper().getCommand(),
                audioPath.toString(),
                "--model", properties.getWhisper().getModel(),
                "--output_format", "srt",
                "--output_dir", captionsDir.toString()
        );

        System.out.println("CAPTIONS: Generating subtitles with whisper...");
        ProcessRunner.run(command, 60);

        Path whisperOutput = captionsDir.resolve(stripExtension(audioPath.getFileName().toString()) + ".srt");
        if (!Files.exists(whisperOutput)) {
            throw new IllegalStateException("whisper did not produce captions at " + whisperOutput);
        }

        if (!whisperOutput.equals(expectedSrt)) {
            Files.move(whisperOutput, expectedSrt, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        return expectedSrt;
    }

    private String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }
}
