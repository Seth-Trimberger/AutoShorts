package com.autoshorts.scraper.service.video;

import java.nio.file.Path;

public interface CaptionService {

    Path generateCaptions(String redditId, Path audioPath) throws Exception;
}
