package com.autoshorts.scraper.service.video;

import java.nio.file.Path;

public interface TtsService {

    Path synthesize(String redditId, String narrationScript) throws Exception;

    default Path synthesize(String redditId, String narrationScript, String voice) throws Exception {
        return synthesize(redditId, narrationScript);
    }
}
