-- V12__remove_ratings.sql
-- Drop the unused app-owned ratings table. Ratings shown in the UI come from
-- TMDB's vote_average and are not persisted locally.

DROP TABLE IF EXISTS ratings;
