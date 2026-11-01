package com.cosmosdomain.controller;

import com.cosmosdomain.entity.Favorite;
import com.cosmosdomain.service.FavoriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Serves the favorites page and the AJAX endpoint used to toggle a favorite.
 *
 * <p>The page shows a card for every saved movie, series, or person. The toggle
 * endpoint is called by the small scripts embedded in the detail and favorites
 * templates, which is why it returns a plain {@code added}/{@code removed}
 * body instead of a redirect.</p>
 */
@Controller
@RequestMapping("/favorites")
public class FavoritesController {

    private final FavoriteService favoriteService;

    public FavoritesController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    // Loads the current user's favorites and enriches each row with TMDB data.
    @GetMapping
    public String favorites(
        @AuthenticationPrincipal UserDetails userDetails,
        Model model) {

        model.addAttribute("favorites", favoriteService.getFavoriteViews(userDetails.getUsername()));
        return "favorites";
    }

    // AJAX endpoint used by the detail and favorites pages. Returns a small
    // text body so the calling script can update the button without a reload.
    @PostMapping("/toggle")
    @ResponseBody
    public ResponseEntity<String> toggleFavorite(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam Favorite.MediaType mediaType,
        @RequestParam Long mediaId) {

        boolean added = favoriteService.toggle(userDetails.getUsername(), mediaType, mediaId);
        return ResponseEntity.ok(added ? "added" : "removed");
    }
}
