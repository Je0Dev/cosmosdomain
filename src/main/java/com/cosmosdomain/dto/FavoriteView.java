package com.cosmosdomain.dto;

import com.cosmosdomain.entity.Favorite;

/**
 * View model for one row on the favorites page.
 *
 * <p>Combines the stored favorite reference (type + TMDB id) with the title,
 * poster URL, and detail link fetched from TMDB at render time.</p>
 *
 * @param mediaId    TMDB id of the favorited entity
 * @param mediaType  whether the entity is a movie, series, or person
 * @param title      display name fetched from TMDB (or a fallback)
 * @param subtitle   release year, department, or other secondary text
 * @param imageUrl   full poster/profile URL, or null for a placeholder
 * @param detailPath application path of the detail page
 */
public record FavoriteView(
    Long mediaId,
    Favorite.MediaType mediaType,
    String title,
    String subtitle,
    String imageUrl,
    String detailPath
) {
    // Human-readable badge text shown on each favorite card.
    public String typeLabel() {
        return switch (mediaType) {
            case MOVIE -> "Movie";
            case TV_SERIES -> "Series";
            case PERSON -> "Person";
        };
    }
}
