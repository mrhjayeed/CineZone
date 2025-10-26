package com.example.movieticket.model;

import java.time.LocalDateTime;

public class Review {
    private int reviewId;
    private int userId;
    private String userName;
    private int rating; // 1-5 stars
    private String title;
    private String comment;
    private LocalDateTime reviewDate;
    private ReviewType reviewType;

    public enum ReviewType {
        THEATER_EXPERIENCE,
        MOVIE_REVIEW,
        SERVICE_FEEDBACK
    }

    public Review() {
        this.reviewDate = LocalDateTime.now();
    }

    public Review(int reviewId, int userId, String userName, int rating, String title,
                  String comment, LocalDateTime reviewDate, ReviewType reviewType) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.userName = userName;
        this.rating = rating;
        this.title = title;
        this.comment = comment;
        this.reviewDate = reviewDate;
        this.reviewType = reviewType;
    }

    // Getters and Setters
    public int getReviewId() { return reviewId; }
    public void setReviewId(int reviewId) { this.reviewId = reviewId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getReviewDate() { return reviewDate; }
    public void setReviewDate(LocalDateTime reviewDate) { this.reviewDate = reviewDate; }

    public ReviewType getReviewType() { return reviewType; }
    public void setReviewType(ReviewType reviewType) { this.reviewType = reviewType; }

    public String getStars() {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < rating) {
                stars.append("⭐");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }
}

