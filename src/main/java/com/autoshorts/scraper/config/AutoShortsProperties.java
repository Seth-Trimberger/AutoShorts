package com.autoshorts.scraper.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "autoshorts")
public class AutoShortsProperties {

    private final Assets assets = new Assets();
    private final Tts tts = new Tts();
    private final Ffmpeg ffmpeg = new Ffmpeg();
    private final Whisper whisper = new Whisper();
    private final Captions captions = new Captions();
    private final Video video = new Video();
    private final Cli cli = new Cli();

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

    public Captions getCaptions() {
        return captions;
    }

    public Video getVideo() {
        return video;
    }

    public Cli getCli() {
        return cli;
    }

    public static class Cli {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
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
        private String voice = "en-GB-SoniaNeural";
        private String provider = "edge-tts";
        private String edgeTtsCommand = "edge-tts";
        private String voiceMode = "random";
        private List<String> voices = new ArrayList<>(List.of(
                "en-AU-NatashaNeural",
                "en-GB-RyanNeural",
                "en-GB-SoniaNeural",
                "en-IE-EmilyNeural"
        ));

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

        public String getVoiceMode() {
            return voiceMode;
        }

        public void setVoiceMode(String voiceMode) {
            this.voiceMode = voiceMode;
        }

        public List<String> getVoices() {
            return voices;
        }

        public void setVoices(List<String> voices) {
            this.voices = voices;
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

    public static class Captions {
        private CaptionMode mode = CaptionMode.SCRIPT_ALIGNED;
        private int maxWordsPerChunk = 6;
        private int maxWordsPerLine = 8;
        private int maxCharsPerLine = 42;
        private String polish = "none";

        public CaptionMode getMode() {
            return mode;
        }

        public void setMode(CaptionMode mode) {
            this.mode = mode;
        }

        public int getMaxWordsPerChunk() {
            return maxWordsPerChunk;
        }

        public void setMaxWordsPerChunk(int maxWordsPerChunk) {
            this.maxWordsPerChunk = maxWordsPerChunk;
        }

        public int getMaxWordsPerLine() {
            return maxWordsPerLine;
        }

        public void setMaxWordsPerLine(int maxWordsPerLine) {
            this.maxWordsPerLine = maxWordsPerLine;
        }

        public int getMaxCharsPerLine() {
            return maxCharsPerLine;
        }

        public void setMaxCharsPerLine(int maxCharsPerLine) {
            this.maxCharsPerLine = maxCharsPerLine;
        }

        public String getPolish() {
            return polish;
        }

        public void setPolish(String polish) {
            this.polish = polish;
        }
    }

    public static class Video {
        private int width = 1080;
        private int height = 1920;
        private BackgroundMode backgroundMode = BackgroundMode.BLUR_FILL;
        private int blurStrength = 20;
        private int captionMarginV = 120;
        private int captionBorderStyle = 1;
        private int captionOutline = 3;
        private int captionFontSize = 28;

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public BackgroundMode getBackgroundMode() {
            return backgroundMode;
        }

        public void setBackgroundMode(BackgroundMode backgroundMode) {
            this.backgroundMode = backgroundMode;
        }

        public int getBlurStrength() {
            return blurStrength;
        }

        public void setBlurStrength(int blurStrength) {
            this.blurStrength = blurStrength;
        }

        public int getCaptionMarginV() {
            return captionMarginV;
        }

        public void setCaptionMarginV(int captionMarginV) {
            this.captionMarginV = captionMarginV;
        }

        public int getCaptionBorderStyle() {
            return captionBorderStyle;
        }

        public void setCaptionBorderStyle(int captionBorderStyle) {
            this.captionBorderStyle = captionBorderStyle;
        }

        public int getCaptionOutline() {
            return captionOutline;
        }

        public void setCaptionOutline(int captionOutline) {
            this.captionOutline = captionOutline;
        }

        public int getCaptionFontSize() {
            return captionFontSize;
        }

        public void setCaptionFontSize(int captionFontSize) {
            this.captionFontSize = captionFontSize;
        }
    }
}
