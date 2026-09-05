package com.autoshorts.scraper.service.video;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CaptionPostProcessor {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Pattern SENTENCE_PATTERN = Pattern.compile("[^.!?]+[.!?]+|[^.!?]+$");

    public void writeSentenceSrt(Path whisperJson, Path outputSrt) throws IOException {
        JsonNode root = MAPPER.readTree(whisperJson.toFile());
        List<CaptionCue> cues = buildSentenceCues(root);
        SrtWriter.write(outputSrt, cues);
    }

    private List<CaptionCue> buildSentenceCues(JsonNode root) {
        List<CaptionCue> cues = new ArrayList<>();

        for (JsonNode segment : root.path("segments")) {
            String text = segment.path("text").asText("").replaceAll("\\s+", " ").strip();
            if (text.isEmpty()) {
                continue;
            }

            double segStart = segment.path("start").asDouble();
            double segEnd = segment.path("end").asDouble();
            if (segEnd <= segStart) {
                segEnd = segStart + 0.5;
            }

            List<String> sentences = splitIntoSentences(text);
            if (sentences.size() == 1) {
                cues.add(new CaptionCue(sentences.get(0), segStart, segEnd));
                continue;
            }

            double duration = segEnd - segStart;
            int totalChars = sentences.stream().mapToInt(String::length).sum();
            double cursor = segStart;

            for (int i = 0; i < sentences.size(); i++) {
                String sentence = sentences.get(i);
                double share = totalChars == 0 ? 1.0 / sentences.size() : sentence.length() / (double) totalChars;
                double sentenceEnd = (i == sentences.size() - 1) ? segEnd : cursor + (duration * share);
                cues.add(new CaptionCue(sentence, cursor, sentenceEnd));
                cursor = sentenceEnd;
            }
        }

        return cues;
    }

    private List<String> splitIntoSentences(String text) {
        List<String> sentences = new ArrayList<>();
        Matcher matcher = SENTENCE_PATTERN.matcher(text);
        while (matcher.find()) {
            String sentence = matcher.group().strip();
            if (!sentence.isEmpty()) {
                sentences.add(sentence);
            }
        }
        return sentences.isEmpty() ? List.of(text) : sentences;
    }
}
