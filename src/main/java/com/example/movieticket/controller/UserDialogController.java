package com.example.movieticket.controller;

import com.example.movieticket.model.User;
import com.example.movieticket.model.User.UserRole;
import com.example.movieticket.service.DataService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class UserDialogController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField fullNameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<UserRole> roleComboBox;
    @FXML private ImageView profileImageView;
    @FXML private Button uploadPhotoButton;
    @FXML private Button deletePhotoButton;

    private Stage dialogStage;
    private User user;
    private boolean okClicked = false;
    private DataService dataService = DataService.getInstance();
    private UserRole originalRole; // Store the original role
    private String selectedImagePath = null;
    private boolean photoDeleted = false;
    private static final String PROFILE_PICTURES_DIR = "profile_pictures";

    @FXML
    private void initialize() {
        roleComboBox.getItems().addAll(UserRole.values());
        setupProfileImage();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setUser(User user) {
        this.user = user;

        if (user != null) {
            usernameField.setText(user.getUsername());
            emailField.setText(user.getEmail());
            fullNameField.setText(user.getFullName());
            passwordField.setText(user.getPassword());
            roleComboBox.setValue(user.getRole());
            originalRole = user.getRole(); // Store the original role
            loadProfilePicture();
        }
    }

    private void setupProfileImage() {
        if (profileImageView != null) {
            // Make the image circular
            Circle clip = new Circle(40, 40, 40);
            profileImageView.setClip(clip);
        }
    }

    private void loadProfilePicture() {
        if (user != null && user.getProfilePicturePath() != null && !user.getProfilePicturePath().isEmpty()) {
            File imageFile = new File(user.getProfilePicturePath());
            if (imageFile.exists()) {
                Image image = new Image(imageFile.toURI().toString());
                profileImageView.setImage(image);
                return;
            }
        }
        // Load default image
        loadDefaultImage();
    }

    private void loadDefaultImage() {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/icon.png"));
            profileImageView.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("Error loading default profile image: " + e.getMessage());
        }
    }

    @FXML
    private void handleUploadPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(dialogStage);
        if (selectedFile != null) {
            try {
                // Create profile pictures directory if it doesn't exist
                Path profilePicturesPath = Paths.get(PROFILE_PICTURES_DIR);
                if (!Files.exists(profilePicturesPath)) {
                    Files.createDirectories(profilePicturesPath);
                }

                // Generate unique filename (use existing user ID if available, or temporary name)
                String fileExtension = getFileExtension(selectedFile.getName());
                String newFileName;
                if (user != null && user.getUserId() > 0) {
                    newFileName = "user_" + user.getUserId() + "_" + System.currentTimeMillis() + fileExtension;
                } else {
                    newFileName = "user_temp_" + System.currentTimeMillis() + fileExtension;
                }
                Path destinationPath = profilePicturesPath.resolve(newFileName);

                // Copy file to profile pictures directory
                Files.copy(selectedFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

                // Update image view
                Image image = new Image(destinationPath.toUri().toString());
                profileImageView.setImage(image);

                // Store the path for later use
                selectedImagePath = destinationPath.toString();
                photoDeleted = false; // Reset deletion flag

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Profile picture uploaded successfully!");
                alert.showAndWait();

            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Upload Failed");
                alert.setHeaderText(null);
                alert.setContentText("Failed to upload profile picture: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void handleDeletePhoto() {
        // Check if there's a photo to delete
        if (user != null && user.getProfilePicturePath() != null && !user.getProfilePicturePath().isEmpty()) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Delete Profile Picture");
            confirmAlert.setHeaderText("Are you sure?");
            confirmAlert.setContentText("Do you want to delete the current profile picture?");

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Mark photo for deletion
                    photoDeleted = true;
                    selectedImagePath = null;

                    // Load default image
                    loadDefaultImage();

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Profile picture will be deleted when you save.");
                    successAlert.showAndWait();
                }
            });
        } else if (selectedImagePath != null) {
            // If a new photo was uploaded but not saved yet
            selectedImagePath = null;
            photoDeleted = true;
            loadDefaultImage();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Profile picture removed.");
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Photo");
            alert.setHeaderText(null);
            alert.setContentText("There is no profile picture to delete.");
            alert.showAndWait();
        }
    }

    private String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return fileName.substring(lastIndexOf);
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    public User getUser() {
        return user;
    }

    @FXML
    private void handleOk() {
        if (isInputValid()) {
            if (user == null) {
                // Adding new user (if needed in the future)
                user = new User();
            }

            user.setUsername(usernameField.getText());
            user.setEmail(emailField.getText());
            user.setFullName(fullNameField.getText());
            user.setPassword(passwordField.getText());
            user.setRole(roleComboBox.getValue());

            // Handle profile picture changes
            if (photoDeleted) {
                // Delete the profile picture
                user.setProfilePicturePath(null);
            } else if (selectedImagePath != null) {
                // Set new profile picture path
                user.setProfilePicturePath(selectedImagePath);
            }

            okClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (usernameField.getText() == null || usernameField.getText().trim().isEmpty()) {
            errorMessage += "Username is required!\n";
        }

        if (emailField.getText() == null || emailField.getText().trim().isEmpty()) {
            errorMessage += "Email is required!\n";
        } else if (!emailField.getText().contains("@")) {
            errorMessage += "Invalid email format!\n";
        }

        if (fullNameField.getText() == null || fullNameField.getText().trim().isEmpty()) {
            errorMessage += "Full name is required!\n";
        }

        if (passwordField.getText() == null || passwordField.getText().trim().isEmpty()) {
            errorMessage += "Password is required!\n";
        }

        if (roleComboBox.getValue() == null) {
            errorMessage += "Role must be selected!\n";
        }

        // Check if this is the last admin trying to change their role to user
        if (user != null && originalRole == UserRole.ADMIN &&
            roleComboBox.getValue() == UserRole.USER) {

            int adminCount = dataService.getAdminCount();

            if (adminCount <= 1) {
                errorMessage += "Cannot change role! You are the only admin in the system.\n" +
                               "At least one admin must remain in the system.\n";
            }
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Input");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
    }
}
