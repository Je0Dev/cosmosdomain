package com.cosmosdomain.controller;

import com.cosmosdomain.dto.TmdbPersonDetail;
import com.cosmosdomain.entity.Favorite;
import com.cosmosdomain.service.FavoriteService;
import com.cosmosdomain.service.TmdbClient;
import com.cosmosdomain.config.TmdbProperties;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Renders a person (actor/director) profile from TMDB.
 *
 * <p>Also reports whether the signed-in user has favorited this person so the
 * template can render the correct button state.</p>
 */
@Controller
public class PersonController {

    private final TmdbClient tmdbClient;
    private final TmdbProperties props;
    private final FavoriteService favoriteService;

    public PersonController(TmdbClient tmdbClient, TmdbProperties props,
                            FavoriteService favoriteService) {
        this.tmdbClient = tmdbClient;
        this.props = props;
        this.favoriteService = favoriteService;
    }

    // Person profiles are keyed by TMDB id and are always fetched live.
    @GetMapping("/person/{tmdbId}")
    public String personDetail(@PathVariable Long tmdbId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        TmdbPersonDetail person = tmdbClient.getPersonDetail(tmdbId);
        if (person == null) {
            return "redirect:/search";
        }
        model.addAttribute("person", person);
        model.addAttribute("imageBaseUrl", props.getImageBaseUrl());
        model.addAttribute("posterSize", props.getPosterSize());
        model.addAttribute("isFavorite", favoriteService.isFavorite(
            userDetails.getUsername(), Favorite.MediaType.PERSON, tmdbId));
        return "person-detail";
    }
}