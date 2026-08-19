package com.cosmosdomain.controller;

import com.cosmosdomain.entity.Favorite;
import com.cosmosdomain.entity.User;
import com.cosmosdomain.repository.FavoriteRepository;
import com.cosmosdomain.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/favorites")
public class FavoritesController {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;

    public FavoritesController(FavoriteRepository favoriteRepository,
                               UserRepository userRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String favorites(
        @AuthenticationPrincipal UserDetails userDetails,
        Model model) {

        User user = findUser(userDetails);
        List<Favorite> favorites = favoriteRepository.findAll().stream()
            .filter(f -> f.getUser().getId().equals(user.getId()))
            .toList();
        model.addAttribute("favorites", favorites);
        return "favorites";
    }

    @PostMapping("/toggle")
    @ResponseBody
    public ResponseEntity<String> toggleFavorite(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam Favorite.MediaType mediaType,
        @RequestParam Long mediaId) {

        User user = findUser(userDetails);
        var existing = favoriteRepository
            .findByUserIdAndMediaTypeAndMediaId(user.getId(), mediaType, mediaId);

        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return ResponseEntity.ok("removed");
        } else {
            Favorite fav = new Favorite();
            fav.setUser(user);
            fav.setMediaType(mediaType);
            fav.setMediaId(mediaId);
            favoriteRepository.save(fav);
            return ResponseEntity.ok("added");
        }
    }

    private User findUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new IllegalStateException("User not found"));
    }
}