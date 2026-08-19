package com.cosmosdomain.service;

import com.cosmosdomain.config.TmdbProperties;
import com.cosmosdomain.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;

@Service
public class TmdbClient {

    private static final Logger log = LoggerFactory.getLogger(TmdbClient.class);

    private static final String API_KEY_PARAM = "api_key";
    private static final String LANGUAGE_PARAM = "language";
    private static final String REGION_PARAM = "region";
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

    @Cacheable("tmdbPopularMovies")
    public List<TmdbMovie> getPopularMovies(int page) {
        return fetchList("/movie/popular", page, new TypeReference<>() {});
    }

    @Cacheable("tmdbNowPlaying")
    public List<TmdbMovie> getNowPlaying(int page) {
        return fetchList("/movie/now_playing", page, new TypeReference<>() {});
    }

    @Cacheable("tmdbUpcoming")
    public List<TmdbMovie> getUpcoming(int page) {
        return fetchList("/movie/upcoming", page, new TypeReference<>() {});
    }

    @Cacheable("tmdbTopRated")
    public List<TmdbMovie> getTopRated(int page) {
        return fetchList("/movie/top_rated", page, new TypeReference<>() {});
    }

    @Cacheable("tmdbTrendingTV")
    public List<TmdbTV> getTrendingTV(int page) {
        return fetchList("/trending/tv/week", page, new TypeReference<>() {});
    }

    @Cacheable("tmdbOnTheAir")
    public List<TmdbTV> getOnTheAir(int page) {
        return fetchList("/tv/on_the_air", page, new TypeReference<>() {});
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
            TmdbMovie movie = objectMapper.readValue(json, TmdbMovie.class);
            return movie;
        } catch (Exception e) {
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
            TmdbTV tv = objectMapper.readValue(json, TmdbTV.class);
            return tv;
        } catch (Exception e) {
            log.error("Failed to fetch TV detail for {}: {}", tmdbId, e.getMessage());
            return null;
        }
    }

    @Cacheable("tmdbPersonDetail")
    public TmdbPersonDetail getPersonDetail(Long tmdbId) {
        String path = "/person/" + tmdbId;
        String url = buildDetailUrl(path);
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

    public List<TmdbPersonResult> searchPeople(String query) {
        return searchList("/search/person", query, new TypeReference<>() {});
    }

    public List<TmdbMovie> searchMovies(String query) {
        return searchList("/search/movie", query, new TypeReference<>() {});
    }

    public List<TmdbTV> searchTV(String query) {
        return searchList("/search/tv", query, new TypeReference<>() {});
    }

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

    private String buildDetailUrl(String path) {
        return UriComponentsBuilder.fromHttpUrl(props.getBaseUrl() + path)
            .queryParam(API_KEY_PARAM, props.getApiKey())
            .queryParam(LANGUAGE_PARAM, "en-US")
            .queryParam(APPEND_TO_RESPONSE, "credits,videos,images,external_ids")
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
        if ("/movie/now_playing".equals(path) || "/tv/on_the_air".equals(path) || "/movie/popular".equals(path) || "/movie/top_rated".equals(path) || "/movie/upcoming".equals(path)) {
            url += "&page=" + page;
        }
        return url;
    }

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