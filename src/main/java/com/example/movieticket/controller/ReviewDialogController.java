package com.example.movieticket.controller;

import com.example.movieticket.model.Review;
import com.example.movieticket.service.DataService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class ReviewDialogController implements Initializable {

    @FXML private ComboBox<String> reviewTypeComboBox;
    @FXML private ToggleButton star1, star2, star3, star4, star5;
    @FXML private Label ratingLabel;
    @FXML private TextField titleField;
    @FXML private TextArea commentArea;
    @FXML private Label charCountLabel;
    @FXML private Button submitButton;
    @FXML private Button cancelButton;

    private Stage dialogStage;
    private boolean okClicked = false;
    private int currentRating = 0;
    private final DataService dataService = DataService.getInstance();
    private Review currentReview; // Track if we're editing an existing review

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set default selection
        reviewTypeComboBox.getSelectionModel().select(0);

        // Add character counter for comment area
        commentArea.textProperty().addListener((observable, oldValue, newValue) -> {
            int length = newValue.length();
            charCountLabel.setText(length + " characters");
        });

        // Initialize stars
        updateStarDisplay(0);
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    /**
     * Set the review to edit (null for new review)
     */
    public void setReview(Review review) {
        this.currentReview = review;

        if (review != null) {
            // Populate fields with existing review data
            titleField.setText(review.getTitle());
            commentArea.setText(review.getComment());
            updateStarDisplay(review.getRating());

            // Set review type
            if (review.getReviewType() != null) {
                switch (review.getReviewType()) {
                    case THEATER_EXPERIENCE:
                        reviewTypeComboBox.setValue("Theater Experience");
                        break;
                    case MOVIE_REVIEW:
                        reviewTypeComboBox.setValue("Movie Review");
                        break;
                    case SERVICE_FEEDBACK:
                        reviewTypeComboBox.setValue("Service Feedback");
                        break;
                }
            }

            // Disable user selection for editing (review already belongs to a user)
            reviewTypeComboBox.setDisable(false);
        }
    }

    @FXML
    private void handleStarRating() {
        // Determine which star was clicked and set the rating accordingly
        // If clicking the same star again, deselect (set to 0)
        int newRating = 0;

        if (star5.isSelected()) {
            newRating = (currentRating == 5) ? 0 : 5;
        } else if (star4.isSelected()) {
            newRating = (currentRating == 4) ? 0 : 4;
        } else if (star3.isSelected()) {
            newRating = (currentRating == 3) ? 0 : 3;
        } else if (star2.isSelected()) {
            newRating = (currentRating == 2) ? 0 : 2;
        } else if (star1.isSelected()) {
            newRating = (currentRating == 1) ? 0 : 1;
        }

        updateStarDisplay(newRating);
    }

    private void updateStarDisplay(int rating) {
        currentRating = rating;

        // Update star buttons
        star1.setText(rating >= 1 ? "⭐" : "☆");
        star2.setText(rating >= 2 ? "⭐" : "☆");
        star3.setText(rating >= 3 ? "⭐" : "☆");
        star4.setText(rating >= 4 ? "⭐" : "☆");
        star5.setText(rating >= 5 ? "⭐" : "☆");

        // Update selection state
        star1.setSelected(rating >= 1);
        star2.setSelected(rating >= 2);
        star3.setSelected(rating >= 3);
        star4.setSelected(rating >= 4);
        star5.setSelected(rating >= 5);

        // Update label
        if (rating > 0) {
            String[] ratingTexts = {"", "Poor", "Fair", "Good", "Very Good", "Excellent"};
            ratingLabel.setText("(" + rating + "/5 - " + ratingTexts[rating] + ")");
            ratingLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
        } else {
            ratingLabel.setText("(Select a rating)");
            ratingLabel.setStyle("-fx-text-fill: #7f8c8d;");
        }
    }

    @FXML
    private void handleSubmit() {
        if (isInputValid()) {
            Review review;
            boolean isNewReview = (currentReview == null);

            if (isNewReview) {
                // Creating a new review
                review = new Review();
                review.setUserId(dataService.getCurrentUser().getUserId());
            } else {
                // Editing an existing review
                review = currentReview;
            }

            review.setRating(currentRating);
            review.setTitle(titleField.getText().trim());
            review.setComment(commentArea.getText().trim());

            if (isNewReview) {
                review.setReviewDate(LocalDateTime.now());
            }

            // Set review type
            String selectedType = reviewTypeComboBox.getValue();
            if (selectedType.equals("Theater Experience")) {
                review.setReviewType(Review.ReviewType.THEATER_EXPERIENCE);
            } else if (selectedType.equals("Movie Review")) {
                review.setReviewType(Review.ReviewType.MOVIE_REVIEW);
            } else {
                review.setReviewType(Review.ReviewType.SERVICE_FEEDBACK);
            }

            // Save or update review
            boolean success;
            if (isNewReview) {
                success = dataService.addReview(review);
            } else {
                success = dataService.updateReview(review);
            }

            if (success) {
                okClicked = true;
                String message = isNewReview ?
                    "Thank you for sharing your experience! Your review has been submitted successfully." :
                    "Your review has been updated successfully.";
                showAlert(Alert.AlertType.INFORMATION, "Success",
                         isNewReview ? "Review Submitted" : "Review Updated", message);
                dialogStage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error",
                         isNewReview ? "Submission Failed" : "Update Failed",
                         "Failed to " + (isNewReview ? "submit" : "update") + " your review. Please try again.");
            }
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        StringBuilder errorMessage = new StringBuilder();

        if (currentRating == 0) {
            errorMessage.append("Please select a rating!\n");
        }

        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            errorMessage.append("Please enter a title!\n");
        } else if (titleField.getText().trim().length() < 3) {
            errorMessage.append("Title must be at least 3 characters!\n");
        }

        if (commentArea.getText() == null || commentArea.getText().trim().isEmpty()) {
            errorMessage.append("Please share your experience!\n");
        } else if (commentArea.getText().trim().length() < 10) {
            errorMessage.append("Comment must be at least 10 characters!\n");
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please correct the following:", errorMessage.toString());
            return false;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.initOwner(dialogStage);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
