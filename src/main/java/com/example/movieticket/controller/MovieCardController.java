package com.example.movieticket.controller;

import com.example.movieticket.model.Movie;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class MovieCardController implements Initializable {

    @FXML private VBox movieCard;
    @FXML private ImageView posterImageView;
    @FXML private Label titleLabel;
    @FXML private Label yearLabel;
    @FXML private FlowPane genreFlowPane;
    @FXML private Label starLabel;
    @FXML private Label ratingLabel;
    @FXML private Button detailsButton;
    @FXML private Button bookButton;

    private Movie movie;
    private Consumer<Movie> onDetailsClicked;
    private Consumer<Movie> onBookClicked;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Setup hover effects
        setupHoverEffects();
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
        updateMovieData();
    }

    public void setOnDetailsClicked(Consumer<Movie> onDetailsClicked) {
        this.onDetailsClicked = onDetailsClicked;
    }

    public void setOnBookClicked(Consumer<Movie> onBookClicked) {
        this.onBookClicked = onBookClicked;
    }

    private void updateMovieData() {
        if (movie == null) return;

        // Set movie title
        titleLabel.setText(movie.getTitle());

        // Set movie year
        if (yearLabel != null) {
            yearLabel.setText(String.valueOf(movie.getReleaseYear()));
        }

        // Set genres as individual labels
        if (genreFlowPane != null) {
            genreFlowPane.getChildren().clear();
            List<String> genres = movie.getGenreList();
            for (String genre : genres) {
                Label genreLabel = new Label(genre);
                genreLabel.setStyle(
                    "-fx-background-color: #3498db; " +
                    "-fx-text-fill: white; " +
                    "-fx-padding: 3 8 3 8; " +
                    "-fx-background-radius: 10; " +
                    "-fx-font-size: 10px; " +
                    "-fx-font-weight: bold;"
                );
                genreFlowPane.getChildren().add(genreLabel);
            }
        }

        // Set rating
        ratingLabel.setText(String.format("%.1f", movie.getRating()));

        // Load poster image
        loadPosterImage();

        // Setup button actions
        detailsButton.setOnAction(e -> {
            if (onDetailsClicked != null) {
                onDetailsClicked.accept(movie);
            }
        });

        bookButton.setOnAction(e -> {
            if (onBookClicked != null) {
                onBookClicked.accept(movie);
            }
        });
    }

    private void loadPosterImage() {
        if (movie.getPosterUrl() != null && !movie.getPosterUrl().isEmpty()) {
            try {
                File imageFile = new File(movie.getPosterUrl());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    posterImageView.setImage(image);
                } else {
                    setPlaceholderImage();
                }
            } catch (Exception e) {
                setPlaceholderImage();
            }
        } else {
            setPlaceholderImage();
        }
    }

    private void setPlaceholderImage() {
        try {
            Image placeholderImage = new Image(getClass().getResourceAsStream("/placeholder-poster.png"));
            posterImageView.setImage(placeholderImage);
        } catch (Exception e) {
            // If no placeholder exists, set a background color
            posterImageView.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-width: 1;");
        }
    }

    private void setupHoverEffects() {
        movieCard.setOnMouseEntered(e ->
            movieCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                              "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 12, 0, 0, 4); " +
                              "-fx-cursor: hand; -fx-scale-x: 1.02; -fx-scale-y: 1.02;"));

        movieCard.setOnMouseExited(e ->
            movieCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                              "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2); " +
                              "-fx-cursor: hand; -fx-scale-x: 1.0; -fx-scale-y: 1.0;"));
    }

    public VBox getMovieCard() {
        return movieCard;
    }
}
