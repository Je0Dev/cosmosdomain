# Utilities (`util/`)

The util folder contains utility classes used throughout the application.

## Folder Structure

```
util/
├── UIUtils.java
├── PasswordHasher.java
├── FileUtils.java
└── DataFileLoader.java
```

## UIUtils

Helper methods for JavaFX UI operations:
```java
UIUtils.showAlert(AlertType, title, message);
UIUtils.showError(title, message);
UIUtils.showInfo(title, message);
```

## PasswordHasher

Password hashing using jBCrypt:
```java
String hash = PasswordHasher.hash(password);
boolean matches = PasswordHasher.verify(password, hash);
```

## FileUtils

File operations:
```java
List<String> lines = FileUtils.readLines(filename);
```

## DataFileLoader

Loads resources from classpath:
```java
InputStream is = DataFileLoader.getResourceAsStream(filename);
```

Used by BaseDataLoader.