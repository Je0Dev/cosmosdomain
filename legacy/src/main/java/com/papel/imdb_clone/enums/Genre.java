package com.papel.imdb_clone.enums;

//Enum for genre
public enum Genre {
    ACTION("Action"),
    COMEDY("Comedy"),
    DRAMA("Drama"),
    HORROR("Horror"),
    THRILLER("Thriller"),
    ROMANCE("Romance"),
    SCI_FI("Science Fiction"),
    FANTASY("Fantasy"),
    DOCUMENTARY("Documentary"),
    ANIMATION("Animation"),
    CRIME("Crime"),
    MYSTERY("Mystery"),
    ADVENTURE("Adventure"),
    BIOGRAPHY("Biography"),
    MUSICAL("Musical"),
    WESTERN("Western"),
    WAR("War"),
    FAMILY("Family"),
    SPORT("Sport"),
    HISTORY("History"),
    MUSIC("Music"),
    UNKNOWN("Unknown");


    private final String displayName;

    //Genre constructor with displayName
    Genre(String displayName) {
        this.displayName = displayName;
    }
    @Override
    public String toString() {
        return displayName;
    }

    public Object getDisplayName() {
        return displayName;
    }
}