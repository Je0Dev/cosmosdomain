package com.cosmosdomain.controller;

import com.cosmosdomain.dto.TmdbMovie;
import com.cosmosdomain.dto.TmdbSeasonDetail;
import com.cosmosdomain.dto.TmdbTV;
import com.cosmosdomain.entity.Favorite;
import com.cosmosdomain.service.FavoriteService;
import com.cosmosdomain.service.TmdbClient;
import com.cosmosdomain.config.TmdbProperties;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Renders the movie and TV catalog pages and their detail pages.
 *
 * <p>All catalog data comes live from TMDB through {@link TmdbClient}; nothing
 * is persisted locally. The detail pages also expose whether the signed-in user
 * has marked the title as a favorite.</p>
 */
@Controller
public class CatalogController {

    private final TmdbClient tmdbClient;
    private final TmdbProperties props;
    private final FavoriteService favoriteService;

    public CatalogController(TmdbClient tmdbClient, TmdbProperties props,
                             FavoriteService favoriteService) {
        this.tmdbClient = tmdbClient;
        this.props = props;
        this.favoriteService = favoriteService;
    }

    // Page 1 is the default; the template exposes previous/next links.
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

    // Movie detail is keyed by the TMDB id, not a local database id.
    @GetMapping("/movie/{tmdbId}")
    public String movieDetail(@org.springframework.web.bind.annotation.PathVariable Long tmdbId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        TmdbMovie movie = tmdbClient.getMovieDetail(tmdbId);
        if (movie == null) {
            return "redirect:/movies";
        }
        model.addAttribute("movie", movie);
        model.addAttribute("imageBaseUrl", props.getImageBaseUrl());
        model.addAttribute("backdropSize", props.getBackdropSize());
        model.addAttribute("posterSize", props.getPosterSize());
        // Drives the initial state of the favorite button on the detail page.
        model.addAttribute("isFavorite", favoriteService.isFavorite(
            userDetails.getUsername(), Favorite.MediaType.MOVIE, tmdbId));
        return "movie-detail";
    }

    @GetMapping("/tv/{tmdbId}")
    public String tvDetail(@org.springframework.web.bind.annotation.PathVariable Long tmdbId,
                          @AuthenticationPrincipal UserDetails userDetails,
                          Model model) {
        TmdbTV tv = tmdbClient.getTVDetail(tmdbId);
        if (tv == null) {
            return "redirect:/series";
        }
        model.addAttribute("series", tv);
        model.addAttribute("imageBaseUrl", props.getImageBaseUrl());
        model.addAttribute("backdropSize", props.getBackdropSize());
        model.addAttribute("posterSize", props.getPosterSize());
        // TV series use the TV_SERIES favorite type.
        model.addAttribute("isFavorite", favoriteService.isFavorite(
            userDetails.getUsername(), Favorite.MediaType.TV_SERIES, tmdbId));
        return "tv-detail";
    }

    // One season of a series, with every episode rendered as its own entry.
    @GetMapping("/tv/{tmdbId}/season/{seasonNumber}")
    public String seasonDetail(
            @org.springframework.web.bind.annotation.PathVariable Long tmdbId,
            @org.springframework.web.bind.annotation.PathVariable int seasonNumber,
            Model model) {
        TmdbSeasonDetail season = tmdbClient.getSeasonDetail(tmdbId, seasonNumber);
        if (season == null) {
            return "redirect:/tv/" + tmdbId;
        }
        TmdbTV tv = tmdbClient.getTVDetail(tmdbId);
        if (tv == null) {
            return "redirect:/series";
        }
        model.addAttribute("series", tv);
        model.addAttribute("season", season);
        model.addAttribute("imageBaseUrl", props.getImageBaseUrl());
        model.addAttribute("backdropSize", props.getBackdropSize());
        model.addAttribute("posterSize", props.getPosterSize());
        model.addAttribute("stillSize", props.getStillSize());
        return "season-detail";
    }
}