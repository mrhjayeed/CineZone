package com.example.movieticket;

import com.example.movieticket.service.RealTimeNotificationService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class MovieTicketApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Initialize socket connection
        RealTimeNotificationService notificationService = RealTimeNotificationService.getInstance();

        // Try to connect to the socket server
        if (!notificationService.connect()) {
            // Show warning if server connection fails
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Connection Warning");
                alert.setHeaderText("Socket Server Not Available");
                alert.setContentText("Could not connect to the real-time notification server. " +
                    "Real-time features will not work. Please ensure the server is running.");
                alert.showAndWait();
            });
        } else {
            System.out.println("Successfully connected to socket server");
        }

        Image icon = new Image("icon.png");
        stage.getIcons().add(icon);

        FXMLLoader fxmlLoader = new FXMLLoader(MovieTicketApp.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1150, 750);
        stage.setTitle("CineZone");
        stage.setScene(scene);

        // Store HostServices in the stage properties for later access
        stage.getProperties().put("hostServices", getHostServices());

        // Add shutdown hook to disconnect from server
        stage.setOnCloseRequest(event -> {
            notificationService.disconnect();
            Platform.exit();
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
