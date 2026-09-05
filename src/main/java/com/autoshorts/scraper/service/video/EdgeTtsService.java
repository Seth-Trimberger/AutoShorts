package com.autoshorts.scraper.service.video;

import com.autoshorts.scraper.config.AutoShortsProperties;
import com.autoshorts.scraper.util.ProcessRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
@ConditionalOnProperty(name = "autoshorts.tts.provider", havingValue = "edge-tts", matchIfMissing = true)
public class EdgeTtsService implements TtsService {

    private final AutoShortsProperties properties;
    private final VoiceResolver voiceResolver;

    public EdgeTtsService(AutoShortsProperties properties, VoiceResolver voiceResolver) {
        this.properties = properties;
        this.voiceResolver = voiceResolver;
    }

    @Override
    public Path synthesize(String redditId, String narrationScript) throws Exception {
        return synthesize(redditId, narrationScript, null);
    }

    @Override
    public Path synthesize(String redditId, String narrationScript, String voice) throws Exception {
        Path audioPath = properties.getAssets().audioPath(redditId);
        Files.createDirectories(audioPath.getParent());

        String resolvedVoice = voiceResolver.resolveVoice(voice);
        Path scriptFile = Files.createTempFile("narration-" + redditId + "-", ".txt");
        try {
            Files.writeString(scriptFile, narrationScript);

            List<String> command = List.of(
                    properties.getTts().getEdgeTtsCommand(),
                    "--voice", resolvedVoice,
                    "--file", scriptFile.toString(),
                    "--write-media", audioPath.toString()
            );

            System.out.println("TTS: Generating audio with edge-tts (" + resolvedVoice + ")...");
            ProcessRunner.run(command, 30);

            if (!Files.exists(audioPath)) {
                throw new IllegalStateException("edge-tts did not produce audio at " + audioPath);
            }

            return audioPath;
        } finally {
            Files.deleteIfExists(scriptFile);
        }
    }
}
