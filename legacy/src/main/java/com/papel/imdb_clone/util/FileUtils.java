package com.papel.imdb_clone.util;

import com.papel.imdb_clone.model.content.Series;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Utility class for file operations.
 */
public class FileUtils {

    private static final Logger logger = Logger.getLogger(FileUtils.class.getName());

    /**
     * Reads all lines from a file, skipping comments and empty lines.
     *
     * @param filePath Path to the file to read
     * @return List of non-comment, non-empty lines from the file
     */
    public static List<String> readLines(String filePath) {
        try {
            // Read all lines from the file, skipping comments and empty lines
            return Files.lines(Paths.get(filePath))
                    .filter(line -> !line.trim().isEmpty() && !line.trim().startsWith("#"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading file: " + filePath, e);
            return List.of();
        }
    }

}
