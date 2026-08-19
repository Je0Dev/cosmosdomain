# IMDB Clone Desktop App

A JavaFX desktop application that clones some of the most core IMDb functionality such as: browsing movies, TV series, and celebrities with search, ratings, and user authentication.


---

## Features

| Category | Details |
|---|---|
| **Content Browsing** | Browse, search, filter, and sort movies and TV series with full metadata |
| **Celebrity Profiles** | Actor and director profiles with filmography and biographical data |
| **User Authentication** | Registration and login with BCrypt password hashing and session management |
| **Rating System** | Rate content on a 1–10 scale with per-user tracking |
| **Advanced Search** | Multi-criteria search across movies, series, and people (title, genre, year range, rating range) |
| **CRUD Operations** | Full create, read, update, delete for movies, series, seasons, episodes, actors, directors |
| **Data Import** | Parses structured TXT data files at startup into in-memory storage |
| **Nested Content** | Series with multiple seasons, each containing episodes |

---

## Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 21 | Language |
| JavaFX | 21.0.3 | Desktop UI framework |
| Maven | 3.8+ | Build tool and dependency management |
| ControlsFX | 11.2.1 | Enhanced JavaFX UI components |
| Jackson | 2.17.1 | JSON serialization |
| jBCrypt | 0.4 | Password hashing (BCrypt) |
| Apache Commons Text | 1.11.0 | String utilities |
| SLF4J + Simple | 2.0.13 | Logging |
| JUnit 5 | 5.10.2 | Unit testing |
| PMD | 3.22.0 | Static code analysis |

All dependencies are managed via `pom.xml` with Maven. The `javafx-maven-plugin` handles JavaFX runtime and module configuration.

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+

### Build & Run

```bash
git clone https://github.com/Je0Dev/ImdbCloneApp.git
cd ImdbCloneApp
mvn clean compile          # Compile
mvn exec:java              # Run (uses exec-maven-plugin with UserDataRegenerator)
mvn clean compile exec:java  # Build & run in one step
mvn test                   # Run tests
```

The compiler is configured with `-Xlint:all` for full warnings and deprecation checks.

---

## Project Structure

```
ImdbCloneApp/
├── data/                        # Runtime serialized user data (user_data.ser)
├── docs/                        # Detailed architecture documentation (12 markdown files)
├── images/                      # Application screenshots
├── src/
│   ├── main/
│   │   ├── java/com/papel/imdb_clone/
│   │   │   ├── StartApplication.java    # Entry point (main method)
│   │   │   ├── module-info.java          # Java module descriptor
│   │   │   ├── config/                  # ApplicationConfig.java
│   │   │   ├── controllers/             # MVC controllers (UI logic)
│   │   │   │   ├── authentication/      # Login/Register
│   │   │   │   ├── content/             # Movies, Series, helpers
│   │   │   │   ├── coordinator/         # Screen navigation
│   │   │   │   ├── people/              # Celebrities
│   │   │   │   └── search/              # Search controllers
│   │   │   ├── data/                    # DataManager facade
│   │   │   ├── enums/                   # Genre, ContentType, Ethnicity
│   │   │   ├── exceptions/              # 10 custom exception classes
│   │   │   ├── gui/                     # MovieAppGui (JavaFX Application class)
│   │   │   ├── model/                   # Domain models
│   │   │   │   ├── content/             # Movie, Series, Season, Episode
│   │   │   │   ├── people/              # Actor, Director, Celebrity, User
│   │   │   │   └── rating/              # Rating, UserRating
│   │   │   ├── repository/              # Data access interfaces + in-memory impls
│   │   │   ├── service/                 # Business logic layer
│   │   │   │   ├── content/             # MoviesService, SeriesService
│   │   │   │   ├── data/               # Data loading framework (loaders)
│   │   │   │   ├── navigation/          # NavigationService
│   │   │   │   ├── people/              # UserService, CelebrityService
│   │   │   │   ├── rating/              # RatingService
│   │   │   │   ├── search/             # SearchService, ServiceLocator
│   │   │   │   └── validation/          # AuthService, validation helpers
│   │   │   ├── tools/                   # UserDataRegenerator
│   │   │   └── util/                    # PasswordHasher, FileUtils, UIUtils
│   │   └── resources/
│   │       ├── application.properties   # App config
│   │       ├── logback.xml              # Logging config
│   │       ├── data/                    # TXT data files
│   │       │   ├── content/             # movies_updated.txt, series_updated.txt
│   │       │   ├── nominations/         # awards_boxoffice_updated.txt
│   │       │   └── people/              # actors, directors, users txt files
│   │       └── fxml/                    # JavaFX FXML view layouts
│   │           ├── auth/                # login-view.fxml, register-view.fxml
│   │           ├── base/                # home-view.fxml
│   │           ├── celebrities/         # celebrities-view.fxml
│   │           ├── content/             # movie-view, series-view, etc.
│   │           └── search/              # search-form, advanced-search, results-table
└── pom.xml                              # Maven build configuration
```

