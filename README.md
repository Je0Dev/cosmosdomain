# CosmosDomain

[![Java](https://img.shields.io/badge/Java-21+-red?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.x-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-005F0F?logo=thymeleaf)](https://www.thymeleaf.org/)
[![H2](https://img.shields.io/badge/H2_Database-2.3-blue?logo=h2)](https://h2database.com/)
[![Flyway](https://img.shields.io/badge/Flyway-10.x-red?logo=flyway)](https://flywaydb.org/)
[![TMDB](https://img.shields.io/badge/TMDB_API-v3-01D277?logo=themoviedatabase)](https://www.themoviedb.org/)
[![Maven](https://img.shields.io/badge/Maven-3.9.x-C71A36?logo=apachemaven)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

A personal film & TV library web app — your universe of movies, series, actors, and directors. Browse real data from The Movie Database (TMDB), store your favorites, and play your own downloaded media files with integrated subtitles.

> **Built with Spring Boot 3.5, Thymeleaf, H2, and the TMDB API.**
> Rewritten from a JavaFX IMDB Clone into a modern web application.

---

## Features

| Category | Details |
|---|---|
| **Catalog Browsing** | Browse, search, filter, and sort movies and TV series with full metadata |
| **Celebrity Profiles** | Actor and director profiles with filmography and biographical data |
| **Latest Releases** | Real-time latest releases, now playing, and popular content from TMDB |
| **Banners & Trailers** | Full backdrop banners and integrated YouTube trailers from TMDB |
| **Cast Lists** | See the full cast of any movie or series, with character names |
| **User Authentication** | Registration and login with BCrypt password hashing and sessions |
| **Rating System** | Rate content on a 1–10 scale with per-user tracking and averages |
| **Favorites** | Add movies, series, actors, and directors to your personal favorites list |
| **Advanced Search** | Multi-criteria search across movies, series, and people |
| **Local Media** | Add your own downloaded movies and series; stream them through the app |
| **Media Player** | Integrated HTML5 video player with subtitle support |
| **Subtitle Support** | Upload SRT files; auto-converted to WebVTT for the player |

 >Note: The last three features are not yet fully completed (19.8.26 update)
---

## Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 21+ (tested on 25) | Language |
| Spring Boot | 3.5.x | Web framework |
| Thymeleaf | 3.x | Template engine |
| Spring Security | 6.5.x | Authentication & authorization |
| Spring Data JPA | 3.5.x | Database ORM |
| H2 | 2.3 | Embedded file-based database |
| Flyway | 10.x | Database migrations |
| TMDB API | v3 | Movie/series metadata |
| Jackson | 2.17+ | JSON processing |
| Spring Cache | 3.5.x | Caching abstraction |
| Maven | 3.9.x | Build tool |

---

## Project Structure

```
cosmosdomain/
├── pom.xml                         # Maven build configuration
├── mvnw                            # Maven wrapper
├── .mvn/                           # Maven wrapper config
├── src/
│   ├── main/
│   │   ├── java/com/cosmosdomain/
│   │   │   ├── CosmosDomainApplication.java      # Entry point
│   │   │   ├── config/                            # SecurityConfig, ApplicationConfig, TmdbProperties
│   │   │   ├── controller/                        # Web controllers
│   │   │   ├── dto/                               # TMDB API response DTOs
│   │   │   ├── entity/                            # JPA entities
│   │   │   ├── repository/                        # Spring Data JPA repositories
│   │   │   └── service/                           # Business logic + TMDB client
│   │   └── resources/
│   │       ├── application.yml                    # Main config
│   │       ├── db/migration/                      # Flyway SQL migrations (V1__..V10__)
│   │       ├── templates/                         # Thymeleaf pages
│   │       └── static/                            # CSS, JS, images, brand assets
│   └── test/                                      # Unit tests (important features)
├── data/                                          # H2 database file (gitignored)
└── legacy/                                        # Original JavaFX IMDB Clone source
```

---

## Getting Started

### Prerequisites

- Java 21+ (JDK, not JRE — needed for `javac`)
- The TMDB API key is pre-configured in `application.yml` (or set `TMDB_API_KEY` env var).

### Build & Run

```bash
# Ensure Java is available
export JAVA_HOME=/path/to/jdk-25

# Build
./mvnw clean compile

# Run tests
./mvnw test

# Run the app (serves at http://localhost:8080)
./mvnw spring-boot:run
```

The app starts immediately with an embedded H2 database. The H2 console is available at `http://localhost:8080/h2-console` (username `sa`, no password).

---

## Default Test Users (dev profile)

| Username | Password | Role |
|---|---|---|
| `testuser` | `test123` | User |
| `admin` | `admin123` | Admin |

---

## Documentation

- [PLAN.md](PLAN.md) - Implementation plan and roadmap
- [CONSTRAINTS.md](CONSTRAINTS.md) - Code & workflow constraints
- [CONTRIBUTING.md](CONTRIBUTING.md) - Branching, commits, merge process
- [CHANGELOG.md](CHANGELOG.md) - Release history
- [AGENTS.md](AGENTS.md) - Guidance for AI coding agents

---

## License

MIT - see [LICENSE](LICENSE).

Copyright © 2026 CosmosDomain contributors
