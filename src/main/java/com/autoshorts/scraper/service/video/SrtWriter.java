package com.autoshorts.scraper.service.video;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class SrtWriter {

    private SrtWriter() {
    }

    public static void write(Path outputSrt, List<CaptionCue> cues) throws IOException {
        StringBuilder builder = new StringBuilder();
        int index = 1;
        for (CaptionCue cue : cues) {
            builder.append(index++).append('\n');
            builder.append(formatTimestamp(cue.start())).append(" --> ").append(formatTimestamp(cue.end())).append('\n');
            builder.append(cue.text()).append("\n\n");
        }
        Files.writeString(outputSrt, cues.isEmpty() ? "" : builder.toString());
    }

    private static String formatTimestamp(double seconds) {
        int totalMillis = Math.max(0, (int) Math.round(seconds * 1000));
        int hours = totalMillis / 3_600_000;
        int minutes = (totalMillis % 3_600_000) / 60_000;
        int secs = (totalMillis % 60_000) / 1000;
        int millis = totalMillis % 1000;
        return String.format(Locale.US, "%02d:%02d:%02d,%03d", hours, minutes, secs, millis);
    }
}
