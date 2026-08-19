package com.cosmosdomain.repository;

import com.cosmosdomain.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {
    Optional<Person> findByTmdbId(Long tmdbId);
    boolean existsByTmdbId(Long tmdbId);
}