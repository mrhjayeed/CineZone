package com.example.movieticket.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Movie {
    private int movieId;
    private String title;
    private String director;
    private int releaseYear;
    private String genre;
    private String description;
    private int duration; // in minutes
    private double rating;
    private String posterUrl; // New field for poster image
    private String trailerUrl; // URL for movie trailer

    public Movie() {}

    public Movie(int movieId, String title, String director, int releaseYear, String genre, String description, int duration, double rating) {
        this.movieId = movieId;
        this.title = title;
        this.director = director;
        this.releaseYear = releaseYear;
        this.genre = genre;
        this.description = description;
        this.duration = duration;
        this.rating = rating;
        this.posterUrl = ""; // Default empty poster URL
        this.trailerUrl = ""; // Default empty trailer URL
    }

    public Movie(int movieId, String title, String director, int releaseYear, String genre, String description, int duration, double rating, String posterUrl) {
        this.movieId = movieId;
        this.title = title;
        this.director = director;
        this.releaseYear = releaseYear;
        this.genre = genre;
        this.description = description;
        this.duration = duration;
        this.rating = rating;
        this.posterUrl = posterUrl;
        this.trailerUrl = ""; // Default empty trailer URL
    }

    // Getters and Setters
    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public int getReleaseYear() { return releaseYear; }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public String getTrailerUrl() { return trailerUrl; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }

    // Helper method to get genres as a list (split by comma)
    public List<String> getGenreList() {
        if (genre == null || genre.trim().isEmpty()) {
            return Arrays.asList();
        }
        return Arrays.stream(genre.split(","))
                .map(String::trim)
                .filter(g -> !g.isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return title + " (" + releaseYear + ")";
    }
}
