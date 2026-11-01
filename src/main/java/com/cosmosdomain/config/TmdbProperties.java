package com.cosmosdomain.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Typed binding for the {@code cosmosdomain.*} settings in
 * {@code application.yml}.
 *
 * <p>Holds the TMDB API credentials plus the image size/base URL fragments
 * used when building poster and backdrop URLs in templates.</p>
 */
@Component
@ConfigurationProperties(prefix = "cosmosdomain")
public class TmdbProperties {

    /** TMDB v3 API key, normally supplied through the TMDB_API_KEY env var. */
    private String apiKey;
    /** API root, for example https://api.themoviedb.org/3. */
    private String baseUrl;
    /** Image CDN root, for example https://image.tmdb.org/t/p. */
    private String imageBaseUrl;
    /** Poster size segment appended to image URLs (for example w500). */
    private String posterSize;
    /** Backdrop size segment appended to image URLs (for example w1280). */
    private String backdropSize;
    /** Episode still size segment appended to image URLs (for example w300). */
    private String stillSize;

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getImageBaseUrl() { return imageBaseUrl; }
    public void setImageBaseUrl(String imageBaseUrl) { this.imageBaseUrl = imageBaseUrl; }

    public String getPosterSize() { return posterSize; }
    public void setPosterSize(String posterSize) { this.posterSize = posterSize; }

    public String getBackdropSize() { return backdropSize; }
    public void setBackdropSize(String backdropSize) { this.backdropSize = backdropSize; }

    public String getStillSize() { return stillSize; }
    public void setStillSize(String stillSize) { this.stillSize = stillSize; }
}
