package com.autoshorts.scraper.service.video;

import java.nio.file.Path;

public interface FfmpegAssemblyService {

    Path assembleVideo(Path backgroundVideo, Path audioPath, Path captionPath, Path outputPath) throws Exception;
}
