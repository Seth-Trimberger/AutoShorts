package com.autoshorts.scraper.service.video;

import com.autoshorts.scraper.config.AutoShortsProperties;
import com.autoshorts.scraper.util.ProcessRunner;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class FfmpegAssemblyServiceImpl implements FfmpegAssemblyService {

    private final AutoShortsProperties properties;

    public FfmpegAssemblyServiceImpl(AutoShortsProperties properties) {
        this.properties = properties;
    }

    @Override
    public Path assembleVideo(Path backgroundVideo, Path audioPath, Path captionPath, Path outputPath) throws Exception {
        Files.createDirectories(outputPath.getParent());

        String subtitleFilter = buildSubtitleFilter(captionPath);
        String videoFilter = "scale=1080:1920:force_original_aspect_ratio=increase,crop=1080:1920" + subtitleFilter;

        List<String> command = new ArrayList<>();
        command.add(properties.getFfmpeg().getPath());
        command.add("-stream_loop");
        command.add("-1");
        command.add("-i");
        command.add(backgroundVideo.toString());
        command.add("-i");
        command.add(audioPath.toString());
        command.add("-vf");
        command.add(videoFilter);
        command.add("-map");
        command.add("0:v");
        command.add("-map");
        command.add("1:a");
        command.add("-c:v");
        command.add("libx264");
        command.add("-preset");
        command.add("fast");
        command.add("-crf");
        command.add("23");
        command.add("-c:a");
        command.add("aac");
        command.add("-b:a");
        command.add("192k");
        command.add("-shortest");
        command.add("-y");
        command.add(outputPath.toString());

        System.out.println("FFMPEG: Assembling 9:16 video...");
        ProcessRunner.run(command, 30);

        if (!Files.exists(outputPath)) {
            throw new IllegalStateException("ffmpeg did not produce video at " + outputPath);
        }

        return outputPath;
    }

    private String buildSubtitleFilter(Path captionPath) {
        if (captionPath == null || !Files.exists(captionPath)) {
            return "";
        }

        String escapedPath = captionPath.toAbsolutePath().toString()
                .replace("\\", "\\\\")
                .replace(":", "\\:")
                .replace("'", "\\'");

        return ",subtitles='" + escapedPath
                + "':force_style='FontName=Arial,FontSize=28,PrimaryColour=&HFFFFFF&,"
                + "OutlineColour=&H000000&,BorderStyle=3,Outline=2,Shadow=1,Alignment=2,MarginV=80'";
    }
}
