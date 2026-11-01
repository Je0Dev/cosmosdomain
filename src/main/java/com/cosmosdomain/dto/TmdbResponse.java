package com.cosmosdomain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Generic wrapper for TMDB list responses.
 *
 * <p>TMDB returns results inside a paging envelope; this class exposes the
 * current page, the result list, and the total counts.</p>
 *
 * @param <T> element type of the {@code results} array
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbResponse<T> {
    // Paging metadata returned by every TMDB list endpoint.
    private Integer page;
    @JsonProperty("results")
    private List<T> results;
    @JsonProperty("total_results")
    private Integer totalResults;
    @JsonProperty("total_pages")
    private Integer totalPages;

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public List<T> getResults() { return results; }
    public void setResults(List<T> results) { this.results = results; }

    public Integer getTotalResults() { return totalResults; }
    public void setTotalResults(Integer totalResults) { this.totalResults = totalResults; }

    public Integer getTotalPages() { return totalPages; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }
}