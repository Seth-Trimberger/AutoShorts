package com.autoshorts.scraper.service.video;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.autoshorts.scraper.config.AutoShortsProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class ScriptCaptionAligner {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int INITIAL_PROMPT_MAX_CHARS = 224;

    private final AutoShortsProperties properties;
    private final CaptionPolishStrategy captionPolishStrategy;

    public ScriptCaptionAligner(AutoShortsProperties properties, CaptionPolishStrategy captionPolishStrategy) {
        this.properties = properties;
        this.captionPolishStrategy = captionPolishStrategy;
    }

    public List<CaptionCue> buildCues(Path whisperJson, String narrationScript) throws IOException {
        JsonNode root = MAPPER.readTree(whisperJson.toFile());
        List<TimedWord> whisperWords = extractWhisperWords(root);
        List<String> scriptWords = tokenizeWords(narrationScript);

        if (scriptWords.isEmpty()) {
            return List.of();
        }

        List<TextChunk> chunks = buildScriptChunks(scriptWords);
        List<CaptionCue> cues = alignChunks(chunks, whisperWords, root.path("segments"));
        return captionPolishStrategy.polish(cues, narrationScript);
    }

    public String initialPrompt(String narrationScript) {
        if (narrationScript == null || narrationScript.isBlank()) {
            return "";
        }
        String normalized = narrationScript.replaceAll("\\s+", " ").strip();
        if (normalized.length() <= INITIAL_PROMPT_MAX_CHARS) {
            return normalized;
        }
        return normalized.substring(0, INITIAL_PROMPT_MAX_CHARS);
    }

    private List<TimedWord> extractWhisperWords(JsonNode root) {
        List<TimedWord> words = new ArrayList<>();
        for (JsonNode segment : root.path("segments")) {
            JsonNode segmentWords = segment.path("words");
            if (segmentWords.isArray() && !segmentWords.isEmpty()) {
                for (JsonNode wordNode : segmentWords) {
                    addWhisperWord(words, wordNode);
                }
            }
        }
        return words;
    }

    private void addWhisperWord(List<TimedWord> words, JsonNode wordNode) {
        String token = wordNode.path("word").asText("").strip();
        if (token.isEmpty()) {
            return;
        }
        double start = wordNode.path("start").asDouble();
        double end = wordNode.path("end").asDouble();
        if (end <= start) {
            end = start + 0.05;
        }
        words.add(new TimedWord(token, start, end));
    }

    private List<String> tokenizeWords(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return List.of(text.trim().split("\\s+"));
    }

    private List<TextChunk> buildScriptChunks(List<String> scriptWords) {
        int maxWords = properties.getCaptions().getMaxWordsPerChunk();
        int maxChars = properties.getCaptions().getMaxCharsPerLine();

        List<TextChunk> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int startIndex = 0;
        int wordCount = 0;

        for (int i = 0; i < scriptWords.size(); i++) {
            String word = scriptWords.get(i);
            if (current.isEmpty()) {
                startIndex = i;
            } else {
                current.append(' ');
            }
            current.append(word);
            wordCount++;

            boolean sentenceEnd = endsSentence(word);
            boolean tooManyWords = wordCount >= maxWords;
            boolean tooLong = current.length() > maxChars;
            boolean commaBreak = word.endsWith(",") && wordCount >= 3;
            boolean lastWord = i == scriptWords.size() - 1;

            if (sentenceEnd || tooManyWords || tooLong || commaBreak || lastWord) {
                chunks.add(new TextChunk(current.toString(), startIndex, i));
                current.setLength(0);
                wordCount = 0;
            }
        }

        return chunks;
    }

    private boolean endsSentence(String word) {
        return word.endsWith(".") || word.endsWith("!") || word.endsWith("?");
    }

    private List<CaptionCue> alignChunks(List<TextChunk> chunks, List<TimedWord> whisperWords, JsonNode segments) {
        List<CaptionCue> cues = new ArrayList<>();
        if (chunks.isEmpty()) {
            return cues;
        }

        double segmentStart = segments.isArray() && !segments.isEmpty()
                ? segments.get(0).path("start").asDouble(0)
                : 0;
        double segmentEnd = segments.isArray() && !segments.isEmpty()
                ? segments.get(segments.size() - 1).path("end").asDouble(segmentStart + 1)
                : segmentStart + 1;

        if (whisperWords.isEmpty()) {
            distributeProportionally(cues, chunks, segmentStart, segmentEnd);
            return cues;
        }

        int whisperIndex = 0;
        for (TextChunk chunk : chunks) {
            int scriptWordCount = chunk.endWordIndex() - chunk.startWordIndex() + 1;
            int whisperStartIndex = whisperIndex;
            int whisperEndIndex = Math.min(whisperIndex + scriptWordCount - 1, whisperWords.size() - 1);

            if (whisperStartIndex >= whisperWords.size()) {
                double duration = Math.max(segmentEnd - segmentStart, 0.5);
                double perChunk = duration / chunks.size();
                double start = segmentStart + (cues.size() * perChunk);
                double end = Math.min(segmentEnd, start + perChunk);
                cues.add(new CaptionCue(chunk.text(), start, end));
                continue;
            }

            double start = whisperWords.get(whisperStartIndex).start();
            double end = whisperWords.get(whisperEndIndex).end();
            cues.add(new CaptionCue(chunk.text(), start, end));
            whisperIndex = whisperEndIndex + 1;
        }

        return cues;
    }

    private void distributeProportionally(List<CaptionCue> cues, List<TextChunk> chunks, double start, double end) {
        double duration = Math.max(end - start, 0.5);
        int totalChars = chunks.stream().mapToInt(chunk -> chunk.text().length()).sum();
        double cursor = start;

        for (int i = 0; i < chunks.size(); i++) {
            TextChunk chunk = chunks.get(i);
            double share = totalChars == 0 ? 1.0 / chunks.size() : chunk.text().length() / (double) totalChars;
            double chunkEnd = (i == chunks.size() - 1) ? end : cursor + (duration * share);
            cues.add(new CaptionCue(chunk.text(), cursor, chunkEnd));
            cursor = chunkEnd;
        }
    }

    private record TimedWord(String word, double start, double end) {
    }

    private record TextChunk(String text, int startWordIndex, int endWordIndex) {
    }
}
