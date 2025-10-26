package com.example.movieticket.controller;

import com.example.movieticket.MovieTicketApp;
import com.example.movieticket.model.Movie;
import com.example.movieticket.model.Screening;
import com.example.movieticket.model.User;
import com.example.movieticket.model.Booking;
import com.example.movieticket.model.Review;
import com.example.movieticket.service.DataService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label totalTicketsLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label availableMoviesLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalMessagesLabel; // Total messages label
    @FXML private Label totalReviewsLabel; // Total reviews label

    // Bookings tab elements
    @FXML private TableView<Booking> bookingsTable;
    @FXML private TableColumn<Booking, Integer> bookingIdColumn;
    @FXML private TableColumn<Booking, String> bookingUserColumn;
    @FXML private TableColumn<Booking, String> bookingMovieColumn;
    @FXML private TableColumn<Booking, String> bookingScreenColumn;
    @FXML private TableColumn<Booking, String> bookingShowTimeColumn;
    @FXML private TableColumn<Booking, String> bookingSeatsColumn;
    @FXML private TableColumn<Booking, Double> bookingAmountColumn;
    @FXML private TableColumn<Booking, String> bookingDateColumn;
    @FXML private TableColumn<Booking, String> bookingStatusColumn;
    @FXML private TextField bookingSearchField;
    @FXML private ComboBox<String> bookingStatusFilterComboBox;
    @FXML private Label totalBookingsLabel;
    @FXML private Label confirmedBookingsLabel;
    @FXML private Label cancelledBookingsLabel;

    // Chat management button and badge
    @FXML private Button chatManagementButton;
    @FXML private Label unreadMessagesLabel;

    // Movies tab - now using TableView
    @FXML private TableView<Movie> moviesTable;
    @FXML private TableColumn<Movie, String> movieTitleColumn;
    @FXML private TableColumn<Movie, String> movieDirectorColumn;
    @FXML private TableColumn<Movie, String> movieGenreColumn;
    @FXML private TableColumn<Movie, Integer> movieYearColumn;
    @FXML private TableColumn<Movie, Integer> movieDurationColumn;
    @FXML private TableColumn<Movie, Double> movieRatingColumn;
    @FXML private TextField movieSearchField;

    // Screenings tab
    @FXML private TableView<Screening> screeningsTable;
    @FXML private TableColumn<Screening, String> screeningMovieColumn;
    @FXML private TableColumn<Screening, String> screeningScreenColumn;
    @FXML private TableColumn<Screening, String> screeningDateTimeColumn;
    @FXML private TableColumn<Screening, Double> screeningPriceColumn;
    @FXML private TableColumn<Screening, Integer> screeningAvailableSeatsColumn;

    // Users tab
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> userIdColumn;
    @FXML private TableColumn<User, String> userUsernameColumn;
    @FXML private TableColumn<User, String> userFullNameColumn;
    @FXML private TableColumn<User, String> userEmailColumn;
    @FXML private TableColumn<User, String> userRoleColumn;
    @FXML private TextField userSearchField;

    // Reviews tab elements
    @FXML private TableView<Review> reviewsTable;
    @FXML private TableColumn<Review, String> reviewUserColumn;
    @FXML private TableColumn<Review, String> reviewTypeColumn;
    @FXML private TableColumn<Review, String> reviewRatingColumn;
    @FXML private TableColumn<Review, String> reviewTitleColumn;
    @FXML private TableColumn<Review, String> reviewCommentColumn;
    @FXML private TableColumn<Review, String> reviewDateColumn;
    @FXML private Label reviewCountLabel;
    @FXML private Label averageRatingAdminLabel;
    @FXML private Label fiveStarCountLabel;
    @FXML private BarChart<String, Number> ratingDistributionChart;
    @FXML private TextField reviewSearchField;
    @FXML private ComboBox<String> reviewTypeFilterComboBox;
    @FXML private ComboBox<String> reviewRatingFilterComboBox;

    // Statistics charts
    @FXML private PieChart bookingStatusChart;
    @FXML private PieChart moviesGenreChart;
    @FXML private BarChart<String, Number> revenueByMovieChart;

    private final DataService dataService = DataService.getInstance();
    private ObservableList<Movie> movieList = FXCollections.observableArrayList();
    private ObservableList<Screening> screeningList = FXCollections.observableArrayList();
    private ObservableList<User> userList = FXCollections.observableArrayList();
    private ObservableList<Review> reviewList = FXCollections.observableArrayList();
    private ObservableList<Booking> bookingList = FXCollections.observableArrayList();
    private Stage chatStage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        welcomeLabel.setText("Welcome, " + dataService.getCurrentUser().getFullName());

        // Initialize booking status filter ComboBox
        bookingStatusFilterComboBox.setItems(FXCollections.observableArrayList("All", "Confirmed", "Cancelled", "Pending"));
        bookingStatusFilterComboBox.setValue("All");

        // Initialize review filter ComboBoxes
        reviewTypeFilterComboBox.setItems(FXCollections.observableArrayList("All Types", "THEATER_EXPERIENCE", "MOVIE_REVIEW", "SERVICE_FEEDBACK"));
        reviewTypeFilterComboBox.setValue("All Types");

        reviewRatingFilterComboBox.setItems(FXCollections.observableArrayList("All Ratings", "5 Stars", "4 Stars", "3 Stars", "2 Stars", "1 Star"));
        reviewRatingFilterComboBox.setValue("All Ratings");

        setupTables();
        loadData();
        updateDashboardStats();
        updateReviewStats();

        // Initialize chat management
        updateUnreadMessagesBadge();

        // Set up periodic update for unread messages
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(10),
                e -> updateUnreadMessagesBadge())
        );
        timeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        timeline.play();
    }

    private void setupTables() {
        setupMoviesTable();
        setupScreeningsTable();
        setupUsersTable();
        setupReviewsTable();
        setupBookingsTable();
    }

    private void setupMoviesTable() {
        movieTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        movieDirectorColumn.setCellValueFactory(new PropertyValueFactory<>("director"));
        movieGenreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        movieYearColumn.setCellValueFactory(new PropertyValueFactory<>("releaseYear"));
        movieDurationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        movieRatingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));

        moviesTable.setItems(movieList);
    }

    private void setupScreeningsTable() {
        screeningMovieColumn.setCellValueFactory(cellData -> {
            Movie movie = dataService.getMovieById(cellData.getValue().getMovieId());
            return new javafx.beans.property.SimpleStringProperty(movie != null ? movie.getTitle() : "Unknown");
        });

        screeningScreenColumn.setCellValueFactory(new PropertyValueFactory<>("screenName"));

        screeningDateTimeColumn.setCellValueFactory(cellData -> {
            LocalDateTime dateTime = cellData.getValue().getDateTime();
            String formatted = dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
            return new javafx.beans.property.SimpleStringProperty(formatted);
        });

        screeningPriceColumn.setCellValueFactory(new PropertyValueFactory<>("ticketPrice"));
        screeningAvailableSeatsColumn.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));

        screeningsTable.setItems(screeningList);
    }

    private void setupUsersTable() {
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        userUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        userFullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        userRoleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        usersTable.setItems(userList);
    }

    private void setupReviewsTable() {
        reviewUserColumn.setCellValueFactory(cellData -> {
            User user = dataService.getUserById(cellData.getValue().getUserId());
            return new javafx.beans.property.SimpleStringProperty(user != null ? user.getFullName() : "Unknown");
        });

        reviewTypeColumn.setCellValueFactory(cellData -> {
            Review.ReviewType type = cellData.getValue().getReviewType();
            return new javafx.beans.property.SimpleStringProperty(type != null ? type.name() : "Unknown");
        });
        reviewRatingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        reviewTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        reviewCommentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));

        reviewDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime dateTime = cellData.getValue().getReviewDate();
            String formatted = dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
            return new javafx.beans.property.SimpleStringProperty(formatted);
        });

        reviewsTable.setItems(reviewList);
    }

    private void setupBookingsTable() {
        bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));

        bookingUserColumn.setCellValueFactory(cellData -> {
            String fullName = cellData.getValue().getFullName();
            return new javafx.beans.property.SimpleStringProperty(fullName != null ? fullName : "Unknown");
        });

        bookingMovieColumn.setCellValueFactory(new PropertyValueFactory<>("movieTitle"));
        bookingScreenColumn.setCellValueFactory(new PropertyValueFactory<>("screenName"));

        bookingShowTimeColumn.setCellValueFactory(cellData -> {
            LocalDateTime dateTime = cellData.getValue().getShowTime();
            if (dateTime != null) {
                String formatted = dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
                return new javafx.beans.property.SimpleStringProperty(formatted);
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        bookingSeatsColumn.setCellValueFactory(cellData -> {
            Booking booking = cellData.getValue();
            List<String> seats = booking.getSeatNumbers();
            return new javafx.beans.property.SimpleStringProperty(seats != null ? String.join(", ", seats) : "N/A");
        });

        bookingAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        bookingDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime dateTime = cellData.getValue().getBookingDate();
            if (dateTime != null) {
                String formatted = dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
                return new javafx.beans.property.SimpleStringProperty(formatted);
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        bookingStatusColumn.setCellValueFactory(cellData -> {
            Booking.BookingStatus status = cellData.getValue().getStatus();
            return new javafx.beans.property.SimpleStringProperty(status != null ? status.name() : "Unknown");
        });

        bookingsTable.setItems(bookingList);
    }

    private void loadData() {
        loadMovies();
        loadScreenings();
        loadUsers();
        loadReviews();
        loadBookings();
    }

    private void loadMovies() {
        movieList.clear();
        movieList.addAll(dataService.getAllMovies());
    }

    private void loadScreenings() {
        screeningList.clear();
        screeningList.addAll(dataService.getAllScreenings());
    }

    private void loadUsers() {
        userList.clear();
        userList.addAll(dataService.getAllUsers());
    }

    private void loadReviews() {
        reviewList.clear();
        reviewList.addAll(dataService.getAllReviews());
    }

    private void loadBookings() {
        bookingList.clear();
        bookingList.addAll(dataService.getAllBookings());
    }

    private void updateDashboardStats() {
        totalTicketsLabel.setText(String.valueOf(dataService.getTotalTicketsSold()));
        totalRevenueLabel.setText(String.format("$%.2f", dataService.getTotalRevenue()));
        availableMoviesLabel.setText(String.valueOf(dataService.getAvailableMoviesCount()));
        totalUsersLabel.setText(String.valueOf(dataService.getTotalUsersCount()));
        totalReviewsLabel.setText(String.valueOf(dataService.getAllReviews().size()));

        updateStatisticsCharts();
    }

    private void updateStatisticsCharts() {
        updateBookingStatusChart();
        updateMoviesGenreChart();
        updateRevenueByMovieChart();
    }

    private void updateBookingStatusChart() {
        try {
            Map<String, Integer> bookingStatusData = dataService.getBookingsByStatus();
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

            bookingStatusData.forEach((status, count) -> {
                pieChartData.add(new PieChart.Data(status + " (" + count + ")", count));
            });

            bookingStatusChart.setData(pieChartData);
            bookingStatusChart.setTitle("Bookings Distribution");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMoviesGenreChart() {
        try {
            Map<String, Integer> genreData = dataService.getMoviesByGenre();
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

            genreData.forEach((genre, count) -> {
                pieChartData.add(new PieChart.Data(genre + " (" + count + ")", count));
            });

            moviesGenreChart.setData(pieChartData);
            moviesGenreChart.setTitle("Genre Distribution");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateRevenueByMovieChart() {
        try {
            Map<String, Double> revenueData = dataService.getRevenueByMovie();

            // Clear existing data
            revenueByMovieChart.getData().clear();

            if (revenueData.isEmpty()) {
                revenueByMovieChart.setTitle("No revenue data available");
                return;
            }

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Revenue");

            revenueData.forEach((movieTitle, revenue) -> {
                // Truncate long movie titles
                String displayTitle = movieTitle.length() > 15 ?
                    movieTitle.substring(0, 12) + "..." : movieTitle;
                series.getData().add(new XYChart.Data<>(displayTitle, revenue));
            });

            revenueByMovieChart.getData().add(series);
            revenueByMovieChart.setTitle("Top Movies by Revenue");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showMovieDialog(Movie movie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/movieticket/movie-dialog.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.setTitle(movie == null ? "Add Movie" : "Edit Movie");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(loader.load()));

            MovieDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setMovie(movie);

            dialogStage.showAndWait();

            if (controller.isOkClicked()) {
                loadMovies();
                updateDashboardStats();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showScreeningDialog(Screening screening) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/movieticket/screening-dialog.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.setTitle(screening == null ? "Add Screening" : "Edit Screening");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(loader.load()));

            ScreeningDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setScreening(screening);

            dialogStage.showAndWait();

            if (controller.isOkClicked()) {
                loadScreenings();
                updateDashboardStats();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showUserDialog(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/movieticket/user-dialog.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit User");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(loader.load()));

            UserDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setUser(user);

            dialogStage.showAndWait();

            if (controller.isOkClicked()) {
                dataService.updateUser(user);
                loadUsers();
                updateDashboardStats();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showReviewDialog(Review review) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/movieticket/review-dialog.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.setTitle(review == null ? "Add Review" : "Edit Review");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(loader.load()));

            ReviewDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setReview(review);

            dialogStage.showAndWait();

            if (controller.isOkClicked()) {
                loadReviews();
                updateDashboardStats();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleSearchMovies() {
        String searchTerm = movieSearchField.getText().trim();
        movieList.clear();

        if (searchTerm.isEmpty()) {
            movieList.addAll(dataService.getAllMovies());
        } else {
            movieList.addAll(dataService.searchMovies(searchTerm));
        }
    }

    @FXML
    private void handleSearchUsers() {
        String searchTerm = userSearchField.getText().trim();
        userList.clear();

        if (searchTerm.isEmpty()) {
            userList.addAll(dataService.getAllUsers());
        } else {
            userList.addAll(dataService.searchUsers(searchTerm));
        }
    }

    @FXML
    private void handleSearchBookings() {
        String searchTerm = bookingSearchField.getText().trim();
        bookingList.clear();

        if (searchTerm.isEmpty()) {
            bookingList.addAll(dataService.getAllBookings());
        } else {
            bookingList.addAll(dataService.searchBookings(searchTerm));
        }
    }

    @FXML
    private void handleAddMovie() {
        showMovieDialog(null);
    }

    @FXML
    private void handleEditMovie() {
        Movie selectedMovie = moviesTable.getSelectionModel().getSelectedItem();
        if (selectedMovie != null) {
            showMovieDialog(selectedMovie);
        } else {
            showAlert("No Selection", "Please select a movie to edit.");
        }
    }

    @FXML
    private void handleDeleteMovie() {
        Movie selectedMovie = moviesTable.getSelectionModel().getSelectedItem();
        if (selectedMovie != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Movie");
            alert.setHeaderText("Delete Movie");
            alert.setContentText("Are you sure you want to delete '" + selectedMovie.getTitle() + "'?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                dataService.deleteMovie(selectedMovie.getMovieId());
                loadMovies();
                loadScreenings();
                updateDashboardStats();
            }
        } else {
            showAlert("No Selection", "Please select a movie to delete.");
        }
    }

    @FXML
    private void handleAddScreening() {
        showScreeningDialog(null);
    }

    @FXML
    private void handleEditScreening() {
        Screening selectedScreening = screeningsTable.getSelectionModel().getSelectedItem();
        if (selectedScreening != null) {
            showScreeningDialog(selectedScreening);
        } else {
            showAlert("No Selection", "Please select a screening to edit.");
        }
    }

    @FXML
    private void handleDeleteScreening() {
        Screening selectedScreening = screeningsTable.getSelectionModel().getSelectedItem();
        if (selectedScreening != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Screening");
            alert.setHeaderText("Delete Screening");
            alert.setContentText("Are you sure you want to delete this screening?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                dataService.deleteScreening(selectedScreening.getScreeningId());
                loadScreenings();
                updateDashboardStats();
            }
        } else {
            showAlert("No Selection", "Please select a screening to delete.");
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {

        dataService.logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/movieticket/login-view.fxml"));
            Scene scene = new Scene(loader.load(), 1150, 750);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            showUserDialog(selectedUser);
        } else {
            showAlert("No Selection", "Please select a user to edit.");
        }
    }

    @FXML
    private void handleAddUser() {
        User newUser = new User();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/movieticket/user-dialog.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add User");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(loader.load()));

            UserDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setUser(null); // Pass null for new user

            dialogStage.showAndWait();

            if (controller.isOkClicked()) {
                // Check if username already exists
                if (dataService.isUsernameExists(newUser.getUsername())) {
                    showAlert("Error", "Username already exists. Please choose a different username.");
                    return;
                }

                // Get the user data from the dialog
                User userToAdd = new User();
                userToAdd.setUsername(controller.getUser().getUsername());
                userToAdd.setEmail(controller.getUser().getEmail());
                userToAdd.setFullName(controller.getUser().getFullName());
                userToAdd.setPassword(controller.getUser().getPassword());
                userToAdd.setRole(controller.getUser().getRole());

                // Register the new user
                if (dataService.registerUser(userToAdd)) {
                    loadUsers();
                    updateDashboardStats();
                    showAlert("Success", "User added successfully!");
                } else {
                    showAlert("Error", "Failed to add user. Please try again.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open user dialog.");
        }
    }

    @FXML
    private void handleDeleteUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            // Check if trying to delete the last admin
            if (selectedUser.getRole() == User.UserRole.ADMIN) {
                int adminCount = dataService.getAdminCount();
                if (adminCount <= 1) {
                    showAlert("Cannot Delete", "Cannot delete the last admin user. At least one admin must remain in the system.");
                    return;
                }
            }

            // Check if trying to delete the current logged-in user
            if (selectedUser.getUserId() == dataService.getCurrentUser().getUserId()) {
                showAlert("Cannot Delete", "You cannot delete your own account while logged in.");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete User");
            alert.setHeaderText("Delete User");
            alert.setContentText("Are you sure you want to delete user '" + selectedUser.getUsername() + "'?\nThis action cannot be undone.");

            if (alert.showAndWait().get() == ButtonType.OK) {
                if (dataService.deleteUser(selectedUser.getUserId())) {
                    loadUsers();
                    updateDashboardStats();
                    showAlert("Success", "User deleted successfully!");
                } else {
                    showAlert("Error", "Failed to delete user. Please try again.");
                }
            }
        } else {
            showAlert("No Selection", "Please select a user to delete.");
        }
    }

    // Refresh methods for real-time data updates
    @FXML
    private void handleRefreshDashboard() {
        updateDashboardStats();
    }

    @FXML
    private void handleRefreshMovies() {
        loadMovies();
        movieSearchField.clear();
    }

    @FXML
    private void handleRefreshScreenings() {
        loadScreenings();
    }

    @FXML
    private void handleRefreshUsers() {
        loadUsers();
        userSearchField.clear();
    }

    @FXML
    private void handleRefreshReviews() {
        loadReviews();
        updateReviewStats();
    }

    @FXML
    private void handleRefreshBookings() {
        updateBookingStats();
        bookingStatusFilterComboBox.setValue("All");
        bookingSearchField.clear();
        loadBookings();
    }

    @FXML
    private void handleAddReview() {
        showReviewDialog(null);
    }

    @FXML
    private void handleEditReview() {
        Review selectedReview = reviewsTable.getSelectionModel().getSelectedItem();
        if (selectedReview != null) {
            showReviewDialog(selectedReview);
        } else {
            showAlert("No Selection", "Please select a review to edit.");
        }
    }

    @FXML
    private void handleDeleteReview() {
        Review selectedReview = reviewsTable.getSelectionModel().getSelectedItem();
        if (selectedReview != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Review");
            alert.setHeaderText("Delete Review");
            alert.setContentText("Are you sure you want to delete this review?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                if (dataService.deleteReview(selectedReview.getReviewId())) {
                    loadReviews();
                    updateDashboardStats();
                    updateReviewStats();
                    showAlert("Success", "Review deleted successfully!");
                } else {
                    showAlert("Error", "Failed to delete review. Please try again.");
                }
            }
        } else {
            showAlert("No Selection", "Please select a review to delete.");
        }
    }

    @FXML
    private void handleDeleteSelectedReview() {
        // Alias method for the FXML button - calls the same logic as handleDeleteReview
        handleDeleteReview();
    }

    @FXML
    private void handleViewReviewDetails() {
        Review selectedReview = reviewsTable.getSelectionModel().getSelectedItem();
        if (selectedReview != null) {
            showReviewDetailsDialog(selectedReview);
        } else {
            showAlert("No Selection", "Please select a review to view details.");
        }
    }

    @FXML
    private void handleSearchReviews() {
        applyReviewFilters();
    }

    @FXML
    private void handleFilterReviewsByType() {
        applyReviewFilters();
    }

    @FXML
    private void handleFilterReviewsByRating() {
        applyReviewFilters();
    }

    @FXML
    private void handleClearReviewFilters() {
        reviewSearchField.clear();
        reviewTypeFilterComboBox.setValue("All Types");
        reviewRatingFilterComboBox.setValue("All Ratings");
        loadReviews();
        updateReviewStats();
    }

    private void applyReviewFilters() {
        String searchTerm = reviewSearchField.getText().trim().toLowerCase();
        String selectedType = reviewTypeFilterComboBox.getValue();
        String selectedRating = reviewRatingFilterComboBox.getValue();

        reviewList.clear();
        List<Review> allReviews = dataService.getAllReviews();

        for (Review review : allReviews) {
            boolean matchesSearch = true;
            boolean matchesType = true;
            boolean matchesRating = true;

            // Apply search filter
            if (!searchTerm.isEmpty()) {
                User user = dataService.getUserById(review.getUserId());
                String userName = user != null ? user.getFullName().toLowerCase() : "";
                String title = review.getTitle() != null ? review.getTitle().toLowerCase() : "";
                String comment = review.getComment() != null ? review.getComment().toLowerCase() : "";

                matchesSearch = userName.contains(searchTerm) ||
                               title.contains(searchTerm) ||
                               comment.contains(searchTerm);
            }

            // Apply type filter
            if (selectedType != null && !selectedType.equals("All Types")) {
                matchesType = review.getReviewType() != null &&
                             review.getReviewType().name().equals(selectedType);
            }

            // Apply rating filter
            if (selectedRating != null && !selectedRating.equals("All Ratings")) {
                int filterRating = Integer.parseInt(selectedRating.split(" ")[0]);
                matchesRating = review.getRating() == filterRating;
            }

            if (matchesSearch && matchesType && matchesRating) {
                reviewList.add(review);
            }
        }

        updateReviewStats();
    }

    private void showReviewDetailsDialog(Review review) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/movieticket/review-details-dialog.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Review Details");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(loader.load(), 600, 750));

            ReviewDetailsController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setReview(review);
            controller.setOnDeleteCallback(() -> {
                loadReviews();
                updateDashboardStats();
                updateReviewStats();
            });

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open review details dialog: " + e.getMessage());
        }
    }

    private void updateReviewStats() {
        try {
            List<Review> allReviews = dataService.getAllReviews();
            int totalReviews = allReviews.size();

            // Update review count
            if (reviewCountLabel != null) {
                reviewCountLabel.setText(String.valueOf(totalReviews));
            }

            if (totalReviews == 0) {
                if (averageRatingAdminLabel != null) {
                    averageRatingAdminLabel.setText("0.0");
                }
                if (fiveStarCountLabel != null) {
                    fiveStarCountLabel.setText("0");
                }
                if (ratingDistributionChart != null) {
                    ratingDistributionChart.getData().clear();
                }
                return;
            }

            // Calculate average rating
            double totalRating = 0;
            int fiveStarCount = 0;
            int[] ratingCounts = new int[6]; // Index 0-5 for ratings 0-5

            for (Review review : allReviews) {
                int rating = review.getRating();
                totalRating += rating;
                ratingCounts[rating]++;
                if (rating == 5) {
                    fiveStarCount++;
                }
            }

            double averageRating = totalRating / totalReviews;

            // Update labels
            if (averageRatingAdminLabel != null) {
                averageRatingAdminLabel.setText(String.format("%.1f", averageRating));
            }
            if (fiveStarCountLabel != null) {
                fiveStarCountLabel.setText(String.valueOf(fiveStarCount));
            }

            // Update rating distribution chart
            if (ratingDistributionChart != null) {
                ratingDistributionChart.getData().clear();
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Reviews");

                for (int i = 1; i <= 5; i++) {
                    series.getData().add(new XYChart.Data<>(i + " Star", ratingCounts[i]));
                }

                ratingDistributionChart.getData().add(series);
                ratingDistributionChart.setTitle("Rating Distribution");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleChatManagement() {
        try {
            // Check if chat window is already open
            if (chatStage != null && chatStage.isShowing()) {
                chatStage.requestFocus();
                return;
            }

            // Get list of users with messages (now shows users who messaged ANY admin)
            List<User> usersWithMessages = dataService.getUsersWithMessages(dataService.getCurrentUser().getUserId());

            if (usersWithMessages.isEmpty()) {
                showAlert("No Messages", "No users have sent messages yet.");
                return;
            }

            // Open the new FXML-based user selection dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/movieticket/user-selection-dialog.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Select User to Chat");
            dialogStage.initModality(Modality.NONE); // Allow interaction with other windows
            dialogStage.initOwner(chatManagementButton.getScene().getWindow());
            dialogStage.setScene(new Scene(loader.load()));

            UserSelectionDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.show(); // Use show() instead of showAndWait() to keep dialog open

            // No need to check for okClicked here since chat opens directly from dialog
            // if (controller.isOkClicked() && controller.getSelectedUser() != null) {
            //     openChatWindow(controller.getSelectedUser());
            // }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not open chat management. Error: " + e.getMessage());
        }
    }

    private void openChatWindow(User user) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/example/movieticket/chat-window.fxml"));
            javafx.scene.layout.BorderPane chatRoot = loader.load();

            ChatController controller = loader.getController();

            // Create new stage for chat window
            chatStage = new Stage();
            chatStage.setTitle("Chat with " + user.getFullName());
            chatStage.initModality(Modality.NONE); // Allow interaction with main window
            chatStage.initOwner(chatManagementButton.getScene().getWindow());

            // Set up the controller
            controller.initializeChat(user);

            Scene scene = new Scene(chatRoot, 450, 550);

            // Try to load CSS file, but don't fail if it doesn't exist
            try {
                java.net.URL cssUrl = getClass().getResource("/com/example/movieticket/chat-styles.css");
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

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not open chat window. Error: " + e.getMessage());
        }
    }

    private void updateUnreadMessagesBadge() {
        // Get total unread messages for admin (messages sent TO admin)
        User currentUser = dataService.getCurrentUser();
        if (currentUser == null) {
            // No user logged in, hide the badge
            javafx.application.Platform.runLater(() -> {
                if (unreadMessagesLabel != null) {
                    unreadMessagesLabel.setVisible(false);
                }
            });
            return;
        }

        int currentUserId = currentUser.getUserId();
        int unreadCount = dataService.getUnreadMessageCount(currentUserId);

        javafx.application.Platform.runLater(() -> {
            if (unreadMessagesLabel != null) {
                if (unreadCount > 0) {
                    unreadMessagesLabel.setText(String.valueOf(unreadCount));
                    unreadMessagesLabel.setVisible(true);
                } else {
                    unreadMessagesLabel.setVisible(false);
                }
            }
        });
    }

    // Booking management handlers
    @FXML
    private void handleClearBookingSearch() {
        bookingSearchField.clear();
        bookingStatusFilterComboBox.setValue("All");
        loadBookings();
        updateBookingStats();
    }

    @FXML
    private void handleFilterAllBookings() {
        bookingList.clear();
        bookingList.addAll(dataService.getAllBookings());
        updateBookingStats();
    }

    @FXML
    private void handleFilterBookingsByStatus() {
        String selectedStatus = bookingStatusFilterComboBox.getValue();
        bookingList.clear();

        if (selectedStatus == null || selectedStatus.equals("All")) {
            bookingList.addAll(dataService.getAllBookings());
        } else {
            List<Booking> allBookings = dataService.getAllBookings();
            for (Booking booking : allBookings) {
                if (booking.getStatus().name().equals(selectedStatus.toUpperCase())) {
                    bookingList.add(booking);
                }
            }
        }

        updateBookingStats();
    }

    @FXML
    private void handleViewBookingDetails() {
        Booking selectedBooking = bookingsTable.getSelectionModel().getSelectedItem();
        if (selectedBooking != null) {
            showBookingDetailsDialog(selectedBooking);
        } else {
            showAlert("No Selection", "Please select a booking to view details.");
        }
    }

    private void showBookingDetailsDialog(Booking booking) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Booking Details");
            alert.setHeaderText("Booking #" + booking.getBookingId());

            StringBuilder details = new StringBuilder();
            details.append("User: ").append(booking.getFullName() != null ? booking.getFullName() : "Unknown").append("\n");
            details.append("Username: ").append(booking.getUsername() != null ? booking.getUsername() : "N/A").append("\n");
            details.append("Movie: ").append(booking.getMovieTitle() != null ? booking.getMovieTitle() : "Unknown").append("\n");
            details.append("Screen: ").append(booking.getScreenName() != null ? booking.getScreenName() : "N/A").append("\n");
            details.append("Show Time: ").append(booking.getShowTime() != null ?
                booking.getShowTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "N/A").append("\n");
            details.append("Seats: ").append(booking.getSeatNumbers() != null ?
                String.join(", ", booking.getSeatNumbers()) : "N/A").append("\n");
            details.append("Total Amount: $").append(String.format("%.2f", booking.getTotalAmount())).append("\n");
            details.append("Booking Date: ").append(booking.getBookingDate() != null ?
                booking.getBookingDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "N/A").append("\n");
            details.append("Status: ").append(booking.getStatus() != null ? booking.getStatus().name() : "Unknown");

            alert.setContentText(details.toString());
            alert.getDialogPane().setPrefWidth(400);
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not display booking details: " + e.getMessage());
        }
    }

    private void updateBookingStats() {
        try {
            List<Booking> allBookings = dataService.getAllBookings();
            int totalBookings = allBookings.size();
            int confirmedBookings = 0;
            int cancelledBookings = 0;

            for (Booking booking : allBookings) {
                if (booking.getStatus() == Booking.BookingStatus.CONFIRMED) {
                    confirmedBookings++;
                } else if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
                    cancelledBookings++;
                }
            }

            // Update labels if they exist
            if (totalBookingsLabel != null) {
                totalBookingsLabel.setText(String.valueOf(totalBookings));
            }
            if (confirmedBookingsLabel != null) {
                confirmedBookingsLabel.setText(String.valueOf(confirmedBookings));
            }
            if (cancelledBookingsLabel != null) {
                cancelledBookingsLabel.setText(String.valueOf(cancelledBookings));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancelBooking() {
        Booking selectedBooking = bookingsTable.getSelectionModel().getSelectedItem();
        if (selectedBooking != null) {
            if (selectedBooking.getStatus() == Booking.BookingStatus.CANCELLED) {
                showAlert("Already Cancelled", "This booking is already cancelled.");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Cancel Booking");
            alert.setHeaderText("Cancel Booking #" + selectedBooking.getBookingId());
            alert.setContentText("Are you sure you want to cancel this booking?\nThis action cannot be undone.");

            if (alert.showAndWait().get() == ButtonType.OK) {
                if (dataService.updateBookingStatus(selectedBooking.getBookingId(), Booking.BookingStatus.CANCELLED)) {
                    loadBookings();
                    updateBookingStats();
                    updateDashboardStats();
                    showAlert("Success", "Booking cancelled successfully!");
                } else {
                    showAlert("Error", "Failed to cancel booking. Please try again.");
                }
            }
        } else {
            showAlert("No Selection", "Please select a booking to cancel.");
        }
    }

    @FXML
    private void handleExportBookings() {
        if (bookingList.isEmpty()) {
            showAlert("No Data", "There are no bookings to export.");
            return;
        }

        // Create a choice dialog for export format
        Alert formatDialog = new Alert(Alert.AlertType.CONFIRMATION);
        formatDialog.setTitle("Export Bookings");
        formatDialog.setHeaderText("Select Export Format");
        formatDialog.setContentText("Choose the format for exporting bookings:");

        ButtonType csvButton = new ButtonType("CSV");
        ButtonType jsonButton = new ButtonType("JSON");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        formatDialog.getButtonTypes().setAll(csvButton, jsonButton, cancelButton);

        formatDialog.showAndWait().ifPresent(response -> {
            if (response == csvButton) {
                exportToCSV();
            } else if (response == jsonButton) {
                exportToJSON();
            }
        });
    }

    private void exportToCSV() {
        try {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Export Bookings to CSV");
            fileChooser.setInitialFileName("bookings_export_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
            fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );

            java.io.File file = fileChooser.showSaveDialog(bookingsTable.getScene().getWindow());
            if (file != null) {
                try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                    // Write CSV header
                    writer.println("Booking ID,User,Username,Movie,Screen,Show Time,Seats,Amount,Booking Date,Status");

                    // Write data
                    for (Booking booking : bookingList) {
                        writer.println(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%.2f,\"%s\",\"%s\"",
                            booking.getBookingId(),
                            escapeCSV(booking.getFullName() != null ? booking.getFullName() : "Unknown"),
                            escapeCSV(booking.getUsername() != null ? booking.getUsername() : "N/A"),
                            escapeCSV(booking.getMovieTitle() != null ? booking.getMovieTitle() : "Unknown"),
                            escapeCSV(booking.getScreenName() != null ? booking.getScreenName() : "N/A"),
                            booking.getShowTime() != null ?
                                booking.getShowTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "N/A",
                            escapeCSV(booking.getSeatNumbers() != null ?
                                String.join("; ", booking.getSeatNumbers()) : "N/A"),
                            booking.getTotalAmount(),
                            booking.getBookingDate() != null ?
                                booking.getBookingDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "N/A",
                            booking.getStatus() != null ? booking.getStatus().name() : "Unknown"
                        ));
                    }

                    showAlert("Success", "Bookings exported successfully to:\n" + file.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to write CSV file: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to export bookings: " + e.getMessage());
        }
    }

    private void exportToJSON() {
        try {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Export Bookings to JSON");
            fileChooser.setInitialFileName("bookings_export_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".json");
            fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("JSON Files", "*.json")
            );

            java.io.File file = fileChooser.showSaveDialog(bookingsTable.getScene().getWindow());
            if (file != null) {
                try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                    writer.println("[");

                    for (int i = 0; i < bookingList.size(); i++) {
                        Booking booking = bookingList.get(i);
                        writer.println("  {");
                        writer.println("    \"bookingId\": " + booking.getBookingId() + ",");
                        writer.println("    \"user\": \"" + escapeJSON(booking.getFullName() != null ? booking.getFullName() : "Unknown") + "\",");
                        writer.println("    \"username\": \"" + escapeJSON(booking.getUsername() != null ? booking.getUsername() : "N/A") + "\",");
                        writer.println("    \"movie\": \"" + escapeJSON(booking.getMovieTitle() != null ? booking.getMovieTitle() : "Unknown") + "\",");
                        writer.println("    \"screen\": \"" + escapeJSON(booking.getScreenName() != null ? booking.getScreenName() : "N/A") + "\",");
                        writer.println("    \"showTime\": \"" + (booking.getShowTime() != null ?
                            booking.getShowTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "N/A") + "\",");

                        // Write seats array
                        writer.print("    \"seats\": [");
                        if (booking.getSeatNumbers() != null && !booking.getSeatNumbers().isEmpty()) {
                            for (int j = 0; j < booking.getSeatNumbers().size(); j++) {
                                writer.print("\"" + escapeJSON(booking.getSeatNumbers().get(j)) + "\"");
                                if (j < booking.getSeatNumbers().size() - 1) {
                                    writer.print(", ");
                                }
                            }
                        }
                        writer.println("],");

                        writer.println("    \"totalAmount\": " + booking.getTotalAmount() + ",");
                        writer.println("    \"bookingDate\": \"" + (booking.getBookingDate() != null ?
                            booking.getBookingDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "N/A") + "\",");
                        writer.print("    \"status\": \"" + (booking.getStatus() != null ? booking.getStatus().name() : "Unknown") + "\"");
                        writer.println();
                        writer.print("  }");

                        if (i < bookingList.size() - 1) {
                            writer.println(",");
                        } else {
                            writer.println();
                        }
                    }

                    writer.println("]");

                    showAlert("Success", "Bookings exported successfully to:\n" + file.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to write JSON file: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to export bookings: " + e.getMessage());
        }
    }

    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        // Replace quotes with double quotes for CSV format
        return value.replace("\"", "\"\"");
    }

    private String escapeJSON(String value) {
        if (value == null) {
            return "";
        }
        // Escape special characters for JSON
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
