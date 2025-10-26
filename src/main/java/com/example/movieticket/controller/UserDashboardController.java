package com.example.movieticket.controller;

import com.example.movieticket.model.*;
import com.example.movieticket.service.DataService;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class UserDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> genreFilter;
    @FXML private Label movieCountLabel;
    @FXML private FlowPane moviesFlowPane;
    @FXML private ScrollPane moviesScrollPane;
    @FXML private TabPane mainTabPane;

    // Screenings tab - now using FlowPane for cards
    @FXML private FlowPane screeningsFlowPane;
    @FXML private Label screeningCountLabel;
    @FXML private Label filteredMovieLabel;
    @FXML private Button clearFilterButton;

    // Bookings tab - ListView instead of TableView
    @FXML private ListView<BookingDisplay> bookingsListView;

    // Chat support button
    @FXML private Button chatSupportButton;
    @FXML private Label unreadMessagesLabel;

    // Profile tab elements
    @FXML private Label profileFullNameLabel;
    @FXML private Label profileUsernameLabel;
    @FXML private Label profileEmailLabel;
    @FXML private Label profileRoleLabel;
    @FXML private Label profileUserIdLabel;
    @FXML private Button editProfileButton;
    @FXML private Button changePasswordButton;
    @FXML private Label totalBookingsLabel;
    @FXML private Label totalTicketsLabel;
    @FXML private Label totalSpentLabel;
    @FXML private javafx.scene.image.ImageView profilePictureImageView;

    // Reviews tab elements
    @FXML private VBox reviewsContainer;
    @FXML private Label totalReviewsLabel;
    @FXML private Label averageRatingLabel;

    private DataService dataService = DataService.getInstance();
    private User currentUser;
    private ObservableList<Movie> allMovies = FXCollections.observableArrayList();
    private ObservableList<Movie> filteredMovies = FXCollections.observableArrayList();
    private ObservableList<Screening> screeningList = FXCollections.observableArrayList();
    private ObservableList<BookingDisplay> bookingList = FXCollections.observableArrayList();
    private ObservableList<Review> reviewList = FXCollections.observableArrayList();
    private Movie selectedMovie = null;
    private Stage chatStage;

    // Helper class for booking display
    public static class BookingDisplay {
        private Booking booking;
        private String movieTitle;
        private String dateTime;

        public BookingDisplay(Booking booking, String movieTitle, String dateTime) {
            this.booking = booking;
            this.movieTitle = movieTitle;
            this.dateTime = dateTime;
        }

        public String getMovieTitle() { return movieTitle; }
        public String getDateTime() { return dateTime; }
        public String getSeats() { return String.join(", ", booking.getSeatNumbers()); }
        public Double getAmount() { return booking.getTotalAmount(); }
        public String getStatus() { return booking.getStatus().toString(); }
        public Booking getBooking() { return booking; }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentUser = dataService.getCurrentUser();
        welcomeLabel.setText("Welcome, " + currentUser.getFullName());

        setupBookingsListView();
        setupFilters();
        loadMovies();
        loadScreenings();
        loadBookings();

        // Initialize profile tab
        loadProfileInformation();
        loadAccountStatistics();

        // Initialize reviews
        loadReviews();

        // Initialize chat support
        updateUnreadMessagesBadge();

        // Set up periodic update for unread messages
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(10),
                e -> updateUnreadMessagesBadge())
        );
        timeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        timeline.play();
    }

    private void setupFilters() {
        // Setup genre filter - collect all unique genres from comma-separated lists
        genreFilter.getItems().add("All Genres");
        List<String> genres = dataService.getAllMovies().stream()
                .flatMap(movie -> movie.getGenreList().stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        genreFilter.getItems().addAll(genres);
        genreFilter.setValue("All Genres");

        // Setup search listeners
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterMovies());
        genreFilter.valueProperty().addListener((observable, oldValue, newValue) -> filterMovies());
    }

    private void loadMovies() {
        allMovies.clear();
        allMovies.addAll(dataService.getAllMovies());
        filteredMovies.clear();
        filteredMovies.addAll(allMovies);
        displayMovieCards();
    }

    private void displayMovieCards() {
        moviesFlowPane.getChildren().clear();

        for (Movie movie : filteredMovies) {
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/com/example/movieticket/movie-card.fxml"));
                VBox movieCard = loader.load();

                MovieCardController controller = loader.getController();
                controller.setMovie(movie);

                // Set up the details button to show movie details dialog
                controller.setOnDetailsClicked(this::handleViewMovieDetails);

                // Set up the book button to show movie showtimes
                controller.setOnBookClicked(this::showMovieShowtimes);

                moviesFlowPane.getChildren().add(movieCard);
            } catch (IOException e) {
                e.printStackTrace();
                // Fallback to a simple card if FXML loading fails
                VBox fallbackCard = new VBox();
                fallbackCard.setPrefWidth(200);
                fallbackCard.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 10;");
                Label errorLabel = new Label("Error loading movie: " + movie.getTitle());
                errorLabel.setStyle("-fx-text-fill: #e74c3c;");
                fallbackCard.getChildren().add(errorLabel);
                moviesFlowPane.getChildren().add(fallbackCard);
            }
        }

        movieCountLabel.setText("Showing " + filteredMovies.size() + " movies");
    }

    private void showMovieShowtimes(Movie movie) {
        selectedMovie = movie;
        // Filter screenings to only show the selected movie's showtimes
        List<Screening> movieScreenings = dataService.getScreeningsByMovie(movie.getMovieId());
        screeningList.setAll(movieScreenings);
        displayScreeningCards();

        // Navigate to the Showtimes tab (index 1)
        mainTabPane.getSelectionModel().select(1);

        // Update filtered movie label
        filteredMovieLabel.setText("Showtimes for: " + movie.getTitle());
        filteredMovieLabel.setVisible(true);

        // Show clear filter button
        clearFilterButton.setVisible(true);

        // Show a brief notification if no showtimes are available
        if (movieScreenings.isEmpty()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("No Showtimes");
                alert.setHeaderText("No showtimes available");
                alert.setContentText("Currently there are no scheduled showtimes for \"" + movie.getTitle() + "\".");
                alert.showAndWait();
            });
        }
    }

    @FXML
    private void handleSearchMovies() {
        filterMovies();
    }

    @FXML
    private void handleClearSearch() {
        searchField.clear();
        genreFilter.setValue("All Genres");
        filterMovies();
    }

    private void filterMovies() {
        String searchText = searchField.getText().toLowerCase().trim();
        String selectedGenre = genreFilter.getValue();

        filteredMovies.clear();

        for (Movie movie : allMovies) {
            boolean matchesSearch = searchText.isEmpty() ||
                                  movie.getTitle().toLowerCase().contains(searchText) ||
                                  movie.getDirector().toLowerCase().contains(searchText) ||
                                  movie.getGenre().toLowerCase().contains(searchText);

            boolean matchesGenre = selectedGenre == null ||
                                 selectedGenre.equals("All Genres") ||
                                 movie.getGenreList().contains(selectedGenre);

            if (matchesSearch && matchesGenre) {
                filteredMovies.add(movie);
            }
        }

        displayMovieCards();
    }


    private void setupBookingsListView() {
        bookingsListView.setItems(bookingList);
        bookingsListView.setCellFactory(listView -> new ListCell<BookingDisplay>() {
            @Override
            protected void updateItem(BookingDisplay item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("");
                } else {
                    VBox card = createBookingCard(item);
                    setGraphic(card);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5;");
                }
            }
        });
    }

    private VBox createBookingCard(BookingDisplay bookingDisplay) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10; " +
                     "-fx-border-color: #e0e0e0; -fx-border-radius: 10; -fx-border-width: 1; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");
        card.setPrefWidth(650);

        // Header with movie title and status badge
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label movieIcon = new Label("🎬");
        movieIcon.setStyle("-fx-font-size: 24px;");

        VBox titleBox = new VBox(3);
        Label movieTitle = new Label(bookingDisplay.getMovieTitle());
        movieTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label dateTime = new Label("📅 " + bookingDisplay.getDateTime());
        dateTime.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");

        Label bookingId = new Label("Booking ID: #" + bookingDisplay.getBooking().getBookingId());
        bookingId.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6; -fx-font-weight: bold;");

        titleBox.getChildren().addAll(movieTitle, dateTime, bookingId);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        // Status badge
        Label statusBadge = new Label(bookingDisplay.getStatus());
        String statusColor = getStatusColor(bookingDisplay.getStatus());
        statusBadge.setStyle("-fx-background-color: " + statusColor + "; -fx-text-fill: white; " +
                            "-fx-padding: 6 15; -fx-background-radius: 15; -fx-font-weight: bold; " +
                            "-fx-font-size: 12px;");

        header.getChildren().addAll(movieIcon, titleBox, statusBadge);

        // Separator
        javafx.scene.control.Separator separator = new javafx.scene.control.Separator();
        separator.setStyle("-fx-background-color: #ecf0f1;");

        // Details section
        HBox details = new HBox(30);
        details.setAlignment(Pos.CENTER_LEFT);
        details.setStyle("-fx-padding: 5 0 0 0;");

        // Seats info
        VBox seatsBox = new VBox(5);
        Label seatsLabel = new Label("SEATS");
        seatsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6; -fx-font-weight: bold;");
        Label seatsValue = new Label("🪑 " + bookingDisplay.getSeats());
        seatsValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e; -fx-font-weight: bold;");
        seatsBox.getChildren().addAll(seatsLabel, seatsValue);

        // Amount info
        VBox amountBox = new VBox(5);
        Label amountLabel = new Label("TOTAL AMOUNT");
        amountLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6; -fx-font-weight: bold;");
        Label amountValue = new Label(String.format("💰 $%.2f", bookingDisplay.getAmount()));
        amountValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
        amountBox.getChildren().addAll(amountLabel, amountValue);

        // Ticket count info
        VBox ticketBox = new VBox(5);
        Label ticketLabel = new Label("TICKETS");
        ticketLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6; -fx-font-weight: bold;");
        int ticketCount = bookingDisplay.getBooking().getSeatNumbers().size();
        Label ticketValue = new Label("🎫 " + ticketCount + " ticket" + (ticketCount > 1 ? "s" : ""));
        ticketValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e; -fx-font-weight: bold;");
        ticketBox.getChildren().addAll(ticketLabel, ticketValue);

        details.getChildren().addAll(seatsBox, amountBox, ticketBox);

        card.getChildren().addAll(header, separator, details);

        return card;
    }

    private String getStatusColor(String status) {
        switch (status.toUpperCase()) {
            case "CONFIRMED":
                return "#27ae60"; // Green
            case "PENDING":
                return "#f39c12"; // Orange
            case "CANCELLED":
                return "#e74c3c"; // Red
            case "COMPLETED":
                return "#3498db"; // Blue
            default:
                return "#95a5a6"; // Gray
        }
    }

    private void loadScreenings() {
        screeningList.clear();
        screeningList.addAll(dataService.getAllScreenings());
        displayScreeningCards();
    }

    private void displayScreeningCards() {
        screeningsFlowPane.getChildren().clear();

        for (Screening screening : screeningList) {
            VBox screeningCard = createScreeningCard(screening);
            screeningsFlowPane.getChildren().add(screeningCard);
        }

        screeningCountLabel.setText("Showing " + screeningList.size() + " showtimes");
    }

    private VBox createScreeningCard(Screening screening) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/example/movieticket/screening-card.fxml"));
            VBox screeningCard = loader.load();

            ScreeningCardController controller = loader.getController();
            controller.setScreening(screening);
            controller.setOnBookClicked(this::handleBookScreening);

            return screeningCard;
        } catch (IOException e) {
            e.printStackTrace();
            // Fallback to a simple card if FXML loading fails
            VBox fallbackCard = new VBox();
            fallbackCard.setPrefWidth(320);
            fallbackCard.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 10;");
            Label errorLabel = new Label("Error loading screening card");
            errorLabel.setStyle("-fx-text-fill: #e74c3c;");
            fallbackCard.getChildren().add(errorLabel);
            return fallbackCard;
        }
    }

    private void loadBookings() {
        bookingList.clear();
        List<Booking> userBookings = dataService.getUserBookings(dataService.getCurrentUser().getUserId());

        for (Booking booking : userBookings) {
            Screening screening = dataService.getScreeningById(booking.getScreeningId());
            if (screening != null) {
                Movie movie = dataService.getMovieById(screening.getMovieId());
                String movieTitle = movie != null ? movie.getTitle() : "Unknown Movie";
                String dateTime = screening.getDateTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
                bookingList.add(new BookingDisplay(booking, movieTitle, dateTime));
            }
        }

        // Show empty state if no bookings
        if (bookingList.isEmpty() && bookingsListView != null) {
            bookingsListView.setPlaceholder(createEmptyBookingsPlaceholder());
        }
    }

    private VBox createEmptyBookingsPlaceholder() {
        VBox placeholder = new VBox(15);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setStyle("-fx-padding: 40;");

        Label icon = new Label("🎟️");
        icon.setStyle("-fx-font-size: 48px;");

        Label message = new Label("No bookings yet");
        message.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #7f8c8d;");

        Label subMessage = new Label("Browse movies and showtimes to book your tickets!");
        subMessage.setStyle("-fx-font-size: 14px; -fx-text-fill: #95a5a6;");

        placeholder.getChildren().addAll(icon, message, subMessage);
        return placeholder;
    }

    private void loadProfileInformation() {
        // Load and display user profile information
        profileUserIdLabel.setText("#" + currentUser.getUserId());
        profileFullNameLabel.setText(currentUser.getFullName());
        profileUsernameLabel.setText(currentUser.getUsername());
        profileEmailLabel.setText(currentUser.getEmail());
        profileRoleLabel.setText(currentUser.getRole().toString());

        // Load and display profile picture
        loadProfilePicture();
    }

    private void loadProfilePicture() {
        if (profilePictureImageView != null) {
            // Make the image circular
            javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(60, 60, 60);
            profilePictureImageView.setClip(clip);

            // Load profile picture if available
            if (currentUser.getProfilePicturePath() != null && !currentUser.getProfilePicturePath().isEmpty()) {
                java.io.File imageFile = new java.io.File(currentUser.getProfilePicturePath());
                if (imageFile.exists()) {
                    javafx.scene.image.Image image = new javafx.scene.image.Image(imageFile.toURI().toString());
                    profilePictureImageView.setImage(image);
                    return;
                }
            }

            // Load default image if no profile picture is set
            try {
                javafx.scene.image.Image defaultImage = new javafx.scene.image.Image(
                    getClass().getResourceAsStream("/icon.png")
                );
                profilePictureImageView.setImage(defaultImage);
            } catch (Exception e) {
                System.err.println("Error loading default profile image: " + e.getMessage());
            }
        }
    }

    private void loadAccountStatistics() {
        // Load and display account statistics
        totalBookingsLabel.setText(String.valueOf(dataService.getUserBookings(currentUser.getUserId()).size()));
        totalTicketsLabel.setText(String.valueOf(dataService.getTotalTicketsBooked(currentUser.getUserId())));
        totalSpentLabel.setText(String.format("$%.2f", dataService.getTotalAmountSpent(currentUser.getUserId())));
    }

    private void loadReviews() {
        reviewList.clear();
        List<Review> reviews = dataService.getReviewsByUser(currentUser.getUserId());
        reviewList.addAll(reviews);

        // Update reviews UI
        updateReviewsUI();
    }

    private void updateReviewsUI() {
        if (reviewsContainer == null) return;

        reviewsContainer.getChildren().clear();

        if (reviewList.isEmpty()) {
            Label emptyLabel = new Label("You haven't written any reviews yet.\nClick 'Write a Review' to share your experience!");
            emptyLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px; -fx-padding: 40;");
            emptyLabel.setAlignment(Pos.CENTER);
            reviewsContainer.getChildren().add(emptyLabel);
        } else {
            for (Review review : reviewList) {
                VBox reviewCard = createReviewCard(review);
                reviewsContainer.getChildren().add(reviewCard);
            }
        }

        // Update review statistics
        updateReviewStatistics();
    }

    private VBox createReviewCard(Review review) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPadding(new Insets(15));

        // Header with type and date
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label typeLabel = new Label(review.getReviewType().name().replace("_", " "));
        typeLabel.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 10; " +
                          "-fx-background-radius: 5; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label dateLabel = new Label(review.getReviewDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        dateLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12px;");

        header.getChildren().addAll(typeLabel, dateLabel);

        // Rating - Create visual stars with better rendering
        HBox ratingBox = new HBox(5);
        ratingBox.setAlignment(Pos.CENTER_LEFT);

        // Create individual star labels for better rendering control
        for (int i = 0; i < 5; i++) {
            Label star = new Label("★");
            if (i < review.getRating()) {
                // Filled star - use gold color
                star.setStyle("-fx-font-size: 20px; -fx-text-fill: #f39c12; -fx-font-weight: bold;");
            } else {
                // Empty star - use light gray
                star.setStyle("-fx-font-size: 20px; -fx-text-fill: #dfe6e9; -fx-font-weight: bold;");
            }
            ratingBox.getChildren().add(star);
        }

        Label ratingTextLabel = new Label("  (" + review.getRating() + "/5)");
        ratingTextLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        ratingBox.getChildren().add(ratingTextLabel);

        // Title
        Label titleLabel = new Label(review.getTitle());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        titleLabel.setWrapText(true);

        // Comment
        Label commentLabel = new Label(review.getComment());
        commentLabel.setStyle("-fx-text-fill: #34495e; -fx-font-size: 13px;");
        commentLabel.setWrapText(true);

        card.getChildren().addAll(header, ratingBox, titleLabel, commentLabel);
        return card;
    }

    private void updateReviewStatistics() {
        if (totalReviewsLabel == null || averageRatingLabel == null) return;

        int reviewCount = reviewList.size();
        double averageRating = reviewCount > 0 ?
                reviewList.stream().mapToInt(Review::getRating).average().orElse(0) : 0;

        totalReviewsLabel.setText(String.valueOf(reviewCount));
        averageRatingLabel.setText(String.format("%.1f ⭐", averageRating));
    }

    @FXML
    private void handleWriteReview() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/example/movieticket/review-dialog.fxml"));
            VBox reviewDialogRoot = loader.load();

            ReviewDialogController controller = loader.getController();

            // Create new stage for review dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Write a Review");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(welcomeLabel.getScene().getWindow());

            // Set up the controller
            controller.setDialogStage(dialogStage);

            Scene scene = new Scene(reviewDialogRoot);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);

            // Show dialog and wait for user action
            dialogStage.showAndWait();

            // If review was submitted, refresh the reviews
            if (controller.isOkClicked()) {
                loadReviews();
            }

        } catch (IOException e) {
            showAlert("Error", "Could not open review form. Please try again. Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Unexpected Error", "An unexpected error occurred. Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewAllReviews() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/example/movieticket/community-reviews.fxml"));
            VBox communityReviewsRoot = loader.load();

            CommunityReviewsController controller = loader.getController();

            // Create new stage for community reviews dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Community Reviews");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(welcomeLabel.getScene().getWindow());

            // Set up the controller
            controller.setDialogStage(dialogStage);

            Scene scene = new Scene(communityReviewsRoot, 900, 700);
            dialogStage.setScene(scene);
            dialogStage.setResizable(true);

            // Show dialog
            dialogStage.showAndWait();

        } catch (IOException e) {
            showAlert("Error", "Could not open community reviews. Please try again. Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Unexpected Error", "An unexpected error occurred. Error: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/example/movieticket/login-view.fxml"));
            Scene scene = new Scene(loader.load(), 1150, 750);
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Refresh methods for real-time data updates
    @FXML
    private void handleRefreshScreenings() {
        // If a movie filter is active, refresh only that movie's screenings
        if (selectedMovie != null) {
            List<Screening> movieScreenings = dataService.getScreeningsByMovie(selectedMovie.getMovieId());
            screeningList.setAll(movieScreenings);
            displayScreeningCards();
        } else {
            // Otherwise, load all screenings
            loadScreenings();
        }
    }

    @FXML
    private void handleRefreshBookings() {
        loadBookings();
    }

    @FXML
    private void handleRefreshStatistics() {
        loadAccountStatistics();
    }

    @FXML
    private void handleRefreshProfile() {
        // Refresh current user from database to get latest data
        currentUser = dataService.getUserById(currentUser.getUserId());
        dataService.setCurrentUser(currentUser);
        loadProfileInformation();
        welcomeLabel.setText("Welcome, " + currentUser.getFullName());
    }

    @FXML
    private void handleRefreshMovies() {
        loadMovies();
        searchField.clear();
        genreFilter.setValue("All Genres");
    }

    @FXML
    private void handleChatSupport() {
        try {
            // Check if chat window is already open
            if (chatStage != null && chatStage.isShowing()) {
                chatStage.requestFocus();
                return;
            }

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/example/movieticket/chat-window.fxml"));
            BorderPane chatRoot = loader.load();

            ChatController controller = loader.getController();

            // Create new stage for chat window
            chatStage = new Stage();
            chatStage.setTitle("Chat Support");
            chatStage.initModality(Modality.NONE); // Allow interaction with main window
            chatStage.initOwner(chatSupportButton.getScene().getWindow());

            // Find ANY admin user for chat (users can message any admin, all admins will see it)
            // We'll use the first admin as the "receiver", but the chat will be visible to all admins
            User adminUser = dataService.getAllUsers().stream()
                    .filter(user -> user.getRole() == User.UserRole.ADMIN)
                    .findFirst()
                    .orElse(null);

            if (adminUser == null) {
                showAlert("Chat Unavailable", "No support staff available at the moment. Please try again later.");
                return;
            }

            // Set up the controller - it will load the shared admin inbox history
            controller.initializeChat(adminUser);

            Scene scene = new Scene(chatRoot, 400, 500);

            // Try to load CSS file, but don't fail if it doesn't exist
            try {
                URL cssUrl = getClass().getResource("/com/example/movieticket/chat-styles.css");
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                }
            } catch (Exception e) {
                // CSS file not found, continue without custom styling
                System.out.println("Chat CSS file not found, using default styling");
            }

            chatStage.setScene(scene);
            chatStage.setResizable(true);
            chatStage.setMinWidth(350);
            chatStage.setMinHeight(400);

            // Handle chat window closing
            chatStage.setOnCloseRequest(event -> {
                chatStage = null;
                updateUnreadMessagesBadge();
            });

            // Show chat window
            chatStage.show();

        } catch (IOException e) {
            showAlert("Error", "Could not open chat window. Please try again. Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Unexpected Error", "An unexpected error occurred while opening chat. Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateUnreadMessagesBadge() {
        int unreadCount = dataService.getUnreadMessageCount(currentUser.getUserId());
        Platform.runLater(() -> {
            // Check if unreadMessagesLabel exists before trying to use it
            if (unreadMessagesLabel != null) {
                if (unreadCount > 0) {
                    unreadMessagesLabel.setText(String.valueOf(unreadCount));
                    unreadMessagesLabel.setVisible(true);
                    unreadMessagesLabel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                            "-fx-background-radius: 10; -fx-padding: 2 6; -fx-font-size: 10px;");
                } else {
                    unreadMessagesLabel.setVisible(false);
                }
            }
        });
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

    @FXML
    private void handleEditProfile() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/example/movieticket/profile-edit-dialog.fxml"));
            VBox profileEditRoot = loader.load();

            ProfileEditController controller = loader.getController();

            // Create new stage for profile edit dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Profile");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(editProfileButton.getScene().getWindow());

            // Set up the controller
            controller.setDialogStage(dialogStage);

            Scene scene = new Scene(profileEditRoot);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);

            // Show dialog and wait for user action
            dialogStage.showAndWait();

            // If profile was updated, refresh the display
            if (controller.isProfileUpdated()) {
                // Refresh current user from database
                currentUser = dataService.getCurrentUser();
                loadProfileInformation();
                welcomeLabel.setText("Welcome, " + currentUser.getFullName());

                showAlert("Profile Updated", "Your profile has been updated successfully!");
            }

        } catch (IOException e) {
            showAlert("Error", "Could not open profile editor. Please try again. Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Unexpected Error", "An unexpected error occurred while opening profile editor. Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleChangePassword() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/example/movieticket/password-change-dialog.fxml"));
            VBox passwordChangeRoot = loader.load();

            PasswordChangeController controller = loader.getController();

            // Create new stage for password change dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Change Password");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(changePasswordButton.getScene().getWindow());

            // Set up the controller
            controller.setDialogStage(dialogStage);

            Scene scene = new Scene(passwordChangeRoot);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);

            // Show dialog and wait for user action
            dialogStage.showAndWait();

            // If password was changed, show success message
            if (controller.isPasswordChanged()) {
                showAlert("Password Changed", "Your password has been changed successfully!");
            }

        } catch (IOException e) {
            showAlert("Error", "Could not open password change dialog. Please try again. Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Unexpected Error", "An unexpected error occurred while opening password change dialog. Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefreshReviews() {
        loadReviews();
    }

    private void handleBookScreening(Screening selectedScreening) {
        if (selectedScreening == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No showtime selected");
            alert.setContentText("Please select a showtime to book tickets.");
            alert.showAndWait();
            return;
        }

        // Check if screening has available seats
        if (selectedScreening.getAvailableSeats() <= 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Seats Available");
            alert.setHeaderText("Screening is full");
            alert.setContentText("This screening has no available seats. Please select a different showtime.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/example/movieticket/seat-selection.fxml"));
            VBox seatSelectionRoot = loader.load();

            SeatSelectionController controller = loader.getController();

            // Create new stage for seat selection dialog
            Stage dialogStage = new Stage();

            // Get movie title for the dialog title
            Movie movie = dataService.getMovieById(selectedScreening.getMovieId());
            String movieTitle = movie != null ? movie.getTitle() : "Movie";

            dialogStage.setTitle("Select Seats - " + movieTitle);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(welcomeLabel.getScene().getWindow());

            // Set up the controller
            controller.setDialogStage(dialogStage);
            controller.setScreening(selectedScreening);

            Scene scene = new Scene(seatSelectionRoot);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);

            // Show dialog and wait for user action
            dialogStage.showAndWait();

            // If booking was confirmed, refresh the data
            if (controller.isBookingConfirmed()) {
                handleRefreshScreenings(); // Use the refresh method that respects the movie filter
                loadBookings(); // Refresh bookings to show new booking
            }

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Booking Error");
            alert.setContentText("Could not open seat selection. Please try again. Error: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unexpected Error");
            alert.setHeaderText("An unexpected error occurred");
            alert.setContentText("Please try again or contact support. Error: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClearFilter() {
        // Clear movie filter
        selectedMovie = null;
        filteredMovieLabel.setText("");
        filteredMovieLabel.setVisible(false);
        clearFilterButton.setVisible(false);

        // Reload all screenings
        loadScreenings();
    }

    private void handleViewMovieDetails(Movie movie) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/example/movieticket/movie-details-dialog.fxml"));
            VBox movieDetailsRoot = loader.load();

            MovieDetailsDialogController controller = loader.getController();

            // Create new stage for movie details dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Movie Details - " + movie.getTitle());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(welcomeLabel.getScene().getWindow());

            // Set up the controller
            controller.setDialogStage(dialogStage);
            controller.setMovie(movie);

            // Pass HostServices to enable opening trailer URLs
            try {
                HostServices hostServices = (HostServices) welcomeLabel.getScene().getWindow().getProperties().get("hostServices");
                controller.setHostServices(hostServices);
            } catch (Exception e) {
                System.err.println("Could not get HostServices: " + e.getMessage());
            }

            // Set up callback for "Book Now" button
            controller.setOnBookNowCallback(() -> {
                // Navigate to showtimes for this movie
                showMovieShowtimes(movie);
            });

            Scene scene = new Scene(movieDetailsRoot, 700, 600);
            dialogStage.setScene(scene);
            dialogStage.setResizable(true);
            dialogStage.setMinWidth(600);
            dialogStage.setMinHeight(500);

            // Show dialog
            dialogStage.showAndWait();

        } catch (IOException e) {
            showAlert("Error", "Could not open movie details. Please try again. Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Unexpected Error", "An unexpected error occurred. Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
