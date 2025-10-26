package com.example.movieticket.service;

import com.example.movieticket.model.*;
import com.example.movieticket.model.User.UserRole;
import com.example.movieticket.model.Booking.BookingStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class DataService {
    private static DataService instance;
    private User currentUser;
    private static final Object lock = new Object(); // Lock object for synchronization
    private final RealTimeNotificationService notificationService;

    private DataService() {
        // Initialize database and tables from SQL file
        DatabaseConnection.initializeDatabase();
        initializeSampleData();
        this.notificationService = RealTimeNotificationService.getInstance();

        // Start background task to clean up expired seat locks
        startSeatLockCleanupTask();
    }

    public static synchronized DataService getInstance() {
        if (instance == null) {
            instance = new DataService();
        }
        return instance;
    }

    // Authentication methods
    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getString("full_name"),
                    UserRole.valueOf(rs.getString("role")),
                    rs.getString("profile_picture_path")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
        }
        return null;
    }

    // Check if username already exists
    public boolean isUsernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking username existence: " + e.getMessage());
        }
        return false;
    }

    // Check if email already exists
    public boolean isEmailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking email existence: " + e.getMessage());
        }
        return false;
    }

    /**
     * Check if username is available for a specific user (excluding their current username)
     */
    public boolean isUsernameAvailable(String username, int excludeUserId) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ? AND user_id != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setInt(2, excludeUserId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) == 0; // Available if count is 0
            }
        } catch (SQLException e) {
            System.err.println("Error checking username availability: " + e.getMessage());
        }
        return false;
    }

    /**
     * Check if email is available for a specific user (excluding their current email)
     */
    public boolean isEmailAvailable(String email, int excludeUserId) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ? AND user_id != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setInt(2, excludeUserId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) == 0; // Available if count is 0
            }
        } catch (SQLException e) {
            System.err.println("Error checking email availability: " + e.getMessage());
        }
        return false;
    }

    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (username, password, email, full_name, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getFullName());
            stmt.setString(5, user.getRole().toString());

            int result = stmt.executeUpdate();
            if (result > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    user.setUserId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
        }
        return false;
    }

    // User operations
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getString("full_name"),
                    UserRole.valueOf(rs.getString("role")),
                    rs.getString("profile_picture_path")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY full_name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getString("full_name"),
                    UserRole.valueOf(rs.getString("role")),
                    rs.getString("profile_picture_path")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
        }
        return users;
    }

    // Count admin users in the system
    public int getAdminCount() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting admin users: " + e.getMessage());
        }
        return 0;
    }

    // Movie operations
    public List<Movie> getAllMovies() {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies ORDER BY title";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Movie movie = new Movie();
                movie.setMovieId(rs.getInt("movie_id"));
                movie.setTitle(rs.getString("title"));
                movie.setDirector(rs.getString("director"));
                movie.setReleaseYear(rs.getInt("release_year"));
                movie.setDescription(rs.getString("description"));
                movie.setDuration(rs.getInt("duration"));
                movie.setGenre(rs.getString("genre"));
                movie.setRating(rs.getDouble("rating"));
                movie.setPosterUrl(rs.getString("poster_url"));
                movie.setTrailerUrl(rs.getString("trailer_url"));
                movies.add(movie);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all movies: " + e.getMessage());
        }
        return movies;
    }

    public boolean addMovie(Movie movie) {
        String sql = "INSERT INTO movies (title, director, release_year, description, duration, genre, rating, poster_url, trailer_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, movie.getTitle());
            stmt.setString(2, movie.getDirector());
            stmt.setInt(3, movie.getReleaseYear());
            stmt.setString(4, movie.getDescription());
            stmt.setInt(5, movie.getDuration());
            stmt.setString(6, movie.getGenre());
            stmt.setDouble(7, movie.getRating());
            stmt.setString(8, movie.getPosterUrl());
            stmt.setString(9, movie.getTrailerUrl());

            int result = stmt.executeUpdate();
            if (result > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    movie.setMovieId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding movie: " + e.getMessage());
        }
        return false;
    }

    public boolean updateMovie(Movie movie) {
        String sql = "UPDATE movies SET title = ?, director = ?, release_year = ?, description = ?, duration = ?, genre = ?, rating = ?, poster_url = ?, trailer_url = ? WHERE movie_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, movie.getTitle());
            stmt.setString(2, movie.getDirector());
            stmt.setInt(3, movie.getReleaseYear());
            stmt.setString(4, movie.getDescription());
            stmt.setInt(5, movie.getDuration());
            stmt.setString(6, movie.getGenre());
            stmt.setDouble(7, movie.getRating());
            stmt.setString(8, movie.getPosterUrl());
            stmt.setString(9, movie.getTrailerUrl());
            stmt.setInt(10, movie.getMovieId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating movie: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteMovie(int movieId) {
        String sql = "DELETE FROM movies WHERE movie_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, movieId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting movie: " + e.getMessage());
        }
        return false;
    }

    public Movie getMovieById(int movieId) {
        String sql = "SELECT * FROM movies WHERE movie_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, movieId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Movie movie = new Movie();
                movie.setMovieId(rs.getInt("movie_id"));
                movie.setTitle(rs.getString("title"));
                movie.setDirector(rs.getString("director"));
                movie.setReleaseYear(rs.getInt("release_year"));
                movie.setDescription(rs.getString("description"));
                movie.setDuration(rs.getInt("duration"));
                movie.setGenre(rs.getString("genre"));
                movie.setRating(rs.getDouble("rating"));
                movie.setPosterUrl(rs.getString("poster_url"));
                movie.setTrailerUrl(rs.getString("trailer_url"));
                return movie;
            }
        } catch (SQLException e) {
            System.err.println("Error getting movie by ID: " + e.getMessage());
        }
        return null;
    }

    public List<Movie> searchMovies(String searchTerm) {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies WHERE title LIKE ? OR description LIKE ? OR genre LIKE ? ORDER BY title";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Movie movie = new Movie();
                movie.setMovieId(rs.getInt("movie_id"));
                movie.setTitle(rs.getString("title"));
                movie.setDirector(rs.getString("director"));
                movie.setReleaseYear(rs.getInt("release_year"));
                movie.setDescription(rs.getString("description"));
                movie.setDuration(rs.getInt("duration"));
                movie.setGenre(rs.getString("genre"));
                movie.setRating(rs.getDouble("rating"));
                movie.setPosterUrl(rs.getString("poster_url"));
                movie.setTrailerUrl(rs.getString("trailer_url"));
                movies.add(movie);
            }
        } catch (SQLException e) {
            System.err.println("Error searching movies: " + e.getMessage());
        }
        return movies;
    }

    // Screening operations
    public List<Screening> getAllScreenings() {
        List<Screening> screenings = new ArrayList<>();
        String sql = """
            SELECT s.*, m.title 
            FROM screenings s 
            JOIN movies m ON s.movie_id = m.movie_id 
            ORDER BY s.show_time
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Screening screening = new Screening();
                screening.setScreeningId(rs.getInt("screening_id"));
                screening.setMovieId(rs.getInt("movie_id"));
                screening.setMovieTitle(rs.getString("title"));
                screening.setScreenName(rs.getString("screen_name"));
                screening.setShowTime(rs.getTimestamp("show_time").toLocalDateTime());
                screening.setTicketPrice(rs.getDouble("ticket_price"));
                screening.setTotalSeats(rs.getInt("total_seats"));
                screening.setAvailableSeats(rs.getInt("available_seats"));
                screenings.add(screening);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all screenings: " + e.getMessage());
        }
        return screenings;
    }

    public boolean addScreening(Screening screening) {
        String sql = "INSERT INTO screenings (movie_id, screen_name, show_time, ticket_price, total_seats, available_seats) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, screening.getMovieId());
            stmt.setString(2, screening.getScreenName());
            stmt.setTimestamp(3, Timestamp.valueOf(screening.getShowTime()));
            stmt.setDouble(4, screening.getTicketPrice());
            stmt.setInt(5, screening.getTotalSeats());
            stmt.setInt(6, screening.getAvailableSeats());

            int result = stmt.executeUpdate();
            if (result > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    screening.setScreeningId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding screening: " + e.getMessage());
        }
        return false;
    }

    public boolean updateScreening(Screening screening) {
        String sql = "UPDATE screenings SET movie_id = ?, screen_name = ?, show_time = ?, ticket_price = ?, total_seats = ?, available_seats = ? WHERE screening_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, screening.getMovieId());
            stmt.setString(2, screening.getScreenName());
            stmt.setTimestamp(3, Timestamp.valueOf(screening.getShowTime()));
            stmt.setDouble(4, screening.getTicketPrice());
            stmt.setInt(5, screening.getTotalSeats());
            stmt.setInt(6, screening.getAvailableSeats());
            stmt.setInt(7, screening.getScreeningId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating screening: " + e.getMessage());
        }
        return false;
    }

    public List<Screening> getScreeningsByMovie(int movieId) {
        List<Screening> screenings = new ArrayList<>();
        String sql = """
            SELECT s.*, m.title 
            FROM screenings s 
            JOIN movies m ON s.movie_id = m.movie_id 
            WHERE s.movie_id = ? 
            ORDER BY s.show_time
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, movieId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Screening screening = new Screening();
                screening.setScreeningId(rs.getInt("screening_id"));
                screening.setMovieId(rs.getInt("movie_id"));
                screening.setMovieTitle(rs.getString("title"));
                screening.setScreenName(rs.getString("screen_name"));
                screening.setShowTime(rs.getTimestamp("show_time").toLocalDateTime());
                screening.setTicketPrice(rs.getDouble("ticket_price"));
                screening.setTotalSeats(rs.getInt("total_seats"));
                screening.setAvailableSeats(rs.getInt("available_seats"));
                screenings.add(screening);
            }
        } catch (SQLException e) {
            System.err.println("Error getting screenings by movie: " + e.getMessage());
        }
        return screenings;
    }

    public Screening getScreeningById(int screeningId) {
        String sql = """
            SELECT s.*, m.title 
            FROM screenings s 
            JOIN movies m ON s.movie_id = m.movie_id 
            WHERE s.screening_id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, screeningId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Screening screening = new Screening();
                screening.setScreeningId(rs.getInt("screening_id"));
                screening.setMovieId(rs.getInt("movie_id"));
                screening.setMovieTitle(rs.getString("title"));
                screening.setScreenName(rs.getString("screen_name"));
                screening.setShowTime(rs.getTimestamp("show_time").toLocalDateTime());
                screening.setTicketPrice(rs.getDouble("ticket_price"));
                screening.setTotalSeats(rs.getInt("total_seats"));
                screening.setAvailableSeats(rs.getInt("available_seats"));
                return screening;
            }
        } catch (SQLException e) {
            System.err.println("Error getting screening by ID: " + e.getMessage());
        }
        return null;
    }

    // Booking operations
    public List<Booking> getBookingsByUser(int userId) {
        List<Booking> bookings = new ArrayList<>();
        String sql = """
            SELECT b.*, s.show_time, s.screen_name, m.title 
            FROM bookings b 
            JOIN screenings s ON b.screening_id = s.screening_id 
            JOIN movies m ON s.movie_id = m.movie_id 
            WHERE b.user_id = ? 
            ORDER BY b.booking_date DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Booking booking = new Booking();
                booking.setBookingId(rs.getInt("booking_id"));
                booking.setUserId(rs.getInt("user_id"));
                booking.setScreeningId(rs.getInt("screening_id"));
                booking.setSeatIds(rs.getString("seat_ids"));
                booking.setTotalAmount(rs.getDouble("total_amount"));
                booking.setBookingDate(rs.getTimestamp("booking_date").toLocalDateTime());
                booking.setStatus(BookingStatus.valueOf(rs.getString("status")));
                booking.setMovieTitle(rs.getString("title"));
                booking.setScreenName(rs.getString("screen_name"));
                booking.setShowTime(rs.getTimestamp("show_time").toLocalDateTime());
                bookings.add(booking);
            }
        } catch (SQLException e) {
            System.err.println("Error getting bookings by user: " + e.getMessage());
        }
        return bookings;
    }

    public boolean addBooking(Booking booking) {
        String sql = "INSERT INTO bookings (user_id, screening_id, seat_ids, total_amount, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, booking.getUserId());
            stmt.setInt(2, booking.getScreeningId());
            stmt.setString(3, booking.getSeatIds());
            stmt.setDouble(4, booking.getTotalAmount());
            stmt.setString(5, booking.getStatus().toString());

            int result = stmt.executeUpdate();
            if (result > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    booking.setBookingId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding booking: " + e.getMessage());
        }
        return false;
    }

    // Alias method for getUserBookings - delegates to getBookingsByUser
    public List<Booking> getUserBookings(int userId) {
        return getBookingsByUser(userId);
    }

    // Get all bookings for admin view
    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String sql = """
            SELECT b.*, s.show_time, s.screen_name, m.title, u.username, u.full_name
            FROM bookings b 
            JOIN screenings s ON b.screening_id = s.screening_id 
            JOIN movies m ON s.movie_id = m.movie_id 
            JOIN users u ON b.user_id = u.user_id
            ORDER BY b.booking_date DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Booking booking = new Booking();
                booking.setBookingId(rs.getInt("booking_id"));
                booking.setUserId(rs.getInt("user_id"));
                booking.setScreeningId(rs.getInt("screening_id"));
                booking.setSeatIds(rs.getString("seat_ids"));
                booking.setTotalAmount(rs.getDouble("total_amount"));
                booking.setBookingDate(rs.getTimestamp("booking_date").toLocalDateTime());
                booking.setStatus(BookingStatus.valueOf(rs.getString("status")));
                booking.setMovieTitle(rs.getString("title"));
                booking.setScreenName(rs.getString("screen_name"));
                booking.setShowTime(rs.getTimestamp("show_time").toLocalDateTime());
                booking.setUsername(rs.getString("username"));
                booking.setFullName(rs.getString("full_name"));
                bookings.add(booking);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all bookings: " + e.getMessage());
        }
        return bookings;
    }

    // Update booking status (for cancellations, etc.)
    public boolean updateBookingStatus(int bookingId, BookingStatus newStatus) {
        String sql = "UPDATE bookings SET status = ? WHERE booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus.toString());
            stmt.setInt(2, bookingId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating booking status: " + e.getMessage());
        }
        return false;
    }

    // Search bookings by user name, movie title, or booking ID
    public List<Booking> searchBookings(String searchTerm) {
        List<Booking> bookings = new ArrayList<>();
        String sql = """
            SELECT b.*, s.show_time, s.screen_name, m.title, u.username, u.full_name
            FROM bookings b 
            JOIN screenings s ON b.screening_id = s.screening_id 
            JOIN movies m ON s.movie_id = m.movie_id 
            JOIN users u ON b.user_id = u.user_id
            WHERE CAST(b.booking_id AS CHAR) LIKE ? 
               OR u.full_name LIKE ? 
               OR u.username LIKE ? 
               OR m.title LIKE ?
            ORDER BY b.booking_date DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Booking booking = new Booking();
                booking.setBookingId(rs.getInt("booking_id"));
                booking.setUserId(rs.getInt("user_id"));
                booking.setScreeningId(rs.getInt("screening_id"));
                booking.setSeatIds(rs.getString("seat_ids"));
                booking.setTotalAmount(rs.getDouble("total_amount"));
                booking.setBookingDate(rs.getTimestamp("booking_date").toLocalDateTime());
                booking.setStatus(BookingStatus.valueOf(rs.getString("status")));
                booking.setMovieTitle(rs.getString("title"));
                booking.setScreenName(rs.getString("screen_name"));
                booking.setShowTime(rs.getTimestamp("show_time").toLocalDateTime());
                booking.setUsername(rs.getString("username"));
                booking.setFullName(rs.getString("full_name"));
                bookings.add(booking);
            }
        } catch (SQLException e) {
            System.err.println("Error searching bookings: " + e.getMessage());
        }
        return bookings;
    }

    public List<User> getUsersWithMessages(int adminId) {
        List<User> users = new ArrayList<>();
        String sql = """
            SELECT DISTINCT u.* FROM users u 
            JOIN messages m ON (u.user_id = m.sender_id OR u.user_id = m.receiver_id) 
            WHERE u.user_id != ? AND u.role = 'USER'
            AND EXISTS (
                SELECT 1 FROM messages m2 
                WHERE (m2.sender_id = u.user_id OR m2.receiver_id = u.user_id)
                AND (m2.sender_id IN (SELECT user_id FROM users WHERE role = 'ADMIN') 
                     OR m2.receiver_id IN (SELECT user_id FROM users WHERE role = 'ADMIN'))
            )
            ORDER BY (
                SELECT MAX(timestamp) FROM messages 
                WHERE sender_id = u.user_id OR receiver_id = u.user_id
            ) DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, adminId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getString("full_name"),
                    UserRole.valueOf(rs.getString("role"))
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting users with messages: " + e.getMessage());
        }
        return users;
    }

    /**
     * Get chat history between a user and any admin (for shared admin inbox)
     * This allows all admins to see all user conversations
     */
    public List<ChatMessage> getChatHistoryWithAdmins(int userId) {
        List<ChatMessage> messages = new ArrayList<>();
        String sql = """
            SELECT m.*, 
                   s.full_name as sender_name, 
                   r.full_name as receiver_name,
                   s.role as sender_role,
                   r.role as receiver_role
            FROM messages m
            JOIN users s ON m.sender_id = s.user_id
            JOIN users r ON m.receiver_id = r.user_id
            WHERE (m.sender_id = ? AND r.role = 'ADMIN') 
               OR (m.receiver_id = ? AND s.role = 'ADMIN')
            ORDER BY m.timestamp ASC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, userId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ChatMessage message = new ChatMessage();
                message.setMessageId(rs.getInt("message_id"));
                message.setSenderId(rs.getInt("sender_id"));
                message.setReceiverId(rs.getInt("receiver_id"));
                message.setContent(rs.getString("content"));
                message.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                message.setRead(rs.getBoolean("is_read"));
                message.setSenderName(rs.getString("sender_name"));
                message.setReceiverName(rs.getString("receiver_name"));
                messages.add(message);
            }
        } catch (SQLException e) {
            System.err.println("Error getting chat history with admins: " + e.getMessage());
        }
        return messages;
    }

    /**
     * Get the first available admin user ID for initial message routing
     */
    public int getFirstAdminId() {
        String sql = "SELECT user_id FROM users WHERE role = 'ADMIN' LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            System.err.println("Error getting first admin ID: " + e.getMessage());
        }
        return -1;
    }

    // Current user management - synchronized for thread safety
    public synchronized void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public synchronized User getCurrentUser() {
        return currentUser;
    }

    /**
     * Get the notification service instance
     */
    public RealTimeNotificationService getNotificationService() {
        return notificationService;
    }

    /**
     * Start typing indicator for a user
     */
    public void startTyping(int userId, int chatWithUserId) {
        if (notificationService != null) {
            notificationService.notifyTypingStarted(userId, chatWithUserId);
        }
    }

    /**
     * Stop typing indicator for a user
     */
    public void stopTyping(int userId, int chatWithUserId) {
        if (notificationService != null) {
            notificationService.notifyTypingStopped(userId, chatWithUserId);
        }
    }

    private void initializeSampleData() {
        // Check if admin user exists, if not create one
        if (!isUsernameExists("admin")) {
            User adminUser = new User(0, "admin", "admin123", "admin@movieticket.com", "System Administrator", UserRole.ADMIN);
            registerUser(adminUser);
        }

        // Add sample movies if none exist
        if (getAllMovies().isEmpty()) {
            addSampleMovies();
            // Add sample screenings after movies are created
            addSampleScreenings();
        }
    }

    private void addSampleMovies() {
        Movie movie1 = new Movie();
        movie1.setTitle("Annihilation");
        movie1.setDirector("Alex Garland");
        movie1.setReleaseYear(2018);
        movie1.setDescription("A biologist signs up for a dangerous, secret expedition into a mysterious zone where the laws of nature don’t apply.");
        movie1.setDuration(115);
        movie1.setGenre("Sci-Fi, Horror");
        movie1.setRating(6.8);
        movie1.setPosterUrl("");
        addMovie(movie1);

        Movie movie2 = new Movie();
        movie2.setTitle("Charlie");
        movie2.setDirector("Martin Prakkat");
        movie2.setReleaseYear(2015);
        movie2.setDescription("A young, nonconforming woman named Tessa gets entangled in a cat-and-mouse chase in the by-lanes of Kerala, hunting for a mysterious artist who previously lived in her apartment.");
        movie2.setDuration(130);
        movie2.setGenre("Drama, Romance");
        movie2.setRating(8.1);
        movie2.setPosterUrl("");
        addMovie(movie2);

        Movie movie3 = new Movie();
        movie3.setTitle("Blade Runner 2049");
        movie3.setDirector("Denis Villeneuve");
        movie3.setReleaseYear(2017);
        movie3.setDescription("Thirty years after the events of the first film, a new blade runner, LAPD Officer K, unearths a long-buried secret that has the potential to plunge what’s left of society into chaos. K’s discovery leads him on a quest to find Rick Deckard, a former LAPD blade runner who has been missing for 30 years.");
        movie3.setDuration(164);
        movie3.setGenre("Sci-Fi, Drama");
        movie3.setRating(8.0);
        movie3.setPosterUrl("");
        addMovie(movie3);
    }

    private void addSampleScreenings() {
        List<Movie> movies = getAllMovies();
        if (movies.isEmpty()) return;

        LocalDateTime baseDate = LocalDateTime.now().plusDays(1); // Start tomorrow

        for (Movie movie : movies) {
            // Create multiple screenings for each movie
            for (int day = 0; day < 3; day++) { // 3 days of screenings
                for (int timeSlot = 0; timeSlot < 3; timeSlot++) { // 3 time slots per day
                    LocalDateTime screeningTime = baseDate.plusDays(day).withHour(14 + (timeSlot * 3)).withMinute(0).withSecond(0);

                    Screening screening = new Screening();
                    screening.setMovieId(movie.getMovieId());
                    screening.setScreenName("Screen " + ((timeSlot % 2) + 1)); // Alternate between Screen 1 and Screen 2
                    screening.setShowTime(screeningTime);
                    screening.setTicketPrice(12.50 + (timeSlot * 2.50)); // Varying prices
                    screening.setTotalSeats(100);
                    screening.setAvailableSeats(100);

                    addScreening(screening);
                }
            }
        }
    }

    private void createSeatsForScreening(int screeningId) {
        String sql = "INSERT INTO seats (screening_id, seat_number, `row_number`) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Create seats A1-A10, B1-B10, etc. (10 rows, 10 seats each = 100 seats)
            for (char row = 'A'; row <= 'J'; row++) {
                for (int seatNum = 1; seatNum <= 10; seatNum++) {
                    stmt.setInt(1, screeningId);
                    stmt.setString(2, String.valueOf(row) + seatNum); // Combined seat name like A1, A2, etc.
                    stmt.setString(3, String.valueOf(row));
                    stmt.addBatch();
                }
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            System.err.println("Error creating seats for screening: " + e.getMessage());
        }
    }

    public List<Seat> getSeatsByScreening(int screeningId) {
        List<Seat> seats = new ArrayList<>();
        String sql = "SELECT * FROM seats WHERE screening_id = ? ORDER BY `row_number`, CAST(SUBSTRING(seat_number, 2) AS UNSIGNED)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, screeningId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Seat seat = new Seat();
                seat.setSeatId(rs.getInt("seat_id"));
                seat.setScreeningId(rs.getInt("screening_id"));
                seat.setSeatNumber(rs.getString("seat_number"));
                seat.setRowNumber(rs.getString("row_number"));
                seat.setBooked(rs.getBoolean("is_booked"));
                seats.add(seat);
            }
        } catch (SQLException e) {
            System.err.println("Error getting seats by screening: " + e.getMessage());
        }

        // If no seats exist for this screening, create them
        if (seats.isEmpty()) {
            createSeatsForScreening(screeningId);
            return getSeatsByScreening(screeningId); // Recursive call to get the newly created seats
        }

        // Check if seats have old format (numeric seat_number) and recreate if needed
        if (!seats.isEmpty() && seats.get(0).getSeatNumber().matches("\\d+")) {
            recreateSeatsWithNewFormat(screeningId);
            return getSeatsByScreening(screeningId); // Recursive call to get the newly created seats
        }

        return seats;
    }

    private void recreateSeatsWithNewFormat(int screeningId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Delete existing seats for this screening
            String deleteSql = "DELETE FROM seats WHERE screening_id = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
            deleteStmt.setInt(1, screeningId);
            deleteStmt.executeUpdate();
            deleteStmt.close();

            // Create new seats with proper format
            createSeatsForScreening(screeningId);
        } catch (SQLException e) {
            System.err.println("Error recreating seats with new format: " + e.getMessage());
        }
    }

    // Synchronized booking method to prevent race conditions with seat selection
    public synchronized Booking createBooking(int userId, int screeningId, List<String> seatNumbers, double totalAmount) {
        // Use synchronized block with specific screening lock for better granularity
        synchronized (lock) {
            Connection conn = null;
            try {
                conn = DatabaseConnection.getConnection();
                conn.setAutoCommit(false); // Start transaction

                // First check if seats are still available before booking
                String checkSeatSql = "SELECT seat_number FROM seats WHERE screening_id = ? AND seat_number IN (" +
                    String.join(",", Collections.nCopies(seatNumbers.size(), "?")) + ") AND is_booked = TRUE";
                PreparedStatement checkStmt = conn.prepareStatement(checkSeatSql);
                checkStmt.setInt(1, screeningId);
                for (int i = 0; i < seatNumbers.size(); i++) {
                    checkStmt.setString(i + 2, seatNumbers.get(i));
                }

                ResultSet bookedSeats = checkStmt.executeQuery();
                if (bookedSeats.next()) {
                    // Some seats are already booked
                    checkStmt.close();
                    conn.rollback();
                    return null; // Booking failed - seats no longer available
                }
                checkStmt.close();

                // Then, mark seats as booked
                String updateSeatSql = "UPDATE seats SET is_booked = TRUE WHERE screening_id = ? AND seat_number = ?";
                PreparedStatement seatStmt = conn.prepareStatement(updateSeatSql);

                for (String seatNumber : seatNumbers) {
                    seatStmt.setInt(1, screeningId);
                    seatStmt.setString(2, seatNumber);
                    seatStmt.addBatch();
                }

                seatStmt.executeBatch();
                seatStmt.close();

                // Update available seats count
                String updateScreeningSql = "UPDATE screenings SET available_seats = available_seats - ? WHERE screening_id = ?";
                PreparedStatement screeningStmt = conn.prepareStatement(updateScreeningSql);
                screeningStmt.setInt(1, seatNumbers.size());
                screeningStmt.setInt(2, screeningId);
                screeningStmt.executeUpdate();
                screeningStmt.close();

                // Create booking record - store actual seat names instead of seat IDs
                String insertBookingSql = "INSERT INTO bookings (user_id, screening_id, seat_ids, total_amount, status) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement bookingStmt = conn.prepareStatement(insertBookingSql, Statement.RETURN_GENERATED_KEYS);
                bookingStmt.setInt(1, userId);
                bookingStmt.setInt(2, screeningId);
                bookingStmt.setString(3, String.join(",", seatNumbers)); // Store actual seat names like "A1,A2,A3"
                bookingStmt.setDouble(4, totalAmount);
                bookingStmt.setString(5, Booking.BookingStatus.CONFIRMED.toString());

                int result = bookingStmt.executeUpdate();
                if (result > 0) {
                    ResultSet keys = bookingStmt.getGeneratedKeys();
                    if (keys.next()) {
                        int bookingId = keys.getInt(1);
                        conn.commit(); // Commit transaction

                        Booking booking = new Booking();
                        booking.setBookingId(bookingId);
                        booking.setUserId(userId);
                        booking.setScreeningId(screeningId);
                        booking.setSeatIds(String.join(",", seatNumbers)); // Store seat names, not IDs
                        booking.setSeatNumbers(seatNumbers); // Set the actual seat names list
                        booking.setTotalAmount(totalAmount);
                        booking.setStatus(Booking.BookingStatus.CONFIRMED);
                        booking.setBookingDate(LocalDateTime.now());

                        bookingStmt.close();
                        return booking;
                    }
                }
                bookingStmt.close();

            } catch (SQLException e) {
                try {
                    if (conn != null) conn.rollback(); // Rollback on error
                } catch (SQLException rollbackEx) {
                    System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
                }
                System.err.println("Error creating booking: " + e.getMessage());
            } finally {
                try {
                    if (conn != null) {
                        conn.setAutoCommit(true);
                        conn.close();
                    }
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
        return null;
    }

    public void logout() {
        currentUser = null;
    }

    // =======================
    // REVIEW MANAGEMENT METHODS
    // =======================

    /**
     * Add a new review from a user
     */
    public boolean addReview(Review review) {
        String sql = "INSERT INTO reviews (user_id, rating, title, comment, review_type, review_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, review.getUserId());
            stmt.setInt(2, review.getRating());
            stmt.setString(3, review.getTitle());
            stmt.setString(4, review.getComment());
            stmt.setString(5, review.getReviewType().name());
            stmt.setTimestamp(6, Timestamp.valueOf(review.getReviewDate()));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    review.setReviewId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding review: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Update an existing review
     */
    public boolean updateReview(Review review) {
        String sql = "UPDATE reviews SET rating = ?, title = ?, comment = ?, review_type = ? WHERE review_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, review.getRating());
            stmt.setString(2, review.getTitle());
            stmt.setString(3, review.getComment());
            stmt.setString(4, review.getReviewType().name());
            stmt.setInt(5, review.getReviewId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating review: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get all reviews
     */
    public List<Review> getAllReviews() {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT r.*, u.full_name FROM reviews r " +
                     "JOIN users u ON r.user_id = u.user_id " +
                     "ORDER BY r.review_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Review review = new Review(
                    rs.getInt("review_id"),
                    rs.getInt("user_id"),
                    rs.getString("full_name"),
                    rs.getInt("rating"),
                    rs.getString("title"),
                    rs.getString("comment"),
                    rs.getTimestamp("review_date").toLocalDateTime(),
                    Review.ReviewType.valueOf(rs.getString("review_type"))
                );
                reviews.add(review);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching reviews: " + e.getMessage());
        }
        return reviews;
    }

    /**
     * Get reviews by user
     */
    public List<Review> getReviewsByUser(int userId) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT r.*, u.full_name FROM reviews r " +
                     "JOIN users u ON r.user_id = u.user_id " +
                     "WHERE r.user_id = ? ORDER BY r.review_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Review review = new Review(
                    rs.getInt("review_id"),
                    rs.getInt("user_id"),
                    rs.getString("full_name"),
                    rs.getInt("rating"),
                    rs.getString("title"),
                    rs.getString("comment"),
                    rs.getTimestamp("review_date").toLocalDateTime(),
                    Review.ReviewType.valueOf(rs.getString("review_type"))
                );
                reviews.add(review);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user reviews: " + e.getMessage());
        }
        return reviews;
    }

    /**
     * Get reviews by type
     */
    public List<Review> getReviewsByType(Review.ReviewType type) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT r.*, u.full_name FROM reviews r " +
                     "JOIN users u ON r.user_id = u.user_id " +
                     "WHERE r.review_type = ? ORDER BY r.review_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, type.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Review review = new Review(
                    rs.getInt("review_id"),
                    rs.getInt("user_id"),
                    rs.getString("full_name"),
                    rs.getInt("rating"),
                    rs.getString("title"),
                    rs.getString("comment"),
                    rs.getTimestamp("review_date").toLocalDateTime(),
                    Review.ReviewType.valueOf(rs.getString("review_type"))
                );
                reviews.add(review);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching reviews by type: " + e.getMessage());
        }
        return reviews;
    }

    /**
     * Get average rating from all reviews
     */
    public double getAverageRating() {
        String sql = "SELECT AVG(rating) as avg_rating FROM reviews";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble("avg_rating");
            }
        } catch (SQLException e) {
            System.err.println("Error calculating average rating: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Get total number of reviews
     */
    public int getTotalReviewCount() {
        String sql = "SELECT COUNT(*) as count FROM reviews";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error counting reviews: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Get review distribution by rating
     */
    public Map<Integer, Integer> getReviewDistribution() {
        Map<Integer, Integer> distribution = new HashMap<>();
        // Initialize with all ratings
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0);
        }

        String sql = "SELECT rating, COUNT(*) as count FROM reviews GROUP BY rating";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                distribution.put(rs.getInt("rating"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting review distribution: " + e.getMessage());
        }
        return distribution;
    }

    /**
     * Delete a review
     */
    public boolean deleteReview(int reviewId) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reviewId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting review: " + e.getMessage());
        }
        return false;
    }

    // =======================
    // SEAT LOCKING METHODS
    // =======================

    /**
     * Temporarily lock seats for a user (2-minute hold)
     */
    public synchronized boolean lockSeats(int screeningId, List<String> seatNumbers, int userId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // First, clean up expired locks
            cleanupExpiredSeatLocks();

            // Check if any of the seats are already locked or booked
            String checkSql = "SELECT seat_number FROM seats WHERE screening_id = ? AND seat_number IN (" +
                String.join(",", Collections.nCopies(seatNumbers.size(), "?")) + ") AND is_booked = TRUE " +
                "UNION " +
                "SELECT seat_number FROM seat_locks WHERE screening_id = ? AND seat_number IN (" +
                String.join(",", Collections.nCopies(seatNumbers.size(), "?")) + ") AND expires_at > NOW() AND user_id != ?";

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, screeningId);
                for (int i = 0; i < seatNumbers.size(); i++) {
                    checkStmt.setString(i + 2, seatNumbers.get(i));
                }
                checkStmt.setInt(seatNumbers.size() + 2, screeningId);
                for (int i = 0; i < seatNumbers.size(); i++) {
                    checkStmt.setString(seatNumbers.size() + 3 + i, seatNumbers.get(i));
                }
                checkStmt.setInt(seatNumbers.size() * 2 + 3, userId);

                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    conn.rollback();
                    return false; // Some seats are already locked/booked
                }
            }

            // Remove any existing locks for this user on this screening
            String removeLocksSql = "DELETE FROM seat_locks WHERE screening_id = ? AND user_id = ?";
            try (PreparedStatement removeLocks = conn.prepareStatement(removeLocksSql)) {
                removeLocks.setInt(1, screeningId);
                removeLocks.setInt(2, userId);
                removeLocks.executeUpdate();
            }

            // Create new locks
            String lockSql = "INSERT INTO seat_locks (screening_id, seat_number, user_id, expires_at) VALUES (?, ?, ?, ?)";
            try (PreparedStatement lockStmt = conn.prepareStatement(lockSql)) {
                LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(SeatLock.LOCK_DURATION_MINUTES);

                for (String seatNumber : seatNumbers) {
                    lockStmt.setInt(1, screeningId);
                    lockStmt.setString(2, seatNumber);
                    lockStmt.setInt(3, userId);
                    lockStmt.setTimestamp(4, Timestamp.valueOf(expiresAt));
                    lockStmt.addBatch();
                }

                lockStmt.executeBatch();
            }

            conn.commit();

            // Notify observers about seat locks
            for (String seatNumber : seatNumbers) {
                notificationService.notifySeatLocked(screeningId, seatNumber, userId);
            }

            return true;

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error rolling back seat lock transaction: " + rollbackEx.getMessage());
            }
            System.err.println("Error locking seats: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Error resetting auto-commit: " + e.getMessage());
                }
                DatabaseConnection.releaseConnection(conn);
            }
        }
    }

    /**
     * Release seat locks for a user
     */
    public synchronized void releaseSeatLocks(int screeningId, int userId) {
        // First, get the seat numbers that will be unlocked
        List<String> seatNumbersToUnlock = new ArrayList<>();
        String selectSql = "SELECT seat_number FROM seat_locks WHERE screening_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectSql)) {

            stmt.setInt(1, screeningId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                seatNumbersToUnlock.add(rs.getString("seat_number"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching seat locks: " + e.getMessage());
        }

        // Now delete the locks
        String deleteSql = "DELETE FROM seat_locks WHERE screening_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSql)) {

            stmt.setInt(1, screeningId);
            stmt.setInt(2, userId);
            int deletedRows = stmt.executeUpdate();

            if (deletedRows > 0) {
                // Notify observers about each unlocked seat
                for (String seatNumber : seatNumbersToUnlock) {
                    notificationService.notifySeatUnlocked(screeningId, seatNumber);
                }

                // Also send a general seat update
                List<Seat> updatedSeats = getSeatsByScreeningWithLocks(screeningId);
                notificationService.notifySeatUpdated(screeningId, updatedSeats);
            }

        } catch (SQLException e) {
            System.err.println("Error releasing seat locks: " + e.getMessage());
        }
    }

    /**
     * Unlock a specific seat for a user
     */
    public synchronized void unlockSeat(int screeningId, String seatNumber, int userId) {
        String sql = "DELETE FROM seat_locks WHERE screening_id = ? AND seat_number = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, screeningId);
            stmt.setString(2, seatNumber);
            stmt.setInt(3, userId);
            int deletedRows = stmt.executeUpdate();

            if (deletedRows > 0) {
                // Notify observers about seat unlock
                notificationService.notifySeatUnlocked(screeningId, seatNumber);
            }

        } catch (SQLException e) {
            System.err.println("Error unlocking seat: " + e.getMessage());
        }
    }

    /**
     * Start a background task to periodically clean up expired seat locks
     */
    private void startSeatLockCleanupTask() {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30000); // Run every 30 seconds
                    cleanupExpiredSeatLocks();
                } catch (InterruptedException e) {
                    System.err.println("Seat lock cleanup task interrupted: " + e.getMessage());
                    break;
                }
            }
        });
        cleanupThread.setDaemon(true); // Make it a daemon thread so it doesn't prevent JVM shutdown
        cleanupThread.setName("SeatLockCleanupThread");
        cleanupThread.start();
    }

    /**
     * Clean up expired seat locks from the database
     */
    private synchronized void cleanupExpiredSeatLocks() {
        String sql = "DELETE FROM seat_locks WHERE expires_at <= NOW()";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int deletedRows = stmt.executeUpdate();
            if (deletedRows > 0) {
                System.out.println("Cleaned up " + deletedRows + " expired seat locks");
            }
        } catch (SQLException e) {
            System.err.println("Error cleaning up expired seat locks: " + e.getMessage());
        }
    }

    /**
     * Get seats with lock information for real-time updates
     */
    public List<Seat> getSeatsByScreeningWithLocks(int screeningId) {
        List<Seat> seats = new ArrayList<>();
        String sql = """
            SELECT s.*, sl.user_id as locked_by_user, sl.expires_at
            FROM seats s 
            LEFT JOIN seat_locks sl ON s.screening_id = sl.screening_id AND s.seat_number = sl.seat_number AND sl.expires_at > NOW()
            WHERE s.screening_id = ? 
            ORDER BY s.row_number, CAST(SUBSTRING(s.seat_number, 2) AS UNSIGNED)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, screeningId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Seat seat = new Seat();
                seat.setSeatId(rs.getInt("seat_id"));
                seat.setScreeningId(rs.getInt("screening_id"));
                seat.setSeatNumber(rs.getString("seat_number"));
                seat.setRowNumber(rs.getString("row_number"));
                seat.setBooked(rs.getBoolean("is_booked"));

                // Set lock information
                int lockedByUser = rs.getInt("locked_by_user");
                if (lockedByUser > 0) {
                    seat.setLocked(true);
                    seat.setLockedByUserId(lockedByUser);
                    Timestamp expiresAt = rs.getTimestamp("expires_at");
                    if (expiresAt != null) {
                        seat.setLockExpiresAt(expiresAt.toLocalDateTime());
                    }
                } else {
                    seat.setLocked(false);
                    seat.setLockedByUserId(0);
                    seat.setLockExpiresAt(null);
                }

                seats.add(seat);
            }
        } catch (SQLException e) {
            System.err.println("Error getting seats with locks: " + e.getMessage());
        }

        // If no seats exist for this screening, create them
        if (seats.isEmpty()) {
            createSeatsForScreening(screeningId);
            return getSeatsByScreeningWithLocks(screeningId);
        }

        return seats;
    }

    /**
     * Enhanced booking method with seat lock verification
     */
    public synchronized Booking createBookingWithLocks(int userId, int screeningId, List<String> seatNumbers, double totalAmount) {
        synchronized (lock) {
            Connection conn = null;
            try {
                conn = DatabaseConnection.getConnection();
                conn.setAutoCommit(false);

                // Verify user has valid locks for these seats
                String verifyLocksSql = "SELECT COUNT(*) FROM seat_locks WHERE screening_id = ? AND user_id = ? AND seat_number IN (" +
                    String.join(",", Collections.nCopies(seatNumbers.size(), "?")) + ") AND expires_at > NOW()";
                PreparedStatement verifyStmt = conn.prepareStatement(verifyLocksSql);
                verifyStmt.setInt(1, screeningId);
                verifyStmt.setInt(2, userId);
                for (int i = 0; i < seatNumbers.size(); i++) {
                    verifyStmt.setString(i + 3, seatNumbers.get(i));
                }

                ResultSet lockResult = verifyStmt.executeQuery();
                lockResult.next();
                int validLocks = lockResult.getInt(1);
                verifyStmt.close();

                if (validLocks != seatNumbers.size()) {
                    conn.rollback();
                    return null; // User doesn't have valid locks for all seats
                }

                // Check if seats are still available
                String checkSeatSql = "SELECT seat_number FROM seats WHERE screening_id = ? AND seat_number IN (" +
                    String.join(",", Collections.nCopies(seatNumbers.size(), "?")) + ") AND is_booked = TRUE";
                PreparedStatement checkStmt = conn.prepareStatement(checkSeatSql);
                checkStmt.setInt(1, screeningId);
                for (int i = 0; i < seatNumbers.size(); i++) {
                    checkStmt.setString(i + 2, seatNumbers.get(i));
                }

                ResultSet bookedSeats = checkStmt.executeQuery();
                if (bookedSeats.next()) {
                    checkStmt.close();
                    conn.rollback();
                    return null; // Some seats are already booked
                }
                checkStmt.close();

                // Mark seats as booked
                String updateSeatSql = "UPDATE seats SET is_booked = TRUE WHERE screening_id = ? AND seat_number = ?";
                PreparedStatement seatStmt = conn.prepareStatement(updateSeatSql);

                for (String seatNumber : seatNumbers) {
                    seatStmt.setInt(1, screeningId);
                    seatStmt.setString(2, seatNumber);
                    seatStmt.addBatch();
                }

                seatStmt.executeBatch();
                seatStmt.close();

                // Remove seat locks
                String removeLocksSql = "DELETE FROM seat_locks WHERE screening_id = ? AND user_id = ? AND seat_number IN (" +
                    String.join(",", Collections.nCopies(seatNumbers.size(), "?")) + ")";
                PreparedStatement removeLocks = conn.prepareStatement(removeLocksSql);
                removeLocks.setInt(1, screeningId);
                removeLocks.setInt(2, userId);
                for (int i = 0; i < seatNumbers.size(); i++) {
                    removeLocks.setString(i + 3, seatNumbers.get(i));
                }
                removeLocks.executeUpdate();
                removeLocks.close();

                // Update available seats count
                String updateScreeningSql = "UPDATE screenings SET available_seats = available_seats - ? WHERE screening_id = ?";
                PreparedStatement screeningStmt = conn.prepareStatement(updateScreeningSql);
                screeningStmt.setInt(1, seatNumbers.size());
                screeningStmt.setInt(2, screeningId);
                screeningStmt.executeUpdate();
                screeningStmt.close();

                // Create booking record
                String insertBookingSql = "INSERT INTO bookings (user_id, screening_id, seat_ids, total_amount, status) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement bookingStmt = conn.prepareStatement(insertBookingSql, Statement.RETURN_GENERATED_KEYS);
                bookingStmt.setInt(1, userId);
                bookingStmt.setInt(2, screeningId);
                bookingStmt.setString(3, String.join(",", seatNumbers));
                bookingStmt.setDouble(4, totalAmount);
                bookingStmt.setString(5, Booking.BookingStatus.CONFIRMED.toString());

                int result = bookingStmt.executeUpdate();
                if (result > 0) {
                    ResultSet keys = bookingStmt.getGeneratedKeys();
                    if (keys.next()) {
                        int bookingId = keys.getInt(1);
                        conn.commit();

                        // Notify observers about seat bookings
                        for (String seatNumber : seatNumbers) {
                            notificationService.notifySeatBooked(screeningId, seatNumber);
                        }

                        // Create and return booking object
                        Booking booking = new Booking();
                        booking.setBookingId(bookingId);
                        booking.setUserId(userId);
                        booking.setScreeningId(screeningId);
                        booking.setSeatIds(String.join(",", seatNumbers));
                        booking.setSeatNumbers(seatNumbers);
                        booking.setTotalAmount(totalAmount);
                        booking.setStatus(Booking.BookingStatus.CONFIRMED);
                        booking.setBookingDate(LocalDateTime.now());

                        bookingStmt.close();
                        return booking;
                    }
                }
                bookingStmt.close();

            } catch (SQLException e) {
                try {
                    if (conn != null) conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error rolling back booking transaction: " + rollbackEx.getMessage());
                }
                System.err.println("Error creating booking with locks: " + e.getMessage());
            } finally {
                try {
                    if (conn != null) {
                        conn.setAutoCommit(true);
                        conn.close();
                    }
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
        return null;
    }


    // Statistical methods for admin dashboard
    public int getTotalTicketsSold() {
        int totalTickets = 0;
        String sql = "SELECT seat_ids FROM bookings WHERE status = 'CONFIRMED'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String seatIds = rs.getString("seat_ids");
                if (seatIds != null && !seatIds.trim().isEmpty()) {
                    // Count the number of seats by splitting the comma-separated seat IDs
                    String[] seats = seatIds.split(",");
                    totalTickets += seats.length;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting total tickets sold: " + e.getMessage());
        }
        return totalTickets;
    }

    public double getTotalRevenue() {
        String sql = "SELECT SUM(total_amount) FROM bookings WHERE status = 'CONFIRMED'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting total revenue: " + e.getMessage());
        }
        return 0.0;
    }

    public int getAvailableMoviesCount() {
        String sql = "SELECT COUNT(*) FROM movies";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting available movies count: " + e.getMessage());
        }
        return 0;
    }

    public int getTotalUsersCount() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'USER'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting total users count: " + e.getMessage());
        }
        return 0;
    }

    public List<User> searchUsers(String searchTerm) {
        List<User> users = new ArrayList<>();

        // Check if search term is a number (user ID search)
        boolean isNumericSearch = searchTerm.matches("\\d+");

        String sql;
        if (isNumericSearch) {
            // Search by user ID or other fields
            sql = "SELECT * FROM users WHERE user_id = ? OR username LIKE ? OR full_name LIKE ? OR email LIKE ? ORDER BY full_name";
        } else {
            // Search by username, full name, or email only
            sql = "SELECT * FROM users WHERE username LIKE ? OR full_name LIKE ? OR email LIKE ? ORDER BY full_name";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchTerm + "%";

            if (isNumericSearch) {
                stmt.setInt(1, Integer.parseInt(searchTerm));
                stmt.setString(2, searchPattern);
                stmt.setString(3, searchPattern);
                stmt.setString(4, searchPattern);
            } else {
                stmt.setString(1, searchPattern);
                stmt.setString(2, searchPattern);
                stmt.setString(3, searchPattern);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getString("full_name"),
                    UserRole.valueOf(rs.getString("role"))
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error searching users: " + e.getMessage());
        }
        return users;
    }

    public boolean updateUser(User user) {
        String sql = "UPDATE users SET username = ?, email = ?, full_name = ?, password = ?, role = ?, profile_picture_path = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getPassword());
            stmt.setString(5, user.getRole().toString());
            stmt.setString(6, user.getProfilePicturePath());
            stmt.setInt(7, user.getUserId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
        }
        return false;
    }

    /**
     * Update user profile (username, email, full name, profile picture - used by users to edit their own profile)
     * Also updates the current user object if it's the same user
     */
    public boolean updateUserProfile(User user) {
        String sql = "UPDATE users SET username = ?, email = ?, full_name = ?, profile_picture_path = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getProfilePicturePath());
            stmt.setInt(5, user.getUserId());

            boolean success = stmt.executeUpdate() > 0;

            // If successful and this is the current user, update the current user object
            if (success && currentUser != null && currentUser.getUserId() == user.getUserId()) {
                currentUser.setUsername(user.getUsername());
                currentUser.setEmail(user.getEmail());
                currentUser.setFullName(user.getFullName());
                currentUser.setProfilePicturePath(user.getProfilePicturePath());
            }

            return success;
        } catch (SQLException e) {
            System.err.println("Error updating user profile: " + e.getMessage());
        }
        return false;
    }

    /**
     * Reset user password by verifying username and email
     */
    public boolean resetUserPassword(String username, String email, String newPassword) {
        // First verify that the username and email combination exists
        String verifySql = "SELECT user_id FROM users WHERE username = ? AND email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement verifyStmt = conn.prepareStatement(verifySql)) {

            verifyStmt.setString(1, username);
            verifyStmt.setString(2, email);
            ResultSet rs = verifyStmt.executeQuery();

            if (rs.next()) {
                // User found, update the password
                int userId = rs.getInt("user_id");
                String updateSql = "UPDATE users SET password = ? WHERE user_id = ?";

                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, newPassword);
                    updateStmt.setInt(2, userId);

                    int rowsUpdated = updateStmt.executeUpdate();
                    return rowsUpdated > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error resetting user password: " + e.getMessage());
        }
        return false;
    }

    public boolean updateUserPassword(int userId, String currentPassword, String newPassword) {
        // First verify the current password
        String verifySQL = "SELECT password FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement verifyStmt = conn.prepareStatement(verifySQL)) {

            verifyStmt.setInt(1, userId);
            ResultSet rs = verifyStmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                if (!storedPassword.equals(currentPassword)) {
                    return false; // Current password doesn't match
                }
            } else {
                return false; // User not found
            }
        } catch (SQLException e) {
            System.err.println("Error verifying current password: " + e.getMessage());
            return false;
        }

        // Update the password
        String updateSQL = "UPDATE users SET password = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {

            updateStmt.setString(1, newPassword);
            updateStmt.setInt(2, userId);

            boolean result = updateStmt.executeUpdate() > 0;

            // Update current user password if it's the same user
            if (result && currentUser != null && currentUser.getUserId() == userId) {
                currentUser.setPassword(newPassword);
            }

            return result;
        } catch (SQLException e) {
            System.err.println("Error updating user password: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteUser(int userId) {
        // Don't allow deletion of admin users
        User user = getUserById(userId);
        if (user != null && user.getRole() == UserRole.ADMIN) {
            return false;
        }

        String sql = "DELETE FROM users WHERE user_id = ? AND role != 'ADMIN'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteScreening(int screeningId) {
        String sql = "DELETE FROM screenings WHERE screening_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, screeningId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting screening: " + e.getMessage());
        }
        return false;
    }

    // =======================
    // CHAT MESSAGING METHODS
    // =======================

    /**
     * Send a chat message between users
     */
    public boolean sendMessage(int senderId, int receiverId, String content) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, content) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setString(3, content);

            int result = stmt.executeUpdate();
            if (result > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    int messageId = keys.getInt(1);

                    // Create ChatMessage object and notify observers
                    ChatMessage message = new ChatMessage(senderId, receiverId, content);
                    message.setMessageId(messageId);
                    message.setTimestamp(LocalDateTime.now());

                    // Get sender and receiver names
                    User sender = getUserById(senderId);
                    User receiver = getUserById(receiverId);
                    if (sender != null) message.setSenderName(sender.getFullName());
                    if (receiver != null) message.setReceiverName(receiver.getFullName());

                    // Notify observers about new message
                    notificationService.notifyMessageReceived(message);

                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get chat history between two users
     */
    public List<ChatMessage> getChatHistory(int userId1, int userId2) {
        List<ChatMessage> messages = new ArrayList<>();
        String sql = """
            SELECT m.*, 
                   s.full_name as sender_name, 
                   r.full_name as receiver_name
            FROM messages m
            JOIN users s ON m.sender_id = s.user_id
            JOIN users r ON m.receiver_id = r.user_id
            WHERE (m.sender_id = ? AND m.receiver_id = ?) 
               OR (m.sender_id = ? AND m.receiver_id = ?)
            ORDER BY m.timestamp ASC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId1);
            stmt.setInt(2, userId2);
            stmt.setInt(3, userId2);
            stmt.setInt(4, userId1);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ChatMessage message = new ChatMessage();
                message.setMessageId(rs.getInt("message_id"));
                message.setSenderId(rs.getInt("sender_id"));
                message.setReceiverId(rs.getInt("receiver_id"));
                message.setContent(rs.getString("content"));
                message.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                message.setRead(rs.getBoolean("is_read"));
                message.setSenderName(rs.getString("sender_name"));
                message.setReceiverName(rs.getString("receiver_name"));
                messages.add(message);
            }
        } catch (SQLException e) {
            System.err.println("Error getting chat history: " + e.getMessage());
        }
        return messages;
    }

    /**
     * Mark messages as read
     */
    public boolean markMessagesAsRead(int receiverId, int senderId) {
        String sql = "UPDATE messages SET is_read = TRUE WHERE receiver_id = ? AND sender_id = ? AND is_read = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, receiverId);
            stmt.setInt(2, senderId);

            int updatedRows = stmt.executeUpdate();
            if (updatedRows > 0) {
                // Notify observers about messages being read
                notificationService.notifyMessageRead(0, receiverId); // messageId 0 indicates bulk update
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error marking messages as read: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get unread message count for a user
     */
    public int getUnreadMessageCount(int userId) {
        String sql = "SELECT COUNT(*) FROM messages WHERE receiver_id = ? AND is_read = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting unread message count: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Delete entire chat history between two users (admin only)
     */
    public boolean deleteChatHistory(int userId1, int userId2) {
        String sql = "DELETE FROM messages WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId1);
            stmt.setInt(2, userId2);
            stmt.setInt(3, userId2);
            stmt.setInt(4, userId1);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting chat history: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get all users who have chatted with admin (for admin dashboard)
     */
    public List<User> getUsersWithUnreadMessages(int adminId) {
        List<User> users = new ArrayList<>();
        String sql = """
            SELECT DISTINCT u.*, 
                   COUNT(m.message_id) as unread_count,
                   MAX(m.timestamp) as last_message_time
            FROM users u 
            JOIN messages m ON u.user_id = m.sender_id 
            WHERE m.receiver_id = ? AND m.is_read = FALSE AND u.role = 'USER'
            GROUP BY u.user_id, u.username, u.password, u.email, u.full_name, u.role
            ORDER BY last_message_time DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, adminId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getString("full_name"),
                    UserRole.valueOf(rs.getString("role"))
                );
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error getting users with unread messages: " + e.getMessage());
        }
        return users;
    }

    // Statistics methods for charts
    public Map<String, Integer> getBookingsByStatus() {
        Map<String, Integer> statusMap = new LinkedHashMap<>();
        String sql = "SELECT status, COUNT(*) as count FROM bookings GROUP BY status";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                statusMap.put(rs.getString("status"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting bookings by status: " + e.getMessage());
        }
        return statusMap;
    }

    public Map<String, Integer> getMoviesByGenre() {
        Map<String, Integer> genreMap = new HashMap<>();
        String sql = "SELECT genre FROM movies";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String genreString = rs.getString("genre");
                if (genreString != null && !genreString.isEmpty()) {
                    // Split by comma and trim whitespace
                    String[] genres = genreString.split(",");
                    for (String genre : genres) {
                        String trimmedGenre = genre.trim();
                        if (!trimmedGenre.isEmpty()) {
                            genreMap.put(trimmedGenre, genreMap.getOrDefault(trimmedGenre, 0) + 1);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting movies by genre: " + e.getMessage());
        }

        // Sort by count in descending order
        return genreMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);
    }

    public Map<String, Double> getRevenueByMovie() {
        Map<String, Double> revenueMap = new LinkedHashMap<>();
        String sql = """
            SELECT m.title, SUM(b.total_amount) as revenue
            FROM bookings b
            JOIN screenings s ON b.screening_id = s.screening_id
            JOIN movies m ON s.movie_id = m.movie_id
            WHERE b.status = 'CONFIRMED'
            GROUP BY m.movie_id, m.title
            ORDER BY revenue DESC
            LIMIT 10
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                revenueMap.put(rs.getString("title"), rs.getDouble("revenue"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting revenue by movie: " + e.getMessage());
        }
        return revenueMap;
    }

    public Map<String, Integer> getUsersByRole() {
        Map<String, Integer> roleMap = new LinkedHashMap<>();
        String sql = "SELECT role, COUNT(*) as count FROM users GROUP BY role";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                roleMap.put(rs.getString("role"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting users by role: " + e.getMessage());
        }
        return roleMap;
    }

    public int getTotalTicketsBooked(int userId) {
        int totalTickets = 0;
        String sql = "SELECT seat_ids FROM bookings WHERE user_id = ? AND status = 'CONFIRMED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String seatIds = rs.getString("seat_ids");
                if (seatIds != null && !seatIds.trim().isEmpty()) {
                    // Count the number of seats by splitting the comma-separated seat IDs
                    String[] seats = seatIds.split(",");
                    totalTickets += seats.length;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting total tickets booked for user: " + e.getMessage());
        }
        return totalTickets;
    }

    public double getTotalAmountSpent(int userId) {
        String sql = "SELECT SUM(total_amount) FROM bookings WHERE user_id = ? AND status = 'CONFIRMED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting total amount spent for user: " + e.getMessage());
        }
        return 0.0;
    }

    // Payment-related methods

    /**
     * Save payment information to the database
     */
    public boolean savePayment(Payment payment) {
        String sql = "INSERT INTO payments (user_id, booking_id, payment_method, mobile_banking_provider, amount, status, transaction_id, card_last_four_digits, mobile_number) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, payment.getUserId());
            stmt.setInt(2, payment.getBookingId());
            stmt.setString(3, payment.getPaymentMethod().toString());
            stmt.setString(4, payment.getMobileBankingProvider().toString());
            stmt.setDouble(5, payment.getAmount());
            stmt.setString(6, payment.getStatus().toString());
            stmt.setString(7, payment.getTransactionId());
            stmt.setString(8, payment.getCardLastFourDigits());
            stmt.setString(9, payment.getMobileNumber());

            int result = stmt.executeUpdate();
            if (result > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    payment.setPaymentId(keys.getInt(1));
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving payment: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get payment by booking ID
     */
    public Payment getPaymentByBookingId(int bookingId) {
        String sql = "SELECT * FROM payments WHERE booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Payment payment = new Payment();
                payment.setPaymentId(rs.getInt("payment_id"));
                payment.setBookingId(rs.getInt("booking_id"));
                payment.setPaymentMethod(Payment.PaymentMethod.valueOf(rs.getString("payment_method")));
                payment.setMobileBankingProvider(Payment.MobileBankingProvider.valueOf(rs.getString("mobile_banking_provider")));
                payment.setAmount(rs.getDouble("amount"));
                payment.setPaymentDate(rs.getTimestamp("payment_date").toLocalDateTime());
                payment.setStatus(Payment.PaymentStatus.valueOf(rs.getString("status")));
                payment.setTransactionId(rs.getString("transaction_id"));
                payment.setCardLastFourDigits(rs.getString("card_last_four_digits"));
                payment.setMobileNumber(rs.getString("mobile_number"));
                return payment;
            }
        } catch (SQLException e) {
            System.err.println("Error getting payment by booking ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get all payments for a user
     */
    public List<Payment> getPaymentsByUserId(int userId) {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT p.* FROM payments p JOIN bookings b ON p.booking_id = b.booking_id WHERE b.user_id = ? ORDER BY p.payment_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Payment payment = new Payment();
                payment.setPaymentId(rs.getInt("payment_id"));
                payment.setBookingId(rs.getInt("booking_id"));
                payment.setPaymentMethod(Payment.PaymentMethod.valueOf(rs.getString("payment_method")));
                payment.setMobileBankingProvider(Payment.MobileBankingProvider.valueOf(rs.getString("mobile_banking_provider")));
                payment.setAmount(rs.getDouble("amount"));
                payment.setPaymentDate(rs.getTimestamp("payment_date").toLocalDateTime());
                payment.setStatus(Payment.PaymentStatus.valueOf(rs.getString("status")));
                payment.setTransactionId(rs.getString("transaction_id"));
                payment.setCardLastFourDigits(rs.getString("card_last_four_digits"));
                payment.setMobileNumber(rs.getString("mobile_number"));
                payments.add(payment);
            }
        } catch (SQLException e) {
            System.err.println("Error getting payments for user: " + e.getMessage());
        }
        return payments;
    }

    /**
     * Update payment status
     */
    public boolean updatePaymentStatus(int paymentId, Payment.PaymentStatus status) {
        String sql = "UPDATE payments SET status = ? WHERE payment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.toString());
            stmt.setInt(2, paymentId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating payment status: " + e.getMessage());
        }
        return false;
    }
}
