package com.cosmosdomain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * TMDB {@code combined_credits} sub-object for a person.
 *
 * <p>Contains every movie and TV credit for the person in the {@code cast}
 * list. The person page uses this to build a "Known For" section, because the
 * person detail endpoint does not return the shorter {@code known_for} list.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbCombinedCredits {

    private List<TmdbPersonResult> cast;
    private List<TmdbPersonResult> crew;

    public List<TmdbPersonResult> getCast() { return cast; }
    public void setCast(List<TmdbPersonResult> cast) { this.cast = cast; }

    public List<TmdbPersonResult> getCrew() { return crew; }
    public void setCrew(List<TmdbPersonResult> crew) { this.crew = crew; }
}
