package com.example.movieticket.controller;

import com.example.movieticket.model.User;
import com.example.movieticket.service.DataService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;

public class ProfileEditController implements Initializable {

    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private Label fullNameErrorLabel;
    @FXML private Label usernameErrorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private ImageView profileImageView;
    @FXML private Button uploadPhotoButton;
    @FXML private Button deletePhotoButton;

    private DataService dataService = DataService.getInstance();
    private User currentUser;
    private Stage dialogStage;
    private boolean profileUpdated = false;
    private String selectedImagePath = null;
    private boolean photoDeleted = false;
    private static final String PROFILE_PICTURES_DIR = "profile_pictures";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.currentUser = dataService.getCurrentUser();
        loadUserProfile();
        setupValidation();
        setupProfileImage();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isProfileUpdated() {
        return profileUpdated;
    }

    private void loadUserProfile() {
        if (currentUser != null) {
            fullNameField.setText(currentUser.getFullName());
            usernameField.setText(currentUser.getUsername());
            emailField.setText(currentUser.getEmail());
            loadProfilePicture();
        }
    }

    private void setupProfileImage() {
        if (profileImageView != null) {
            // Make the image circular
            Circle clip = new Circle(50, 50, 50);
            profileImageView.setClip(clip);
        }
    }

    private void loadProfilePicture() {
        if (currentUser.getProfilePicturePath() != null && !currentUser.getProfilePicturePath().isEmpty()) {
            File imageFile = new File(currentUser.getProfilePicturePath());
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

    private void setupValidation() {
        // Add real-time validation listeners
        fullNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateFullName();
        });

        usernameField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateUsername();
        });

        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateEmail();
        });
    }

    private boolean validateFullName() {
        String fullName = fullNameField.getText().trim();
        if (fullName.isEmpty()) {
            showFieldError(fullNameErrorLabel, "Full name is required");
            return false;
        } else if (fullName.length() < 2) {
            showFieldError(fullNameErrorLabel, "Full name must be at least 2 characters");
            return false;
        } else {
            hideFieldError(fullNameErrorLabel);
            return true;
        }
    }

    private boolean validateUsername() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            showFieldError(usernameErrorLabel, "Username is required");
            return false;
        } else if (username.length() < 3) {
            showFieldError(usernameErrorLabel, "Username must be at least 3 characters");
            return false;
        } else if (!username.matches("^[a-zA-Z0-9_]+$")) {
            showFieldError(usernameErrorLabel, "Username can only contain letters, numbers, and underscores");
            return false;
        } else {
            // Check if username is available (excluding current user)
            if (!username.equals(currentUser.getUsername()) &&
                !dataService.isUsernameAvailable(username, currentUser.getUserId())) {
                showFieldError(usernameErrorLabel, "Username is already taken");
                return false;
            } else {
                hideFieldError(usernameErrorLabel);
                return true;
            }
        }
    }

    private boolean validateEmail() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showFieldError(emailErrorLabel, "Email is required");
            return false;
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showFieldError(emailErrorLabel, "Please enter a valid email address");
            return false;
        } else {
            // Check if email is available (excluding current user)
            if (!email.equals(currentUser.getEmail()) &&
                !dataService.isEmailAvailable(email, currentUser.getUserId())) {
                showFieldError(emailErrorLabel, "Email is already in use");
                return false;
            } else {
                hideFieldError(emailErrorLabel);
                return true;
            }
        }
    }

    private void showFieldError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void hideFieldError(Label errorLabel) {
        errorLabel.setVisible(false);
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

                // Generate unique filename
                String fileExtension = getFileExtension(selectedFile.getName());
                String newFileName = "user_" + currentUser.getUserId() + "_" + System.currentTimeMillis() + fileExtension;
                Path destinationPath = profilePicturesPath.resolve(newFileName);

                // Copy file to profile pictures directory
                Files.copy(selectedFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

                // Update image view
                Image image = new Image(destinationPath.toUri().toString());
                profileImageView.setImage(image);

                // Store the path for later use
                selectedImagePath = destinationPath.toString();
                photoDeleted = false; // Reset deletion flag

                showAlert(Alert.AlertType.INFORMATION, "Success", "Profile picture uploaded successfully!");

            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Upload Failed", "Failed to upload profile picture: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDeletePhoto() {
        // Check if there's a photo to delete
        if (currentUser.getProfilePicturePath() != null && !currentUser.getProfilePicturePath().isEmpty()) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Delete Profile Picture");
            confirmAlert.setHeaderText("Are you sure?");
            confirmAlert.setContentText("Do you want to delete the current profile picture?");

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == javafx.scene.control.ButtonType.OK) {
                    // Mark photo for deletion
                    photoDeleted = true;
                    selectedImagePath = null;

                    // Load default image
                    loadDefaultImage();

                    showAlert(Alert.AlertType.INFORMATION, "Success", "Profile picture will be deleted when you save.");
                }
            });
        } else if (selectedImagePath != null) {
            // If a new photo was uploaded but not saved yet
            selectedImagePath = null;
            photoDeleted = true;
            loadDefaultImage();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Profile picture removed.");
        } else {
            showAlert(Alert.AlertType.INFORMATION, "No Photo", "There is no profile picture to delete.");
        }
    }

    private String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return fileName.substring(lastIndexOf);
    }

    @FXML
    private void handleSave() {
        // Validate all fields
        boolean isValid = validateFullName() && validateUsername() && validateEmail();

        if (!isValid) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fix the errors before saving.");
            return;
        }

        // Check if anything actually changed
        String newFullName = fullNameField.getText().trim();
        String newUsername = usernameField.getText().trim();
        String newEmail = emailField.getText().trim();

        boolean profilePictureChanged = selectedImagePath != null || photoDeleted;

        if (newFullName.equals(currentUser.getFullName()) &&
            newUsername.equals(currentUser.getUsername()) &&
            newEmail.equals(currentUser.getEmail()) &&
            !profilePictureChanged) {
            showAlert(Alert.AlertType.INFORMATION, "No Changes", "No changes were made to your profile.");
            return;
        }

        // Create updated user object
        User updatedUser = new User();
        updatedUser.setUserId(currentUser.getUserId());
        updatedUser.setFullName(newFullName);
        updatedUser.setUsername(newUsername);
        updatedUser.setEmail(newEmail);
        updatedUser.setRole(currentUser.getRole());
        updatedUser.setPassword(currentUser.getPassword()); // Keep existing password

        // Handle profile picture changes
        if (photoDeleted) {
            // Delete the profile picture
            updatedUser.setProfilePicturePath(null);
        } else if (selectedImagePath != null) {
            // Set new profile picture path
            updatedUser.setProfilePicturePath(selectedImagePath);
        } else {
            // Keep existing profile picture
            updatedUser.setProfilePicturePath(currentUser.getProfilePicturePath());
        }

        // Update the user profile
        boolean success = dataService.updateUserProfile(updatedUser);

        if (success) {
            profileUpdated = true;
            showAlert(Alert.AlertType.INFORMATION, "Success", "Your profile has been updated successfully!");

            if (dialogStage != null) {
                dialogStage.close();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Update Failed", "Failed to update your profile. Please try again.");
        }
    }

    @FXML
    private void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
