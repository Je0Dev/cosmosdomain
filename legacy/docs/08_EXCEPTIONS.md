# Exceptions (`exceptions/`)

The exceptions folder contains custom exception classes for the application.

## Folder Structure

```
exceptions/
├── AuthException.java
├── AuthErrorType.java
├── ValidationException.java
├── InvalidInputException.java
├── InvalidEntityException.java
├── EntityNotFoundException.java
├── DuplicateEntryException.java
├── UserAlreadyExistsException.java
├── DataPersistenceException.java
└── FileParsingException.java
```

## Exception Categories

### Authentication Exceptions

#### AuthException
Main authentication exception. Uses AuthErrorType enum.

```java
public AuthException(String message, AuthErrorType type) {
    super(message);
}
```

#### AuthErrorType Enum
Types of auth errors:
- INVALID_CREDENTIALS
- USER_NOT_FOUND
- USER_ALREADY_EXISTS
- SESSION_EXPIRED
- INTERNAL_ERROR

### Validation Exceptions

#### ValidationException
Base validation exception.

#### InvalidInputException
For invalid user input.

#### InvalidEntityException  
When an entity is invalid.

### Data Exceptions

#### EntityNotFoundException
When entity lookup fails.

#### DuplicateEntryException
When trying to add duplicate.

#### UserAlreadyExistsException
Specific to user registration.

#### DataPersistenceException
For data save/load errors.

#### FileParsingException
For file format errors.

## Usage

```java
try {
    userService.register(username, email, password);
} catch (UserAlreadyExistsException e) {
    showAlert("Registration Failed", "Username already exists");
}
```