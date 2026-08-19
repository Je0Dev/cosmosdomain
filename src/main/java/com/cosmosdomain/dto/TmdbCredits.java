package com.cosmosdomain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbCredits {
    private List<TmdbPersonResult> cast;
    private List<TmdbPersonResult> crew;

    public List<TmdbPersonResult> getCast() { return cast; }
    public void setCast(List<TmdbPersonResult> cast) { this.cast = cast; }

    public List<TmdbPersonResult> getCrew() { return crew; }
    public void setCrew(List<TmdbPersonResult> crew) { this.crew = crew; }

    public List<TmdbPersonResult> getDirectors() {
        if (crew == null) return List.of();
        return crew.stream()
            .filter(c -> "Director".equals(c.getJob()))
            .toList();
    }
}