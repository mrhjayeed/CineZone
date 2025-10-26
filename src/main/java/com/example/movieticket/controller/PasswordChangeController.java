package com.example.movieticket.controller;

import com.example.movieticket.model.User;
import com.example.movieticket.service.DataService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class PasswordChangeController implements Initializable {

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label currentPasswordErrorLabel;
    @FXML private Label newPasswordErrorLabel;
    @FXML private Label confirmPasswordErrorLabel;
    @FXML private Button changePasswordButton;
    @FXML private Button cancelButton;

    private DataService dataService = DataService.getInstance();
    private User currentUser;
    private Stage dialogStage;
    private boolean passwordChanged = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.currentUser = dataService.getCurrentUser();
        setupValidation();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isPasswordChanged() {
        return passwordChanged;
    }

    private void setupValidation() {
        // Add real-time validation listeners
        currentPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateCurrentPassword();
        });

        newPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateNewPassword();
            validateConfirmPassword(); // Re-validate confirm password when new password changes
        });

        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateConfirmPassword();
        });
    }

    private boolean validateCurrentPassword() {
        String currentPassword = currentPasswordField.getText();
        if (currentPassword.isEmpty()) {
            showFieldError(currentPasswordErrorLabel, "Current password is required");
            return false;
        } else {
            hideFieldError(currentPasswordErrorLabel);
            return true;
        }
    }

    private boolean validateNewPassword() {
        String newPassword = newPasswordField.getText();
        if (newPassword.isEmpty()) {
            showFieldError(newPasswordErrorLabel, "New password is required");
            return false;
        } else if (newPassword.length() < 6) {
            showFieldError(newPasswordErrorLabel, "Password must be at least 6 characters long");
            return false;
        } else if (newPassword.equals(currentPasswordField.getText())) {
            showFieldError(newPasswordErrorLabel, "New password must be different from current password");
            return false;
        } else {
            hideFieldError(newPasswordErrorLabel);
            return true;
        }
    }

    private boolean validateConfirmPassword() {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (confirmPassword.isEmpty()) {
            showFieldError(confirmPasswordErrorLabel, "Please confirm your new password");
            return false;
        } else if (!confirmPassword.equals(newPassword)) {
            showFieldError(confirmPasswordErrorLabel, "Passwords do not match");
            return false;
        } else {
            hideFieldError(confirmPasswordErrorLabel);
            return true;
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
    private void handleChangePassword() {
        // Validate all fields
        boolean isValid = validateCurrentPassword() && validateNewPassword() && validateConfirmPassword();

        if (!isValid) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fix the errors before changing your password.");
            return;
        }

        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();

        // Update the password
        boolean success = dataService.updateUserPassword(currentUser.getUserId(), currentPassword, newPassword);

        if (success) {
            passwordChanged = true;
            showAlert(Alert.AlertType.INFORMATION, "Success", "Your password has been changed successfully!");

            if (dialogStage != null) {
                dialogStage.close();
            }
        } else {
            showFieldError(currentPasswordErrorLabel, "Current password is incorrect");
            showAlert(Alert.AlertType.ERROR, "Password Change Failed", "Current password is incorrect. Please try again.");
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
