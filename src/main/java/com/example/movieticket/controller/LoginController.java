package com.example.movieticket.controller;

import com.example.movieticket.model.User;
import com.example.movieticket.service.DataService;
import javafx.animation.*;
import javafx.application.Platform;
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

public class LoginController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private Button forgotPasswordButton;
    @FXML private Button loginButton;
    @FXML private Button signupButton;
    @FXML private VBox loginCard;
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

        // Add enter key listener for password field
        passwordField.setOnAction(e -> handleLogin(null));
    }

    private void playEntryAnimations() {
        // Initially hide login card
        loginCard.setOpacity(0);
        loginCard.setTranslateX(100);

        // Animate login card entrance from right
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), loginCard);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(800), loginCard);
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
        // Login button hover effect
        addButtonHoverEffect(loginButton, 1.05, "-fx-background-color: linear-gradient(to right, #7c8ef7, #8a5cb8); -fx-text-fill: white; -fx-background-radius: 25; -fx-font-weight: bold; -fx-font-size: 16px; -fx-cursor: hand;");

        // Signup button hover effect
        addButtonHoverEffect(signupButton, 1.1, "-fx-background-color: rgba(102, 126, 234, 0.1); -fx-text-fill: #667eea; -fx-underline: true; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");

        // Forgot password button hover effect
        addButtonHoverEffect(forgotPasswordButton, 1.05, "-fx-background-color: rgba(231, 76, 60, 0.1); -fx-text-fill: #e74c3c; -fx-underline: true; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
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
        // Username field focus effect
        addInputFieldFocusEffect(usernameField.getParent());

        // Password field focus effect
        addInputFieldFocusEffect(passwordField.getParent());
    }

    private void addInputFieldFocusEffect(Node fieldContainer) {
        String normalStyle = "-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 5;";
        String focusStyle = "-fx-background-color: white; -fx-border-color: #667eea; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 5; -fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.3), 10, 0, 0, 0);";

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
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Please enter both username and password.", false);
            playShakeAnimation(loginCard);
            return;
        }

        // Show loading state
        setLoadingState(true);

        // Simulate a brief loading period for better UX
        PauseTransition pause = new PauseTransition(Duration.millis(800));
        pause.setOnFinished(e -> {
            User user = dataService.authenticate(username, password);
            if (user != null) {
                dataService.setCurrentUser(user);
                showMessage("✓ Login successful! Welcome back!", true);

                // Play success animation before navigating
                playSuccessAnimation(() -> navigateToDashboard(event, user));
            } else {
                setLoadingState(false);
                showMessage("✗ Invalid username or password.", false);
                playShakeAnimation(loginCard);

                // Add a wiggle to the input fields
                playShakeAnimation(usernameField.getParent());
                playShakeAnimation(passwordField.getParent());
            }
        });
        pause.play();
    }

    private void setLoadingState(boolean loading) {
        Platform.runLater(() -> {
            loginButton.setDisable(loading);
            usernameField.setDisable(loading);
            passwordField.setDisable(loading);

            if (loading) {
                loginButton.setText("");
                loadingIndicator.setVisible(true);
            } else {
                loginButton.setText("Sign In");
                loadingIndicator.setVisible(false);
            }
        });
    }

    private void playShakeAnimation(Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setFromX(0);
        tt.setByX(10);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();
    }

    private void playSuccessAnimation(Runnable onComplete) {
        // Pulse effect on login card
        ScaleTransition pulse = new ScaleTransition(Duration.millis(300), loginCard);
        pulse.setToX(1.05);
        pulse.setToY(1.05);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);

        // Fade out
        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), loginCard);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setDelay(Duration.millis(600));

        SequentialTransition success = new SequentialTransition(pulse, fadeOut);
        success.setOnFinished(e -> onComplete.run());
        success.play();
    }

    @FXML
    private void handleSignup(ActionEvent event) {
        // Animate transition
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), loginCard);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/movieticket/signup-view.fxml"));
                Scene scene = new Scene(loader.load(), 1150, 750);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        fadeOut.play();
    }

    @FXML
    private void handleForgotPassword() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/example/movieticket/password-reset-dialog.fxml"));
            VBox passwordResetRoot = loader.load();

            PasswordResetController controller = loader.getController();

            // Create new stage for password reset dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Reset Password");
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialogStage.initOwner(forgotPasswordButton.getScene().getWindow());

            // Set up the controller
            controller.setDialogStage(dialogStage);

            Scene scene = new Scene(passwordResetRoot);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);

            // Show dialog and wait for user action
            dialogStage.showAndWait();

            // If password was reset, show success message
            if (controller.isPasswordReset()) {
                showMessage("✓ Password reset successful! Please login with your new password.", true);
            }

        } catch (IOException e) {
            showMessage("Could not open password reset dialog. Please try again.", false);
            e.printStackTrace();
        } catch (Exception e) {
            showMessage("An unexpected error occurred. Please try again.", false);
            e.printStackTrace();
        }
    }

    private void navigateToDashboard(ActionEvent event, User user) {
        try {
            String fxmlFile = user.getRole() == User.UserRole.ADMIN ?
                    "/com/example/movieticket/admin-dashboard.fxml" :
                    "/com/example/movieticket/user-dashboard.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(loader.load(), 1150, 750);
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showMessage(String message, boolean isSuccess) {
        Platform.runLater(() -> {
            messageLabel.setText(message);
            messageLabel.setStyle(isSuccess ?
                "-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-padding: 5; -fx-background-color: rgba(39, 174, 96, 0.1); -fx-background-radius: 5;" :
                "-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-padding: 5; -fx-background-color: rgba(231, 76, 60, 0.1); -fx-background-radius: 5;");

            // Animate message appearance
            messageLabel.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(300), messageLabel);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        });
    }
}
