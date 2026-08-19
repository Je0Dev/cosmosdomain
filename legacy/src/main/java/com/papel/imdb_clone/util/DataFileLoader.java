package com.papel.imdb_clone.util;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for loading data files from various locations.
 * This class cannot be instantiated which means that the constructor is private.
 */
public class DataFileLoader {

    //Logger
    private static final Logger logger = LoggerFactory.getLogger(DataFileLoader.class);
    
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private DataFileLoader() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    private static final Map<String, String[]> FILE_TYPE_PATHS = Map.of(
        // Content files (movies, series)
        "content", new String[]{
            "src/main/resources/data/content/",
            "target/classes/data/content/",
            "data/content/",
            "../data/content/"
        },
        // People files (users, actors, directors)
        "people", new String[]{
            "src/main/resources/data/people/",
            "target/classes/data/people/",
            "data/people/",
            "../data/people/"
        },
        // Nominations files
        "nominations", new String[]{
            "src/main/resources/data/nominations/",
            "target/classes/data/nominations/",
            "data/nominations/",
            "../data/nominations/"
        },
        // Default fallback paths
        "default", new String[]{
            "src/main/resources/data/",
            "target/classes/data/",
            "data/",
            "../data/"
        }
    );

    public static InputStream getResourceAsStream(String filename) throws IOException {
        Objects.requireNonNull(filename, "Filename cannot be null");
        
        // Determine the file type based on the filename
        String fileType = determineFileType(filename);
        String[] searchPaths = FILE_TYPE_PATHS.getOrDefault(fileType, FILE_TYPE_PATHS.get("default"));
        
        // First try classpath resources
        for (String path : getClasspathPaths(fileType)) {
            String resourcePath = path + filename;
            InputStream is = DataFileLoader.class.getClassLoader().getResourceAsStream(resourcePath);
            if (is != null) {
                logger.debug("Loaded {} from classpath: {}", filename, resourcePath);
                return is;
            }
        }
        
        // Then try file system locations
        for (String path : searchPaths) {
            File file = new File(path + filename);
            if (file.exists() && !file.isDirectory()) {
                logger.debug("Loaded {} from filesystem: {}", filename, file.getAbsolutePath());
                return new FileInputStream(file);
            }
        }
        
        // Try with just the filename in the root of each path
        for (String path : searchPaths) {
            File file = new File(path + "/" + filename);
            if (file.exists() && !file.isDirectory()) {
                logger.debug("Loaded {} from filesystem: {}", filename, file.getAbsolutePath());
                return new FileInputStream(file);
            }
        }

        // Try absolute path as last resort
        File file = new File(filename);
        if (file.exists() && !file.isDirectory()) {
            logger.debug("Loaded {} using absolute path: {}", filename, file.getAbsolutePath());
            return new FileInputStream(file);
        }

        throw new FileNotFoundException("Could not find data file: " + filename + " in any of the searched locations");
    }

    // Determine the file type based on the filename
    private static String determineFileType(String filename) {
        if (filename.contains("user") || filename.contains("actor") || filename.contains("director")) {
            return "people";
        } else if (filename.contains("movie") || filename.contains("series") || filename.contains("content")) {
            return "content";
        } else if (filename.contains("nomination") || filename.contains("award")) {
            return "nominations";
        }
        return "default";
    }

    // Get the classpath paths for the given file type
    private static List<String> getClasspathPaths(String fileType) {
        List<String> paths = new ArrayList<>();
        
        // Add type-specific paths
        if (FILE_TYPE_PATHS.containsKey(fileType)) {
            for (String path : FILE_TYPE_PATHS.get(fileType)) {
                // Convert filesystem path to classpath-relative path
                if (path.startsWith("src/main/resources/")) {
                    paths.add(path.substring("src/main/resources/".length()));
                } else if (path.startsWith("target/classes/")) {
                    paths.add(path.substring("target/classes/".length()));
                }
            }
        }
        
        // Always include default paths as fallback
        for (String path : FILE_TYPE_PATHS.get("default")) {
            if (path.startsWith("src/main/resources/")) {
                paths.add(path.substring("src/main/resources/".length()));
            } else if (path.startsWith("target/classes/")) {
                paths.add(path.substring("target/classes/".length()));
            }
        }
        // Always include default paths as fallback
        paths.addAll(List.of(FILE_TYPE_PATHS.get("default")));
        return paths;
    }

    public static String getResourcePath(String filename) throws IOException {
        String fileType = determineFileType(filename);
        //
        String[] searchPaths = FILE_TYPE_PATHS.getOrDefault(fileType, FILE_TYPE_PATHS.get("default"));
        
        // Try classpath resources first
        for (String path : getClasspathPaths(fileType)) {
            String resourcePath = path + filename;
            URL url = DataFileLoader.class.getClassLoader().getResource(resourcePath);
            if (url != null) {
                return url.getPath();
            }
        }
        
        // Then try file system locations
        for (String path : searchPaths) {
            File file = new File(path + filename);
            if (file.exists() && !file.isDirectory()) {
                return file.getAbsolutePath();
            }
        }
        
        // Try with just the filename in the root of each path
        for (String path : searchPaths) {
            File file = new File(path + "/" + filename);
            if (file.exists() && !file.isDirectory()) {
                return file.getAbsolutePath();
            }
        }

        // Try absolute path as last resort
        File file = new File(filename);
        if (file.exists() && !file.isDirectory()) {
            return file.getAbsolutePath();
        }
        throw new FileNotFoundException("Could not find data file: " + filename);
    }
}
