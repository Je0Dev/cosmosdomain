package com.cosmosdomain.service;

import com.cosmosdomain.config.TmdbProperties;
import com.cosmosdomain.dto.FavoriteView;
import com.cosmosdomain.dto.TmdbMovie;
import com.cosmosdomain.dto.TmdbPersonDetail;
import com.cosmosdomain.dto.TmdbTV;
import com.cosmosdomain.entity.Favorite;
import com.cosmosdomain.entity.User;
import com.cosmosdomain.repository.FavoriteRepository;
import com.cosmosdomain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for user favorites.
 *
 * <p>Resolves the current user, toggles favorite rows, and enriches stored
 * favorites with TMDB metadata (title, poster, year/department) for display on
 * the favorites page.</p>
 */
@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final TmdbClient tmdbClient;
    private final TmdbProperties props;

    public FavoriteService(FavoriteRepository favoriteRepository,
                           UserRepository userRepository,
                           TmdbClient tmdbClient,
                           TmdbProperties props) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.tmdbClient = tmdbClient;
        this.props = props;
    }

    /**
     * Loads the user's favorite rows and turns each one into a display-ready
     * view by fetching the corresponding TMDB detail (cached by TmdbClient).
     */
    public List<FavoriteView> getFavoriteViews(String username) {
        User user = requireUser(username);
        return favoriteRepository.findByUserId(user.getId()).stream()
            .map(this::toView)
            .toList();
    }

    /**
     * Used by the detail pages to decide whether to render the add or remove
     * state of the favorite button.
     */
    public boolean isFavorite(String username, Favorite.MediaType mediaType, Long mediaId) {
        User user = requireUser(username);
        return favoriteRepository
            .findByUserIdAndMediaTypeAndMediaId(user.getId(), mediaType, mediaId)
            .isPresent();
    }

    /**
     * Adds the favorite if it does not exist, otherwise removes it.
     *
     * @return {@code true} when the item is now favorited, {@code false} when it
     *         was removed
     */
    @Transactional
    public boolean toggle(String username, Favorite.MediaType mediaType, Long mediaId) {
        User user = requireUser(username);
        var existing = favoriteRepository
            .findByUserIdAndMediaTypeAndMediaId(user.getId(), mediaType, mediaId);

        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return false;
        }

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setMediaType(mediaType);
        favorite.setMediaId(mediaId);
        favoriteRepository.save(favorite);
        return true;
    }

    // Converts a stored reference into the fields the favorites template shows.
    // A failed TMDB lookup still yields a card, just with fallback text.
    private FavoriteView toView(Favorite favorite) {
        Long id = favorite.getMediaId();
        return switch (favorite.getMediaType()) {
            case MOVIE -> {
                TmdbMovie movie = tmdbClient.getMovieDetail(id);
                yield new FavoriteView(
                    id,
                    Favorite.MediaType.MOVIE,
                    movie != null ? movie.getTitle() : "Unknown movie",
                    year(movie != null ? movie.getReleaseDate() : null),
                    image(movie != null ? movie.getPosterPath() : null),
                    "/movie/" + id
                );
            }
            case TV_SERIES -> {
                TmdbTV tv = tmdbClient.getTVDetail(id);
                yield new FavoriteView(
                    id,
                    Favorite.MediaType.TV_SERIES,
                    tv != null ? tv.getName() : "Unknown series",
                    year(tv != null ? tv.getFirstAirDate() : null),
                    image(tv != null ? tv.getPosterPath() : null),
                    "/tv/" + id
                );
            }
            case PERSON -> {
                TmdbPersonDetail person = tmdbClient.getPersonDetail(id);
                yield new FavoriteView(
                    id,
                    Favorite.MediaType.PERSON,
                    person != null ? person.getName() : "Unknown person",
                    person != null ? person.getKnownForDepartment() : null,
                    image(person != null ? person.getProfilePath() : null),
                    "/person/" + id
                );
            }
        };
    }

    // TMDB dates arrive as ISO strings; the UI only needs the release year.
    private String year(String date) {
        return date != null && date.length() >= 4 ? date.substring(0, 4) : null;
    }

    // Builds a full TMDB image URL, or null so the template can show a placeholder.
    private String image(String path) {
        return path != null
            ? props.getImageBaseUrl() + "/" + props.getPosterSize() + path
            : null;
    }

    private User requireUser(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalStateException("User not found: " + username));
    }
}
