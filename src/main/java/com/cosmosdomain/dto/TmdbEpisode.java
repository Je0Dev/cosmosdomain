package com.cosmosdomain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TMDB episode payload used by the season detail response.
 *
 * <p>Carries the still image, title, overview, air date, runtime, and TMDB
 * vote data for a single episode.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbEpisode {

    // Snake_case TMDB keys mapped to Java-style field names.
    @JsonProperty("air_date")
    private String airDate;
    @JsonProperty("episode_number")
    private Integer episodeNumber;
    @JsonProperty("season_number")
    private Integer seasonNumber;
    @JsonProperty("still_path")
    private String stillPath;
    @JsonProperty("vote_average")
    private Double voteAverage;
    @JsonProperty("vote_count")
    private Integer voteCount;

    private String name;
    private String overview;
    private Integer runtime;

    public String getAirDate() { return airDate; }
    public void setAirDate(String airDate) { this.airDate = airDate; }

    public Integer getEpisodeNumber() { return episodeNumber; }
    public void setEpisodeNumber(Integer episodeNumber) { this.episodeNumber = episodeNumber; }

    public Integer getSeasonNumber() { return seasonNumber; }
    public void setSeasonNumber(Integer seasonNumber) { this.seasonNumber = seasonNumber; }

    public String getStillPath() { return stillPath; }
    public void setStillPath(String stillPath) { this.stillPath = stillPath; }

    public Double getVoteAverage() { return voteAverage; }
    public void setVoteAverage(Double voteAverage) { this.voteAverage = voteAverage; }

    public Integer getVoteCount() { return voteCount; }
    public void setVoteCount(Integer voteCount) { this.voteCount = voteCount; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public Integer getRuntime() { return runtime; }
    public void setRuntime(Integer runtime) { this.runtime = runtime; }
}
