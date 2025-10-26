package com.example.movieticket.model;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class Booking {
    private int bookingId;
    private int userId;
    private int screeningId;
    private List<String> seatNumbers;
    private String seatIds; // Added for database storage
    private LocalDateTime bookingTime;
    private LocalDateTime bookingDate; // Added for database compatibility
    private double totalAmount;
    private BookingStatus status;

    // Added fields for display purposes
    private String movieTitle;
    private String screenName;
    private LocalDateTime showTime;
    private String username;
    private String fullName;

    public enum BookingStatus {
        CONFIRMED, CANCELLED, PENDING
    }

    public Booking() {}

    public Booking(int bookingId, int userId, int screeningId, List<String> seatNumbers,
                   LocalDateTime bookingTime, double totalAmount, BookingStatus status) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.screeningId = screeningId;
        this.seatNumbers = seatNumbers;
        this.bookingTime = bookingTime;
        this.bookingDate = bookingTime; // Same as bookingTime
        this.totalAmount = totalAmount;
        this.status = status;
    }

    // Getters and Setters
    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getScreeningId() { return screeningId; }
    public void setScreeningId(int screeningId) { this.screeningId = screeningId; }

    public List<String> getSeatNumbers() { return seatNumbers; }
    public void setSeatNumbers(List<String> seatNumbers) { this.seatNumbers = seatNumbers; }

    public LocalDateTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    // Added missing methods for database compatibility
    public String getSeatIds() { return seatIds; }
    public void setSeatIds(String seatIds) {
        this.seatIds = seatIds;
        // Also update seatNumbers list if seatIds is provided
        if (seatIds != null && !seatIds.isEmpty()) {
            this.seatNumbers = Arrays.asList(seatIds.split(","));
        }
    }

    public LocalDateTime getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
        this.bookingTime = bookingDate; // Keep both in sync
    }

    // Added display fields
    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }

    public String getScreenName() { return screenName; }
    public void setScreenName(String screenName) { this.screenName = screenName; }

    public LocalDateTime getShowTime() { return showTime; }
    public void setShowTime(LocalDateTime showTime) { this.showTime = showTime; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    @Override
    public String toString() {
        return "Booking #" + bookingId + " - " + (seatNumbers != null ? seatNumbers.size() : 0) + " seats - $" + totalAmount;
    }
}
