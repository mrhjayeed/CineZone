package com.example.movieticket.controller;

import com.example.movieticket.model.*;
import com.example.movieticket.service.DataService;
import com.example.movieticket.service.RealTimeNotificationService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SeatSelectionController implements Initializable, RealTimeNotificationService.SeatUpdateObserver {

    @FXML private Label movieTitleLabel;
    @FXML private Label screeningInfoLabel;
    @FXML private GridPane seatsGrid;
    @FXML private Label selectedSeatsLabel;
    @FXML private Label totalAmountLabel;
    @FXML private Button confirmBookingButton;
    @FXML private Button cancelButton;
    @FXML private VBox lockTimerSection;
    @FXML private Label lockTimerLabel;
    @FXML private ProgressBar lockProgressBar;

    // All seat buttons defined in FXML
    @FXML private Button seatA1, seatA2, seatA3, seatA4, seatA5, seatA6, seatA7, seatA8, seatA9, seatA10;
    @FXML private Button seatB1, seatB2, seatB3, seatB4, seatB5, seatB6, seatB7, seatB8, seatB9, seatB10;
    @FXML private Button seatC1, seatC2, seatC3, seatC4, seatC5, seatC6, seatC7, seatC8, seatC9, seatC10;
    @FXML private Button seatD1, seatD2, seatD3, seatD4, seatD5, seatD6, seatD7, seatD8, seatD9, seatD10;
    @FXML private Button seatE1, seatE2, seatE3, seatE4, seatE5, seatE6, seatE7, seatE8, seatE9, seatE10;
    @FXML private Button seatF1, seatF2, seatF3, seatF4, seatF5, seatF6, seatF7, seatF8, seatF9, seatF10;
    @FXML private Button seatG1, seatG2, seatG3, seatG4, seatG5, seatG6, seatG7, seatG8, seatG9, seatG10;
    @FXML private Button seatH1, seatH2, seatH3, seatH4, seatH5, seatH6, seatH7, seatH8, seatH9, seatH10;
    @FXML private Button seatI1, seatI2, seatI3, seatI4, seatI5, seatI6, seatI7, seatI8, seatI9, seatI10;
    @FXML private Button seatJ1, seatJ2, seatJ3, seatJ4, seatJ5, seatJ6, seatJ7, seatJ8, seatJ9, seatJ10;

    private DataService dataService = DataService.getInstance();
    private RealTimeNotificationService notificationService;
    private Screening screening;
    private List<Seat> allSeats;
    private List<String> selectedSeatNumbers = new ArrayList<>();
    private Map<String, Button> seatButtonMap = new HashMap<>();
    private Stage dialogStage;
    private boolean bookingConfirmed = false;
    private Timer lockTimer;
    private User currentUser;

    public void setScreening(Screening screening) {
        this.screening = screening;
        loadScreeningInfo();
        initializeSeatButtons();
        loadSeats();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isBookingConfirmed() {
        return bookingConfirmed;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.notificationService = dataService.getNotificationService();
        this.currentUser = dataService.getCurrentUser();

        selectedSeatsLabel.setText("Selected Seats: None");
        totalAmountLabel.setText("Total: $0.00");
        confirmBookingButton.setDisable(true);
        lockTimerSection.setVisible(false);

        // Register for real-time updates
        if (screening != null) {
            notificationService.registerSeatObserver(screening.getScreeningId(), this);
        }
    }

    private void initializeSeatButtons() {
        // Initialize the seat button map with all FXML-defined buttons
        seatButtonMap.put("A1", seatA1); seatButtonMap.put("A2", seatA2); seatButtonMap.put("A3", seatA3); seatButtonMap.put("A4", seatA4); seatButtonMap.put("A5", seatA5);
        seatButtonMap.put("A6", seatA6); seatButtonMap.put("A7", seatA7); seatButtonMap.put("A8", seatA8); seatButtonMap.put("A9", seatA9); seatButtonMap.put("A10", seatA10);
        seatButtonMap.put("B1", seatB1); seatButtonMap.put("B2", seatB2); seatButtonMap.put("B3", seatB3); seatButtonMap.put("B4", seatB4); seatButtonMap.put("B5", seatB5);
        seatButtonMap.put("B6", seatB6); seatButtonMap.put("B7", seatB7); seatButtonMap.put("B8", seatB8); seatButtonMap.put("B9", seatB9); seatButtonMap.put("B10", seatB10);
        seatButtonMap.put("C1", seatC1); seatButtonMap.put("C2", seatC2); seatButtonMap.put("C3", seatC3); seatButtonMap.put("C4", seatC4); seatButtonMap.put("C5", seatC5);
        seatButtonMap.put("C6", seatC6); seatButtonMap.put("C7", seatC7); seatButtonMap.put("C8", seatC8); seatButtonMap.put("C9", seatC9); seatButtonMap.put("C10", seatC10);
        seatButtonMap.put("D1", seatD1); seatButtonMap.put("D2", seatD2); seatButtonMap.put("D3", seatD3); seatButtonMap.put("D4", seatD4); seatButtonMap.put("D5", seatD5);
        seatButtonMap.put("D6", seatD6); seatButtonMap.put("D7", seatD7); seatButtonMap.put("D8", seatD8); seatButtonMap.put("D9", seatD9); seatButtonMap.put("D10", seatD10);
        seatButtonMap.put("E1", seatE1); seatButtonMap.put("E2", seatE2); seatButtonMap.put("E3", seatE3); seatButtonMap.put("E4", seatE4); seatButtonMap.put("E5", seatE5);
        seatButtonMap.put("E6", seatE6); seatButtonMap.put("E7", seatE7); seatButtonMap.put("E8", seatE8); seatButtonMap.put("E9", seatE9); seatButtonMap.put("E10", seatE10);
        seatButtonMap.put("F1", seatF1); seatButtonMap.put("F2", seatF2); seatButtonMap.put("F3", seatF3); seatButtonMap.put("F4", seatF4); seatButtonMap.put("F5", seatF5);
        seatButtonMap.put("F6", seatF6); seatButtonMap.put("F7", seatF7); seatButtonMap.put("F8", seatF8); seatButtonMap.put("F9", seatF9); seatButtonMap.put("F10", seatF10);
        seatButtonMap.put("G1", seatG1); seatButtonMap.put("G2", seatG2); seatButtonMap.put("G3", seatG3); seatButtonMap.put("G4", seatG4); seatButtonMap.put("G5", seatG5);
        seatButtonMap.put("G6", seatG6); seatButtonMap.put("G7", seatG7); seatButtonMap.put("G8", seatG8); seatButtonMap.put("G9", seatG9); seatButtonMap.put("G10", seatG10);
        seatButtonMap.put("H1", seatH1); seatButtonMap.put("H2", seatH2); seatButtonMap.put("H3", seatH3); seatButtonMap.put("H4", seatH4); seatButtonMap.put("H5", seatH5);
        seatButtonMap.put("H6", seatH6); seatButtonMap.put("H7", seatH7); seatButtonMap.put("H8", seatH8); seatButtonMap.put("H9", seatH9); seatButtonMap.put("H10", seatH10);
        seatButtonMap.put("I1", seatI1); seatButtonMap.put("I2", seatI2); seatButtonMap.put("I3", seatI3); seatButtonMap.put("I4", seatI4); seatButtonMap.put("I5", seatI5);
        seatButtonMap.put("I6", seatI6); seatButtonMap.put("I7", seatI7); seatButtonMap.put("I8", seatI8); seatButtonMap.put("I9", seatI9); seatButtonMap.put("I10", seatI10);
        seatButtonMap.put("J1", seatJ1); seatButtonMap.put("J2", seatJ2); seatButtonMap.put("J3", seatJ3); seatButtonMap.put("J4", seatJ4); seatButtonMap.put("J5", seatJ5);
        seatButtonMap.put("J6", seatJ6); seatButtonMap.put("J7", seatJ7); seatButtonMap.put("J8", seatJ8); seatButtonMap.put("J9", seatJ9); seatButtonMap.put("J10", seatJ10);
    }

    private void loadScreeningInfo() {
        if (screening != null) {
            Movie movie = dataService.getMovieById(screening.getMovieId());
            if (movie != null) {
                movieTitleLabel.setText(movie.getTitle());
            }

            String screeningInfo = String.format("%s - %s - $%.2f per ticket",
                screening.getScreenName(),
                screening.getShowTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                screening.getTicketPrice());
            screeningInfoLabel.setText(screeningInfo);

            // Register for real-time updates now that we have screening info
            notificationService.registerSeatObserver(screening.getScreeningId(), this);
        }
    }

    private void loadSeats() {
        if (screening == null) return;

        allSeats = dataService.getSeatsByScreeningWithLocks(screening.getScreeningId());

        // Update the existing FXML-defined seat buttons
        for (Map.Entry<String, Button> entry : seatButtonMap.entrySet()) {
            String seatNumber = entry.getKey();
            Button button = entry.getValue();
            Seat seat = findSeatByNumber(seatNumber);
            updateSeatButtonStyle(button, seat);
        }
    }

    @FXML
    private void handleSeatClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String seatNumber = clickedButton.getText();
        handleSeatSelection(seatNumber);
    }

    private void updateSeatButtonStyle(Button button, Seat seat) {
        if (seat == null) {
            button.setStyle("-fx-background-color: #cccccc;"); // Not found
            button.setDisable(true);
            return;
        }

        if (seat.isBooked()) {
            // Seat is booked - red
            button.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
            button.setDisable(true);
        } else if (seat.isLocked() && seat.getLockedByUserId() == currentUser.getUserId()) {
            // Your selection (locked by you) - green
            button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            button.setDisable(false);
        } else if (seat.isLocked()) {
            // Locked by another user - orange
            button.setStyle("-fx-background-color: #ff8c00; -fx-text-fill: white;");
            button.setDisable(true);

            // Show tooltip with remaining time
            long remainingSeconds = seat.getRemainingLockTimeSeconds();
            if (remainingSeconds > 0) {
                Tooltip tooltip = new Tooltip("Seat locked by another user (" + remainingSeconds + "s remaining)");
                button.setTooltip(tooltip);
            }
        } else {
            // Available - light green
            button.setStyle("-fx-background-color: #81C784; -fx-text-fill: black;");
            button.setDisable(false);
        }
    }

    private void handleSeatSelection(String seatNumber) {
        Seat seat = findSeatByNumber(seatNumber);
        if (seat == null || seat.isBooked()) return;

        // Check if this seat is locked by current user (i.e., already selected)
        if (seat.isLocked() && seat.getLockedByUserId() == currentUser.getUserId()) {
            // Deselect/unlock this seat
            selectedSeatNumbers.remove(seatNumber);
            dataService.unlockSeat(screening.getScreeningId(), seatNumber, currentUser.getUserId());

            // If no seats selected, stop the timer
            if (selectedSeatNumbers.isEmpty()) {
                stopLockTimer();
            }

            // Refresh to show updated state
            loadSeats();
            updateSeatDisplay();
        } else {
            // Check if seat is available
            if (!seat.isAvailableForUser(currentUser.getUserId())) {
                showAlert("Seat Unavailable", "This seat is currently being selected by another user.");
                return;
            }

            // Check if user already has seats selected
            if (!selectedSeatNumbers.isEmpty()) {
                // User wants to select an additional seat - add to existing selection
                List<String> allSeatsToLock = new ArrayList<>(selectedSeatNumbers);
                allSeatsToLock.add(seatNumber);

                boolean locked = dataService.lockSeats(screening.getScreeningId(), allSeatsToLock, currentUser.getUserId());

                if (!locked) {
                    showAlert("Booking Conflict", "This seat is no longer available. Please select a different seat.");
                    loadSeats();
                    return;
                }

                // Add to selection
                selectedSeatNumbers.add(seatNumber);
            } else {
                // First seat selection
                List<String> seatToLock = new ArrayList<>();
                seatToLock.add(seatNumber);
                boolean locked = dataService.lockSeats(screening.getScreeningId(), seatToLock, currentUser.getUserId());

                if (!locked) {
                    showAlert("Booking Conflict", "This seat is no longer available. Please select a different seat.");
                    loadSeats();
                    return;
                }

                // Add to selection
                selectedSeatNumbers.add(seatNumber);
            }

            // Start or restart the lock timer
            startLockTimer();

            // Refresh to show updated state
            loadSeats();
            updateSeatDisplay();
        }
    }

    private void startLockTimer() {
        stopLockTimer(); // Stop any existing timer

        // Show the timer section
        lockTimerSection.setVisible(true);

        lockTimer = new Timer();
        lockTimer.scheduleAtFixedRate(new TimerTask() {
            private int secondsRemaining = 2 * 60; // 2 minutes

            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (secondsRemaining <= 0) {
                        // Time expired - release locks in database
                        dataService.releaseSeatLocks(screening.getScreeningId(), currentUser.getUserId());

                        // Clear local selection
                        selectedSeatNumbers.clear();

                        // Reload seats to get fresh data from database
                        loadSeats();
                        updateSeatDisplay();
                        stopLockTimer();
                        showAlert("Time Expired", "Your seat selection has expired. Please select seats again.");
                        return;
                    }

                    int minutes = secondsRemaining / 60;
                    int seconds = secondsRemaining % 60;
                    lockTimerLabel.setText(String.format("Time remaining: %02d:%02d", minutes, seconds));

                    double progress = (2 * 60 - secondsRemaining) / (double)(2 * 60);
                    lockProgressBar.setProgress(progress);

                    secondsRemaining--;
                });
            }
        }, 0, 1000);
    }

    private void stopLockTimer() {
        if (lockTimer != null) {
            lockTimer.cancel();
            lockTimer = null;
        }
        // Hide the timer section
        lockTimerSection.setVisible(false);
    }

    private Seat findSeatByNumber(String seatNumber) {
        return allSeats.stream()
                .filter(seat -> seat.getSeatNumber().equals(seatNumber))
                .findFirst()
                .orElse(null);
    }

    private void updateSeatDisplay() {
        // Update UI
        if (selectedSeatNumbers.isEmpty()) {
            selectedSeatsLabel.setText("Selected Seats: None");
            totalAmountLabel.setText("Total: $0.00");
            confirmBookingButton.setDisable(true);
        } else {
            selectedSeatsLabel.setText("Selected Seats: " + String.join(", ", selectedSeatNumbers));
            double total = selectedSeatNumbers.size() * screening.getTicketPrice();
            totalAmountLabel.setText(String.format("Total: $%.2f", total));
            confirmBookingButton.setDisable(false);
        }

        // Update seat button styles
        for (Seat seat : allSeats) {
            Button button = seatButtonMap.get(seat.getSeatNumber());
            if (button != null) {
                updateSeatButtonStyle(button, seat);
            }
        }
    }

    @FXML
    private void handleConfirmBooking() {
        if (selectedSeatNumbers.isEmpty()) return;

        // Show payment dialog
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/movieticket/payment-dialog.fxml"));
            Parent root = loader.load();

            PaymentDialogController controller = loader.getController();

            // Get movie title
            Movie movie = dataService.getMovieById(screening.getMovieId());
            String movieTitle = movie != null ? movie.getTitle() : "Movie";

            // Set booking details in payment dialog
            controller.setBookingDetails(movieTitle, selectedSeatNumbers, selectedSeatNumbers.size() * screening.getTicketPrice());

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Payment");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();

            // Check if payment was completed
            if (controller.isPaymentCompleted()) {
                // Create booking with locks
                Booking booking = dataService.createBookingWithLocks(
                        currentUser.getUserId(),
                        screening.getScreeningId(),
                        selectedSeatNumbers,
                        selectedSeatNumbers.size() * screening.getTicketPrice()
                );

                if (booking != null) {
                    // Get the payment from controller and link it to booking
                    Payment payment = controller.getCompletedPayment();
                    if (payment != null) {
                        payment.setUserId(currentUser.getUserId());
                        payment.setBookingId(booking.getBookingId());
                        dataService.savePayment(payment);
                    }

                    bookingConfirmed = true;
                    stopLockTimer();
                    showAlert("Booking Confirmed", "Your booking and payment have been confirmed successfully!\n\nBooking ID: " + booking.getBookingId());

                    if (this.dialogStage != null) {
                        this.dialogStage.close();
                    }
                } else {
                    showAlert("Booking Failed", "Failed to complete booking. Some seats may no longer be available.");
                    // Refresh seat display
                    loadSeats();
                    selectedSeatNumbers.clear();
                    updateSeatDisplay();
                }
            } else {
                // Payment was cancelled, keep seats locked
                showAlert("Payment Cancelled", "Payment was cancelled. Your seats are still reserved for " +
                         "the remaining time.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open payment dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        // Release any locks
        if (!selectedSeatNumbers.isEmpty()) {
            dataService.releaseSeatLocks(screening.getScreeningId(), currentUser.getUserId());
        }

        stopLockTimer();
        cleanup();

        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    @FXML
    private void handleRefreshSeats() {
        // Reload seats from database to get latest availability
        loadSeats();
        showAlert("Seats Refreshed", "Seat availability has been updated with the latest information.");
    }

    private void cleanup() {
        if (screening != null) {
            notificationService.unregisterSeatObserver(screening.getScreeningId(), this);
        }
        stopLockTimer();
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // Real-time seat update observer methods
    @Override
    public void onSeatUpdated(int screeningId, List<Seat> updatedSeats) {
        if (screening != null && screening.getScreeningId() == screeningId) {
            Platform.runLater(() -> {
                this.allSeats = updatedSeats;
                updateSeatDisplay();
            });
        }
    }

    @Override
    public void onSeatLocked(int screeningId, String seatNumber, int userId) {
        if (screening != null && screening.getScreeningId() == screeningId) {
            Platform.runLater(() -> {
                // Find and update the seat
                Seat seat = findSeatByNumber(seatNumber);
                if (seat != null) {
                    seat.setLocked(true);
                    seat.setLockedByUserId(userId);
                    Button button = seatButtonMap.get(seatNumber);
                    if (button != null) {
                        updateSeatButtonStyle(button, seat);
                    }
                }
            });
        }
    }

    @Override
    public void onSeatUnlocked(int screeningId, String seatNumber) {
        if (screening != null && screening.getScreeningId() == screeningId) {
            Platform.runLater(() -> {
                // Find and update the seat
                Seat seat = findSeatByNumber(seatNumber);
                if (seat != null) {
                    seat.setLocked(false);
                    seat.setLockedByUserId(0);
                    seat.setLockExpiresAt(null);
                    Button button = seatButtonMap.get(seatNumber);
                    if (button != null) {
                        updateSeatButtonStyle(button, seat);
                    }
                }
            });
        }
    }

    @Override
    public void onSeatBooked(int screeningId, String seatNumber) {
        if (screening != null && screening.getScreeningId() == screeningId) {
            Platform.runLater(() -> {
                // Find and update the seat
                Seat seat = findSeatByNumber(seatNumber);
                if (seat != null) {
                    seat.setBooked(true);
                    seat.setLocked(false);
                    seat.setLockedByUserId(0);
                    seat.setLockExpiresAt(null);
                    Button button = seatButtonMap.get(seatNumber);
                    if (button != null) {
                        updateSeatButtonStyle(button, seat);
                    }
                }
            });
        }
    }
}
