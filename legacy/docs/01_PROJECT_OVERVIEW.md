# Project Architecture

This document provides an overview of the project structure and how the different packages work together.

## Technology Stack
- **Framework**: JavaFX 21 (Desktop UI)
- **Language**: Java 21
- **Build Tool**: Maven
- **Architecture**: MVC with Service Locator pattern

## Directory Structure

```
src/main/java/com/papel/imdb_clone/
├── config/              # Application configuration
├── controllers/        # MVC Controllers (UI logic)
├── data/              # Data facade
├── enums/             # Enumerations
├── exceptions/         # Custom exceptions
├── gui/               # JavaFX Application
├── model/             # Domain models
├── repository/         # Data access layer
├── service/           # Business logic
├── util/              # Utility classes
└── StartApplication.java  # Main entry point
```

## Flow of Control

```
StartApplication.main()
    → MovieAppGui.start()
        → ServiceLocator.initialize()
        → DataManager.loadAllData()
        → Load FXML views
            → Controllers handle UI events
                → Services contain business logic
                    → Repositories access data
```

This architecture follows the standard MVC pattern with additional service and repository layers for separation of concerns.