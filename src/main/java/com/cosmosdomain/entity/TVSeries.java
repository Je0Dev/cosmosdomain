package com.cosmosdomain.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tv_series")
public class TVSeries {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tmdb_id", unique = true)
    private Long tmdbId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(name = "first_air_date")
    private LocalDate firstAirDate;

    @Column(name = "last_air_date")
    private LocalDate lastAirDate;

    @Column(name = "poster_path")
    private String posterPath;

    @Column(name = "backdrop_path")
    private String backdropPath;

    @Column(name = "vote_average")
    private Double voteAverage;

    @Column(name = "vote_count")
    private Integer voteCount;

    @Column(name = "number_of_seasons")
    private Integer numberOfSeasons;

    @Column(name = "number_of_episodes")
    private Integer numberOfEpisodes;

    @Column(name = "status")
    private String status;

    @Column(name = "in_production")
    private boolean inProduction;

    @Column(name = "youtube_trailer_key")
    private String youtubeTrailerKey;

    @OneToMany(mappedBy = "series", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Season> seasons = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "series_genres",
        joinColumns = @JoinColumn(name = "series_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "series_actors",
        joinColumns = @JoinColumn(name = "series_id"),
        inverseJoinColumns = @JoinColumn(name = "person_id")
    )
    private Set<Person> actors = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "series_creators",
        joinColumns = @JoinColumn(name = "series_id"),
        inverseJoinColumns = @JoinColumn(name = "person_id")
    )
    private Set<Person> creators = new HashSet<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTmdbId() { return tmdbId; }
    public void setTmdbId(Long tmdbId) { this.tmdbId = tmdbId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public LocalDate getFirstAirDate() { return firstAirDate; }
    public void setFirstAirDate(LocalDate firstAirDate) { this.firstAirDate = firstAirDate; }

    public LocalDate getLastAirDate() { return lastAirDate; }
    public void setLastAirDate(LocalDate lastAirDate) { this.lastAirDate = lastAirDate; }

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

    public Set<Season> getSeasons() { return seasons; }
    public void setSeasons(Set<Season> seasons) { this.seasons = seasons; }

    public Set<Genre> getGenres() { return genres; }
    public void setGenres(Set<Genre> genres) { this.genres = genres; }

    public Set<Person> getActors() { return actors; }
    public void setActors(Set<Person> actors) { this.actors = actors; }

    public Set<Person> getCreators() { return creators; }
    public void setCreators(Set<Person> creators) { this.creators = creators; }

    public String getPosterUrl(String imageBaseUrl, String posterSize) {
        return posterPath != null ? imageBaseUrl + "/" + posterSize + posterPath : "/images/no-poster.png";
    }

    public String getBackdropUrl(String imageBaseUrl, String backdropSize) {
        return backdropPath != null ? imageBaseUrl + "/" + backdropSize + backdropPath : "/images/no-backdrop.jpg";
    }

    public void addSeason(Season season) {
        this.seasons.add(season);
        season.setSeries(this);
    }
}