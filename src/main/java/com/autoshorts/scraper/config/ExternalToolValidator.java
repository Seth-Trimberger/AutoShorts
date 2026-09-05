package com.autoshorts.scraper.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Component
public class ExternalToolValidator {

    private final AutoShortsProperties properties;

    public ExternalToolValidator(AutoShortsProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void validateTools() {
        checkTool("ffmpeg", properties.getFfmpeg().getPath(), List.of("-version"));
        checkFfmpegSubtitleSupport();
        checkTool("edge-tts", properties.getTts().getEdgeTtsCommand(), List.of("--version"));
        checkTool("whisper", properties.getWhisper().getCommand(), List.of("--help"));
    }

    private void checkFfmpegSubtitleSupport() {
        List<String> command = List.of(properties.getFfmpeg().getPath(), "-hide_banner", "-filters");
        try {
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Thread reader = new Thread(() -> {
                try {
                    process.getInputStream().transferTo(output);
                } catch (Exception ignored) {
                }
            }, "ffmpeg-filter-probe");
            reader.setDaemon(true);
            reader.start();

            if (!process.waitFor(15, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                reader.join(5_000);
                warnMissing("ffmpeg subtitles filter", properties.getFfmpeg().getPath(), "timed out");
                return;
            }

            reader.join(5_000);
            String filterList = output.toString(StandardCharsets.UTF_8);
            if (process.exitValue() != 0 || !filterList.contains(" subtitles ")) {
                warnMissing("ffmpeg subtitles filter", properties.getFfmpeg().getPath(),
                        "FFmpeg must be built with libass; install ffmpeg-full or configure FFMPEG_PATH");
            }
        } catch (Exception e) {
            warnMissing("ffmpeg subtitles filter", properties.getFfmpeg().getPath(), e.getMessage());
        }
    }

    private void checkTool(String name, String command, List<String> probeArgs) {
        List<String> cmd = new ArrayList<>();
        cmd.add(command);
        cmd.addAll(probeArgs);

        try {
            Process process = new ProcessBuilder(cmd)
                    .redirectErrorStream(true)
                    .start();

            if (!process.waitFor(15, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                warnMissing(name, command, "timed out");
                return;
            }

            if (process.exitValue() != 0) {
                warnMissing(name, command, "exit code " + process.exitValue());
            }
        } catch (Exception e) {
            warnMissing(name, command, e.getMessage());
        }
    }

    private void warnMissing(String name, String command, String reason) {
        System.err.println("WARNING: External tool '" + name + "' not available at '" + command
                + "' (" + reason + "). Video rendering will fail until this is fixed.");
        System.err.println("         Set the appropriate env var (FFMPEG_PATH, EDGE_TTS_COMMAND, WHISPER_COMMAND)"
                + " or install the tool. See README.md.");
    }
}
