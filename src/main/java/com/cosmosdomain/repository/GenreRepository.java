package com.cosmosdomain.repository;

import com.cosmosdomain.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Spring Data repository for {@link Genre} rows.
 *
 * <p>Lookups are by the original TMDB genre id so catalog sync code can avoid
 * inserting duplicates.</p>
 */
public interface GenreRepository extends JpaRepository<Genre, Long> {
    // Look up a genre by its TMDB id instead of the local primary key.
    Optional<Genre> findByTmdbGenreId(Integer tmdbGenreId);

    // Cheap existence check used before inserting imported genres.
    boolean existsByTmdbGenreId(Integer tmdbGenreId);
}