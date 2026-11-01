package com.cosmosdomain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * TMDB genre payload ({@code id} and {@code name}).
 *
 * <p>Used inside movie and TV detail responses.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbGenre {
    private Integer id;
    private String name;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}