Every directory contains its own `README.md` with a detailed breakdown of its contents.

---

## Architecture

### Layered MVC Pattern

```
┌─────────────────────────────────────────────────────────┐
│                    FXML Views (resources/fxml/)          │
│                    (login, movies, series, search, etc.) │
└─────────────────────┬───────────────────────────────────┘
                      │ @FXML injection
┌─────────────────────▼───────────────────────────────────┐
│              Controllers (controllers/)                  │
│    Handle UI events, coordinate views ↔ services         │
│    Extend BaseController or BaseSearchController          │
└─────────────────────┬───────────────────────────────────┘
                      │ method calls
┌─────────────────────▼───────────────────────────────────┐
│               Services (service/)                       │
│    Business logic, data processing, validation          │
│    Singleton pattern via ServiceLocator                  │
└─────────────────────┬───────────────────────────────────┘
                      │ calls
┌─────────────────────▼───────────────────────────────────┐
│           Repositories (repository/)                    │
│    Data access interfaces + in-memory implementations    │
│    Repository pattern for abstraction                    │
└─────────────────────┬───────────────────────────────────┘
                      │ stores/retrieves
┌─────────────────────▼───────────────────────────────────┐
│              Models (model/) + Data files               │
│    Domain objects + TXT file data sources                │
└─────────────────────────────────────────────────────────┘
```

### Application Startup Flow

```
StartApplication.main()
  └─ Application.launch(MovieAppGui.class)
       └─ MovieAppGui.start()
            ├─ ApplicationConfig.initialize()        — load config properties
            ├─ ServiceLocator.getInstance()           — register all services
            ├─ DataManager.getInstance().loadAllData() — parse TXT files
            │    ├─ MovieDataLoader.load()
            │    ├─ SeriesDataLoader.load()
            │    ├─ ActorDataLoader.load()
            │    ├─ DirectorDataLoader.load()
            │    └─ UserDataLoader.load()
            └─ NavigationService.navigate("login")   — show login screen
```

### Service Locator Pattern

Dependency management uses a custom `ServiceLocator` (singleton registry) rather than a DI framework:

```java
ServiceLocator.getInstance().getMoviesService();
ServiceLocator.getInstance().getSearchService();
ServiceLocator.getInstance().getAuthService();
```

All services follow the lazy-initialized double-checked locking singleton pattern.

---

## Package Breakdown

### `config/` — Application Configuration
Single class `ApplicationConfig.java` manages file paths, data source locations, and runtime settings. Loaded once at startup by `MovieAppGui`.

### `controllers/` — UI Controllers
All controllers extend `BaseController` (providing `DataManager` access, `showAlert()`, `showError()`, `showConfirmationDialog()`). Search-related controllers extend `BaseSearchController` for shared search service access.

- **`authentication/AuthController.java`** — Login form, registration form, session management via `AuthService`
- **`content/MoviesController.java`** (~1390 lines) — Movie table, CRUD dialogs, filtering, sorting. Uses `MoviesDialogHelper` and `MoviesLogicHelper` for extracted logic
- **`content/SeriesController.java`** (~1875 lines) — Series with nested seasons/episodes table, CRUD. Uses `SeriesDialogHelper` and `SeriesLogicHelper`
- **`content/EditContentController.java`** — Generic content editing form with type parameterization
- **`coordinator/UICoordinator.java`** — Manages screen transitions and UI state
- **`people/CelebritiesController.java`** (~1587 lines) — Actor/director listing and management
- **`search/AdvancedSearchController.java`** (~1008 lines) — Multi-criteria advanced search form
- **`search/SearchFormController.java`** — Basic search with title, genre, year, rating filters

### `model/` — Domain Objects
- **`Content.java`** — Abstract base with title, id, releaseDate, synopsis, ratings map
- **`Movie.java`** — Extends Content: boxOffice, awards, genres, duration, cast
- **`Series.java`** — Extends Content: seasons list, genre(s)
- **`Season.java`** / **`Episode.java`** — Nested content hierarchy
- **`Celebrity.java`** — Abstract base for people (firstName, lastName, birthDate, ethnicity)
- **`Actor.java`** / **`Director.java`** — Specific celebrity types
- **`User.java`** — Account model with username, email, passwordHash, ratings map
- **`Rating.java`** / **`UserRating.java`** — Rating value objects

