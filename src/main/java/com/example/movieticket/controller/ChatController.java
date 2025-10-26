package com.example.movieticket.controller;

import com.example.movieticket.model.ChatMessage;
import com.example.movieticket.model.User;
import com.example.movieticket.service.DataService;
import com.example.movieticket.service.RealTimeNotificationService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Set;
import java.util.HashSet;

public class ChatController implements Initializable,
    RealTimeNotificationService.ChatMessageObserver,
    RealTimeNotificationService.TypingIndicatorObserver {

    @FXML private Label chatTitleLabel;
    @FXML private Label typingIndicatorLabel;
    @FXML private Button closeButton;
    @FXML private ScrollPane chatScrollPane;
    @FXML private VBox messagesContainer;
    @FXML private TextArea messageInput;
    @FXML private Button sendButton;
    @FXML private Button clearChatButton;
    @FXML private Label connectionStatusLabel;
    @FXML private Label unreadCountLabel;

    private DataService dataService;
    private RealTimeNotificationService notificationService;
    private User currentUser;
    private User chatWithUser;
    private Timer typingTimer;
    private boolean isTyping = false;
    private Set<Integer> displayedMessageIds = new HashSet<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.dataService = DataService.getInstance();
        this.notificationService = dataService.getNotificationService();
        this.currentUser = dataService.getCurrentUser();

        // Register observers
        notificationService.registerChatObserver(this);
        notificationService.registerTypingObserver(this);

        // Set up UI bindings
        setupUI();
        setupTypingDetection();
    }

    private void setupUI() {
        // Auto-scroll to bottom when new messages are added
        messagesContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
        });

        // Enable/disable send button based on message input
        sendButton.disableProperty().bind(messageInput.textProperty().isEmpty());

        // Show clear button only for admin users
        if (currentUser != null && currentUser.getRole() == User.UserRole.ADMIN) {
            clearChatButton.setVisible(true);
        }
    }

    private void setupTypingDetection() {
        messageInput.textProperty().addListener((obs, oldText, newText) -> {
            if (currentUser != null && chatWithUser != null && !newText.trim().isEmpty() && !isTyping) {
                // Start typing
                isTyping = true;
                dataService.startTyping(currentUser.getUserId(), chatWithUser.getUserId());

                // Reset typing timer
                if (typingTimer != null) {
                    typingTimer.cancel();
                }

                typingTimer = new Timer();
                typingTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            if (currentUser != null && chatWithUser != null) {
                                isTyping = false;
                                dataService.stopTyping(currentUser.getUserId(), chatWithUser.getUserId());
                            }
                        });
                    }
                }, 2000); // Stop typing after 2 seconds of inactivity
            }
        });
    }

    public void initializeChat(User chatWithUser) {
        this.chatWithUser = chatWithUser;

        if (chatWithUser != null && currentUser != null) {
            if (currentUser.getRole() == User.UserRole.ADMIN) {
                chatTitleLabel.setText("Chat with " + chatWithUser.getFullName());
                // For admin, load chat history with this specific user (showing messages with ANY admin)
                loadChatHistoryForAdmin();
            } else {
                chatTitleLabel.setText("Chat with Support");
                // For user, load chat history with all admins
                loadChatHistoryForUser();
            }

            markMessagesAsRead();
        }
    }

    private void loadChatHistory() {
        if (chatWithUser == null || currentUser == null) return;

        List<ChatMessage> messages = dataService.getChatHistory(
            currentUser.getUserId(),
            chatWithUser.getUserId()
        );

        messagesContainer.getChildren().clear();
        displayedMessageIds.clear();

        for (ChatMessage message : messages) {
            addMessageToUI(message);
        }
    }

    private void loadChatHistoryForAdmin() {
        if (chatWithUser == null || currentUser == null) return;

        // For admin, load all messages between this user and ANY admin (shared inbox)
        List<ChatMessage> messages = dataService.getChatHistoryWithAdmins(chatWithUser.getUserId());

        messagesContainer.getChildren().clear();
        displayedMessageIds.clear();

        for (ChatMessage message : messages) {
            addMessageToUI(message);
        }
    }

    private void loadChatHistoryForUser() {
        if (chatWithUser == null || currentUser == null) return;

        // For user, load all messages between this user and ANY admin
        List<ChatMessage> messages = dataService.getChatHistoryWithAdmins(currentUser.getUserId());

        messagesContainer.getChildren().clear();
        displayedMessageIds.clear();

        for (ChatMessage message : messages) {
            addMessageToUI(message);
        }
    }

    private void addMessageToUI(ChatMessage message) {
        // Prevent duplicate messages
        if (displayedMessageIds.contains(message.getMessageId())) {
            return;
        } else {
            displayedMessageIds.add(message.getMessageId());
        }

        VBox messageBox = new VBox(3);
        messageBox.setPadding(new Insets(8, 12, 8, 12));
        messageBox.setMaxWidth(300);

        // Determine if this is from current user OR from any admin (for admin viewing)
        boolean isFromCurrentUser;

        if (currentUser != null && currentUser.getRole() == User.UserRole.ADMIN) {
            // For admins: show messages from ANY admin on the right side (sent messages)
            // Get sender to check if they're an admin
            User sender = dataService.getUserById(message.getSenderId());
            isFromCurrentUser = (sender != null && sender.getRole() == User.UserRole.ADMIN);
        } else {
            // For users: only show their own messages on the right
            isFromCurrentUser = currentUser != null && message.getSenderId() == currentUser.getUserId();
        }

        // Create message bubble
        HBox messageRow = new HBox();

        if (isFromCurrentUser) {
            messageBox.setAlignment(Pos.CENTER_RIGHT);
            messageBox.setStyle("-fx-background-color: #0084FF; -fx-background-radius: 18; -fx-text-fill: white;");
            messageRow.setAlignment(Pos.CENTER_RIGHT);
        } else {
            messageBox.setAlignment(Pos.CENTER_LEFT);
            messageBox.setStyle("-fx-background-color: #E4E6EA; -fx-background-radius: 18; -fx-text-fill: black;");
            messageRow.setAlignment(Pos.CENTER_LEFT);
        }

        // Message content
        Label messageLabel = new Label(message.getContent());
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(280);
        messageLabel.setTextFill(isFromCurrentUser ? Color.WHITE : Color.BLACK);
        messageLabel.setFont(Font.font("System", 14));

        // Timestamp with sender name for admins viewing shared inbox
        String timestampText = message.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm"));

        // If current user is admin and this message is from another admin, show the sender's name
        if (currentUser != null && currentUser.getRole() == User.UserRole.ADMIN &&
            message.getSenderId() != currentUser.getUserId() && isFromCurrentUser) {
            timestampText = message.getSenderName() + " - " + timestampText;
        }

        Label timestampLabel = new Label(timestampText);
        timestampLabel.setTextFill(isFromCurrentUser ? Color.LIGHTGRAY : Color.GRAY);
        timestampLabel.setFont(Font.font("System", 10));

        messageBox.getChildren().addAll(messageLabel, timestampLabel);

        // Add spacing
        if (isFromCurrentUser) {
            Region spacer = new Region();
            spacer.setPrefWidth(50);
            messageRow.getChildren().addAll(spacer, messageBox);
        } else {
            Region spacer = new Region();
            spacer.setPrefWidth(50);
            messageRow.getChildren().addAll(messageBox, spacer);
        }

        messagesContainer.getChildren().add(messageRow);
    }

    @FXML
    private void handleSendMessage() {
        String content = messageInput.getText().trim();
        if (content.isEmpty() || chatWithUser == null || currentUser == null) return;

        boolean sent = dataService.sendMessage(
            currentUser.getUserId(),
            chatWithUser.getUserId(),
            content
        );

        if (sent) {
            messageInput.clear();

            // Stop typing indicator
            if (isTyping && currentUser != null && chatWithUser != null) {
                isTyping = false;
                dataService.stopTyping(currentUser.getUserId(), chatWithUser.getUserId());
            }
        } else {
            showAlert("Error", "Failed to send message. Please try again.");
        }
    }

    @FXML
    private void handleClearChat() {
        if (chatWithUser == null || currentUser == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Chat History");
        alert.setHeaderText("Are you sure you want to clear this chat history?");
        alert.setContentText("This action cannot be undone.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            boolean cleared = dataService.deleteChatHistory(
                currentUser.getUserId(),
                chatWithUser.getUserId()
            );

            if (cleared) {
                messagesContainer.getChildren().clear();
                displayedMessageIds.clear();
                showAlert("Success", "Chat history cleared successfully.");
            } else {
                showAlert("Error", "Failed to clear chat history.");
            }
        }
    }

    private void markMessagesAsRead() {
        if (currentUser != null && chatWithUser != null) {
            if (currentUser.getRole() == User.UserRole.ADMIN) {
                // For admin, mark messages from this user to ANY admin as read
                // We need to mark messages where sender is the user and receiver is current admin
                dataService.markMessagesAsRead(currentUser.getUserId(), chatWithUser.getUserId());
            } else {
                // For user, mark messages from any admin to this user as read
                dataService.markMessagesAsRead(currentUser.getUserId(), chatWithUser.getUserId());
            }
        }
    }

    private void updateUnreadCount() {
        if (currentUser != null) {
            int unreadCount = dataService.getUnreadMessageCount(currentUser.getUserId());
            if (unreadCount > 0) {
                unreadCountLabel.setText(unreadCount + " unread");
                unreadCountLabel.setVisible(true);
            } else {
                unreadCountLabel.setVisible(false);
            }
        }
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // ChatMessageObserver implementation
    @Override
    public void onMessageReceived(ChatMessage message) {
        Platform.runLater(() -> {
            if (currentUser == null || chatWithUser == null) return;

            boolean shouldShowMessage = false;

            if (currentUser.getRole() == User.UserRole.ADMIN) {
                // For admin viewing shared inbox: show messages between chatWithUser and ANY admin
                User sender = dataService.getUserById(message.getSenderId());
                User receiver = dataService.getUserById(message.getReceiverId());

                // Show if message is from the user we're chatting with to any admin
                // OR from any admin to the user we're chatting with
                shouldShowMessage = (message.getSenderId() == chatWithUser.getUserId() &&
                                    receiver != null && receiver.getRole() == User.UserRole.ADMIN) ||
                                   (message.getReceiverId() == chatWithUser.getUserId() &&
                                    sender != null && sender.getRole() == User.UserRole.ADMIN);
            } else {
                // For regular users: show messages between current user and any admin
                User sender = dataService.getUserById(message.getSenderId());
                User receiver = dataService.getUserById(message.getReceiverId());

                shouldShowMessage = (message.getSenderId() == currentUser.getUserId() &&
                                    receiver != null && receiver.getRole() == User.UserRole.ADMIN) ||
                                   (message.getReceiverId() == currentUser.getUserId() &&
                                    sender != null && sender.getRole() == User.UserRole.ADMIN);
            }

            if (shouldShowMessage) {
                addMessageToUI(message);

                // Mark as read if this chat window is open and message is to current user
                if (message.getReceiverId() == currentUser.getUserId()) {
                    dataService.markMessagesAsRead(currentUser.getUserId(), message.getSenderId());
                }
            }

            updateUnreadCount();
        });
    }

    @Override
    public void onMessageRead(int messageId, int userId) {
        Platform.runLater(() -> {
            updateUnreadCount();
        });
    }

    // TypingIndicatorObserver implementation
    @Override
    public void onTypingStarted(int userId, int chatWithUserId) {
        Platform.runLater(() -> {
            if (currentUser != null && chatWithUser != null && userId == chatWithUser.getUserId() && chatWithUserId == currentUser.getUserId()) {
                typingIndicatorLabel.setText(chatWithUser.getFullName() + " is typing...");
            }
        });
    }

    @Override
    public void onTypingStopped(int userId, int chatWithUserId) {
        Platform.runLater(() -> {
            if (currentUser != null && chatWithUser != null && userId == chatWithUser.getUserId() && chatWithUserId == currentUser.getUserId()) {
                typingIndicatorLabel.setText("");
            }
        });
    }
}
