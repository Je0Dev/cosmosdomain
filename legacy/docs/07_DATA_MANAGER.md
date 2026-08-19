# Data Manager (`data/`)

The data folder contains the DataManager class, which acts as a facade for all data operations.

## Folder Structure

```
data/
└── DataManager.java
```

## DataManager

The DataManager is the central data facade that provides access to all services.

### Responsibilities
1. Loads all data at application startup
2. Provides access to services
3. Coordinates data operations

### Key Methods

```java
public class DataManager {
    // Services access
    public MoviesService getMoviesService();
    public SeriesService getSeriesService();
    public UserService getUserService();
    
    // Data access
    public List<Movie> getAllMovies();
    public List<Series> getAllSeries();
}
```

## Data Loading Flow

```
DataManager.loadAllData()
    → moviesDataLoader.load()
    → seriesDataLoader.load()
    → actorDataLoader.load()
    → directorDataLoader.load()
    → userDataLoader.load()
```

## Singleton Pattern

DataManager uses singleton pattern:

```java
private static DataManager instance;

public static DataManager getInstance() {
    if (instance == null) {
        instance = new DataManager();
    }
    return instance;
}
```

## Initialization

```java
public void loadAllData() {
    logger.info("Starting data loading...");
    initializeDataLoaders();
    loadData();
    logger.info("Data loading completed");
}
```

All data is loaded in memory at application startup.