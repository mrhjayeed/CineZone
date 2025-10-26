package com.example.movieticket.controller;

import com.example.movieticket.model.Review;
import com.example.movieticket.service.DataService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CommunityReviewsController implements Initializable {

    @FXML private Label reviewCountLabel;
    @FXML private VBox reviewsContainer;
    @FXML private VBox emptyStateBox;
    @FXML private ComboBox<String> typeFilterCombo;
    @FXML private ComboBox<String> ratingFilterCombo;

    private final DataService dataService = DataService.getInstance();
    private Stage dialogStage;
    private List<Review> allReviews;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeFilters();
        loadReviews();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    private void initializeFilters() {
        // Initialize type filter
        typeFilterCombo.getItems().addAll(
                "All Types",
                "THEATER EXPERIENCE",
                "MOVIE REVIEW",
                "SERVICE FEEDBACK"
        );
        typeFilterCombo.setValue("All Types");

        // Initialize rating filter
        ratingFilterCombo.getItems().addAll(
                "All Ratings",
                "5 Stars",
                "4 Stars",
                "3 Stars",
                "2 Stars",
                "1 Star"
        );
        ratingFilterCombo.setValue("All Ratings");

        // Add listeners to filters
        typeFilterCombo.setOnAction(e -> applyFilters());
        ratingFilterCombo.setOnAction(e -> applyFilters());
    }

    private void loadReviews() {
        try {
            allReviews = dataService.getAllReviews();
            applyFilters();
        } catch (Exception e) {
            System.err.println("Error loading community reviews: " + e.getMessage());
        }
    }

    private void applyFilters() {
        if (allReviews == null) {
            return;
        }

        List<Review> filteredReviews = allReviews.stream()
                .filter(this::matchesTypeFilter)
                .filter(this::matchesRatingFilter)
                .collect(Collectors.toList());

        displayReviews(filteredReviews);
    }

    private boolean matchesTypeFilter(Review review) {
        String selectedType = typeFilterCombo.getValue();
        if (selectedType == null || selectedType.equals("All Types")) {
            return true;
        }

        if (review.getReviewType() == null) {
            return false;
        }

        return review.getReviewType().name().replace("_", " ").equals(selectedType);
    }

    private boolean matchesRatingFilter(Review review) {
        String selectedRating = ratingFilterCombo.getValue();
        if (selectedRating == null || selectedRating.equals("All Ratings")) {
            return true;
        }

        // Extract the rating number from the selection (e.g., "5 Stars" -> 5)
        int filterRating = Integer.parseInt(selectedRating.split(" ")[0]);
        return review.getRating() == filterRating;
    }

    private void displayReviews(List<Review> reviews) {
        // Update review count
        reviewCountLabel.setText(reviews.size() + (reviews.size() == 1 ? " review" : " reviews"));

        if (reviews.isEmpty()) {
            // Show empty state
            emptyStateBox.setVisible(true);
            emptyStateBox.setManaged(true);

            // Clear existing review cards
            reviewsContainer.getChildren().removeIf(node -> node != emptyStateBox);
        } else {
            // Hide empty state
            emptyStateBox.setVisible(false);
            emptyStateBox.setManaged(false);

            // Clear any existing review cards (except empty state)
            reviewsContainer.getChildren().removeIf(node -> node != emptyStateBox);

            // Load review cards
            for (Review review : reviews) {
                try {
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("/com/example/movieticket/community-review-card.fxml"));
                    VBox reviewCard = loader.load();

                    CommunityReviewCardController cardController = loader.getController();
                    cardController.setReview(review);

                    reviewsContainer.getChildren().add(reviewCard);
                } catch (IOException e) {
                    System.err.println("Could not load review card for review: " + review.getReviewId());
                }
            }
        }
    }

    @FXML
    private void handleClose() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    @FXML
    private void handleRefresh() {
        loadReviews();
    }

    /**
     * Refresh the reviews list (useful after adding/editing reviews)
     */
    public void refreshReviews() {
        loadReviews();
    }
}
