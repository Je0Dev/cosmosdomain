package com.cosmosdomain.repository;

import com.cosmosdomain.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Spring Data repository for {@link Person} rows.
 *
 * <p>Lookups are by TMDB id; used when persisting catalog data locally.</p>
 */
public interface PersonRepository extends JpaRepository<Person, Long> {
    // Catalog sync helpers keyed by the external TMDB id.
    Optional<Person> findByTmdbId(Long tmdbId);
    boolean existsByTmdbId(Long tmdbId);
}