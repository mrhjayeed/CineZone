package com.example.movieticket.model;

import java.time.LocalDateTime;

public class Seat {
    private int seatId;
    private int screeningId;
    private String seatNumber;
    private int row;
    private int column;
    private String rowNumber; // Added for database compatibility
    private boolean isBooked;

    // New fields for real-time seat locking
    private boolean isLocked;
    private int lockedByUserId;
    private LocalDateTime lockExpiresAt;

    public Seat() {}

    public Seat(int seatId, int screeningId, String seatNumber, int row, int column) {
        this.seatId = seatId;
        this.screeningId = screeningId;
        this.seatNumber = seatNumber;
        this.row = row;
        this.column = column;
        this.rowNumber = String.valueOf((char)('A' + row - 1)); // Convert int to letter
        this.isBooked = false;
        this.isLocked = false;
        this.lockedByUserId = 0;
        this.lockExpiresAt = null;
    }

    // Check if seat is available for selection (not booked and not locked by another user)
    public boolean isAvailableForUser(int userId) {
        if (isBooked) return false;
        if (!isLocked) return true;
        return lockedByUserId == userId; // Available if locked by the same user
    }

    // Check if the lock has expired
    public boolean isLockExpired() {
        if (!isLocked || lockExpiresAt == null) return false;
        return LocalDateTime.now().isAfter(lockExpiresAt);
    }

    // Get remaining lock time in seconds
    public long getRemainingLockTimeSeconds() {
        if (!isLocked || lockExpiresAt == null) return 0;
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(lockExpiresAt)) return 0;
        return java.time.Duration.between(now, lockExpiresAt).getSeconds();
    }

    // Getters and Setters
    public int getSeatId() { return seatId; }
    public void setSeatId(int seatId) { this.seatId = seatId; }

    public int getScreeningId() { return screeningId; }
    public void setScreeningId(int screeningId) { this.screeningId = screeningId; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; }

    public int getColumn() { return column; }
    public void setColumn(int column) { this.column = column; }

    public String getRowNumber() { return rowNumber; }
    public void setRowNumber(String rowNumber) {
        this.rowNumber = rowNumber;
        // Also update the int row field if rowNumber is a letter
        if (rowNumber != null && rowNumber.length() == 1) {
            this.row = rowNumber.charAt(0) - 'A' + 1;
        }
    }

    public boolean isBooked() { return isBooked; }
    public void setBooked(boolean booked) { isBooked = booked; }

    // New lock-related getters and setters
    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { isLocked = locked; }

    public int getLockedByUserId() { return lockedByUserId; }
    public void setLockedByUserId(int lockedByUserId) { this.lockedByUserId = lockedByUserId; }

    public LocalDateTime getLockExpiresAt() { return lockExpiresAt; }
    public void setLockExpiresAt(LocalDateTime lockExpiresAt) { this.lockExpiresAt = lockExpiresAt; }

    // Setter for remainingLockTimeSeconds - ignores the value since it's computed
    public void setRemainingLockTimeSeconds(long remainingLockTimeSeconds) {
        // This setter is needed for JSON deserialization but we ignore the value
        // since remainingLockTimeSeconds is a computed field based on lockExpiresAt
    }

    // Setter for lockExpired - ignores the value since it's computed
    public void setLockExpired(boolean lockExpired) {
        // This setter is needed for JSON deserialization but we ignore the value
        // since lockExpired is a computed field based on lockExpiresAt and current time
    }

    @Override
    public String toString() {
        return "Seat{" +
                "seatId=" + seatId +
                ", screeningId=" + screeningId +
                ", seatNumber='" + seatNumber + '\'' +
                ", row=" + row +
                ", column=" + column +
                ", rowNumber='" + rowNumber + '\'' +
                ", isBooked=" + isBooked +
                ", isLocked=" + isLocked +
                ", lockedByUserId=" + lockedByUserId +
                ", lockExpiresAt=" + lockExpiresAt +
                '}';
    }
}
