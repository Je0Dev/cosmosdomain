package com.cosmosdomain.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "seasons")
public class Season {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id")
    private TVSeries series;

    @Column(name = "tmdb_id")
    private Long tmdbId;

    @Column(name = "season_number")
    private Integer seasonNumber;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(name = "air_date")
    private LocalDate airDate;

    @Column(name = "poster_path")
    private String posterPath;

    @Column(name = "episode_count")
    private Integer episodeCount;

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Episode> episodes = new HashSet<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TVSeries getSeries() { return series; }
    public void setSeries(TVSeries series) { this.series = series; }

    public Long getTmdbId() { return tmdbId; }
    public void setTmdbId(Long tmdbId) { this.tmdbId = tmdbId; }

    public Integer getSeasonNumber() { return seasonNumber; }
    public void setSeasonNumber(Integer seasonNumber) { this.seasonNumber = seasonNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public LocalDate getAirDate() { return airDate; }
    public void setAirDate(LocalDate airDate) { this.airDate = airDate; }

    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

    public Integer getEpisodeCount() { return episodeCount; }
    public void setEpisodeCount(Integer episodeCount) { this.episodeCount = episodeCount; }

    public Set<Episode> getEpisodes() { return episodes; }
    public void setEpisodes(Set<Episode> episodes) { this.episodes = episodes; }

    public String getPosterUrl(String imageBaseUrl, String posterSize) {
        return posterPath != null ? imageBaseUrl + "/" + posterSize + posterPath : "/images/no-poster.png";
    }
}