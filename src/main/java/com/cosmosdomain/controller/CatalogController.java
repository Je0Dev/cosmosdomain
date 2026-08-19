package com.cosmosdomain.controller;

import com.cosmosdomain.dto.TmdbMovie;
import com.cosmosdomain.dto.TmdbTV;
import com.cosmosdomain.service.TmdbClient;
import com.cosmosdomain.config.TmdbProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class CatalogController {

    private final TmdbClient tmdbClient;
    private final TmdbProperties props;

    public CatalogController(TmdbClient tmdbClient, TmdbProperties props) {
        this.tmdbClient = tmdbClient;
        this.props = props;
    }

    @GetMapping("/movies")
    public String movies(@RequestParam(defaultValue = "1") int page, Model model) {
        List<TmdbMovie> popular = tmdbClient.getPopularMovies(page);
        model.addAttribute("movies", popular);
        model.addAttribute("imageBaseUrl", props.getImageBaseUrl());
        model.addAttribute("posterSize", props.getPosterSize());
        model.addAttribute("page", page);
        return "movies";
    }

    @GetMapping("/series")
    public String series(@RequestParam(defaultValue = "1") int page, Model model) {
        List<TmdbTV> trending = tmdbClient.getTrendingTV(page);
        model.addAttribute("series", trending);
        model.addAttribute("imageBaseUrl", props.getImageBaseUrl());
        model.addAttribute("posterSize", props.getPosterSize());
        model.addAttribute("page", page);
        return "series";
    }

    @GetMapping("/movie/{tmdbId}")
    public String movieDetail(@org.springframework.web.bind.annotation.PathVariable Long tmdbId,
                             Model model) {
        TmdbMovie movie = tmdbClient.getMovieDetail(tmdbId);
        if (movie == null) {
            return "redirect:/movies";
        }
        model.addAttribute("movie", movie);
        model.addAttribute("imageBaseUrl", props.getImageBaseUrl());
        model.addAttribute("backdropSize", props.getBackdropSize());
        model.addAttribute("posterSize", props.getPosterSize());
        return "movie-detail";
    }

    @GetMapping("/tv/{tmdbId}")
    public String tvDetail(@org.springframework.web.bind.annotation.PathVariable Long tmdbId,
                          Model model) {
        TmdbTV tv = tmdbClient.getTVDetail(tmdbId);
        if (tv == null) {
            return "redirect:/series";
        }
        model.addAttribute("series", tv);
        model.addAttribute("imageBaseUrl", props.getImageBaseUrl());
        model.addAttribute("backdropSize", props.getBackdropSize());
        model.addAttribute("posterSize", props.getPosterSize());
        return "tv-detail";
    }
}