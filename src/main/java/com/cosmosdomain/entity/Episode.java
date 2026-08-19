package com.cosmosdomain.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "episodes")
public class Episode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id")
    private Season season;

    @Column(name = "tmdb_id")
    private Long tmdbId;

    @Column(name = "episode_number")
    private Integer episodeNumber;

    @Column(name = "season_number")
    private Integer seasonNumber;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(name = "air_date")
    private LocalDate airDate;

    @Column(name = "still_path")
    private String stillPath;

    private Integer runtime;

    @Column(name = "vote_average")
    private Double voteAverage;

    @Column(name = "youtube_trailer_key")
    private String youtubeTrailerKey;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Season getSeason() { return season; }
    public void setSeason(Season season) { this.season = season; }

    public Long getTmdbId() { return tmdbId; }
    public void setTmdbId(Long tmdbId) { this.tmdbId = tmdbId; }

    public Integer getEpisodeNumber() { return episodeNumber; }
    public void setEpisodeNumber(Integer episodeNumber) { this.episodeNumber = episodeNumber; }

    public Integer getSeasonNumber() { return seasonNumber; }
    public void setSeasonNumber(Integer seasonNumber) { this.seasonNumber = seasonNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public LocalDate getAirDate() { return airDate; }
    public void setAirDate(LocalDate airDate) { this.airDate = airDate; }

    public String getStillPath() { return stillPath; }
    public void setStillPath(String stillPath) { this.stillPath = stillPath; }

    public Integer getRuntime() { return runtime; }
    public void setRuntime(Integer runtime) { this.runtime = runtime; }

    public Double getVoteAverage() { return voteAverage; }
    public void setVoteAverage(Double voteAverage) { this.voteAverage = voteAverage; }

    public String getYoutubeTrailerKey() { return youtubeTrailerKey; }
    public void setYoutubeTrailerKey(String youtubeTrailerKey) { this.youtubeTrailerKey = youtubeTrailerKey; }

    public String getStillUrl(String imageBaseUrl, String stillSize) {
        return stillPath != null ? imageBaseUrl + "/" + stillSize + stillPath : "/images/no-backdrop.jpg";
    }
}