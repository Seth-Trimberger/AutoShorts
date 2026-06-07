package com.autoshorts.scraper.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class ProcessRunner {

    private ProcessRunner() {
    }

    public static void run(List<String> command, long timeoutMinutes) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);

        Process process = builder.start();

        // Drain stdout on a background thread so the process buffer never fills and deadlocks.
        // waitFor() below enforces the actual timeout.
        StringBuilder output = new StringBuilder();
        Thread reader = new Thread(() -> {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            } catch (IOException ignored) {}
        });
        reader.setDaemon(true);
        reader.start();

        if (!process.waitFor(timeoutMinutes, TimeUnit.MINUTES)) {
            process.destroyForcibly();
            throw new IOException("Command timed out after " + timeoutMinutes + " minutes: " + String.join(" ", command));
        }

        reader.join(5_000);

        if (process.exitValue() != 0) {
            throw new IOException("Command failed (exit " + process.exitValue() + "): " + String.join(" ", command)
                    + "\n" + output);
        }
    }
}
