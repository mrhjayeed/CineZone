package com.example.movieticket.controller;

import com.example.movieticket.service.DataService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class PasswordResetController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField newPasswordField;
    @FXML private Label usernameErrorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label newPasswordErrorLabel;
    @FXML private Button resetPasswordButton;
    @FXML private Button cancelButton;

    private DataService dataService = DataService.getInstance();
    private Stage dialogStage;
    private boolean passwordReset = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupValidation();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isPasswordReset() {
        return passwordReset;
    }

    private void setupValidation() {
        // Add real-time validation listeners
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateUsername();
        });

        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateEmail();
        });

        newPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateNewPassword();
        });
    }

    private boolean validateUsername() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            showFieldError(usernameErrorLabel, "Username is required");
            return false;
        } else if (username.length() < 3) {
            showFieldError(usernameErrorLabel, "Username must be at least 3 characters");
            return false;
        } else {
            hideFieldError(usernameErrorLabel);
            return true;
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
            hideFieldError(emailErrorLabel);
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
        } else {
            hideFieldError(newPasswordErrorLabel);
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
    private void handleResetPassword() {
        // Validate all fields
        boolean isValid = validateUsername() && validateEmail() && validateNewPassword();

        if (!isValid) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fix the errors before resetting your password.");
            return;
        }

        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String newPassword = newPasswordField.getText();

        // Reset the password
        boolean success = dataService.resetUserPassword(username, email, newPassword);

        if (success) {
            passwordReset = true;
            showAlert(Alert.AlertType.INFORMATION, "Success",
                "Your password has been reset successfully! You can now login with your new password.");

            if (dialogStage != null) {
                dialogStage.close();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Reset Failed",
                "Username and email combination not found. Please verify your information and try again.");
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
