package com.cosmosdomain.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cosmosdomain")
public class TmdbProperties {

    private String apiKey;
    private String baseUrl;
    private String imageBaseUrl;
    private String posterSize;
    private String backdropSize;
    private String mediaDir;

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

    public String getMediaDir() { return mediaDir; }
    public void setMediaDir(String mediaDir) { this.mediaDir = mediaDir; }
}