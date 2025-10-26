package com.example.movieticket.model;

import java.time.LocalDateTime;

public class SeatLock {
    private int lockId;
    private int screeningId;
    private String seatNumber;
    private int userId;
    private LocalDateTime lockTimestamp;
    private LocalDateTime expiresAt;

    // Lock duration in minutes
    public static final int LOCK_DURATION_MINUTES = 5;

    public SeatLock() {
        this.lockTimestamp = LocalDateTime.now();
        this.expiresAt = this.lockTimestamp.plusMinutes(LOCK_DURATION_MINUTES);
    }

    public SeatLock(int screeningId, String seatNumber, int userId) {
        this();
        this.screeningId = screeningId;
        this.seatNumber = seatNumber;
        this.userId = userId;
    }

    // Check if the lock has expired
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    // Get remaining time in seconds
    public long getRemainingTimeSeconds() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expiresAt)) {
            return 0;
        }
        return java.time.Duration.between(now, expiresAt).getSeconds();
    }

    // Getters and Setters
    public int getLockId() {
        return lockId;
    }

    public void setLockId(int lockId) {
        this.lockId = lockId;
    }

    public int getScreeningId() {
        return screeningId;
    }

    public void setScreeningId(int screeningId) {
        this.screeningId = screeningId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDateTime getLockTimestamp() {
        return lockTimestamp;
    }

    public void setLockTimestamp(LocalDateTime lockTimestamp) {
        this.lockTimestamp = lockTimestamp;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public String toString() {
        return "SeatLock{" +
                "lockId=" + lockId +
                ", screeningId=" + screeningId +
                ", seatNumber='" + seatNumber + '\'' +
                ", userId=" + userId +
                ", lockTimestamp=" + lockTimestamp +
                ", expiresAt=" + expiresAt +
                ", expired=" + isExpired() +
                '}';
    }
}
