package com.example.movieticket.controller;

import com.example.movieticket.model.Movie;
import com.example.movieticket.service.DataService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class MovieDialogController {

    @FXML private TextField titleField;
    @FXML private TextField directorField;
    @FXML private TextField yearField;
    @FXML private TextField genreField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField durationField;
    @FXML private TextField ratingField;
    @FXML private TextField trailerUrlField;
    @FXML private ImageView posterImageView;
    @FXML private Button uploadPosterButton;
    @FXML private Button removePosterButton;
    @FXML private Button okButton;
    @FXML private Button cancelButton;

    private Stage dialogStage;
    private Movie movie;
    private boolean okClicked = false;
    private DataService dataService = DataService.getInstance();
    private String selectedPosterPath = "";

    @FXML
    private void initialize() {
        // Add validation listeners
        yearField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                yearField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        durationField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                durationField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        ratingField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                ratingField.setText(newValue.replaceAll("[^\\d.]", ""));
            }
        });

        // Initialize poster image display
        loadPlaceholderImage();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;

        if (movie != null) {
            titleField.setText(movie.getTitle());
            directorField.setText(movie.getDirector());
            yearField.setText(String.valueOf(movie.getReleaseYear()));
            genreField.setText(movie.getGenre());
            descriptionArea.setText(movie.getDescription());
            durationField.setText(String.valueOf(movie.getDuration()));
            ratingField.setText(String.valueOf(movie.getRating()));
            trailerUrlField.setText(movie.getTrailerUrl() != null ? movie.getTrailerUrl() : "");

            // Load existing poster if available
            if (movie.getPosterUrl() != null && !movie.getPosterUrl().isEmpty()) {
                loadPosterImage(movie.getPosterUrl());
                selectedPosterPath = movie.getPosterUrl();
            } else {
                loadPlaceholderImage();
            }
        }
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleUploadPoster() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Movie Poster");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(dialogStage);
        if (selectedFile != null) {
            try {
                // Create posters directory if it doesn't exist
                Path postersDir = Paths.get("posters");
                if (!Files.exists(postersDir)) {
                    Files.createDirectories(postersDir);
                }

                // Generate unique filename
                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path destinationPath = postersDir.resolve(fileName);

                // Copy file to posters directory
                Files.copy(selectedFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

                // Update image view and store path
                selectedPosterPath = destinationPath.toString();
                loadPosterImage(selectedPosterPath);

            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to upload poster");
                alert.setContentText("Could not save the poster image. Please try again.");
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void handleRemovePoster() {
        selectedPosterPath = "";
        loadPlaceholderImage();
    }

    private void loadPosterImage(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                Image image = new Image(imageFile.toURI().toString());
                posterImageView.setImage(image);
            } else {
                loadPlaceholderImage();
            }
        } catch (Exception e) {
            loadPlaceholderImage();
        }
    }

    private void loadPlaceholderImage() {
        try {
            // Try to load a placeholder image, or create a simple colored rectangle
            Image placeholderImage = new Image(getClass().getResourceAsStream("/placeholder-poster.png"));
            posterImageView.setImage(placeholderImage);
        } catch (Exception e) {
            // If no placeholder image exists, just clear the image view
            posterImageView.setImage(null);
        }
    }

    @FXML
    private void handleOk() {
        if (isInputValid()) {
            boolean success = false;

            if (movie == null) {
                // Add new movie - create Movie object first
                Movie newMovie = new Movie();
                newMovie.setTitle(titleField.getText());
                newMovie.setDirector(directorField.getText());
                newMovie.setReleaseYear(Integer.parseInt(yearField.getText()));
                newMovie.setGenre(genreField.getText());
                newMovie.setDescription(descriptionArea.getText());
                newMovie.setDuration(Integer.parseInt(durationField.getText()));
                newMovie.setRating(Double.parseDouble(ratingField.getText()));
                newMovie.setPosterUrl(selectedPosterPath);
                newMovie.setTrailerUrl(trailerUrlField.getText() != null ? trailerUrlField.getText().trim() : "");

                success = dataService.addMovie(newMovie);
                if (!success) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Failed to add movie");
                    alert.setContentText("Could not add the movie to the database. Please try again.");
                    alert.showAndWait();
                    return;
                }
            } else {
                // Update existing movie
                movie.setTitle(titleField.getText());
                movie.setDirector(directorField.getText());
                movie.setReleaseYear(Integer.parseInt(yearField.getText()));
                movie.setGenre(genreField.getText());
                movie.setDescription(descriptionArea.getText());
                movie.setDuration(Integer.parseInt(durationField.getText()));
                movie.setRating(Double.parseDouble(ratingField.getText()));
                movie.setPosterUrl(selectedPosterPath);
                movie.setTrailerUrl(trailerUrlField.getText() != null ? trailerUrlField.getText().trim() : "");

                success = dataService.updateMovie(movie);
                if (!success) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Failed to update movie");
                    alert.setContentText("Could not update the movie in the database. Please try again.");
                    alert.showAndWait();
                    return;
                }
            }

            okClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            errorMessage += "No valid title!\n";
        }
        if (directorField.getText() == null || directorField.getText().trim().isEmpty()) {
            errorMessage += "No valid director!\n";
        }
        if (yearField.getText() == null || yearField.getText().trim().isEmpty()) {
            errorMessage += "No valid release year!\n";
        } else {
            try {
                int year = Integer.parseInt(yearField.getText());
                if (year < 1800 || year > 2030) {
                    errorMessage += "Release year must be between 1800 and 2030!\n";
                }
            } catch (NumberFormatException e) {
                errorMessage += "No valid release year (must be a number)!\n";
            }
        }
        if (genreField.getText() == null || genreField.getText().trim().isEmpty()) {
            errorMessage += "No valid genre!\n";
        }
        if (durationField.getText() == null || durationField.getText().trim().isEmpty()) {
            errorMessage += "No valid duration!\n";
        } else {
            try {
                int duration = Integer.parseInt(durationField.getText());
                if (duration <= 0) {
                    errorMessage += "Duration must be greater than 0!\n";
                }
            } catch (NumberFormatException e) {
                errorMessage += "No valid duration (must be a number)!\n";
            }
        }
        if (ratingField.getText() == null || ratingField.getText().trim().isEmpty()) {
            errorMessage += "No valid rating!\n";
        } else {
            try {
                double rating = Double.parseDouble(ratingField.getText());
                if (rating < 0.0 || rating > 10.0) {
                    errorMessage += "Rating must be between 0.0 and 10.0!\n";
                }
            } catch (NumberFormatException e) {
                errorMessage += "No valid rating (must be a number)!\n";
            }
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
    }
}
