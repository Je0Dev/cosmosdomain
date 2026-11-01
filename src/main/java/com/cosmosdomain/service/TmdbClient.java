package com.cosmosdomain.service;

import com.cosmosdomain.config.TmdbProperties;
import com.cosmosdomain.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;

/**
 * Thin client around The Movie Database (TMDB) v3 API.
 *
 * <p>Builds authenticated URLs, parses JSON into the DTOs in
 * {@code com.cosmosdomain.dto}, and caches list/detail responses. Network or
 * parsing failures are logged and converted into empty lists or {@code null}
 * so the web layer can fail gracefully.</p>
 */
@Service
public class TmdbClient {

    private static final Logger log = LoggerFactory.getLogger(TmdbClient.class);

    // Query parameter names used by every TMDB request.
    private static final String API_KEY_PARAM = "api_key";
    private static final String LANGUAGE_PARAM = "language";
    private static final String REGION_PARAM = "region";
    // Asks TMDB to inline extra objects (credits, videos, images, external ids)
    // into detail responses instead of requiring a second request.
    private static final String APPEND_TO_RESPONSE = "append_to_response";

    private final TmdbProperties props;
    private final ObjectMapper objectMapper;
    private final org.springframework.web.client.RestClient restClient;

    public TmdbClient(TmdbProperties props, ObjectMapper objectMapper,
                      org.springframework.web.client.RestClient restClient) {
        this.props = props;
        this.objectMapper = objectMapper;
        this.restClient = restClient;
    }

    // List endpoints. Each result is cached under its own cache name so a page
    // of popular movies does not evict a page of upcoming movies.
    @Cacheable("tmdbPopularMovies")
    public List<TmdbMovie> getPopularMovies(int page) {
        return fetchList("/movie/popular", page, new TypeReference<>() {});
    }

    @Cacheable("tmdbTrendingTV")
    public List<TmdbTV> getTrendingTV(int page) {
        return fetchList("/trending/tv/week", page, new TypeReference<>() {});
    }

    @Cacheable("tmdbMovieDetail")
    public TmdbMovie getMovieDetail(Long tmdbId) {
        String path = "/movie/" + tmdbId;
        String url = buildDetailUrl(path);
        try {
            String json = restClient.get().uri(url).retrieve().body(String.class);
            if (json == null || json.isEmpty()) {
                log.warn("Empty response from TMDB for movie {}", tmdbId);
                return null;
            }
            // The appended credits/videos/genres are parsed into nested DTOs.
            return objectMapper.readValue(json, TmdbMovie.class);
        } catch (Exception e) {
            // Callers treat null as "not found / temporarily unavailable".
            log.error("Failed to fetch movie detail for {}: {}", tmdbId, e.getMessage());
            return null;
        }
    }

    @Cacheable("tmdbTVDetail")
    public TmdbTV getTVDetail(Long tmdbId) {
        String path = "/tv/" + tmdbId;
        String url = buildDetailUrl(path);
        try {
            String json = restClient.get().uri(url).retrieve().body(String.class);
            if (json == null || json.isEmpty()) {
                log.warn("Empty response from TMDB for TV {}", tmdbId);
                return null;
            }
            return objectMapper.readValue(json, TmdbTV.class);
        } catch (Exception e) {
            log.error("Failed to fetch TV detail for {}: {}", tmdbId, e.getMessage());
            return null;
        }
    }

    // Season detail includes the full episode list for one season.
    @Cacheable("tmdbSeasonDetail")
    public TmdbSeasonDetail getSeasonDetail(Long tvId, int seasonNumber) {
        String path = "/tv/" + tvId + "/season/" + seasonNumber;
        String url = buildUrl(path);
        try {
            String json = restClient.get().uri(url).retrieve().body(String.class);
            if (json == null || json.isEmpty()) {
                log.warn("Empty response from TMDB for TV {} season {}", tvId, seasonNumber);
                return null;
            }
            return objectMapper.readValue(json, TmdbSeasonDetail.class);
        } catch (Exception e) {
            log.error("Failed to fetch season {} for TV {}: {}",
                seasonNumber, tvId, e.getMessage());
            return null;
        }
    }

