# Entry Points

This document explains the application entry points.

## Main Entry Point

### StartApplication.java
```java
public static void main(String[] args) {
    // Launches the JavaFX application
    Application.launch(MovieAppGui.class, args);
}
```

This is the main entry point that launches the JavaFX application.

### MovieAppGui.java
The JavaFX Application class that:
1. Initializes the configuration via `ApplicationConfig`
2. Sets up the `ServiceLocator`
3. Loads all data via `DataManager`
4. Shows the login screen

## Data Loading Flow

```
MovieAppGui.initialize()
    → ServiceLocator.getInstance()
    → DataManager.getInstance().loadAllData()
        → MovieDataLoader.load()
        → SeriesDataLoader.load()
        → ActorDataLoader.load()
        → DirectorDataLoader.load()
        → UserDataLoader.load()
```

## User Authentication Flow

```
Login Screen (AuthController.fxml)
    → AuthController.handleLogin()
    → AuthService.login(username, password)
    → Validates credentials
    → Creates session token
    → Navigates to Main Screen
```

## Navigation Flow

```
User clicks menu item
    → NavigationService.navigate(screenName)
    → Loads FXML via ServiceLocator
    → Creates new controller instance
    → Shows in main stage
```

All screen navigation goes through `NavigationService` which manages scene transitions.