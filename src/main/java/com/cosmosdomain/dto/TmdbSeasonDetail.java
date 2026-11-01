package com.cosmosdomain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * TMDB season detail payload returned by {@code /tv/{id}/season/{number}}.
 *
 * <p>Extends the season summary with the full list of episodes, which the
 * season page renders as individual browsable entries.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbSeasonDetail {

    // Season metadata; names mirror the TMDB JSON keys.
    @JsonProperty("air_date")
    private String airDate;
    @JsonProperty("poster_path")
    private String posterPath;
    @JsonProperty("season_number")
    private Integer seasonNumber;
    @JsonProperty("vote_average")
    private Double voteAverage;

    private Long id;
    private String name;
    private String overview;

    // Every episode in this season, in TMDB order.
    private List<TmdbEpisode> episodes;

    public String getAirDate() { return airDate; }
    public void setAirDate(String airDate) { this.airDate = airDate; }

    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

    public Integer getSeasonNumber() { return seasonNumber; }
    public void setSeasonNumber(Integer seasonNumber) { this.seasonNumber = seasonNumber; }

    public Double getVoteAverage() { return voteAverage; }
    public void setVoteAverage(Double voteAverage) { this.voteAverage = voteAverage; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public List<TmdbEpisode> getEpisodes() { return episodes; }
    public void setEpisodes(List<TmdbEpisode> episodes) { this.episodes = episodes; }
}
