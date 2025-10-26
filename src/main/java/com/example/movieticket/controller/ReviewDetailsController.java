package com.example.movieticket.controller;

import com.example.movieticket.model.Review;
import com.example.movieticket.model.User;
import com.example.movieticket.service.DataService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ReviewDetailsController {

    @FXML private Label reviewIdLabel;
    @FXML private Label reviewTypeLabel;
    @FXML private Label reviewDateLabel;
    @FXML private Label userIdLabel;
    @FXML private Label userNameLabel;
    @FXML private Label ratingStarsLabel;
    @FXML private Label ratingNumberLabel;
    @FXML private Label reviewTitleLabel;
    @FXML private Label reviewCommentLabel;

    private final DataService dataService = DataService.getInstance();
    private Stage dialogStage;
    private Review review;
    private Runnable onDeleteCallback;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setReview(Review review) {
        this.review = review;
        populateReviewDetails();
    }

    public void setOnDeleteCallback(Runnable callback) {
        this.onDeleteCallback = callback;
    }

    private void populateReviewDetails() {
        if (review == null) {
            return;
        }

        // Review Information
        reviewIdLabel.setText(String.valueOf(review.getReviewId()));

        if (review.getReviewType() != null) {
            String typeText = review.getReviewType().name().replace("_", " ");
            reviewTypeLabel.setText(typeText);

            // Set color based on type
            switch (review.getReviewType()) {
                case THEATER_EXPERIENCE:
                    reviewTypeLabel.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");
                    break;
                case MOVIE_REVIEW:
                    reviewTypeLabel.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");
                    break;
                case SERVICE_FEEDBACK:
                    reviewTypeLabel.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");
                    break;
            }
        }

        if (review.getReviewDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
            reviewDateLabel.setText(review.getReviewDate().format(formatter));
        }

        // User Information
        userIdLabel.setText(String.valueOf(review.getUserId()));

        User user = dataService.getUserById(review.getUserId());
        if (user != null) {
            userNameLabel.setText(user.getFullName() + " (@" + user.getUsername() + ")");
        } else {
            userNameLabel.setText(review.getUserName() != null ? review.getUserName() : "Unknown User");
        }

        // Rating
        ratingStarsLabel.setText(review.getStars());
        ratingNumberLabel.setText(review.getRating() + ".0 / 5.0");

        // Review Content
        reviewTitleLabel.setText(review.getTitle() != null ? review.getTitle() : "No Title");
        reviewCommentLabel.setText(review.getComment() != null ? review.getComment() : "No comment provided.");
    }

    @FXML
    private void handleClose() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    @FXML
    private void handleDeleteReview() {
        if (review == null) {
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Review");
        confirmAlert.setHeaderText("Are you sure you want to delete this review?");
        confirmAlert.setContentText("Review ID: " + review.getReviewId() + "\nTitle: " + review.getTitle() + "\n\nThis action cannot be undone.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = dataService.deleteReview(review.getReviewId());

                if (deleted) {
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Review deleted successfully!");
                    successAlert.showAndWait();

                    // Call the callback to refresh the reviews table
                    if (onDeleteCallback != null) {
                        onDeleteCallback.run();
                    }

                    handleClose();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Failed to delete the review. Please try again.");
                    errorAlert.showAndWait();
                }
            } catch (Exception e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("An error occurred");
                errorAlert.setContentText("Error: " + e.getMessage());
                errorAlert.showAndWait();
            }
        }
    }
}

