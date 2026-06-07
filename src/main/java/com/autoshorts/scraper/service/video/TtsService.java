package com.autoshorts.scraper.service.video;

import java.nio.file.Path;

public interface TtsService {

    Path synthesize(String redditId, String narrationScript) throws Exception;
}
