package com.autoshorts.scraper.service.video;

import java.util.List;

public interface CaptionPolishStrategy {

    List<CaptionCue> polish(List<CaptionCue> cues, String narrationScript);
}
