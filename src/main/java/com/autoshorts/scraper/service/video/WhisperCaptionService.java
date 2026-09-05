package com.autoshorts.scraper.service.video;

import com.autoshorts.scraper.config.AutoShortsProperties;
import com.autoshorts.scraper.config.CaptionMode;
import com.autoshorts.scraper.util.ProcessRunner;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class WhisperCaptionService implements CaptionService {

    private final AutoShortsProperties properties;
    private final CaptionPostProcessor captionPostProcessor;
    private final ScriptCaptionAligner scriptCaptionAligner;

    public WhisperCaptionService(AutoShortsProperties properties,
                                 CaptionPostProcessor captionPostProcessor,
                                 ScriptCaptionAligner scriptCaptionAligner) {
        this.properties = properties;
        this.captionPostProcessor = captionPostProcessor;
        this.scriptCaptionAligner = scriptCaptionAligner;
    }

    @Override
    public Path generateCaptions(String redditId, Path audioPath, String narrationScript) throws Exception {
        Path captionsDir = Path.of(properties.getAssets().getCaptionsDir());
        Files.createDirectories(captionsDir);

        Path expectedSrt = properties.getAssets().captionPath(redditId);
        CaptionMode mode = properties.getCaptions().getMode();

        if (mode == CaptionMode.SCRIPT_ALIGNED) {
            return generateScriptAlignedCaptions(audioPath, captionsDir, expectedSrt, narrationScript);
        }
        if (mode == CaptionMode.SENTENCE) {
            return generateSentenceCaptions(audioPath, captionsDir, expectedSrt);
        }

        return generateBlockCaptions(audioPath, captionsDir, expectedSrt);
    }

    private Path generateBlockCaptions(Path audioPath, Path captionsDir, Path expectedSrt) throws Exception {
        List<String> command = List.of(
                properties.getWhisper().getCommand(),
                audioPath.toString(),
                "--model", properties.getWhisper().getModel(),
                "--output_format", "srt",
                "--output_dir", captionsDir.toString()
        );

        System.out.println("CAPTIONS: Generating block subtitles with whisper...");
        ProcessRunner.run(command, 60);

        return finalizeSrtOutput(audioPath, captionsDir, expectedSrt);
    }

    private Path generateSentenceCaptions(Path audioPath, Path captionsDir, Path expectedSrt) throws Exception {
        runWhisperJson(audioPath, captionsDir, null);

        Path whisperJson = jsonPath(captionsDir, audioPath);
        captionPostProcessor.writeSentenceSrt(whisperJson, expectedSrt);
        return expectedSrt;
    }

    private Path generateScriptAlignedCaptions(Path audioPath,
                                               Path captionsDir,
                                               Path expectedSrt,
                                               String narrationScript) throws Exception {
        String initialPrompt = scriptCaptionAligner.initialPrompt(narrationScript);
        runWhisperJson(audioPath, captionsDir, initialPrompt);

        Path whisperJson = jsonPath(captionsDir, audioPath);
        List<CaptionCue> cues = scriptCaptionAligner.buildCues(whisperJson, narrationScript);
        SrtWriter.write(expectedSrt, cues);

        System.out.println("CAPTIONS: Generated " + cues.size() + " script-aligned subtitle cues.");
        return expectedSrt;
    }

    private void runWhisperJson(Path audioPath, Path captionsDir, String initialPrompt) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(properties.getWhisper().getCommand());
        command.add(audioPath.toString());
        command.add("--model");
        command.add(properties.getWhisper().getModel());
        command.add("--word_timestamps");
        command.add("True");
        command.add("--output_format");
        command.add("json");
        command.add("--output_dir");
        command.add(captionsDir.toString());

        if (initialPrompt != null && !initialPrompt.isBlank()) {
            command.add("--initial_prompt");
            command.add(initialPrompt);
        }

        System.out.println("CAPTIONS: Generating word timestamps with whisper...");
        ProcessRunner.run(command, 60);

        Path whisperJson = jsonPath(captionsDir, audioPath);
        if (!Files.exists(whisperJson)) {
            throw new IllegalStateException("whisper did not produce JSON captions at " + whisperJson);
        }
    }

    private Path jsonPath(Path captionsDir, Path audioPath) {
        return captionsDir.resolve(stripExtension(audioPath.getFileName().toString()) + ".json");
    }

    private Path finalizeSrtOutput(Path audioPath, Path captionsDir, Path expectedSrt) throws Exception {
        Path whisperOutput = captionsDir.resolve(stripExtension(audioPath.getFileName().toString()) + ".srt");
        if (!Files.exists(whisperOutput)) {
            throw new IllegalStateException("whisper did not produce captions at " + whisperOutput);
        }

        if (!whisperOutput.equals(expectedSrt)) {
            Files.move(whisperOutput, expectedSrt, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        return expectedSrt;
    }

    private String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }
}
