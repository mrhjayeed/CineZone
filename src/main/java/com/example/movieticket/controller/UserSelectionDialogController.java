package com.example.movieticket.controller;

import com.example.movieticket.model.ChatMessage;
import com.example.movieticket.model.User;
import com.example.movieticket.service.DataService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class UserSelectionDialogController implements Initializable {

    @FXML private ListView<User> usersListView;
    @FXML private TextField searchField;
    @FXML private Label userCountLabel;
    @FXML private VBox emptyStateBox;
    @FXML private Button openChatButton;
    @FXML private Button refreshButton;

    private DataService dataService = DataService.getInstance();
    private ObservableList<User> usersList = FXCollections.observableArrayList();
    private List<User> allUsersWithMessages;
    private User selectedUser;
    private boolean okClicked = false;
    private Stage dialogStage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set up the ListView with custom cell factory
        usersListView.setCellFactory(lv -> new UserListCell());
        usersListView.setItems(usersList);

        // Handle selection changes
        usersListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                selectedUser = newValue;
                openChatButton.setDisable(newValue == null);
            }
        );

        // Handle double-click to open chat
        usersListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && selectedUser != null) {
                handleOpenChat();
            }
        });

        // Load users with messages
        loadUsers();

        // Set up search field listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterUsers(newValue);
        });
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public User getSelectedUser() {
        return selectedUser;
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    private void loadUsers() {
        allUsersWithMessages = dataService.getUsersWithMessages(dataService.getCurrentUser().getUserId());
        usersList.clear();
        usersList.addAll(allUsersWithMessages);
        updateUserCount();
        updateEmptyState();
    }

    private void filterUsers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            usersList.clear();
            usersList.addAll(allUsersWithMessages);
        } else {
            String search = searchTerm.toLowerCase().trim();
            List<User> filtered = allUsersWithMessages.stream()
                .filter(user ->
                    user.getFullName().toLowerCase().contains(search) ||
                    user.getUsername().toLowerCase().contains(search) ||
                    user.getEmail().toLowerCase().contains(search)
                )
                .collect(Collectors.toList());
            usersList.clear();
            usersList.addAll(filtered);
        }
        updateUserCount();
        updateEmptyState();
    }

    private void updateUserCount() {
        int count = usersList.size();
        userCountLabel.setText(count + (count == 1 ? " user" : " users"));
    }

    private void updateEmptyState() {
        boolean isEmpty = usersList.isEmpty();
        emptyStateBox.setVisible(isEmpty);
        emptyStateBox.setManaged(isEmpty);
        usersListView.setVisible(!isEmpty);
        usersListView.setManaged(!isEmpty);
    }

    @FXML
    private void handleSearch() {
        filterUsers(searchField.getText());
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadUsers();
    }

    @FXML
    private void handleOpenChat() {
        if (selectedUser != null) {
            okClicked = true;
            // Don't close the dialog - let it remain open

            // Open the chat window directly from here
            openChatWindowForUser(selectedUser);
        }
    }

    private void openChatWindowForUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/example/movieticket/chat-window.fxml"));
            javafx.scene.layout.BorderPane chatRoot = loader.load();

            ChatController controller = loader.getController();

            // Create new stage for chat window
            Stage chatStage = new Stage();
            chatStage.setTitle("Chat with " + user.getFullName());
            chatStage.initModality(javafx.stage.Modality.NONE); // Allow interaction with other windows
            chatStage.initOwner(dialogStage);

            // Set up the controller
            controller.initializeChat(user);

            javafx.scene.Scene scene = new javafx.scene.Scene(chatRoot, 450, 550);

            // Try to load CSS file, but don't fail if it doesn't exist
            try {
                java.net.URL cssUrl = getClass().getResource("/com/example/movieticket/chat-styles.css");
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                }
            } catch (Exception e) {
                // CSS file not found, continue without custom styling
            }

            chatStage.setScene(scene);
            chatStage.show();

            // Refresh the user list after opening chat to update unread counts
            Platform.runLater(() -> {
                loadUsers();
            });

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not open chat window");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Custom ListCell for displaying user information with unread message badge
     */
    private class UserListCell extends ListCell<User> {
        private HBox content;
        private VBox userInfo;
        private Label nameLabel;
        private Label usernameLabel;
        private Label lastMessageLabel;
        private Label unreadBadge;
        private Region spacer;

        public UserListCell() {
            super();

            // Create the cell layout
            content = new HBox(12);
            content.setAlignment(Pos.CENTER_LEFT);
            content.setPadding(new Insets(10, 12, 10, 12));

            // User info section
            userInfo = new VBox(4);
            userInfo.setAlignment(Pos.CENTER_LEFT);

            nameLabel = new Label();
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            usernameLabel = new Label();
            usernameLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

            lastMessageLabel = new Label();
            lastMessageLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px; -fx-font-style: italic;");

            userInfo.getChildren().addAll(nameLabel, usernameLabel, lastMessageLabel);

            // Spacer
            spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Unread badge
            unreadBadge = new Label();
            unreadBadge.setStyle(
                "-fx-background-color: #f44336; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 12; " +
                "-fx-padding: 3 8; " +
                "-fx-font-size: 11px; " +
                "-fx-font-weight: bold;"
            );
            unreadBadge.setAlignment(Pos.CENTER);

            content.getChildren().addAll(userInfo, spacer, unreadBadge);
        }

        @Override
        protected void updateItem(User user, boolean empty) {
            super.updateItem(user, empty);

            if (empty || user == null) {
                setGraphic(null);
                setText(null);
            } else {
                nameLabel.setText(user.getFullName());
                usernameLabel.setText("@" + user.getUsername() + " • " + user.getEmail());

                // Get unread message count
                List<ChatMessage> allMessages = dataService.getChatHistoryWithAdmins(user.getUserId());
                long unreadCount = allMessages.stream()
                    .filter(msg -> !msg.isRead() && msg.getSenderId() == user.getUserId())
                    .count();

                // Get last message info
                ChatMessage lastMessage = allMessages.stream()
                    .max((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()))
                    .orElse(null);

                if (lastMessage != null) {
                    String timeAgo = getTimeAgo(lastMessage.getTimestamp());
                    String preview = lastMessage.getContent();
                    if (preview.length() > 40) {
                        preview = preview.substring(0, 40) + "...";
                    }
                    lastMessageLabel.setText("Last: \"" + preview + "\" • " + timeAgo);
                } else {
                    lastMessageLabel.setText("No messages yet");
                }

                // Show/hide unread badge
                if (unreadCount > 0) {
                    unreadBadge.setText(String.valueOf(unreadCount));
                    unreadBadge.setVisible(true);
                    unreadBadge.setManaged(true);

                    // Highlight the cell if there are unread messages
                    content.setStyle("-fx-background-color: #fff3e0; -fx-background-radius: 4;");
                } else {
                    unreadBadge.setVisible(false);
                    unreadBadge.setManaged(false);
                    content.setStyle("-fx-background-color: transparent;");
                }

                setGraphic(content);
                setText(null);
            }
        }

        private String getTimeAgo(LocalDateTime timestamp) {
            LocalDateTime now = LocalDateTime.now();
            long minutes = java.time.Duration.between(timestamp, now).toMinutes();

            if (minutes < 1) {
                return "Just now";
            } else if (minutes < 60) {
                return minutes + "m ago";
            } else if (minutes < 1440) { // Less than 24 hours
                long hours = minutes / 60;
                return hours + "h ago";
            } else {
                long days = minutes / 1440;
                if (days == 1) {
                    return "Yesterday";
                } else if (days < 7) {
                    return days + "d ago";
                } else {
                    return timestamp.format(DateTimeFormatter.ofPattern("MMM dd"));
                }
            }
        }
    }
}
