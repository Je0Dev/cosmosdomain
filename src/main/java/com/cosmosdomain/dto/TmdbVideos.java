package com.cosmosdomain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbVideos {
    private List<TmdbVideo> results;

    public List<TmdbVideo> getResults() { return results; }
    public void setResults(List<TmdbVideo> results) { this.results = results; }

    public String getFirstYouTubeKey() {
        if (results == null) return null;
        return results.stream()
            .filter(v -> "YouTube".equals(v.getSite()))
            .map(TmdbVideo::getKey)
            .findFirst()
            .orElse(null);
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class TmdbVideo {
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