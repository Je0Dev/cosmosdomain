package com.cosmosdomain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * TMDB {@code videos} sub-object.
 *
 * <p>Wraps the video list and provides a helper that returns the first YouTube
 * key, which templates use to build an embed URL.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbVideos {
    // Raw list of trailer/teaser entries.
    private List<TmdbVideo> results;

    public List<TmdbVideo> getResults() { return results; }
    public void setResults(List<TmdbVideo> results) { this.results = results; }

    // Returns the key of the first YouTube video, or null when none exists.
    public String getFirstYouTubeKey() {
        if (results == null) return null;
        return results.stream()
            .filter(v -> "YouTube".equals(v.getSite()))
            .map(TmdbVideo::getKey)
            .findFirst()
            .orElse(null);
    }
}

/**
 * A single video entry nested inside {@link TmdbVideos}.
 *
 * <p>Kept package-private because it is only exposed through the helper on the
 * enclosing class.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class TmdbVideo {
    // Video identity and hosting site (for example YouTube).
    private String key;
    private String site;
    private String name;
    private Integer size;

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getSite() { return site; }
    public void setSite(String site) { this.site = site; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }
}