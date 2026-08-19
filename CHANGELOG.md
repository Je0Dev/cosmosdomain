# Changelog

All notable changes to this project are documented in this file.
Format follows [Keep a Changelog](https://keepachangelog.com/) and this
project adheres to [Semantic Versioning](https://semver.org/).

## Unreleased

### Added

- CosmosDomain brand identity: new name, tagline, and project identity.
- Spring Boot 3.5.x scaffold with Thymeleaf, H2, and Flyway migrations.
- Database schema (V1–V10): users, people, movies, tv_series, seasons,
  episodes, genres, credits, favorites, ratings, local_media, subtitles.
- Maven wrapper (`mvnw`) with Java 21+ target and Java 25 compatibility.
- TMDB API key configuration via environment variable.
- Legacy folder for the original JavaFX IMDB Clone source.

### Changed

- Full rewrite from JavaFX desktop app to Spring Boot web application.
- Project identity renamed from "IMDB Clone" to "CosmosDomain".

### Removed

- Original JavaFX desktop UI, FXML views, and in-memory repositories
  (archived in `legacy/`).