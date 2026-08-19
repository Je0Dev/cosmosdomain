package com.cosmosdomain.repository;

import com.cosmosdomain.entity.TVSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TVSeriesRepository extends JpaRepository<TVSeries, Long> {
    Optional<TVSeries> findByTmdbId(Long tmdbId);
    boolean existsByTmdbId(Long tmdbId);
}