package com.papel.imdb_clone.service.rating;

import com.papel.imdb_clone.model.rating.UserRating;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing user ratings and reviews.
 */
public class RatingService {

    private static final Logger logger = LoggerFactory.getLogger(RatingService.class);
    private static RatingService instance; // Singleton instance of RatingService
    private final Map<Integer, UserRating> ratings = new ConcurrentHashMap<>(); //contains all ratings
    private final Map<Integer, List<Integer>> userRatings = new ConcurrentHashMap<>(); //contains all ratings for a specific user
    private final Map<Integer, List<Integer>> contentRatings = new ConcurrentHashMap<>();//contains all ratings for a specific content
    private int nextRatingId = 1;

    private RatingService() {
        // Private constructor for singleton which means only one instance of RatingService can exist
        loadRatings();
    }
    
    /**
     * Loads ratings from the data file
     */
    private void loadRatings() {
        try {
            // Try to load from serialized file first which means loading from the file
            File file = new File("ratings.ser");
            if (file.exists()) {
                // Try to load from serialized file first which means loading from the file
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                    @SuppressWarnings("unchecked")
                    List<UserRating> loadedRatings = (List<UserRating>) ois.readObject();
                    
                    // Clear existing data
                    ratings.clear();
                    userRatings.clear();
                    contentRatings.clear();
                    
                    // Repopulate the maps
                    for (UserRating rating : loadedRatings) {
                        int ratingId = rating.getId();
                        int userId = rating.getUserId();
                        int contentId = rating.getContentId();
                        
                        // Add to ratings map
                        ratings.put(ratingId, rating);
                        
                        // Update user ratings index
                        userRatings.computeIfAbsent(userId, k -> new ArrayList<>()).add(ratingId);
                        
                        // Update content ratings index
                        contentRatings.computeIfAbsent(contentId, k -> new ArrayList<>()).add(ratingId);
                        
                        // Update nextRatingId if needed
                        if (ratingId >= nextRatingId) {
                            nextRatingId = ratingId + 1;
                        }
                    }
                    
                    logger.info("Successfully loaded {} ratings from file", loadedRatings.size());
                }
            } else {
                logger.info("No existing ratings file found, starting with empty ratings");
            }
        } catch (Exception e) {
            logger.error("Error loading ratings: {}", e.getMessage(), e);
            // Continue with empty ratings if loading fails
            ratings.clear();
            userRatings.clear();
            contentRatings.clear();
        }
    }

    // Singleton pattern implementation for RatingService
    public static synchronized RatingService getInstance() {
        if (instance == null) {
            instance = new RatingService();
        }
        return instance;
    }


    /**
     * Creates a new rating for content.
     * @param userId The ID of the user creating the rating
     * @param contentId The ID of the content being rated
     * @param ratingValue The rating score (1-10)
     * @return The created UserRating
     * @throws IllegalArgumentException if the rating value is invalid
     * @throws IllegalStateException if the user has already rated this content
     * @throws RuntimeException if there's an error creating the rating
     */
    public UserRating createRating(int userId, int contentId, double ratingValue) {
        try {
            logger.debug("Creating new rating - User: {}, Content: {}, Rating: {}", userId, contentId, ratingValue);
            validateRating(ratingValue);
            
            // Check if user has already rated this content
            UserRating existingRating = getUserRating(userId, contentId);
            if (existingRating != null) {
                String errorMsg = String.format("User %d has already rated content %d", userId, contentId);
                logger.warn(errorMsg);
                throw new IllegalStateException("User has already rated this content");
            }
            
            int ratingId = nextRatingId++;
            // Create new rating with the double value
            UserRating rating = new UserRating(userId, contentId, ratingValue);
            rating.setId(ratingId);
            
            // Store the rating
            ratings.put(ratingId, rating);
            
            // Update user and content indices
            userRatings.computeIfAbsent(userId, k -> new ArrayList<>()).add(ratingId);
            contentRatings.computeIfAbsent(contentId, k -> new ArrayList<>()).add(ratingId);
            
            logger.info("Successfully created rating ID: {} for user: {} and content: {}", 
                ratingId, userId, contentId);
            return rating;
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Re-throw validation/state exceptions
            throw e;
        } catch (Exception e) {
            String errorMsg = String.format("Failed to create rating for user %d and content %d: %s", 
                userId, contentId, e.getMessage());
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }
    
    /**
     * Updates an existing rating.
     *
     * @param ratingId  The ID of the rating to update
     * @param newRating The new rating score (1.0-10.0)
     * @throws IllegalArgumentException if the new rating value is invalid
     * @throws NoSuchElementException if the rating with the specified ID is not found
     * @throws RuntimeException if there's an error updating the rating
     */
    public void updateRating(int ratingId, double newRating) {
        try {
            logger.debug("Updating rating ID: {} with new rating: {}", ratingId, newRating);
            validateRating(newRating);
            
            UserRating rating = ratings.get(ratingId);
            if (rating == null) {
                String errorMsg = String.format("Rating with ID %d not found", ratingId);
                logger.warn(errorMsg);
                throw new NoSuchElementException("Rating not found");
            }
            
            rating.setRating(newRating);
            logger.info("Successfully updated rating ID: {} with new rating: {}", ratingId, newRating);
            
        } catch (IllegalArgumentException | NoSuchElementException e) {
            // Re-throw validation/not found exceptions
            throw e;
        } catch (Exception e) {
            String errorMsg = String.format("Failed to update rating ID %d: %s", ratingId, e.getMessage());
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    
    /**
     * Deletes a rating.
     *
     * @param ratingId The ID of the rating to delete
     * @throws RuntimeException if there's an error deleting the rating
     */
    public void deleteRating(int ratingId) {
        try {
            logger.debug("Deleting rating ID: {}", ratingId);
            UserRating rating = ratings.remove(ratingId);
            if (rating != null) {
                // Remove from user index
                List<Integer> userRatingsList = userRatings.get(rating.getUserId());
                if (userRatingsList != null) {
                    userRatingsList.remove(Integer.valueOf(ratingId));
                }
                
                // Remove from content index
                List<Integer> contentRatingsList = contentRatings.get(rating.getContentId());
                if (contentRatingsList != null) {
                    contentRatingsList.remove(Integer.valueOf(ratingId));
                }
                
                logger.info("Successfully deleted rating ID: {}", ratingId);
                return;
            }
            logger.warn("Rating ID {} not found for deletion", ratingId);

        } catch (Exception e) {
            String errorMsg = String.format("Failed to delete rating ID %d: %s", 
                ratingId, e.getMessage());
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }
    
    /**
     * Gets a rating by ID.
     * @param ratingId The ID of the rating to retrieve
     * @return The UserRating
     * @throws NoSuchElementException if the rating with the specified ID is not found
     * @throws RuntimeException if there's an error retrieving the rating
     */
    public UserRating getRating(int ratingId) {
        try {
            logger.debug("Retrieving rating ID: {}", ratingId);
            UserRating rating = ratings.get(ratingId);
            if (rating == null) {
                String errorMsg = String.format("Rating with ID %d not found", ratingId);
                logger.warn(errorMsg);
                throw new NoSuchElementException(errorMsg);
            }
            return rating;
        } catch (NoSuchElementException e) {
            // Re-throw not found exception
            throw e;
        } catch (Exception e) {
            String errorMsg = String.format("Error retrieving rating ID %d: %s", 
                ratingId, e.getMessage());
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }
    
    /**
     * Gets a user's rating for specific content.
     * @param userId The ID of the user
     * @param contentId The ID of the content
     * @return The UserRating, or null if not found
     * @throws IllegalArgumentException if userId or contentId is invalid
     * @throws RuntimeException if there's an error retrieving the user rating
     */
    public UserRating getUserRating(int userId, int contentId) {
        try {
            if (userId <= 0) {
                throw new IllegalArgumentException("Invalid user ID: " + userId);
            }
            if (contentId <= 0) {
                throw new IllegalArgumentException("Invalid content ID: " + contentId);
            }
            
            logger.debug("Retrieving rating for user: {} and content: {}", userId, contentId);
            List<Integer> userRatingIds = userRatings.getOrDefault(userId, Collections.emptyList());
            
            for (int ratingId : userRatingIds) {
                UserRating rating = ratings.get(ratingId);
                if (rating != null && rating.getContentId() == contentId) {
                    logger.debug("Found rating ID: {} for user: {} and content: {}", 
                        ratingId, userId, contentId);
                    return rating;
                }
            }
            
            logger.debug("No rating found for user: {} and content: {}", userId, contentId);
            return null;
            
        } catch (IllegalArgumentException e) {
            // Re-throw validation exceptions
            throw e;
        } catch (Exception e) {
            String errorMsg = String.format("Error retrieving rating for user %d and content %d: %s", 
                userId, contentId, e.getMessage());
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }
    
    /**
     * Gets all ratings by a specific user.
     * @param userId The ID of the user
     * @return A list of UserRatings by the user (never null, but may be empty)
     * @throws IllegalArgumentException if userId is invalid
     * @throws RuntimeException if there's an error retrieving the ratings
     */
    public List<UserRating> getRatingsByUser(int userId) {
        try {
            if (userId <= 0) {
                throw new IllegalArgumentException("Invalid user ID: " + userId);
            }
            
            logger.debug("Retrieving all ratings for user: {}", userId);
            List<UserRating> result = userRatings.getOrDefault(userId, Collections.emptyList())
                    .stream()
                    .map(ratings::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            logger.debug("Found {} ratings for user: {}", result.size(), userId);
            return result;
            
        } catch (IllegalArgumentException e) {
            // Re-throw validation exceptions
            throw e;
        } catch (Exception e) {
            String errorMsg = String.format("Error retrieving ratings for user %d: %s", 
                userId, e.getMessage());
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }
    
    /**
     * Gets all ratings for specific content.
     * @param contentId The ID of the content
     * @return A list of UserRatings for the content (never null, but may be empty)
     * @throws IllegalArgumentException if contentId is invalid
     * @throws RuntimeException if there's an error retrieving the ratings
     */
    public List<UserRating> getRatingsForContent(int contentId) {
        try {
            if (contentId <= 0) {
                throw new IllegalArgumentException("Invalid content ID: " + contentId);
            }
            
            logger.debug("Retrieving all ratings for content: {}", contentId);
            List<UserRating> result = contentRatings.getOrDefault(contentId, Collections.emptyList())
                    .stream()
                    .map(ratings::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            logger.debug("Found {} ratings for content: {}", result.size(), contentId);
            return result;
            
        } catch (IllegalArgumentException e) {
            // Re-throw validation exceptions
            throw e;
        } catch (Exception e) {
            String errorMsg = String.format("Error retrieving ratings for content %d: %s", 
                contentId, e.getMessage());
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }
    
    /**
     * Validates that a rating score is within the valid range.
     * @param rating The score to validate
     * @throws IllegalArgumentException if the score is invalid
     */
    private void validateRating(double rating) {
        if (rating < 1 || rating > 10) {
            String errorMsg = String.format("Invalid rating value: %.1f. Rating must be between 1 and 10", rating);
            logger.warn(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
    }
}
