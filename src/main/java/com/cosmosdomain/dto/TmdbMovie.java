package com.cosmosdomain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbMovie {

    private Long id;
    private String title;
    private String overview;
    @JsonProperty("release_date")
    private String releaseDate;
    @JsonProperty("poster_path")
    private String posterPath;
    @JsonProperty("backdrop_path")
    private String backdropPath;
    @JsonProperty("vote_average")
    private Double voteAverage;
    @JsonProperty("vote_count")
    private Integer voteCount;
    private Integer runtime;
    private String tagline;
    private Long budget;
    private Long revenue;
    private String status;
    @JsonProperty("imdb_id")
    private String imdbId;
    @JsonProperty("home_page")
    private String homePage;
    private String youtubeTrailerKey;
    private List<TmdbGenre> genres;
    private TmdbCredits credits;
    private TmdbVideos videos;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

    public String getBackdropPath() { return backdropPath; }
    public void setBackdropPath(String backdropPath) { this.backdropPath = backdropPath; }

    public Double getVoteAverage() { return voteAverage; }
    public void setVoteAverage(Double voteAverage) { this.voteAverage = voteAverage; }

    public Integer getVoteCount() { return voteCount; }
    public void setVoteCount(Integer voteCount) { this.voteCount = voteCount; }

    public Integer getRuntime() { return runtime; }
    public void setRuntime(Integer runtime) { this.runtime = runtime; }

    public String getTagline() { return tagline; }
    public void setTagline(String tagline) { this.tagline = tagline; }

    public Long getBudget() { return budget; }
    public void setBudget(Long budget) { this.budget = budget; }

    public Long getRevenue() { return revenue; }
    public void setRevenue(Long revenue) { this.revenue = revenue; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImdbId() { return imdbId; }
    public void setImdbId(String imdbId) { this.imdbId = imdbId; }

    public String getHomePage() { return homePage; }
    public void setHomePage(String homePage) { this.homePage = homePage; }

    public String getYoutubeTrailerKey() { return youtubeTrailerKey; }
    public void setYoutubeTrailerKey(String youtubeTrailerKey) { this.youtubeTrailerKey = youtubeTrailerKey; }

    public List<TmdbGenre> getGenres() { return genres; }
    public void setGenres(List<TmdbGenre> genres) { this.genres = genres; }

    public TmdbCredits getCredits() { return credits; }
    public void setCredits(TmdbCredits credits) { this.credits = credits; }

    public TmdbVideos getVideos() { return videos; }
    public void setVideos(TmdbVideos videos) { this.videos = videos; }
}