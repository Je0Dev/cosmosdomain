package com.cosmosdomain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "local_media")
public class LocalMedia {

    public enum MediaType {
        MOVIE, TV_SERIES, EPISODE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type")
    private MediaType mediaType;

    @Column(name = "catalog_movie_id")
    private Long catalogMovieId;

    @Column(name = "catalog_series_id")
    private Long catalogSeriesId;

    @Column(name = "catalog_episode_id")
    private Long catalogEpisodeId;

    @Column(name = "file_path", nullable = false, columnDefinition = "TEXT")
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "thumbnail_path")
    private String thumbnailPath;

    @Column(name = "added_at", updatable = false)
    private LocalDateTime addedAt = LocalDateTime.now();

    @Column(name = "playback_position")
    private Integer playbackPosition = 0;

    @Column(name = "last_played")
    private LocalDateTime lastPlayed;

    @OneToMany(mappedBy = "localMedia", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Subtitle> subtitles = new HashSet<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public MediaType getMediaType() { return mediaType; }
    public void setMediaType(MediaType mediaType) { this.mediaType = mediaType; }

    public Long getCatalogMovieId() { return catalogMovieId; }
    public void setCatalogMovieId(Long catalogMovieId) { this.catalogMovieId = catalogMovieId; }

    public Long getCatalogSeriesId() { return catalogSeriesId; }
    public void setCatalogSeriesId(Long catalogSeriesId) { this.catalogSeriesId = catalogSeriesId; }

    public Long getCatalogEpisodeId() { return catalogEpisodeId; }
    public void setCatalogEpisodeId(Long catalogEpisodeId) { this.catalogEpisodeId = catalogEpisodeId; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

    public String getThumbnailPath() { return thumbnailPath; }
    public void setThumbnailPath(String thumbnailPath) { this.thumbnailPath = thumbnailPath; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }

    public Integer getPlaybackPosition() { return playbackPosition; }
    public void setPlaybackPosition(Integer playbackPosition) { this.playbackPosition = playbackPosition; }

    public LocalDateTime getLastPlayed() { return lastPlayed; }
    public void setLastPlayed(LocalDateTime lastPlayed) { this.lastPlayed = lastPlayed; }

    public Set<Subtitle> getSubtitles() { return subtitles; }
    public void setSubtitles(Set<Subtitle> subtitles) { this.subtitles = subtitles; }

    public void addSubtitle(Subtitle subtitle) {
        this.subtitles.add(subtitle);
        subtitle.setLocalMedia(this);
    }
}