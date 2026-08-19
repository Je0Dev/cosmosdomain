# Controllers (`controllers/`)

The controllers folder contains all MVC controller classes that handle UI interaction and coordinate between views and services.

## Folder Structure

```
controllers/
├── BaseController.java           # Base class for all controllers
├── MainController.java          # Main window controller
├── authentication/             # Login/Register controllers
│   └── AuthController.java
├── content/                 # Content controllers (Movies/Series)
│   ├── MoviesController.java
│   ├── SeriesController.java
│   ├── EditContentController.java
│   ├── ContentDetailsController.java
│   ├── MoviesDialogHelper.java      # Helper for movie dialogs
│   └── SeriesDialogHelper.java    # Helper for series dialogs
├── coordinator/
│   └── UICoordinator.java     # UI coordination
├── people/                  # Celebrity controllers
│   ├── CelebritiesController.java
│   └── CelebritiesDialogHelper.java
└── search/                 # Search controllers
    ├── BaseSearchController.java
    ├── SearchFormController.java
    ├── AdvancedSearchController.java
    ├── SearchCriteria.java
    └── ResultsTableController.java
```

## BaseController

All controllers extend `BaseController` which provides:
- `DataManager` access
- `showAlert()`, `showError()`, `showSuccess()` methods
- `showConfirmationDialog()` for yes/no dialogs

```java
public abstract class BaseController implements Initializable {
    protected DataManager dataManager;
    
    protected void showAlert(String title, String message);
    protected void showError(String title, String message);
    protected void showSuccess(String title, String message);
    protected boolean showConfirmationDialog(String title, String message);
}
```

## How Controllers Work

1. **FXML Loading**: JavaFX loads FXML files and injects `@FXML` annotated fields
2. **Initialization**: `initialize()` method is called after FXML fields are injected
3. **Event Handling**: Methods annotated with `@FXML` handle UI events

## Content Controllers

### MoviesController
- Displays list of movies in table
- Handles Add/Edit/Delete movie operations
- Manages movie search and filtering

### SeriesController
- Displays TV series with seasons/episodes
- Manages series, seasons, and episodes
- Complex nested table structure

### EditContentController / ContentDetailsController
- Forms for editing content
- Details view for viewing content

## Search Controllers

### BaseSearchController
- Base class providing search service access

### SearchFormController
- Main search form UI
- Title, genres, rating range, year range filters

### AdvancedSearchController
- More advanced search options
- Multiple filter combinations

### ResultsTableController
- Displays search results in table format

## Authentication Controllers

### AuthController
- Login form
- Registration form
- Session management