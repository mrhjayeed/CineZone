package com.example.movieticket.controller;

import com.example.movieticket.model.Movie;
import com.example.movieticket.model.Screening;
import com.example.movieticket.service.DataService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class ScreeningCardController {

    @FXML private VBox screeningCard;
    @FXML private Label movieTitleLabel;
    @FXML private Label screenNameLabel;
    @FXML private Label dateTimeLabel;
    @FXML private Label priceLabel;
    @FXML private Label availableSeatsLabel;
    @FXML private Button bookButton;

    private Screening screening;
    private Consumer<Screening> onBookClicked;
    private DataService dataService = DataService.getInstance();

    public void setScreening(Screening screening) {
        this.screening = screening;
        updateCardDisplay();
    }

    public void setOnBookClicked(Consumer<Screening> onBookClicked) {
        this.onBookClicked = onBookClicked;
    }

    @FXML
    private void initialize() {
        bookButton.setOnAction(event -> {
            if (onBookClicked != null && screening != null) {
                onBookClicked.accept(screening);
            }
        });

        // Add hover effect
        screeningCard.setOnMouseEntered(event -> {
            screeningCard.setStyle(screeningCard.getStyle() +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 12, 0, 0, 3);");
        });

        screeningCard.setOnMouseExited(event -> {
            screeningCard.setStyle(screeningCard.getStyle().replace(
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 12, 0, 0, 3);",
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2);"));
        });
    }

    private void updateCardDisplay() {
        if (screening == null) return;

        // Get movie details
        Movie movie = dataService.getMovieById(screening.getMovieId());
        if (movie != null) {
            movieTitleLabel.setText(movie.getTitle());
        } else {
            movieTitleLabel.setText("Unknown Movie");
        }

        // Set screen name
        screenNameLabel.setText(screening.getScreenName());

        // Format and set date/time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy • HH:mm");
        dateTimeLabel.setText(screening.getDateTime().format(formatter));

        // Set price
        priceLabel.setText(String.format("$%.2f", screening.getTicketPrice()));

        // Set available seats with color coding
        int availableSeats = screening.getAvailableSeats();
        availableSeatsLabel.setText(String.valueOf(availableSeats));

        // Color code based on availability
        if (availableSeats == 0) {
            availableSeatsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #e74c3c;");
            bookButton.setDisable(true);
            bookButton.setText("Sold Out");
            bookButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 10 20; -fx-font-size: 12px; -fx-font-weight: bold;");
        } else if (availableSeats <= 10) {
            availableSeatsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #f39c12;");
        } else {
            availableSeatsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #3498db;");
        }
    }
}

