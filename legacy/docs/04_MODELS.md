# Models (`model/`)

The model folder contains all domain model classes representing the application data.

## Folder Structure

```
model/
├── content/              # Content models
│   ├── Content.java     # Base content class
│   ├── Movie.java
│   ├── Series.java
│   ├── Season.java
│   └── Episode.java
├── people/             # People models
│   ├── User.java
│   ├── Actor.java
│   ├── Director.java
│   └── Celebrity.java   # Base celebrity class
└── rating/            # Rating models
    ├── Rating.java
    └── UserRating.java
```

## Content Models

### Content.java (Base Class)
Base class for all content types (movies, series).
- Contains common fields: title, id, releaseDate, synopsis, director, ratings
- Implements basic functionality shared by all content types

### Movie.java
Extends `Content`. Represents a movie.
- Additional fields: boxOffice, awards, genres, duration
- Related to: List<Actor>, double imdbRating

### Series.java
Extends `Content`. Represents a TV series.
- Contains: List<Season>, genre(s)
- Has methods to manage seasons/episodes

### Season.java
Represents a season within a series.
- Contains: seasonNumber, title, List<Episode>
- Episode count calculated from episodes list

### Episode.java
Represents an individual episode.
- Contains: episodeNumber, title, duration, releaseDate

## People Models

### User.java
Represents a registered user.
- Fields: username, email, passwordHash, userId
- User ratings stored as Map<Integer, Integer>

### Celebrity.java (Base Class)
Base class for celebrities (actors, directors).
- Common fields: firstName, lastName, birthDate, gender, ethnicity, notableWorks

### Actor.java
Extends `Celebrity`. Represents an actor.
- Additional fields: role (in specific movie/series)

### Director.java
Extends `Celebrity`. Represents a director.
- Factory method: `getInstance()` (singleton pattern used)

## Rating Models

### Rating.java
Represents a rating.
- Fields: rating (double), userId, contentId

### UserRating.java
Extends Rating. Contains user-specific rating info.
- Additional user rating value

## Key Relationships

```
Content (Movie/Series)
  ├── hasMany: Actor (via List<Actor>)
  ├── hasMany: Rating (via ratings map)
  └── belongsTo: Genre

User
  ├── hasMany: UserRating
  └── hasMany: Content (rated content)

Series
  ├── hasMany: Season
  └── Season hasMany: Episode
```

All models use standard Java getter/setter patterns.