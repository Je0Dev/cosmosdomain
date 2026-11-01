package com.cosmosdomain.controller;

import com.cosmosdomain.dto.TmdbMovie;
import com.cosmosdomain.dto.TmdbPersonResult;
import com.cosmosdomain.dto.TmdbTV;
import com.cosmosdomain.service.TmdbClient;
import com.cosmosdomain.config.TmdbProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Handles the multi-type search page.
 *
 * <p>Delegates to TMDB for movies, TV series, and people. When the caller does
 * not choose a type, all three searches run and their results are combined in
 * the model under separate attributes.</p>
 */
@Controller
public class SearchController {

    private final TmdbClient tmdbClient;
    private final TmdbProperties props;

    public SearchController(TmdbClient tmdbClient, TmdbProperties props) {
        this.tmdbClient = tmdbClient;
        this.props = props;
    }

    // Runs one or all TMDB searches depending on the selected type. A blank
    // query simply renders the empty search form.
    @GetMapping("/search")
    public String search(@RequestParam(defaultValue = "") String query,
                         @RequestParam(defaultValue = "all") String type,
                         Model model) {
        if (query.isBlank()) {
            model.addAttribute("query", "");
            model.addAttribute("type", type);
            return "search";
        }

        // Each branch adds only the result lists relevant to the requested type;
        // the template renders whichever attributes are present.
        switch (type) {
            case "movie" -> {
                List<TmdbMovie> movies = tmdbClient.searchMovies(query);
                model.addAttribute("movies", movies);
            }
            case "tv" -> {
                List<TmdbTV> series = tmdbClient.searchTV(query);
                model.addAttribute("series", series);
            }
            case "person" -> {
                List<TmdbPersonResult> people = tmdbClient.searchPeople(query);
                model.addAttribute("people", people);
            }
            default -> {
                List<TmdbMovie> movies = tmdbClient.searchMovies(query);
                List<TmdbTV> series = tmdbClient.searchTV(query);
                List<TmdbPersonResult> people = tmdbClient.searchPeople(query);
                model.addAttribute("movies", movies);
                model.addAttribute("series", series);
                model.addAttribute("people", people);
            }
        }

        model.addAttribute("imageBaseUrl", props.getImageBaseUrl());
        model.addAttribute("posterSize", props.getPosterSize());
        model.addAttribute("query", query);
        model.addAttribute("type", type);
        return "search";
    }
}