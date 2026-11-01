package com.cosmosdomain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * TMDB {@code credits} sub-object containing cast and crew lists.
 *
 * <p>Jackson maps the TMDB JSON into this object; the {@link #getDirectors()}
 * helper filters the crew list down to entries whose job is "Director".</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbCredits {
    // Actors appearing on screen and the production crew.
    private List<TmdbPersonResult> cast;
    private List<TmdbPersonResult> crew;

    public List<TmdbPersonResult> getCast() { return cast; }
    public void setCast(List<TmdbPersonResult> cast) { this.cast = cast; }

    public List<TmdbPersonResult> getCrew() { return crew; }
    public void setCrew(List<TmdbPersonResult> crew) { this.crew = crew; }

    // Convenience filter over the crew list; returns an empty list when absent.
    public List<TmdbPersonResult> getDirectors() {
        if (crew == null) return List.of();
        return crew.stream()
            .filter(c -> "Director".equals(c.getJob()))
            .toList();
    }
}