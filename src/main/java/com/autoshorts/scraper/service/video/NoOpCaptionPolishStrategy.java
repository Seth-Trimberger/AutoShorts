package com.autoshorts.scraper.service.video;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NoOpCaptionPolishStrategy implements CaptionPolishStrategy {

    @Override
    public List<CaptionCue> polish(List<CaptionCue> cues, String narrationScript) {
        return cues;
    }
}