    @Cacheable("tmdbPersonDetail")
    public TmdbPersonDetail getPersonDetail(Long tmdbId) {
        String path = "/person/" + tmdbId;
        // Person detail does not return known_for, so request combined credits
        // and derive the "Known For" list from the cast entries.
        String url = buildUrl(path) + "&append_to_response=combined_credits,external_ids,images";
        try {
            String json = restClient.get().uri(url).retrieve().body(String.class);
            if (json == null || json.isEmpty()) {
                log.warn("Empty response from TMDB for person {}", tmdbId);
                return null;
            }
            return objectMapper.readValue(json, TmdbPersonDetail.class);
        } catch (Exception e) {
            log.error("Failed to fetch person detail for {}: {}", tmdbId, e.getMessage());
            return null;
        }
    }

    // Search endpoints are intentionally not cached: queries are user-driven and
    // would otherwise create an unbounded number of cache entries.
    public List<TmdbPersonResult> searchPeople(String query) {
        return searchList("/search/person", query, new TypeReference<>() {});
    }

    public List<TmdbMovie> searchMovies(String query) {
        return searchList("/search/movie", query, new TypeReference<>() {});
    }

    public List<TmdbTV> searchTV(String query) {
        return searchList("/search/tv", query, new TypeReference<>() {});
    }

    // Shared list fetch: unwraps the TMDB paging envelope and never returns null.
    private <T> List<T> fetchList(String path, int page, TypeReference<TmdbResponse<T>> ref) {
        String url = buildListUrl(path, page);
        try {
            String json = restClient.get().uri(url).retrieve().body(String.class);
            if (json == null || json.isEmpty()) return Collections.emptyList();
            TmdbResponse<T> response = objectMapper.readValue(json, ref);
            return response.getResults() != null ? response.getResults() : Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch list for {}: {}", path, e.getMessage());
            return Collections.emptyList();
        }
    }

    // Shared search fetch; search results are always read from the first page.
    private <T> List<T> searchList(String path, String query, TypeReference<TmdbResponse<T>> ref) {
        String url = buildSearchUrl(path, query, 1);
        try {
            String json = restClient.get().uri(url).retrieve().body(String.class);
            if (json == null || json.isEmpty()) return Collections.emptyList();
            TmdbResponse<T> response = objectMapper.readValue(json, ref);
            return response.getResults() != null ? response.getResults() : Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to search for {}: {}", query, e.getMessage());
            return Collections.emptyList();
        }
    }

    // Detail URLs request the extra nested objects the templates need.
    private String buildDetailUrl(String path) {
        return buildUrl(path)
            + "&" + APPEND_TO_RESPONSE + "=credits,videos,images,external_ids";
    }

    // Base URL with credentials and language; used by detail-style endpoints.
    private String buildUrl(String path) {
        return UriComponentsBuilder.fromHttpUrl(props.getBaseUrl() + path)
            .queryParam(API_KEY_PARAM, props.getApiKey())
            .queryParam(LANGUAGE_PARAM, "en-US")
            .build()
            .toUriString();
    }

    private String buildListUrl(String path, int page) {
        UriComponents uri = UriComponentsBuilder.fromHttpUrl(props.getBaseUrl() + path)
            .queryParam(API_KEY_PARAM, props.getApiKey())
            .queryParam(LANGUAGE_PARAM, "en-US")
            .queryParam(REGION_PARAM, "US")
            .build();
        String url = uri.toUriString();
        // The page parameter is only appended for the movie/on-the-air endpoints
        // used by the UI. The trending endpoint currently always returns page 1.
        if ("/movie/now_playing".equals(path) || "/tv/on_the_air".equals(path) || "/movie/popular".equals(path) || "/movie/top_rated".equals(path) || "/movie/upcoming".equals(path)) {
            url += "&page=" + page;
        }
        return url;
    }

    // Search URLs add the free-text query and US region filter.
    private String buildSearchUrl(String path, String query, int page) {
        UriComponents uri = UriComponentsBuilder.fromHttpUrl(props.getBaseUrl() + path)
            .queryParam(API_KEY_PARAM, props.getApiKey())
            .queryParam(LANGUAGE_PARAM, "en-US")
            .queryParam("query", query)
            .queryParam(REGION_PARAM, "US")
            .build();
        return uri.toUriString() + "&page=" + page;
    }
}