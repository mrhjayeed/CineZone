# API Documentation - CineZone Movie Ticket Booking System

## Table of Contents

1. [Overview](#overview)
2. [Authentication API](#authentication-api)
3. [User Management API](#user-management-api)
4. [Movie Management API](#movie-management-api)
5. [Screening API](#screening-api)
6. [Booking API](#booking-api)
7. [Payment API](#payment-api)
8. [Review API](#review-api)
9. [Chat API](#chat-api)
10. [Network Protocol](#network-protocol)

---

## Overview

This document describes the internal API methods available in the CineZone application. All API calls are implemented as static methods in the `DataService` class and interact directly with the MySQL database.

### Base Configuration

```java
package com.example.movieticket.service;

public class DataService {
    // All methods are static and can be called directly
    // Example: DataService.authenticateUser(username, password);
}
```

---

## Authentication API

### Authenticate User

Validates user credentials and returns user object if successful.

**Method:**
```java
public static User authenticateUser(String username, String password)
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| username | String | Yes | Username or email |
| password | String | Yes | User password (plain text, hashed internally) |

**Returns:**
- `User` object if authentication successful
- `null` if authentication fails

**Example:**
```java
User user = DataService.authenticateUser("john_doe", "password123");
if (user != null) {
    System.out.println("Login successful: " + user.getFullName());
    // Proceed to dashboard
} else {
    System.out.println("Invalid credentials");
}
```

**Error Handling:**
```java
try {
    User user = DataService.authenticateUser(username, password);
} catch (Exception e) {
    // Handle database connection errors
    e.printStackTrace();
}
```

---

### Register User

Creates a new user account.

**Method:**
```java
public static boolean createUser(User user)
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| user | User | Yes | User object with registration details |

**User Object Fields:**
- `username` (String, required): Unique username
- `password` (String, required): User password
- `email` (String, required): Valid email address
- `fullName` (String, required): User's full name
- `role` (UserRole, optional): Default is USER

**Returns:**
- `true` if user created successfully
- `false` if creation fails (duplicate username/email)

**Example:**
```java
User newUser = new User();
newUser.setUsername("jane_doe");
newUser.setPassword("SecurePass123");
newUser.setEmail("jane@example.com");
newUser.setFullName("Jane Doe");
newUser.setRole(User.UserRole.USER);

boolean success = DataService.createUser(newUser);
if (success) {
    System.out.println("Account created successfully");
} else {
    System.out.println("Username or email already exists");
}
```

---

### Password Management

**Reset Password:**
```java
public static boolean resetPassword(String email, String newPassword)
```

**Change Password:**
```java
public static boolean changePassword(int userId, String oldPassword, String newPassword)
```

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| email | String | User's registered email |
| userId | int | User ID |
| oldPassword | String | Current password for verification |
| newPassword | String | New password |

**Example:**
```java
// Reset password
boolean reset = DataService.resetPassword("user@example.com", "NewPassword123");

// Change password
boolean changed = DataService.changePassword(1, "OldPass123", "NewPass123");
```

---

## User Management API

### Get User by ID

Retrieves user information by user ID.

**Method:**
```java
public static User getUserById(int userId)
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| userId | int | Yes | User ID |

**Returns:**
- `User` object if found
- `null` if not found

**Example:**
```java
User user = DataService.getUserById(1);
if (user != null) {
    System.out.println("User: " + user.getFullName());
}
```

---

### Get All Users

Retrieves all registered users (admin only).

**Method:**
```java
public static List<User> getAllUsers()
```

**Returns:**
- `List<User>` containing all users

**Example:**
```java
List<User> users = DataService.getAllUsers();
for (User user : users) {
    System.out.println(user.getUsername() + " - " + user.getRole());
}
```

---

### Update User

Updates user profile information.

**Method:**
```java
public static boolean updateUser(User user)
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| user | User | Yes | User object with updated information |

**Updatable Fields:**
- Full name
- Email
- Profile picture path
- Role (admin only)

**Returns:**
- `true` if update successful
- `false` if update fails

**Example:**
```java
User user = DataService.getUserById(1);
user.setFullName("John Smith");
user.setEmail("john.smith@example.com");

boolean updated = DataService.updateUser(user);
if (updated) {
    System.out.println("Profile updated successfully");
}
```

---

### Delete User

Deletes a user account (admin only).

**Method:**
```java
public static boolean deleteUser(int userId)
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| userId | int | Yes | User ID to delete |

**Returns:**
- `true` if deletion successful
- `false` if deletion fails

**Cascade Effects:**
- All user bookings are deleted
- All user reviews are deleted
- All user messages are deleted

**Example:**
```java
boolean deleted = DataService.deleteUser(5);
if (deleted) {
    System.out.println("User account deleted");
}
```

---

## Movie Management API

### Get All Movies

Retrieves all movies in the catalog.

**Method:**
```java
public static List<Movie> getAllMovies()
```

**Returns:**
- `List<Movie>` containing all movies

**Example:**
```java
List<Movie> movies = DataService.getAllMovies();
for (Movie movie : movies) {
    System.out.println(movie.getTitle() + " (" + movie.getReleaseYear() + ")");
}
```

---

### Get Movie by ID

Retrieves a specific movie by ID.

**Method:**
```java
public static Movie getMovieById(int movieId)
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| movieId | int | Yes | Movie ID |

**Returns:**
- `Movie` object if found
- `null` if not found

**Example:**
```java
Movie movie = DataService.getMovieById(1);
if (movie != null) {
    System.out.println("Title: " + movie.getTitle());
    System.out.println("Director: " + movie.getDirector());
    System.out.println("Rating: " + movie.getRating());
}
```

---

### Add Movie

Adds a new movie to the catalog (admin only).

**Method:**
```java
public static boolean addMovie(Movie movie)
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| movie | Movie | Yes | Movie object with all details |

**Required Fields:**
- title
- director
- releaseYear
- duration
- genre

**Optional Fields:**
- description
- rating
- posterUrl
- trailerUrl

**Returns:**
- `true` if movie added successfully
- `false` if addition fails

**Example:**
```java
Movie movie = new Movie();
movie.setTitle("Inception");
movie.setDirector("Christopher Nolan");
movie.setReleaseYear(2010);
movie.setDuration(148);
movie.setGenre("Sci-Fi");
movie.setDescription("A thief who steals corporate secrets...");
movie.setRating(4.8);
movie.setPosterUrl("/posters/inception.jpg");
movie.setTrailerUrl("https://youtube.com/watch?v=...");

boolean added = DataService.addMovie(movie);
if (added) {
    System.out.println("Movie added successfully");
}
```

---

### Update Movie

Updates movie information (admin only).

**Method:**
```java
public static boolean updateMovie(Movie movie)
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| movie | Movie | Yes | Movie object with updated information |

**Returns:**
- `true` if update successful
- `false` if update fails

**Example:**
```java
Movie movie = DataService.getMovieById(1);
movie.setRating(4.9);
movie.setDescription("Updated description...");

boolean updated = DataService.updateMovie(movie);
```

---

### Delete Movie

Deletes a movie from the catalog (admin only).

**Method:**
```java
public static boolean deleteMovie(int movieId)
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| movieId | int | Yes | Movie ID to delete |

**Returns:**
- `true` if deletion successful
- `false` if deletion fails

**Cascade Effects:**
- All screenings for this movie are deleted
- All bookings for these screenings are deleted

**Example:**
```java
boolean deleted = DataService.deleteMovie(5);
```

---

## Screening API

### Get Screenings for Movie

Retrieves all screenings for a specific movie.

**Method:**
```java
public static List<Screening> getScreeningsForMovie(int movieId)
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| movieId | int | Yes | Movie ID |

**Returns:**
- `List<Screening>` containing all screenings for the movie

**Example:**
```java
List<Screening> screenings = DataService.getScreeningsForMovie(1);
for (Screening screening : screenings) {
    System.out.println("Screen: " + screening.getScreenName());
    System.out.println("Time: " + screening.getShowTime());
    System.out.println("Price: $" + screening.getTicketPrice());
    System.out.println("Available: " + screening.getAvailableSeats());
}
```

---

### Get All Screenings

Retrieves all screenings (admin only).

**Method:**
```java
public static List<Screening> getAllScreenings()
```

**Returns:**
- `List<Screening>` containing all screenings

**Example:**
```java
List<Screening> allScreenings = DataService.getAllScreenings();
```

---

### Add Screening

Creates a new screening (admin only).

**Method:**
```java
public static boolean addScreening(Screening screening)
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| screening | Screening | Yes | Screening object with details |

**Required Fields:**
- movieId
- screenName
- showTime
- ticketPrice
- totalSeats
- availableSeats

**Returns:**
- `true` if screening created successfully
- `false` if creation fails

**Example:**
```java
Screening screening = new Screening();
screening.setMovieId(1);
screening.setScreenName("Screen 1");
screening.setShowTime(LocalDateTime.of(2024, 10, 25, 18, 30));
screening.setTicketPrice(12.50);
screening.setTotalSeats(100);
screening.setAvailableSeats(100);

boolean added = DataService.addScreening(screening);
```

---

### Update Screening

Updates screening information (admin only).

**Method:**
```java
public static boolean updateScreening(Screening screening)
```

**Example:**
```java
Screening screening = DataService.getScreeningById(1);
screening.setTicketPrice(15.00);
boolean updated = DataService.updateScreening(screening);
```

---

### Delete Screening

Deletes a screening (admin only).

**Method:**
```java
public static boolean deleteScreening(int screeningId)
```

**Cascade Effects:**
- All seats for this screening are deleted
- All bookings are deleted

---

## Booking API

### Get Seats for Screening

Retrieves all seats and their status for a screening.

**Method:**
```java
public static List<Seat> getSeatsForScreening(int screeningId)
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| screeningId | int | Yes | Screening ID |

**Returns:**
- `List<Seat>` containing all seats with booking status

**Example:**
```java
List<Seat> seats = DataService.getSeatsForScreening(1);
for (Seat seat : seats) {
    String status = seat.isBooked() ? "Booked" : "Available";
    System.out.println("Seat " + seat.getRowNumber() + seat.getSeatNumber() + ": " + status);
}
```

---

### Lock Seats

Temporarily locks seats during booking process.

**Method:**
```java
public static boolean lockSeats(int screeningId, List<String> seatNumbers, int userId)
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| screeningId | int | Yes | Screening ID |
| seatNumbers | List<String> | Yes | List of seat identifiers (e.g., "A1", "A2") |
| userId | int | Yes | User ID locking the seats |

**Returns:**
- `true` if seats locked successfully
- `false` if any seat is already locked/booked

**Lock Duration:** 5 minutes

**Example:**
```java
List<String> seats = Arrays.asList("A1", "A2", "A3");
boolean locked = DataService.lockSeats(1, seats, userId);
if (locked) {
    System.out.println("Seats locked for 5 minutes");
    // Proceed to payment
} else {
    System.out.println("One or more seats unavailable");
}
```

---

### Release Seat Locks

Releases temporary seat locks.

**Method:**
```java
public static boolean releaseSeatLocks(int screeningId, int userId)
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| screeningId | int | Yes | Screening ID |
| userId | int | Yes | User ID |

**Returns:**
- `true` if locks released successfully

**Example:**
```java
// Release locks when user cancels or times out
DataService.releaseSeatLocks(screeningId, userId);
```

---

### Create Booking

Creates a confirmed booking.

**Method:**
```java
public static boolean createBooking(Booking booking)
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| booking | Booking | Yes | Booking object with details |

**Required Fields:**
- userId
- screeningId
- seatIds (comma-separated)
- totalAmount
- status

**Returns:**
- `true` if booking created successfully
- `false` if creation fails

**Example:**
```java
Booking booking = new Booking();
booking.setUserId(1);
booking.setScreeningId(1);
booking.setSeatIds("1,2,3");
booking.setTotalAmount(37.50);
booking.setStatus("CONFIRMED");

boolean booked = DataService.createBooking(booking);
if (booked) {
    System.out.println("Booking confirmed!");
}
```

---

### Get User Bookings

Retrieves all bookings for a user.

**Method:**
```java
public static List<Booking> getUserBookings(int userId)
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| userId | int | Yes | User ID |

**Returns:**
- `List<Booking>` containing all user bookings

**Example:**
```java
List<Booking> bookings = DataService.getUserBookings(userId);
for (Booking booking : bookings) {
    System.out.println("Booking #" + booking.getBookingId());
    System.out.println("Status: " + booking.getStatus());
    System.out.println("Amount: $" + booking.getTotalAmount());
}
```

---

### Cancel Booking

Cancels a booking.

**Method:**
```java
public static boolean cancelBooking(int bookingId)
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| bookingId | int | Yes | Booking ID to cancel |

**Returns:**
- `true` if cancellation successful
- `false` if cancellation fails

**Example:**
```java
boolean cancelled = DataService.cancelBooking(bookingId);
if (cancelled) {
    System.out.println("Booking cancelled, refund initiated");
}
```

---

## Payment API

### Process Payment

Records a payment transaction.

**Method:**
```java
public static boolean processPayment(Payment payment)
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| payment | Payment | Yes | Payment object with transaction details |

**Required Fields:**
- bookingId
- paymentMethod
- amount
- status

**Payment Methods:**
- CREDIT_CARD
- DEBIT_CARD
- UPI
- NET_BANKING

**Example:**
```java
Payment payment = new Payment();
payment.setBookingId(1);
payment.setPaymentMethod("CREDIT_CARD");
payment.setAmount(37.50);
payment.setStatus("COMPLETED");
payment.setTransactionId("TXN" + System.currentTimeMillis());
payment.setCardLastFourDigits("4532");

boolean processed = DataService.processPayment(payment);
if (processed) {
    System.out.println("Payment successful");
}
```

---

### Get Payment History

Retrieves payment history for a user.

**Method:**
```java
public static List<Payment> getPaymentHistory(int userId)
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| userId | int | Yes | User ID |

**Returns:**
- `List<Payment>` containing all payments

**Example:**
```java
List<Payment> payments = DataService.getPaymentHistory(userId);
```

---

## Review API

### Add Review

Submits a new review.

**Method:**
```java
public static boolean addReview(Review review)
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| review | Review | Yes | Review object with content |

**Required Fields:**
- userId
- rating (1-5)
- title
- comment
- reviewType

**Review Types:**
- THEATER_EXPERIENCE
- MOVIE_REVIEW
- SERVICE_FEEDBACK

**Returns:**
- `true` if review added successfully
- `false` if addition fails

**Example:**
```java
Review review = new Review();
review.setUserId(1);
review.setRating(5);
review.setTitle("Amazing Experience!");
review.setComment("The theater was clean and staff was friendly...");
review.setReviewType("THEATER_EXPERIENCE");

boolean added = DataService.addReview(review);
```

---

### Get All Reviews

Retrieves all community reviews.

**Method:**
```java
public static List<Review> getAllReviews()
```

**Returns:**
- `List<Review>` containing all reviews

**Example:**
```java
List<Review> reviews = DataService.getAllReviews();
for (Review review : reviews) {
    System.out.println(review.getTitle() + " - " + review.getRating() + " stars");
}
```

---

### Get User Reviews

Retrieves reviews by a specific user.

**Method:**
```java
public static List<Review> getUserReviews(int userId)
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| userId | int | Yes | User ID |

**Returns:**
- `List<Review>` containing user's reviews

---

### Delete Review

Deletes a review (user's own or admin).

**Method:**
```java
public static boolean deleteReview(int reviewId)
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| reviewId | int | Yes | Review ID to delete |

**Returns:**
- `true` if deletion successful
- `false` if deletion fails

---

## Chat API

### Send Message

Sends a chat message.

**Method:**
```java
public static boolean sendMessage(ChatMessage message)
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| message | ChatMessage | Yes | Message object |

**Required Fields:**
- senderId
- receiverId
- content

**Returns:**
- `true` if message sent successfully
- `false` if sending fails

**Example:**
```java
ChatMessage message = new ChatMessage();
message.setSenderId(1);
message.setReceiverId(2);
message.setContent("Hello, how are you?");

boolean sent = DataService.sendMessage(message);
```

---

### Get Conversation

Retrieves chat history between two users.

**Method:**
```java
public static List<ChatMessage> getConversation(int userId1, int userId2)
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| userId1 | int | Yes | First user ID |
| userId2 | int | Yes | Second user ID |

**Returns:**
- `List<ChatMessage>` containing conversation history

**Example:**
```java
List<ChatMessage> conversation = DataService.getConversation(1, 2);
for (ChatMessage msg : conversation) {
    System.out.println(msg.getSenderName() + ": " + msg.getContent());
}
```

---

### Mark Message as Read

Marks a message as read.

**Method:**
```java
public static boolean markMessageAsRead(int messageId)
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| messageId | int | Yes | Message ID |

**Returns:**
- `true` if marked successfully

---

## Network Protocol

### Real-time Communication

The application uses socket-based communication for real-time features.

**Server Configuration:**
```
Host: localhost
Port: 8888
Protocol: TCP
Serialization: Java Object Serialization
```

### Network Message Structure

```java
public class NetworkMessage implements Serializable {
    private MessageType type;
    private String content;
    private int senderId;
    private int receiverId;
    private LocalDateTime timestamp;
    
    public enum MessageType {
        CHAT_MESSAGE,
        SEAT_LOCK,
        SEAT_UNLOCK,
        BOOKING_NOTIFICATION,
        USER_STATUS,
        SYSTEM_MESSAGE
    }
}
```

### Sending Messages

```java
RealTimeNotificationService service = RealTimeNotificationService.getInstance();

NetworkMessage message = new NetworkMessage();
message.setType(MessageType.CHAT_MESSAGE);
message.setContent("Hello");
message.setSenderId(1);
message.setReceiverId(2);

service.sendMessage(message);
```

### Receiving Messages

```java
service.setMessageListener(new MessageListener() {
    @Override
    public void onMessageReceived(NetworkMessage message) {
        switch (message.getType()) {
            case CHAT_MESSAGE:
                handleChatMessage(message);
                break;
            case SEAT_LOCK:
                handleSeatLock(message);
                break;
            // ... handle other types
        }
    }
});
```

---

## Error Handling

### Common Error Codes

All API methods handle exceptions internally. Check return values:

- `null`: Record not found
- `false`: Operation failed
- `empty list`: No records found
- Exceptions are logged to console

### Best Practices

```java
// Always check for null
User user = DataService.getUserById(userId);
if (user == null) {
    System.err.println("User not found");
    return;
}

// Check boolean returns
boolean success = DataService.createBooking(booking);
if (!success) {
    System.err.println("Booking failed");
    // Rollback or retry
}

// Handle empty lists
List<Movie> movies = DataService.getAllMovies();
if (movies.isEmpty()) {
    System.out.println("No movies available");
}
```

---

## Rate Limiting

Currently, there are no rate limits on API calls. For production:

1. Implement request throttling
2. Add connection limits
3. Use caching for frequent queries
4. Monitor database load

---

## Conclusion

This API documentation covers all major operations in the CineZone system. For implementation details, refer to the `DataService.java` source code.

**API Version**: 1.0  
**Last Updated**: October 23, 2025  
**Contact**: dev@cinezone.com

