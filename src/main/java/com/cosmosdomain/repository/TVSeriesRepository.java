package com.cosmosdomain.repository;

import com.cosmosdomain.entity.TVSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Spring Data repository for {@link TVSeries} rows.
 *
 * <p>Lookups are by TMDB id; used when persisting catalog data locally.</p>
 */
public interface TVSeriesRepository extends JpaRepository<TVSeries, Long> {
    // Catalog sync helpers keyed by the external TMDB id.
    Optional<TVSeries> findByTmdbId(Long tmdbId);
    boolean existsByTmdbId(Long tmdbId);
}