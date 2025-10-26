package com.example.movieticket.controller;

import com.example.movieticket.model.Movie;
import com.example.movieticket.service.DataService;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class MovieDetailsDialogController {

    @FXML private Label titleLabel;
    @FXML private Label ratingLabel;
    @FXML private Label yearLabel;
    @FXML private Label durationLabel;
    @FXML private Label directorLabel;
    @FXML private Label descriptionLabel;
    @FXML private ImageView posterImageView;
    @FXML private FlowPane genreFlowPane;
    @FXML private Button bookNowButton;
    @FXML private Button watchTrailerButton;

    private Stage dialogStage;
    private Movie movie;
    private final DataService dataService = DataService.getInstance();
    private Runnable onBookNowCallback;
    private HostServices hostServices;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
        populateMovieDetails();
    }

    public void setOnBookNowCallback(Runnable callback) {
        this.onBookNowCallback = callback;
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    private void populateMovieDetails() {
        if (movie == null) return;

        // Set basic information
        titleLabel.setText(movie.getTitle());
        ratingLabel.setText(String.format("%.1f", movie.getRating()));
        yearLabel.setText(String.valueOf(movie.getReleaseYear()));
        durationLabel.setText(movie.getDuration() + " min");
        directorLabel.setText(movie.getDirector());
        descriptionLabel.setText(movie.getDescription() != null ? movie.getDescription() : "No description available.");

        // Load poster image
        loadPosterImage();

        // Populate genre tags
        populateGenreTags();

        // Enable/disable trailer button based on trailer URL availability
        if (movie.getTrailerUrl() == null || movie.getTrailerUrl().trim().isEmpty()) {
            watchTrailerButton.setDisable(true);
            watchTrailerButton.setStyle(
                "-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14px; " +
                "-fx-font-weight: bold; -fx-padding: 12 30; -fx-background-radius: 5; -fx-opacity: 0.6;"
            );
        } else {
            watchTrailerButton.setDisable(false);
            watchTrailerButton.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; " +
                "-fx-font-weight: bold; -fx-padding: 12 30; -fx-background-radius: 5; -fx-cursor: hand;"
            );
        }
    }

    private void loadPosterImage() {
        if (movie.getPosterUrl() != null && !movie.getPosterUrl().isEmpty()) {
            File imageFile = new File(movie.getPosterUrl());
            if (imageFile.exists()) {
                Image image = new Image(imageFile.toURI().toString());
                posterImageView.setImage(image);
                return;
            }
        }

        // Load placeholder if no poster is available
        try {
            Image placeholderImage = new Image(
                getClass().getResourceAsStream("/placeholder-poster.png")
            );
            posterImageView.setImage(placeholderImage);
        } catch (Exception e) {
            System.err.println("Error loading placeholder poster: " + e.getMessage());
        }
    }

    private void populateGenreTags() {
        genreFlowPane.getChildren().clear();

        List<String> genres = movie.getGenreList();

        if (genres.isEmpty()) {
            Label noGenreLabel = new Label("No genres specified");
            noGenreLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
            genreFlowPane.getChildren().add(noGenreLabel);
            return;
        }

        // Define colors for genre tags
        String[] colors = {
            "#e74c3c", "#3498db", "#2ecc71", "#f39c12", "#9b59b6",
            "#1abc9c", "#e67e22", "#34495e", "#16a085", "#c0392b"
        };

        for (int i = 0; i < genres.size(); i++) {
            String genre = genres.get(i);
            Label genreTag = new Label(genre);

            String color = colors[i % colors.length];
            genreTag.setStyle(
                "-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 6 12; " +
                "-fx-background-radius: 15; " +
                "-fx-font-size: 12px; " +
                "-fx-font-weight: bold;"
            );

            genreFlowPane.getChildren().add(genreTag);
        }
    }

    @FXML
    private void handleWatchTrailer() {
        if (movie == null || movie.getTrailerUrl() == null || movie.getTrailerUrl().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Trailer Available",
                     "Trailer is not available for this movie.");
            return;
        }

        try {
            // Open the trailer URL in the default web browser using JavaFX HostServices
            if (hostServices != null) {
                hostServices.showDocument(movie.getTrailerUrl());
            } else {
                // Fallback if HostServices is not available
                showAlert(Alert.AlertType.INFORMATION, "Trailer URL",
                         "Please copy this URL and open it in your browser:\n\n" +
                         movie.getTrailerUrl());
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error Opening Trailer",
                     "An error occurred while trying to open the trailer:\n" + e.getMessage());
        }
    }

    @FXML
    private void handleBookNow() {
        // Close the dialog and trigger the book now callback
        if (onBookNowCallback != null) {
            onBookNowCallback.run();
        }
        dialogStage.close();
    }

    @FXML
    private void handleClose() {
        dialogStage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
