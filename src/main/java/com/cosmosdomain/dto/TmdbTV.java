package com.cosmosdomain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbTV {

    private Long id;
    private String name;
    private String overview;
    @JsonProperty("first_air_date")
    private String firstAirDate;
    @JsonProperty("last_air_date")
    private String lastAirDate;
    @JsonProperty("poster_path")
    private String posterPath;
    @JsonProperty("backdrop_path")
    private String backdropPath;
    @JsonProperty("vote_average")
    private Double voteAverage;
    @JsonProperty("vote_count")
    private Integer voteCount;
    @JsonProperty("number_of_seasons")
    private Integer numberOfSeasons;
    @JsonProperty("number_of_episodes")
    private Integer numberOfEpisodes;
    private String status;
    @JsonProperty("in_production")
    private boolean inProduction;
    @JsonProperty("youtube_trailer_key")
    private String youtubeTrailerKey;
    private List<TmdbGenre> genres;
    private List<TmdbSeason> seasons;
    private TmdbCredits credits;
    private TmdbVideos videos;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public String getFirstAirDate() { return firstAirDate; }
    public void setFirstAirDate(String firstAirDate) { this.firstAirDate = firstAirDate; }

    public String getLastAirDate() { return lastAirDate; }
    public void setLastAirDate(String lastAirDate) { this.lastAirDate = lastAirDate; }

    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

    public String getBackdropPath() { return backdropPath; }
    public void setBackdropPath(String backdropPath) { this.backdropPath = backdropPath; }

    public Double getVoteAverage() { return voteAverage; }
    public void setVoteAverage(Double voteAverage) { this.voteAverage = voteAverage; }

    public Integer getVoteCount() { return voteCount; }
    public void setVoteCount(Integer voteCount) { this.voteCount = voteCount; }

    public Integer getNumberOfSeasons() { return numberOfSeasons; }
    public void setNumberOfSeasons(Integer numberOfSeasons) { this.numberOfSeasons = numberOfSeasons; }

    public Integer getNumberOfEpisodes() { return numberOfEpisodes; }
    public void setNumberOfEpisodes(Integer numberOfEpisodes) { this.numberOfEpisodes = numberOfEpisodes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isInProduction() { return inProduction; }
    public void setInProduction(boolean inProduction) { this.inProduction = inProduction; }

    public String getYoutubeTrailerKey() { return youtubeTrailerKey; }
    public void setYoutubeTrailerKey(String youtubeTrailerKey) { this.youtubeTrailerKey = youtubeTrailerKey; }

    public List<TmdbGenre> getGenres() { return genres; }
    public void setGenres(List<TmdbGenre> genres) { this.genres = genres; }

    public List<TmdbSeason> getSeasons() { return seasons; }
    public void setSeasons(List<TmdbSeason> seasons) { this.seasons = seasons; }

    public TmdbCredits getCredits() { return credits; }
    public void setCredits(TmdbCredits credits) { this.credits = credits; }

    public TmdbVideos getVideos() { return videos; }
    public void setVideos(TmdbVideos videos) { this.videos = videos; }
}