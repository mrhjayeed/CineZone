package com.example.movieticket.controller;

import com.example.movieticket.service.DataService;
import com.example.movieticket.model.User;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class SignupController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField emailField;
    @FXML private TextField fullNameField;
    @FXML private Label messageLabel;
    @FXML private Button signupButton;
    @FXML private Button loginButton;
    @FXML private VBox signupCard;
    @FXML private VBox headerBox;
    @FXML private VBox formContainer;
    @FXML private Label movieIcon;
    @FXML private Label titleLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private StackPane floatingElementsPane;
    @FXML private StackPane animatedLeftPane;
    @FXML private Label welcomeIcon;

    private DataService dataService = DataService.getInstance();
    private Random random = new Random();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Start entry animations
        playEntryAnimations();

        // Setup interactive effects
        setupButtonHoverEffects();
        setupInputFieldEffects();
        setupLeftPaneAnimations();

        // Add enter key listener for confirm password field
        confirmPasswordField.setOnAction(e -> handleSignup(null));
    }

    private void playEntryAnimations() {
        // Initially hide signup card
        signupCard.setOpacity(0);
        signupCard.setTranslateX(100);

        // Animate signup card entrance from right
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), signupCard);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(800), signupCard);
        slideIn.setFromX(100);
        slideIn.setToX(0);

        ParallelTransition cardEntrance = new ParallelTransition(fadeIn, slideIn);
        cardEntrance.setDelay(Duration.millis(300));
        cardEntrance.play();

        // Animate movie icon with bounce
        ScaleTransition iconBounce = new ScaleTransition(Duration.millis(600), movieIcon);
        iconBounce.setFromX(0);
        iconBounce.setFromY(0);
        iconBounce.setToX(1.2);
        iconBounce.setToY(1.2);
        iconBounce.setAutoReverse(true);
        iconBounce.setCycleCount(1);
        iconBounce.setDelay(Duration.millis(400));
        iconBounce.play();
    }

    private void setupLeftPaneAnimations() {
        // Animate welcome icon with continuous bounce and rotation
        ScaleTransition bounce = new ScaleTransition(Duration.seconds(2), welcomeIcon);
        bounce.setFromX(1.0);
        bounce.setFromY(1.0);
        bounce.setToX(1.15);
        bounce.setToY(1.15);
        bounce.setAutoReverse(true);
        bounce.setCycleCount(Timeline.INDEFINITE);

        // Initial entrance animation for welcome icon
        ScaleTransition welcomeEntrance = new ScaleTransition(Duration.millis(1000), welcomeIcon);
        welcomeEntrance.setFromX(0);
        welcomeEntrance.setFromY(0);
        welcomeEntrance.setToX(1.0);
        welcomeEntrance.setToY(1.0);
        welcomeEntrance.setInterpolator(Interpolator.EASE_OUT);
        welcomeEntrance.play();
    }

    private void setupButtonHoverEffects() {
        // Signup button hover effect
        addButtonHoverEffect(signupButton, 1.05, "-fx-background-color: linear-gradient(to right, #7c8ef7, #8a5cb8); -fx-text-fill: white; -fx-background-radius: 25; -fx-font-weight: bold; -fx-font-size: 15px; -fx-cursor: hand;");

        // Login button hover effect
        addButtonHoverEffect(loginButton, 1.1, "-fx-background-color: rgba(102, 126, 234, 0.1); -fx-text-fill: #667eea; -fx-underline: true; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
    }

    private void addButtonHoverEffect(Button button, double scale, String hoverStyle) {
        String originalStyle = button.getStyle();

        button.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), button);
            st.setToX(scale);
            st.setToY(scale);
            st.play();
            button.setStyle(hoverStyle);
        });

        button.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
            button.setStyle(originalStyle);
        });
    }

    private void setupInputFieldEffects() {
        // Add focus effects for all input fields
        addInputFieldFocusEffect(fullNameField.getParent());
        addInputFieldFocusEffect(emailField.getParent());
        addInputFieldFocusEffect(usernameField.getParent());
        addInputFieldFocusEffect(passwordField.getParent());
        addInputFieldFocusEffect(confirmPasswordField.getParent());
    }

    private void addInputFieldFocusEffect(Node fieldContainer) {
        String normalStyle = "-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 4;";
        String focusStyle = "-fx-background-color: white; -fx-border-color: #667eea; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 4; -fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.3), 10, 0, 0, 0);";

        // Find the text field in the container
        if (fieldContainer instanceof javafx.scene.layout.HBox) {
            javafx.scene.layout.HBox hbox = (javafx.scene.layout.HBox) fieldContainer;
            hbox.getChildren().forEach(child -> {
                if (child instanceof TextField || child instanceof PasswordField) {
                    child.focusedProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal) {
                            // Focused
                            hbox.setStyle(focusStyle);
                            ScaleTransition st = new ScaleTransition(Duration.millis(150), hbox);
                            st.setToX(1.02);
                            st.setToY(1.02);
                            st.play();
                        } else {
                            // Lost focus
                            hbox.setStyle(normalStyle);
                            ScaleTransition st = new ScaleTransition(Duration.millis(150), hbox);
                            st.setToX(1.0);
                            st.setToY(1.0);
                            st.play();
                        }
                    });
                }
            });
        }
    }

    @FXML
    private void handleSignup(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String email = emailField.getText().trim();
        String fullName = fullNameField.getText().trim();

        // Validation
        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || fullName.isEmpty()) {
            showMessage("Please fill in all fields.", false);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showMessage("Passwords do not match.", false);
            return;
        }

        if (password.length() < 6) {
            showMessage("Password must be at least 6 characters long.", false);
            return;
        }

        if (!email.contains("@")) {
            showMessage("Please enter a valid email address.", false);
            return;
        }

        // Check for existing username and email before attempting registration
        if (dataService.isUsernameExists(username)) {
            showMessage("Username '" + username + "' is already taken. Please choose a different username.", false);
            return;
        }

        if (dataService.isEmailExists(email)) {
            showMessage("Email '" + email + "' is already registered. Please use a different email address.", false);
            return;
        }

        // Register user
        User newUser = new User(0, username, password, email, fullName, User.UserRole.USER);

        if (dataService.registerUser(newUser)) {
            showMessage("Account created successfully! Please login.", true);
            clearFields();
        } else {
            showMessage("Registration failed. Please try again or contact support.", false);
        }
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/movieticket/login-view.fxml"));
            Scene scene = new Scene(loader.load(), 1150, 750);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        emailField.clear();
        fullNameField.clear();
    }

    private void showMessage(String message, boolean isSuccess) {
        messageLabel.setText(message);
        messageLabel.setStyle(isSuccess ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
    }
}
