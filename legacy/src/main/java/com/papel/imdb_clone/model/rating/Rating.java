package com.papel.imdb_clone.model.rating;

import java.time.LocalDateTime;

/**
 * Represents a rating.
 */
public class Rating {

    private int id; //id of the rating
    private int userId; //id of the user who gave the rating
    private int contentId; //id of the content that was rated
    private double score; //score of the rating
    private String review;//review of the rating
    private LocalDateTime createdAt; //when the rating was created
    private LocalDateTime updatedAt; //when the rating was updated


    /**
     * Constructor for rating creation
     * @param id id of the rating
     * @param rating score of the rating
     */
    public Rating(int id, double rating) {
        this.id = id;
        this.score = rating;
        this.review = "";
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Rating constructor for rating creation
     * @param userId user id
     * @param contentId content id
     * @param score score of the rating
     */
    public Rating(int userId, int contentId, double score) {
        this.userId = userId;
        this.contentId = contentId;
        this.score = score;
        this.createdAt = LocalDateTime.now();
        this.review = "";
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setScore(double score) {
        if (score >= 0 && score <= 10) {
            this.score = score;
        }
    }

    public double getRating() {
        return score;
    }

    public void setRating(int rating) {
        setScore(rating);
    }


    @Override
    public String toString() {
        return "Rating{" +
                "id=" + id +
                ", userId=" + userId +
                ", contentId=" + contentId +
                ", score=" + score +
                ", review='" + review + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

}