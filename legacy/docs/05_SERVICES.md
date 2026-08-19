# Services (`service/`)

The service folder contains business logic classes. Services act as intermediaries between controllers and repositories.

## Folder Structure

```
service/
├── content/                   # Content services
│   ├── BaseContentService.java
│   ├── ContentService.java    # Interface
│   ├── MoviesService.java
│   └── SeriesService.java
├── data/                     # Data loading
│   ├── base/
│   │   ├── BaseDataLoader.java
│   │   ├── DataLoaderService.java
│   │   └── FileDataLoaderService.java
│   └── loader/
│       ├── DataLoaderFactory.java
│       ├── AwardsDataLoader.java
│       ├── content/
│       │   ├── MovieDataLoader.java
│       │   └── SeriesDataLoader.java
│       └── people/
│           ├── ActorDataLoader.java
│           ├── DirectorDataLoader.java
│           └── UserDataLoader.java
├── navigation/
│   └── NavigationService.java
├── people/                   # People services
│   ├── CelebrityManager.java
│   ├── CelebrityService.java
│   ├── UserService.java
│   └── UserStorageService.java
├── rating/
│   ├── RatingService.java
│   └── RatingHelper.java      # Helper class
├── search/
│   ├── ServiceLocator.java   # Service registry (singleton)
│   ├── SearchService.java
│   └── SearchCriteriaHelper.java  # Helper class
├── validation/
│   ├── AuthService.java
│   ├── AuthValidationHelper.java  # Helper class
│   └── UserInputValidator.java
└── util/
    └── (see util folder)
```

## Service Layer Pattern

Services follow singleton pattern:
1. Private constructor
2. Static `getInstance()` method
3. Static field holding single instance

Example:
```java
public class MoviesService extends BaseContentService<Movie> {
    private static volatile MoviesService instance;
    
    private MoviesService() {
        super(Movie.class);
    }
    
    public static MoviesService getInstance() {
        if (instance == null) {
            synchronized(MoviesService.class) {
                if (instance == null) {
                    instance = new MoviesService();
                }
            }
        }
        return instance;
    }
}
```

## Content Services

### MoviesService
- CRUD operations for movies
- File loading from `movies_updated.txt`
- Thread-safe operations with locks

### SeriesService  
- CRUD for TV series
- Season/episode management

## Data Loading Services

### DataLoaderFactory
Factory for creating appropriate data loaders based on content type.

### BaseDataLoader (abstract)
- Base class for all data loaders
- Provides `getResourceAsStream()` to load files
- File path resolution logic

### *DataLoader classes
Each loads specific data type from files:
- MovieDataLoader → movies
- SeriesDataLoader → series
- ActorDataLoader → actors
- DirectorDataLoader → directors

## Navigation Service

### NavigationService
Manages screen navigation:
- Loads FXML files
- Creates controllers
- Manages scene transitions

```java
NavigationService.getInstance().navigate("movies-view");
```

## People Services

### UserService
- User registration/login
- Session management

### CelebrityService
- Actor/Director management
- Search functionality

## Rating Services

### RatingService
- User ratings management
- Rating calculations
- Rating updates

## Search Services

### ServiceLocator (Important!)
Central service registry. All services register here.

```java
ServiceLocator.getInstance().getMoviesService();
ServiceLocator.getInstance().getSearchService();
```

### SearchService
- Search across movies/series
- Criteria building and filtering

## Validation Services

### AuthService
- User authentication
- Session tokens
- Login attempts tracking (throttling)

## Helper Classes

Helper classes extract complex logic:
- `AuthValidationHelper` - validation logic
- `SearchCriteriaHelper` - search filtering
- `RatingHelper` - rating calculations