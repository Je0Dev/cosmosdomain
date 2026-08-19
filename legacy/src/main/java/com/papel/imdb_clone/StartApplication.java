package com.papel.imdb_clone;

import com.papel.imdb_clone.gui.MovieAppGui;
import javafx.application.Application;

/**
 * Main application entry point for the IMDB Clone application.
 */
public class StartApplication {
    private StartApplication() {
        // Private constructor to prevent instantiation
    }

    /**
     * Launches the ImprovedMovieApp application.
     * @param args the command line arguments-
     *             args[0] is the path to the data file...
     */
    public static void main(String[] args) {
        Application.launch(MovieAppGui.class, args);
    }
}