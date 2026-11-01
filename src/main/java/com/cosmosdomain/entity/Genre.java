package com.cosmosdomain.entity;

import jakarta.persistence.*;

/**
 * JPA entity for the {@code genres} table.
 *
 * <p>Mirrors TMDB genre ids and names so they can be joined to movies and
 * series in the persisted catalog model.</p>
 */
@Entity
@Table(name = "genres")
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Original TMDB genre id, used to deduplicate imported genres.
    @Column(name = "tmdb_genre_id", unique = true)
    private Integer tmdbGenreId;

    private String name;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getTmdbGenreId() { return tmdbGenreId; }
    public void setTmdbGenreId(Integer tmdbGenreId) { this.tmdbGenreId = tmdbGenreId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}