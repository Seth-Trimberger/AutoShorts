package com.autoshorts.scraper.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@ConfigurationProperties(prefix = "autoshorts")
public class AutoShortsProperties {

    private final Assets assets = new Assets();
    private final Tts tts = new Tts();
    private final Ffmpeg ffmpeg = new Ffmpeg();
    private final Whisper whisper = new Whisper();

    @PostConstruct
    public void ensureDirectoriesExist() throws IOException {
        Files.createDirectories(Paths.get(assets.getBackgroundsDir()));
        Files.createDirectories(Paths.get(assets.getAudioDir()));
        Files.createDirectories(Paths.get(assets.getOutputDir()));
        Files.createDirectories(Paths.get(assets.getCaptionsDir()));
    }

    public Assets getAssets() {
        return assets;
    }

    public Tts getTts() {
        return tts;
    }

    public Ffmpeg getFfmpeg() {
        return ffmpeg;
    }

    public Whisper getWhisper() {
        return whisper;
    }

    public static class Assets {
        private String backgroundsDir = "assets/backgrounds";
        private String outputDir = "output/videos";
        private String audioDir = "output/audio";
        private String captionsDir = "output/captions";

        public String getBackgroundsDir() {
            return backgroundsDir;
        }

        public void setBackgroundsDir(String backgroundsDir) {
            this.backgroundsDir = backgroundsDir;
        }

        public String getOutputDir() {
            return outputDir;
        }

        public void setOutputDir(String outputDir) {
            this.outputDir = outputDir;
        }

        public String getAudioDir() {
            return audioDir;
        }

        public void setAudioDir(String audioDir) {
            this.audioDir = audioDir;
        }

        public String getCaptionsDir() {
            return captionsDir;
        }

        public void setCaptionsDir(String captionsDir) {
            this.captionsDir = captionsDir;
        }

        public Path audioPath(String redditId) {
            return Paths.get(audioDir, redditId + ".mp3");
        }

        public Path videoPath(String redditId) {
            return Paths.get(outputDir, redditId + ".mp4");
        }

        public Path captionPath(String redditId) {
            return Paths.get(captionsDir, redditId + ".srt");
        }
    }

    public static class Tts {
        private String voice = "en-US-JennyNeural";
        private String provider = "edge-tts";
        private String edgeTtsCommand = "edge-tts";

        public String getVoice() {
            return voice;
        }

        public void setVoice(String voice) {
            this.voice = voice;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getEdgeTtsCommand() {
            return edgeTtsCommand;
        }

        public void setEdgeTtsCommand(String edgeTtsCommand) {
            this.edgeTtsCommand = edgeTtsCommand;
        }
    }

    public static class Ffmpeg {
        private String path = "ffmpeg";

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    public static class Whisper {
        private String command = "whisper";
        private String model = "base";

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }
    }
}
