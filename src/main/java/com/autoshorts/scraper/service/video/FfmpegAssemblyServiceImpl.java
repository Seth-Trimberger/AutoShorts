package com.autoshorts.scraper.service.video;

import com.autoshorts.scraper.config.AutoShortsProperties;
import com.autoshorts.scraper.config.BackgroundMode;
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

        int width = properties.getVideo().getWidth();
        int height = properties.getVideo().getHeight();
        String subtitleFilter = buildSubtitleFilter(captionPath);

        List<String> command = new ArrayList<>();
        command.add(properties.getFfmpeg().getPath());
        command.add("-stream_loop");
        command.add("-1");
        command.add("-i");
        command.add(backgroundVideo.toString());
        command.add("-i");
        command.add(audioPath.toString());

        if (properties.getVideo().getBackgroundMode() == BackgroundMode.BLUR_FILL) {
            command.add("-filter_complex");
            command.add(buildBlurFillFilter(width, height, subtitleFilter));
            command.add("-map");
            command.add("[vout]");
        } else {
            String videoFilter = buildCropFilter(width, height) + subtitleFilter;
            command.add("-vf");
            command.add(videoFilter);
            command.add("-map");
            command.add("0:v");
        }

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

        System.out.println("FFMPEG: Assembling " + width + "x" + height + " video...");
        ProcessRunner.run(command, 30);

        if (!Files.exists(outputPath)) {
            throw new IllegalStateException("ffmpeg did not produce video at " + outputPath);
        }

        return outputPath;
    }

    private String buildCropFilter(int width, int height) {
        return "scale=" + width + ":" + height + ":force_original_aspect_ratio=increase,crop=" + width + ":" + height;
    }

    private String buildBlurFillFilter(int width, int height, String subtitleFilter) {
        int blur = properties.getVideo().getBlurStrength();
        String compose = "[0:v]split=2[bg][fg];"
                + "[bg]scale=" + width + ":" + height + ":force_original_aspect_ratio=increase,crop="
                + width + ":" + height + ",boxblur=" + blur + ":1[blurred];"
                + "[fg]scale=" + width + ":" + height + ":force_original_aspect_ratio=decrease[sharp];"
                + "[blurred][sharp]overlay=(W-w)/2:(H-h)/2";

        if (subtitleFilter.isEmpty()) {
            return compose + "[vout]";
        }

        return compose + subtitleFilter + "[vout]";
    }

    private String buildSubtitleFilter(Path captionPath) {
        if (captionPath == null || !Files.exists(captionPath)) {
            return "";
        }

        String subtitlePath = escapeFilterPath(toFilterRelativePath(captionPath).toString());
        AutoShortsProperties.Video video = properties.getVideo();
        String forceStyle = escapeFilterOption(
                "FontName=Arial,FontSize=" + video.getCaptionFontSize() + ",PrimaryColour=&HFFFFFF&,"
                        + "OutlineColour=&H000000&,BackColour=&H00000000&,BorderStyle="
                        + video.getCaptionBorderStyle() + ",Outline=" + video.getCaptionOutline()
                        + ",Shadow=0,Alignment=2,MarginV=" + video.getCaptionMarginV());

        return ",subtitles=" + subtitlePath + ":force_style=" + forceStyle;
    }

    private Path toFilterRelativePath(Path captionPath) {
        Path absolute = captionPath.toAbsolutePath().normalize();
        Path workingDir = Path.of("").toAbsolutePath().normalize();
        if (absolute.startsWith(workingDir)) {
            return workingDir.relativize(absolute);
        }
        return absolute;
    }

    private String escapeFilterPath(String path) {
        return path.replace("\\", "\\\\")
                .replace(":", "\\:")
                .replace("'", "\\'")
                .replace(",", "\\,");
    }

    private String escapeFilterOption(String value) {
        return value.replace("\\", "\\\\")
                .replace(":", "\\:")
                .replace("'", "\\'")
                .replace(",", "\\,")
                .replace("&", "\\&");
    }
}
