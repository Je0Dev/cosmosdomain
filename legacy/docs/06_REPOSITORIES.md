# Repository (`repository/`)

The repository folder contains the data access layer. Repositories provide methods to access and manipulate data.

## Folder Structure

```
repository/
├── MovieRepository.java           # Interface for movies
├── SeriesRepository.java        # Interface for series
├── UserRepository.java         # Interface for users
├── CelebritiesRepository.java   # Interface for celebrities
└── impl/                     # Implementations
    ├── InMemoryMovieRepository.java
    ├── InMemorySeriesRepository.java
    ├── InMemoryUserRepository.java
    └── InMemoryCelebritiesRepository.java
```

## Repository Pattern

Repositories follow the Repository Pattern, providing a collection-like interface for data access:

```java
public interface MovieRepository {
    List<Movie> findAll();
    Movie findById(int id);
    void save(Movie movie);
    void delete(int id);
}
```

## Interfaces

### MovieRepository
- `findAll()` - Get all movies
- `findByTitle(String title)` - Search by title
- `findByYear(int year)` - Search by year
- `save(Movie movie)` - Add/update movie
- `delete(int id)` - Remove movie

### SeriesRepository
- `findAll()` - Get all series
- `findById(int id)` - Get by ID
- `save(Series series)` - Add/update series

### UserRepository
- `findByUsername(String username)` - Find user
- `save(User user)` - Add/update user
- `delete(int id)` - Remove user

## In-Memory Implementations

All current implementations are in-memory (stored in Lists/HashMaps).

### InMemoryMovieRepository
- Uses `List<Movie>` internally
- Simple find/save/delete operations

### InMemorySeriesRepository
- Uses `List<Series>` internally
- Manages series, seasons, episodes

### InMemoryUserRepository
- Uses `Map<String, User>` by username
- Fast lookup

### InMemoryCelebritiesRepository
- Manages actors and directors
- Search functionality

## Usage in Services

Services use repositories:

```java
public class MoviesService extends BaseContentService<Movie> {
    private final MovieRepository repository;
    
    public List<Movie> getAll() {
        return repository.findAll();
    }
}
```

Repository implementations can be swapped (e.g., for database implementation in the future).