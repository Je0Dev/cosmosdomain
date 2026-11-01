package com.cosmosdomain.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * JPA entity for the {@code favorites} table.
 *
 * <p>Stores a user's reference to a TMDB movie, series, or person. Only the
 * media type and TMDB id are stored; display metadata is fetched from TMDB
 * when the favorites page is rendered.</p>
 */
@Entity
@Table(name = "favorites")
public class Favorite {

    // The kind of TMDB entity the favorite points at.
    public enum MediaType {
        MOVIE, TV_SERIES, PERSON
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owner of the favorite.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Together with mediaId this identifies the favorited TMDB entity.
    @Enumerated(EnumType.STRING)
    @Column(name = "media_type")
    private MediaType mediaType;

    @Column(name = "media_id")
    private Long mediaId;

    // Set once on creation; not updated afterwards.
    @Column(name = "created_at", updatable = false)
    private LocalDate createdAt = LocalDate.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public MediaType getMediaType() { return mediaType; }
    public void setMediaType(MediaType mediaType) { this.mediaType = mediaType; }

    public Long getMediaId() { return mediaId; }
    public void setMediaId(Long mediaId) { this.mediaId = mediaId; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
}