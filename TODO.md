# TODO

## Local database integration (deferred)
- Wire the currently unused local catalog entities and repositories into the app: `Movie`, `TVSeries`, `Person`, `Genre`, `Season`, `Episode`, plus `MovieRepository`, `TVSeriesRepository`, `PersonRepository`, and `GenreRepository`.
- Choose an integration strategy: write-through cache with DB fallback, persist favorites only, or a full local mirror.
- Add `SeasonRepository` and `EpisodeRepository` if seasons and episodes will be persisted directly.
- Populate the local tables from TMDB detail/list responses and map genres, cast, crew, seasons, and episodes.
- Use the local DB as a fallback when TMDB is unavailable.
- Consider rendering favorites from local data instead of refetching TMDB on every request.
- Decide whether the existing `V2`–`V8` catalog tables should stay until they are actually used.

## Remaining dead code and cleanup
- Remove or integrate `TmdbMovie.youtubeTrailerKey` and `TmdbTV.youtubeTrailerKey`; they are not standard TMDB response fields.
- Remove or display unused `TmdbPersonResult` fields: `overview`, `gender`, `originCountry`, and `department`.
- Remove or use `TmdbResponse` paging fields: `page`, `totalResults`, and `totalPages`.
- Remove or display unused fields: `TmdbSeason.id`, `TmdbSeason.overview`, `TmdbSeasonDetail.id`, `TmdbEpisode.voteCount`, and `TmdbCombinedCredits.crew`.
- Stop requesting `images` and `external_ids` in `append_to_response` until those objects are mapped and displayed.

## Fetched-but-not-displayed details
- Show a full filmography from `combined_credits` on person pages; only the top 8 “Known For” titles are shown now.
- Show episode vote counts and guest stars; guest stars are not mapped yet.
- Show season overviews on the series page or season cards.
- Add genre filtering or clickable genre links.
- Add production companies, networks, countries, and languages if mapped from TMDB.
- Use `totalPages` and `totalResults` for real pagination.

## Pagination
- Return paging metadata from `TmdbClient` instead of discarding it.
- Fix `buildListUrl` so `/trending/tv/week` honors the page parameter.
- Add pagination to search results; `searchList` currently always requests page 1.
- Add previous/next or numbered pagination using `totalPages`.

## UI and templates
- Add image placeholders when `posterPath` or `profilePath` is null; search and list pages currently build broken image URLs.
- Style or remove remaining unstyled classes such as `runtime`, `year`, and `remove-favorite`.
- Add loading and error states for TMDB failures.
- Make the favorites “Remove” button match the detail-page favorite button styling.
- Add a dedicated error-page test for 404 and 500 responses.

## Security and configuration
- Move the hardcoded TMDB API key out of `application.yml`, require `TMDB_API_KEY`, and rotate the exposed key.
- Re-enable CSRF protection and update the fetch-based favorite toggle to send the CSRF token.
- Implement roles and authorities, or remove the README claim that `admin` has an Admin role.
- Review whether the H2 console should remain enabled outside the `dev` profile.

## Documentation
- Update the README feature list; “Latest Releases” is no longer shown on the home page.
- Document the new season and episode browsing routes.
- Document TMDB API key configuration and profile usage.

## Tests
- Add controller/template smoke tests for home, catalog, movie detail, TV detail, season detail, person detail, search, and favorites.
- Add `TmdbClient` mapping tests for person `combined_credits`, `media_type`, and empty release dates.
- Add tests for favorite toggle add/remove behavior.
- Run tests with `./mvnw test`; offline mode cannot resolve the Surefire plugin.
