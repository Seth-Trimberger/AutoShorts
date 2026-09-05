package com.autoshorts.scraper.service.video;

import com.autoshorts.scraper.config.AutoShortsProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class VoiceResolver {

    private final AutoShortsProperties properties;

    public VoiceResolver(AutoShortsProperties properties) {
        this.properties = properties;
    }

    public String resolveVoice(String voiceOverride) {
        if (voiceOverride != null && !voiceOverride.isBlank()) {
            return normalizeVoice(voiceOverride.strip());
        }

        AutoShortsProperties.Tts tts = properties.getTts();
        if ("random".equalsIgnoreCase(tts.getVoiceMode())) {
            List<String> voices = tts.getVoices();
            if (voices != null && !voices.isEmpty()) {
                String selected = voices.get(ThreadLocalRandom.current().nextInt(voices.size()));
                return normalizeVoice(selected);
            }
        }

        return normalizeVoice(tts.getVoice());
    }

    private String normalizeVoice(String voice) {
        if (voice.contains("-")) {
            return voice;
        }

        List<String> configuredVoices = properties.getTts().getVoices();
        if (configuredVoices != null) {
            for (String configured : configuredVoices) {
                if (configured.endsWith(voice) || configured.equalsIgnoreCase(voice)) {
                    return configured;
                }
            }
        }

        return "en-US-" + voice;
    }
}
