package com.cosmosdomain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SubtitleServiceTest {

    private final SubtitleService subtitleService = new SubtitleService();

    @TempDir
    Path tempDir;

    @Test
    void testIsSrt_True() {
        assertTrue(subtitleService.isSrt("subtitle.srt"));
        assertTrue(subtitleService.isSrt("SUBTITLE.SRT"));
    }

    @Test
    void testIsSrt_False() {
        assertFalse(subtitleService.isSrt("subtitle.vtt"));
        assertFalse(subtitleService.isSrt("subtitle.txt"));
        assertFalse(subtitleService.isSrt(null));
    }

    @Test
    void testConvertSrtToVtt_BasicConversion() throws IOException {
        Path srtFile = tempDir.resolve("test.srt");
        Path vttFile = tempDir.resolve("test.vtt");

        writeSampleSrt(srtFile);

        assertTrue(subtitleService.convertSrtToVtt(srtFile, vttFile));
        String content = Files.readString(vttFile);

        assertTrue(content.startsWith("WEBVTT"));
        assertTrue(content.contains("00:00:01.000 --> 00:00:04.000"));
        assertTrue(content.contains("Hello, world!"));
        assertTrue(content.contains("00:00:05.500 --> 00:00:08.500"));
        assertTrue(content.contains("Second subtitle"));
    }

    @Test
    void testConvertSrtToVtt_CorrectTimestampFormat() throws IOException {
        Path srtFile = tempDir.resolve("test2.srt");
        Path vttFile = tempDir.resolve("test2.vtt");

        try (PrintWriter writer = new PrintWriter(srtFile.toFile())) {
            writer.println("1");
            writer.println("00:01:30,500 --> 00:01:33,750");
            writer.println("Timestamp with comma");
        }

        assertTrue(subtitleService.convertSrtToVtt(srtFile, vttFile));
        String content = Files.readString(vttFile);

        assertTrue(content.contains("00:01:30.500 --> 00:01:33.750"));
    }

    @Test
    void testConvertSrtToVtt_InvalidFile_ReturnsFalse() throws IOException {
        Path srtFile = tempDir.resolve("nonexistent.srt");
        Path vttFile = tempDir.resolve("output.vtt");

        assertFalse(subtitleService.convertSrtToVtt(srtFile, vttFile));
    }

    private void writeSampleSrt(Path file) throws IOException {
        try (PrintWriter writer = new PrintWriter(file.toFile())) {
            writer.println("1");
            writer.println("00:00:01,000 --> 00:00:04,000");
            writer.println("Hello, world!");
            writer.println();
            writer.println("2");
            writer.println("00:00:05,500 --> 00:00:08,500");
            writer.println("Second subtitle");
        }
    }
}