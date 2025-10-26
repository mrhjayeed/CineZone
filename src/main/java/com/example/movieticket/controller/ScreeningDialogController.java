package com.example.movieticket.controller;

import com.example.movieticket.model.Movie;
import com.example.movieticket.model.Screening;
import com.example.movieticket.service.DataService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ScreeningDialogController {

    @FXML private ComboBox<Movie> movieComboBox;
    @FXML private TextField screenNameField;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private TextField priceField;
    @FXML private TextField totalSeatsField;

    private Stage dialogStage;
    private boolean okClicked = false;
    private DataService dataService = DataService.getInstance();
    private Screening screening; // Keep reference to the screening being edited

    @FXML
    private void initialize() {
        // Load movies into combo box
        movieComboBox.setItems(FXCollections.observableArrayList(dataService.getAllMovies()));

        // Set default values
        datePicker.setValue(LocalDate.now().plusDays(1));
        timeField.setText("14:00");
        priceField.setText("12.50");
        totalSeatsField.setText("50");
        screenNameField.setText("Screen 1");

        // Add validation listeners
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                priceField.setText(oldValue);
            }
        });

        totalSeatsField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                totalSeatsField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        timeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,2}:?\\d{0,2}")) {
                timeField.setText(oldValue);
            }
        });
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setScreening(Screening screening) {
        this.screening = screening;

        if (screening != null) {
            // Populate fields with existing screening data
            Movie movie = dataService.getMovieById(screening.getMovieId());
            movieComboBox.setValue(movie);
            screenNameField.setText(screening.getScreenName());

            LocalDateTime dateTime = screening.getDateTime();
            datePicker.setValue(dateTime.toLocalDate());
            timeField.setText(dateTime.toLocalTime().toString());

            priceField.setText(String.valueOf(screening.getTicketPrice()));
            totalSeatsField.setText(String.valueOf(screening.getTotalSeats()));
        }
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleOk() {
        if (isInputValid()) {
            Movie selectedMovie = movieComboBox.getValue();
            String screenName = screenNameField.getText();
            LocalDate date = datePicker.getValue();
            LocalTime time = LocalTime.parse(timeField.getText());
            LocalDateTime dateTime = LocalDateTime.of(date, time);
            double price = Double.parseDouble(priceField.getText());
            int totalSeats = Integer.parseInt(totalSeatsField.getText());

            if (screening == null) {
                // Adding a new screening
                Screening newScreening = new Screening();
                newScreening.setMovieId(selectedMovie.getMovieId());
                newScreening.setScreenName(screenName);
                newScreening.setShowTime(dateTime);
                newScreening.setTicketPrice(price);
                newScreening.setTotalSeats(totalSeats);
                newScreening.setAvailableSeats(totalSeats); // Initially all seats are available

                dataService.addScreening(newScreening);
            } else {
                // Editing an existing screening
                int currentAvailableSeats = screening.getAvailableSeats();
                int currentTotalSeats = screening.getTotalSeats();
                int bookedSeats = currentTotalSeats - currentAvailableSeats;

                // Calculate new available seats based on the difference
                int newAvailableSeats = totalSeats - bookedSeats;

                // Ensure we don't have negative available seats
                if (newAvailableSeats < 0) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Invalid Total Seats");
                    alert.setHeaderText("Cannot reduce total seats");
                    alert.setContentText("Cannot set total seats to " + totalSeats + " because " +
                            bookedSeats + " seats are already booked. " +
                            "Minimum total seats: " + bookedSeats);
                    alert.showAndWait();
                    return;
                }

                screening.setMovieId(selectedMovie.getMovieId());
                screening.setScreenName(screenName);
                screening.setShowTime(dateTime);
                screening.setTicketPrice(price);
                screening.setTotalSeats(totalSeats);
                screening.setAvailableSeats(newAvailableSeats);

                dataService.updateScreening(screening);
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
        StringBuilder errorMessage = new StringBuilder();

        if (movieComboBox.getValue() == null) {
            errorMessage.append("No movie selected!\n");
        }

        if (screenNameField.getText() == null || screenNameField.getText().trim().isEmpty()) {
            errorMessage.append("No valid screen name!\n");
        }

        if (datePicker.getValue() == null) {
            errorMessage.append("No valid date!\n");
        } else if (datePicker.getValue().isBefore(LocalDate.now())) {
            errorMessage.append("Date cannot be in the past!\n");
        }

        if (timeField.getText() == null || timeField.getText().trim().isEmpty()) {
            errorMessage.append("No valid time!\n");
        } else {
            try {
                LocalTime.parse(timeField.getText());
            } catch (Exception e) {
                errorMessage.append("Invalid time format! Use HH:MM format.\n");
            }
        }

        if (priceField.getText() == null || priceField.getText().trim().isEmpty()) {
            errorMessage.append("No valid price!\n");
        } else {
            try {
                double price = Double.parseDouble(priceField.getText());
                if (price <= 0) {
                    errorMessage.append("Price must be greater than 0!\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Invalid price format!\n");
            }
        }

        if (totalSeatsField.getText() == null || totalSeatsField.getText().trim().isEmpty()) {
            errorMessage.append("No valid total seats!\n");
        } else {
            try {
                int seats = Integer.parseInt(totalSeatsField.getText());
                if (seats <= 0 || seats > 100) {
                    errorMessage.append("Total seats must be between 1 and 100!\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Invalid total seats format!\n");
            }
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage.toString());
            alert.showAndWait();
            return false;
        }
    }
}