### `service/` — Business Logic
- **`content/MoviesService.java`** / **`SeriesService.java`** — CRUD operations, thread-safe with locks
- **`data/base/FileDataLoaderService.java`** — Parses `|`-delimited TXT files from classpath resources
- **`data/loader/*/`** — Specific loaders for movies, series, actors, directors, users
- **`people/UserStorageService.java`** — User persistence via Java serialization (`user_data.ser`)
- **`rating/RatingService.java`** — Per-user rating storage, average calculations
- **`search/SearchService.java`** — Composite search across all content types
- **`validation/AuthService.java`** (~724 lines) — Login, registration, BCrypt verification, session tokens

### `repository/` — Data Access
Interfaces define the contract; `impl/` contains in-memory implementations using `List`/`Map` collections. Designed for easy swapping (e.g., to a database-backed implementation).

### `exceptions/` — Custom Error Types
10 exception classes organized by category: authentication (`AuthException`), validation (`ValidationException`, `InvalidInputException`), data (`EntityNotFoundException`, `DataPersistenceException`, `FileParsingException`), and domain-specific (`UserAlreadyExistsException`, `DuplicateEntryException`).

### `util/` — Utilities
- **`PasswordHasher.java`** — BCrypt hash/verify wrapper
- **`DataFileLoader.java`** — Classpath resource loading
- **`FileUtils.java`** — Line-based file reading
- **`UIUtils.java`** — Alert and dialog helpers

---

## Data Flow

### Search Flow
```
User types in search field
  └─ SearchFormController.handleSearch()
       └─ SearchService.search(criteria)
            ├─ MoviesService.search(title)     — search movies
            ├─ SeriesService.search(title)     — search series
            └─ CelebrityService.search(name)   — search actors/directors
                 └─ ResultsTableController displays results
```

### Rating Flow
```
User clicks rate button
  └─ MoviesController.handleRate(movie)
       └─ RatingService.rateMovie(userId, movieId, rating)
            └─ UserRating stored in User.ratings map
                 └─ UI refreshes to show updated average
```

### Authentication Flow
```
Login form submit
  └─ AuthController.handleLogin()
       └─ AuthService.login(username, password)
            ├─ UserRepository.findByUsername(username)
            ├─ PasswordHasher.verify(password, user.passwordHash)
            └─ Returns session token → navigate to main screen
```

---

## Data File Format

Data files in `src/main/resources/data/` use a pipe-delimited (`|`) format. Each file is parsed by a dedicated loader class:

| File | Loader | Entity |
|---|---|---|
| `content/movies_updated.txt` | `MovieDataLoader` | `Movie` |
| `content/series_updated.txt` | `SeriesDataLoader` | `Series` |
| `people/actors_updated.txt` | `ActorDataLoader` | `Actor` |
| `people/directors_updated.txt` | `DirectorDataLoader` | `Director` |
| `people/users_updated.txt` | `UserDataLoader` | `User` |
| `nominations/awards_boxoffice_updated.txt` | `AwardsDataLoader` | Awards data |

User sessions persist between runs via Java serialization to `data/user_data.ser`.

---

## Refactoring Notes

This project contains some large controller files that are candidates for further refactoring:

| File | Lines | Status |
|---|---|---|
| `SeriesController.java` | ~1875 | Partial helper extraction |
| `CelebritiesController.java` | ~1587 | Has dialog helper |
| `MoviesController.java` | ~1390 | Using helpers |
| `AdvancedSearchController.java` | ~1008 | Needs splitting |
| `AuthService.java` | ~724 | Large service |

Helper classes (`*DialogHelper`, `*LogicHelper`) have been extracted from the largest controllers following a pattern of keeping `@FXML`-bound fields in the controller while moving business logic to helper classes.

---

## License

This project is free to use for **non-commercial, educational, and personal purposes**. You may fork, modify, and share it for non-profit use. Commercial use requires permission. Mostly follows the *MIT* Licence

Copyright © 2026 Je0Dev

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

## Contributing

 Areas that need attention (but will propably wont get that much imporvements):
- Further splitting of large files and removing dead code snippets
- Implementing tests for all major functioanlity to make everything be more maintanable and scalable in the future.
- Creating new meaningful features such as watchlists, sharing capabilities to improve engagement and a lot more to add them here.

> See the [`docs/`](docs/) directory for comprehensive architecture documentation before contributing. Every source directory also contains a `README.md` with its specific contents and purpose.

---

*Last updated: June 2026*
