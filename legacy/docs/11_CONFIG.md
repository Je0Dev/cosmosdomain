# Configuration (`config/`)

Application configuration settings.

## ApplicationConfig

```java
public class ApplicationConfig {
    private static ApplicationConfig instance;
    
    public String getAppTitle();
    public String getVersion();
    // ... other config methods
}
```

Provides centralized configuration for the application.