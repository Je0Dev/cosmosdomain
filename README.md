# CosmosDomain

[![Java](https://img.shields.io/badge/Java-21+-red?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.x-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![TMDB](https://img.shields.io/badge/TMDB_API-v3-01D277?logo=themoviedatabase)](https://www.themoviedb.org/)
[![Maven](https://img.shields.io/badge/Maven-3.9.x-C71A36?logo=apachemaven)](https://maven.apache.org/)
A personal library — your universe of movies, series, actors, and directors.

> **Built with Spring Boot and TMDB.**


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
| **Favorites** | Add movies, series, actors, and directors to your personal favorites list |
| **Advanced Search** | Multi-criteria search across movies, series, and people |

 >Note: Some features are still in progress
---

## Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 21+ | Language |
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

## Getting Started

### Prerequisites

- Java 21+ (JDK, not JRE — needed for `javac`)
- The TMDB API key is pre-configured in `application.yml` (or set `TMDB_API_KEY` env var).

### Build & Run

```bash
# Ensure Java is available(21+)
export JAVA_HOME=/path/to/jdk-25

# Build
./mvnw clean compile

# Run tests
./mvnw test

# Run the app (http://localhost:8080)
./mvnw spring-boot:run
```

The app starts immediately with an embedded H2 database. The H2 console is available at `http://localhost:8080/h2-console` (username `sa`, no password).

---

### Testing

| Username | Password | Role |
|---|---|---|
| `testuser` | `test123` | User |
| `admin` | `admin123` | Admin |

---


## License

MIT - [LICENSE](LICENSE).

Copyright © 2026 Je0Dev
