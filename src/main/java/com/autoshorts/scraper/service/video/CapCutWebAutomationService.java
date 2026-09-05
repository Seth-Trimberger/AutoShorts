package com.autoshorts.scraper.service.video;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
@ConditionalOnProperty(name = "autoshorts.tts.provider", havingValue = "capcut-web")
public class CapCutWebAutomationService implements TtsService {

    @Override
    public Path synthesize(String redditId, String narrationScript) {
        throw new UnsupportedOperationException(
                "CapCut Web TTS automation is not implemented yet. "
                        + "Set autoshorts.tts.provider=edge-tts to use free edge-tts instead. "
                        + "CapCut RPA requires OculiX/SikuliX screenshot templates and a fixed browser layout.");
    }
}
