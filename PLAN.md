# CosmosDomain — Plan

Rewrite of IMDB Clone Desktop App into a personal film & TV library web app.
Brand: **CosmosDomain** ("Your universe of film & TV").

## 🔑 TMDB API Key
- **Key:** `d9d6a0b80584c6c12fdcc041bc430ee5`
- Use via environment variable `TMDB_API_KEY` or `application.properties: tmdb.api-key=d9d6a0b80584c6c12fdcc041bc430ee5`
- Never hardcoded in source.

## Workflow rule (always)

- Implement **one feature at a time**: finish it (test + code + verify) before
  starting the next one.
- **Test first**: write the unit test, see it fail, then implement until green.
- Verify each feature: build clean, `./mvnw verify` green, `wc -l` <= 120 on
  every source file, zero `-Wall:all` / `-Xlint:all` warnings (where applicable).
- Commit each feature on its own, then move on. No commits to `main` unless the
  user explicitly says "merge". Use `git switch main` → pull → `git switch -c feat/<name>`.
- Conventional Commits: `feat:`, `test:`, `fix:`, `docs:`, `chore:`.
- Update `CHANGELOG.md` (Unreleased) with each push.

## Prerequisite

- Java 25 installed, Maven wrapper `./mvnw` used (Maven not globally installed).
- TMDB key set as env var: `export TMDB_API_KEY=d9d6a0b80584c6c12fdcc041bc430ee5`
- If desired, switch database from H2 to PostgreSQL via `application.yml`.

## Project overview

CosmosDomain is a Spring Boot 3.5.x web application (Thymeleaf + Bootstrap)
replacing the JavaFX desktop IMDB Clone. It stores data in an H2 file-based
database with JPA/Hibernate and Flyway migrations. It integrates TMDB for real
movie data (banners, cast, latest releases, YouTube trailers) and provides a
local-media player for user-downloaded files with subtitle support.

### Core data model (entities)

- `users` — username, email, password_hash, first/last name, role, active/locked,
  `created_at`/`updated_at`
- `people` — tmdb_id, name, birth_date, gender, profile_path, bio, department
- `movies` — tmdb_id, title, overview, release_date, runtime, poster_path,
  backdrop_path, vote_average/vote_count, tagline, budget, revenue
- `tv_series` — tmdb_id, name, overview, first_air_date/last_air_date, status,
  poster_path, backdrop_path
- `seasons` — series_id, season_number, name, air_date, poster_path
- `episodes` — season_id, episode_number, name, overview, air_date, runtime,
  still_path, vote_average
- `genres` + `movie_genres` / `series_genres` — genre lookup & join
- `credits` — person_id, media_type(MOVIE/SERIES), media_id, character/job,
  credit_type(CAST/CREW), sort_order
- `favorites` — user_id, media_type, media_id, created_at (unique per user+media)
- `ratings` — user_id, media_type, media_id, score 1–10, timestamps (unique per user+media)
- `local_media` — user_id, title, media_type(MOVIE/SERIES/EPISODE), optional
  catalog media_id, file_path, file_size, added_at, playback_position
- `subtitles` — local_media_id, language, label, file_path (WebVTT format)

### Feature set (carried + new)

**Carried from ImdbCloneApp:** register/login (BCrypt, Spring Security), browse/
search/filter/sort movies & series, advanced search, celebrity profiles with
filmography, per-user 1–10 ratings, aggregate averages, nested series→seasons→episodes.

**New — catalog intelligence (TMDB integration):**
- Real **latest releases / now playing / upcoming** with full metadata from TMDB
- **Banners/backdrops** from TMDB image CDN (`image.tmdb.org`)
- **Full cast list** for any movie (actor character names)
- **Actor/director filmography pages** with TMDB person data
- **YouTube trailers** embedded from TMDB `videos` endpoint (YouTube key)

**New — personal library:**
- **Favorites** — add movies/series/people to per-user favorites list
- **Local media** — add your downloaded movies/series/episodes from disk
- **HTML5 video player** with `<track>` WebVTT subtitle support
- **SRT → WebVTT** conversion service on upload
- **Resume playback** from last `playback_position`

### Markdown files (per reference conventions from freshiki, bananakong, taskmanager, student_bank)

| File | Content |
|---|---|
| `README.md` | CosmosDomain branding, features, quick start (`./mvnw spring-boot:run`), layout, docs links, license |
| `CONSTRAINTS.md` | Git workflow (no commits to `main`), merge approval, files ≤ 100–120 lines, no monolithic classes/functions, comment *why*, no TODO, tests for important features, clean build, style (Java/Spring conventions) |
| `CONTRIBUTING.md` | One feature at a time, test-first, branch naming (`feat/`,`test/`,`fix/`,`docs/`,`chore/`), Conventional Commits, pre-merge checklist, Keep-a-Changelog per push |
| `LICENSE` | MIT — "Copyright (c) 2026 CosmosDomain contributors" |
| `AGENTS.md` | Dev-machine notes (Java 25, `./mvnw`, TMDB key env, H2, commands, architecture, gotchas) |
| `CHANGELOG.md` | Keep a Changelog + SemVer, `Unreleased` section |
| `PLAN.md` | This plan with checkbox implementation order |

## Implementation order (test-first, one feature per commit)

1. **Scaffold** — Maven wrapper (`mvnw`), `pom.xml`, `application.yml`, Flyway + H2 config, package skeleton (`com.cosmosdomain`), all markdown files.
2. **Auth** — User entity/repo, UserDetailsService, BCrypt password, Spring Security config, login/register Thymeleaf pages, tests.
3. **Catalog core** — movie/series/season/episode/person/credit entities + JPA repos + schema init via Flyway, CRUD browse pages, seed data, tests.
4. **Search & browse** — basic + advanced search UI, sorting/filtering across types.
5. **Favorites & ratings** — per-user favorites + 1–10 ratings, average calculations, UI.
6. **TMDB integration** — `TmdbClient` via `RestClient`; popular/now playing/upcoming + details + credits + videos; banners/backdrops from CDN; YouTube trailer embed; caching with Caffeine/Spring Cache.
7. **Local media & player** — media dir config (`application.yml`), add-to-library page, `HttpRangeRequest`-based streaming endpoint, player page with `<video>` + WebVTT `<track>`, SRT→WebVTT converter, resume from `playback_position`.
8. **Brand polish** — dark cinematic theme, logo/banner, error pages (404/500), responsive layout, global error handling.
9. **Verify & ship** — `./mvnw clean verify` green, headless boot smoke test, run app `./mvnw spring-boot:run`, `wc -l` check on all files.

## Open items

- **Repo strategy:** create a new GitHub repo `CosmosDomain`, or rewrite in the existing `ImdbCloneApp` repo on a `feat/cosmosdomain-rewrite` branch?
- **TMDB key:** env var loaded at runtime; do not commit key to repo.
- **Logo/banner assets:** placeholder dark-themed branding for now; replace later.