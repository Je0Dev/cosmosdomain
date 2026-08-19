package com.papel.imdb_clone.model.rating;

import java.time.LocalDateTime;

/**
 * Represents a detailed user rating and review for a movie or series.
 */
public class UserRating {

    private int id; //id of the user rating
    private final int userId; // user id of the user who gave the rating
    private final int contentId; // content id of the content that was rated
    private double rating; // 1.0-10.0
    private String title; // title of the review
    private LocalDateTime createdAt; // created at is the time the rating was created
    private LocalDateTime updatedAt; // updated at is the time the rating was updated

    /**
     * Constructor for UserRating
     * @param userId The user's ID
     * @param contentId The content's ID
     * @param rating The user's rating
     */
    public UserRating(int userId, int contentId, double rating) {
        this.userId = userId;
        this.contentId = contentId;
        setRating(rating);
        this.createdAt = LocalDateTime.now();
    }


    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getRating() {
        return rating;
    }

    //if rating is between 1.0 and 10.0, set it to that value, else throw exception
    public void setRating(double rating) {
        if (rating >= 1.0 && rating <= 10.0) {
            this.rating = Math.round(rating * 10) / 10.0; // Round to 1 decimal place
        } else {
            throw new IllegalArgumentException("Rating must be between 1.0 and 10.0");
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    @Override
    public String toString() {
        return "UserRating{" +
                "id=" + id +
                ", userId=" + userId +
                ", contentId=" + contentId +
                ", rating=" + rating +
                ", title='" + title + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getContentId() {
        return contentId;
    }

}