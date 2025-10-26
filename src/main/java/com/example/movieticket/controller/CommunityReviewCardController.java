package com.example.movieticket.controller;

import com.example.movieticket.model.Review;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class CommunityReviewCardController implements Initializable {

    @FXML private Label userNameLabel;
    @FXML private Label reviewDateLabel;
    @FXML private Label typeBadgeLabel;
    @FXML private Label ratingLabel;
    @FXML private Label titleLabel;
    @FXML private Label commentLabel;

    private Review review;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialization if needed
    }

    public void setReview(Review review) {
        this.review = review;
        updateReviewData();
    }

    private void updateReviewData() {
        if (review == null) {
            return;
        }

        // Set user name
        userNameLabel.setText("👤 " + review.getUserName());

        // Set review date
        if (review.getReviewDate() != null) {
            reviewDateLabel.setText(review.getReviewDate().format(DATE_FORMATTER));
        }

        // Set type badge
        if (review.getReviewType() != null) {
            typeBadgeLabel.setText(review.getReviewType().toString().replace("_", " "));

            // Set badge color based on type
            String badgeColor = switch (review.getReviewType()) {
                case THEATER_EXPERIENCE -> "#3498db";
                case MOVIE_REVIEW -> "#e74c3c";
                case SERVICE_FEEDBACK -> "#27ae60";
            };

            typeBadgeLabel.setStyle("-fx-background-color: " + badgeColor +
                "; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; " +
                "-fx-padding: 4 10; -fx-background-radius: 12;");
        }

        // Set rating stars
        ratingLabel.setText(review.getStars());

        // Set title
        titleLabel.setText(review.getTitle());

        // Set comment
        commentLabel.setText(review.getComment());
    }

    public Review getReview() {
        return review;
    }
}

