# AGENTS.md

CosmosDomain is a Spring Boot 3.5.x web application (Thymeleaf + H2 + TMDB API).
Dev machine is Fedora 44 with Java 25 (JDK) installed at
`/usr/lib/jvm/java-25-openjdk`. Maven 3.9.9 is at `/home/geomastro/maven`.
The app runs here via `DISPLAY=:0` if a browser is available, otherwise via
`http://localhost:8080`.

## Commands

- Build: `./mvnw clean compile` (uses Maven wrapper; Maven 3.9.9 at
  /home/geomastro/maven; JDK at /home/geomastro/jdk-25.0.4+7)
- Tests: `./mvnw test`
- Run app: `./mvnw spring-boot:run` (serves at http://localhost:8080)
- Package: `./mvnw clean package -DskipTests`
- Full verify: `./mvnw clean verify`
- `target/`, `data/` are gitignored staging areas
- **IMPORTANT:** Set `JAVA_HOME` before building:
  `export JAVA_HOME=/home/geomastro/jdk-25.0.4+7`

## Must-follow rules (full text: CONSTRAINTS.md, CONTRIBUTING.md)

- Never commit to `main`. Every change goes on its own branch (`feat/`,
  `test/`, `fix/`, `docs/`, `chore/`), merged with `--no-ff`.
- **Do not merge to or push `main` unless the user explicitly says "merge".**
  Otherwise push the branch and leave it for the user to approve.
- **One feature at a time**: test first (write test, see it fail, implement
  until green), then verify build + tests, then commit and move on.
- Conventional Commits; update `CHANGELOG.md` (Unreleased) with each push.
- Files <= 100-120 lines; no monolithic methods; no `TODO`; comment *why*;
  build + tests green before merge.
- Tests are for **really important features** (auth logic, ratings,
  persistence, TMDB client), not for everything implemented. Small glue
  helpers need no tests.

## Architecture

- `src/main/java/com/cosmosdomain/` - all Java sources under this package:
  - `config/` - SecurityConfig, ApplicationConfig, TmdbProperties
  - `controller/` - HomeController, AuthController, CatalogController,
    SeriesController, PersonController, SearchController,
    LocalMediaController, MediaPlayerController, FavoritesController
  - `dto/` - TMDB API response DTOs (TmdbMovie, TmdbTV, TmdbPersonResult, etc.)
  - `entity/` - JPA entities (User, Movie, TVSeries, Season, Episode,
    Person, Genre, Credit, Favorite, Rating, LocalMedia, Subtitle)
  - `repository/` - Spring Data JPA repositories
  - `service/` - AuthService, CatalogService, TmdbClient, SearchService,
    FavoritesService, RatingService, LocalMediaService, SubtitleService
- `src/main/resources/` - config + templates:
  - `application.yml` - main config (H2, TMDB, Flyway)
  - `db/migration/` - Flyway SQL migrations (V1__ ... V10__)
  - `templates/` - Thymeleaf pages (layout, home, search, movie, series,
    person, auth, library, player, favorites)
  - `static/` - CSS, JS, images, brand assets
- `target/` - Maven build output
- `data/` - H2 database file (`cosmosdomain.mv.db`)

## Gotchas

- TMDB API key must be set as env var `TMDB_API_KEY` or in `application.yml`.
  The key is `d9d6a0b80584c6c12fdcc041bc430ee5` — never commit it directly.
- H2 is file-based: DB lives in `./data/cosmosdomain.mv.db`. The console is at
  `/h2-console` (username `sa`, empty password).
- H2 SQL dialect differs slightly from PostgreSQL/MySQL; use portable SQL in
  migrations and avoid H2-specific features if migrating to Postgres later.
- Flyway manages schema (`spring.jpa.hibernate.ddl-auto=none`); never change
  entity fields without a corresponding migration script.
- TMDB image URLs are external CDN links (`image.tmdb.org`); they require
  network access. Missing paths return fallback local image URLs.
- Local media files are served via HTTP Range requests for streaming. Ensure
  `media-dir` in `application.yml` points to a valid directory.

## Repo state

- Branded as **CosmosDomain** (new identity, migrated from IMDB Clone).
- Start new work from `main`.