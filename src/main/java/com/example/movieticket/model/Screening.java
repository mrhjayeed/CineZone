package com.example.movieticket.model;

import java.time.LocalDateTime;

public class Screening {
    private int screeningId;
    private int movieId;
    private String screenName;
    private LocalDateTime dateTime;
    private double ticketPrice;
    private int totalSeats;
    private int availableSeats;
    private String movieTitle; // Added missing field

    public Screening() {}

    public Screening(int screeningId, int movieId, String screenName, LocalDateTime dateTime,
                     double ticketPrice, int totalSeats) {
        this.screeningId = screeningId;
        this.movieId = movieId;
        this.screenName = screenName;
        this.dateTime = dateTime;
        this.ticketPrice = ticketPrice;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
    }

    // Getters and Setters
    public int getScreeningId() { return screeningId; }
    public void setScreeningId(int screeningId) { this.screeningId = screeningId; }

    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }

    public String getScreenName() { return screenName; }
    public void setScreenName(String screenName) { this.screenName = screenName; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    // Alias for compatibility with existing code
    public LocalDateTime getShowTime() { return dateTime; }
    public void setShowTime(LocalDateTime showTime) { this.dateTime = showTime; }

    public double getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(double ticketPrice) { this.ticketPrice = ticketPrice; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }

    @Override
    public String toString() {
        return screenName + " - " + dateTime.toLocalDate() + " " +
                dateTime.toLocalTime() + " ($" + ticketPrice + ")";
    }
}
