package com.cosmosdomain.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SubtitleService {

    private static final Pattern SRT_TIME_PATTERN =
        Pattern.compile("(\\d{2}):(\\d{2}):(\\d{2})[,.](\\d{3})");

    public boolean isSrt(String filename) {
        return filename != null && filename.toLowerCase().endsWith(".srt");
    }

    public boolean convertSrtToVtt(Path srtPath, Path vttPath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(srtPath, StandardCharsets.UTF_8);
             BufferedWriter writer = Files.newBufferedWriter(vttPath, StandardCharsets.UTF_8)) {

            writer.write("WEBVTT\n\n");

            String line;
            boolean inTimestampBlock = false;
            int index = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty()) continue;

                if (line.matches("\\d+")) {
                    index = Integer.parseInt(line);
                    inTimestampBlock = false;
                    continue;
                }

                Matcher matcher = SRT_TIME_PATTERN.matcher(line);
                if (matcher.find()) {
                    writer.write(convertTimestamp(matcher.group()) + " --> ");
                    String secondPart = line.substring(matcher.end()).trim();
                    Matcher matcher2 = SRT_TIME_PATTERN.matcher(secondPart);
                    if (matcher2.find()) {
                        writer.write(convertTimestamp(matcher2.group()));
                    } else {
                        writer.write(secondPart);
                    }
                    writer.newLine();
                    inTimestampBlock = true;
                } else if (inTimestampBlock && !line.isEmpty()) {
                    writer.write(line + "\n\n");
                    inTimestampBlock = false;
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private String convertTimestamp(String srtTime) {
        Matcher m = SRT_TIME_PATTERN.matcher(srtTime);
        if (m.matches()) {
            return String.format(
                "%s:%s:%s.%s", m.group(1), m.group(2), m.group(3), m.group(4)
            );
        }
        return srtTime;
    }
}