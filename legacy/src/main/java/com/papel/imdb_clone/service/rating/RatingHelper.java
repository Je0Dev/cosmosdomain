package com.papel.imdb_clone.service.rating;

import com.papel.imdb_clone.model.content.Content;
import com.papel.imdb_clone.model.rating.Rating;

import java.util.*;
import java.util.stream.Collectors;

public class RatingHelper {

    private static final double MIN_RATING = 0.0;
    private static final double MAX_RATING = 10.0;

    public boolean isValidRating(double rating) {
        return rating >= MIN_RATING && rating <= MAX_RATING;
    }

    public boolean isValidRating(int rating) {
        return rating >= MIN_RATING && rating <= MAX_RATING;
    }

    public double clampRating(double rating) {
        return Math.max(MIN_RATING, Math.min(MAX_RATING, rating));
    }

    public double calculateAverageRating(List<Rating> ratings) {
        if (ratings == null || ratings.isEmpty()) {
            return 0.0;
        }
        double sum = ratings.stream()
            .mapToDouble(Rating::getRating)
            .sum();
        return sum / ratings.size();
    }

    public Map<Integer, Double> getRatingsByUser(Map<Integer, List<Rating>> userRatings) {
        Map<Integer, Double> averages = new HashMap<>();
        for (Map.Entry<Integer, List<Rating>> entry : userRatings.entrySet()) {
            averages.put(entry.getKey(), calculateAverageRating(entry.getValue()));
        }
        return averages;
    }

    public List<Content> sortByRating(List<Content> items, boolean descending) {
        List<Content> sorted = new ArrayList<>(items);
        if (descending) {
            sorted.sort((a, b) -> Double.compare(b.getImdbRating(), a.getImdbRating()));
        } else {
            sorted.sort((a, b) -> Double.compare(a.getImdbRating(), b.getImdbRating()));
        }
        return sorted;
    }

    public List<Content> filterByRatingRange(List<Content> items, double minRating, double maxRating) {
        return items.stream()
            .filter(c -> c.getImdbRating() >= minRating && c.getImdbRating() <= maxRating)
            .collect(Collectors.toList());
    }

    public String getRatingStars(double rating) {
        int fullStars = (int) (rating / 2);
        boolean halfStar = (rating % 2) >= 1;
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < fullStars; i++) {
            stars.append("*");
        }
        if (halfStar && fullStars < 5) {
            stars.append("*");
        }
        return stars.toString();
    }
}