package com.example.movieticket.model;

public class User {
    private int userId;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private UserRole role;
    private String profilePicturePath;

    public enum UserRole {
        ADMIN, USER
    }

    public User() {}

    public User(int userId, String username, String password, String email, String fullName, UserRole role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    public User(int userId, String username, String password, String email, String fullName, UserRole role, String profilePicturePath) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.profilePicturePath = profilePicturePath;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getProfilePicturePath() { return profilePicturePath; }
    public void setProfilePicturePath(String profilePicturePath) { this.profilePicturePath = profilePicturePath; }

    @Override
    public String toString() {
        return fullName + " (" + username + ")";
    }
}
