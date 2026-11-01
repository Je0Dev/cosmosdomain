package com.cosmosdomain.repository;

import com.cosmosdomain.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Spring Data repository for {@link Movie} rows.
 *
 * <p>Lookups are by TMDB id; used when persisting catalog data locally.</p>
 */
public interface MovieRepository extends JpaRepository<Movie, Long> {
    // Catalog sync helpers keyed by the external TMDB id.
    Optional<Movie> findByTmdbId(Long tmdbId);
    boolean existsByTmdbId(Long tmdbId);
}