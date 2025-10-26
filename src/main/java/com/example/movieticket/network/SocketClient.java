package com.example.movieticket.network;

import com.example.movieticket.model.ChatMessage;
import com.example.movieticket.model.Seat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Client-side socket implementation for real-time communication
 */
public class SocketClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final ObjectMapper objectMapper;
    private volatile boolean isConnected = false;
    private Thread messageListener;

    // Client-side observers
    private final Map<Integer, List<SeatUpdateObserver>> seatObservers = new HashMap<>();
    private final List<ChatMessageObserver> chatObservers = new CopyOnWriteArrayList<>();
    private final List<TypingIndicatorObserver> typingObservers = new CopyOnWriteArrayList<>();

    // Observer interfaces (same as before)
    public interface SeatUpdateObserver {
        void onSeatUpdated(int screeningId, List<Seat> updatedSeats);
        void onSeatLocked(int screeningId, String seatNumber, int userId);
        void onSeatUnlocked(int screeningId, String seatNumber);
        void onSeatBooked(int screeningId, String seatNumber);
    }

    public interface ChatMessageObserver {
        void onMessageReceived(ChatMessage message);
        void onMessageRead(int messageId, int userId);
    }

    public interface TypingIndicatorObserver {
        void onTypingStarted(int userId, int chatWithUserId);
        void onTypingStopped(int userId, int chatWithUserId);
    }

    public SocketClient() {
        // Configure ObjectMapper for Java 8 time support without external dependencies
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // This will automatically find and register JSR310 module if available
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            isConnected = true;

            // Start message listener thread
            messageListener = new Thread(this::listenForMessages);
            messageListener.setDaemon(true);
            messageListener.start();

            System.out.println("Connected to server at " + SERVER_HOST + ":" + SERVER_PORT);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            return false;
        }
    }

    public void disconnect() {
        isConnected = false;
        try {
            if (messageListener != null) {
                messageListener.interrupt();
            }
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            System.out.println("Disconnected from server");
        } catch (IOException e) {
            System.err.println("Error disconnecting from server: " + e.getMessage());
        }
    }

    private void listenForMessages() {
        String inputLine;
        try {
            while (isConnected && (inputLine = in.readLine()) != null) {
                try {
                    NetworkMessage message = objectMapper.readValue(inputLine, NetworkMessage.class);
                    handleIncomingMessage(message);
                } catch (Exception e) {
                    System.err.println("Error processing server message: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            if (isConnected) {
                System.err.println("Connection to server lost: " + e.getMessage());
            }
        }
    }

    private void handleIncomingMessage(NetworkMessage message) {
        Platform.runLater(() -> {
            switch (message.getType()) {
                case "SEAT_UPDATE":
                    handleSeatUpdate(message);
                    break;
                case "CHAT_MESSAGE":
                    handleChatMessage(message);
                    break;
                case "TYPING_INDICATOR":
                    handleTypingIndicator(message);
                    break;
            }
        });
    }

    private void handleSeatUpdate(NetworkMessage message) {
        if (message.getScreeningId() == null) return;

        List<SeatUpdateObserver> observers = seatObservers.get(message.getScreeningId());
        if (observers == null) return;

        switch (message.getEventType()) {
            case "SEAT_LOCKED":
                Map<String, Object> lockData = (Map<String, Object>) message.getData();
                String lockedSeat = (String) lockData.get("seatNumber");
                int lockUserId = (Integer) lockData.get("userId");
                observers.forEach(obs -> obs.onSeatLocked(message.getScreeningId(), lockedSeat, lockUserId));
                break;
            case "SEAT_UNLOCKED":
                Map<String, Object> unlockData = (Map<String, Object>) message.getData();
                String unlockedSeat = (String) unlockData.get("seatNumber");
                observers.forEach(obs -> obs.onSeatUnlocked(message.getScreeningId(), unlockedSeat));
                break;
            case "SEAT_BOOKED":
                Map<String, Object> bookData = (Map<String, Object>) message.getData();
                String bookedSeat = (String) bookData.get("seatNumber");
                observers.forEach(obs -> obs.onSeatBooked(message.getScreeningId(), bookedSeat));
                break;
            case "SEAT_UPDATED":
                // Fix: Properly convert the data to List<Seat> using ObjectMapper
                try {
                    List<Seat> updatedSeats = objectMapper.convertValue(message.getData(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Seat.class));
                    observers.forEach(obs -> obs.onSeatUpdated(message.getScreeningId(), updatedSeats));
                } catch (Exception e) {
                    System.err.println("Error converting seat data: " + e.getMessage());
                }
                break;
        }
    }

    private void handleChatMessage(NetworkMessage message) {
        ChatMessage chatMessage = objectMapper.convertValue(message.getData(), ChatMessage.class);

        switch (message.getEventType()) {
            case "CHAT_MESSAGE_SENT":
                chatObservers.forEach(obs -> obs.onMessageReceived(chatMessage));
                break;
            case "CHAT_MESSAGE_READ":
                Map<String, Object> readData = (Map<String, Object>) message.getData();
                Object messageIdObj = readData.get("messageId");
                Object userIdObj = readData.get("userId");

                if (messageIdObj != null && userIdObj != null) {
                    int messageId = (Integer) messageIdObj;
                    int userId = (Integer) userIdObj;
                    chatObservers.forEach(obs -> obs.onMessageRead(messageId, userId));
                } else {
                    System.err.println("Missing messageId or userId in CHAT_MESSAGE_READ event. messageId: " + messageIdObj + ", userId: " + userIdObj);
                }
                break;
        }
    }

    private void handleTypingIndicator(NetworkMessage message) {
        Map<String, Object> data = (Map<String, Object>) message.getData();
        Object userIdObj = data.get("userId");
        Object chatWithUserIdObj = data.get("chatWithUserId");

        if (userIdObj != null && chatWithUserIdObj != null) {
            int userId = (Integer) userIdObj;
            int chatWithUserId = (Integer) chatWithUserIdObj;

            switch (message.getEventType()) {
                case "TYPING_STARTED":
                    typingObservers.forEach(obs -> obs.onTypingStarted(userId, chatWithUserId));
                    break;
                case "TYPING_STOPPED":
                    typingObservers.forEach(obs -> obs.onTypingStopped(userId, chatWithUserId));
                    break;
            }
        } else {
            System.err.println("Missing userId or chatWithUserId in TYPING_INDICATOR event. userId: " + userIdObj + ", chatWithUserId: " + chatWithUserIdObj);
        }
    }

    private void sendMessage(NetworkMessage message) {
        if (isConnected && out != null) {
            try {
                String jsonMessage = objectMapper.writeValueAsString(message);
                out.println(jsonMessage);
            } catch (Exception e) {
                System.err.println("Error sending message to server: " + e.getMessage());
            }
        }
    }

    // Client registration methods
    public void registerSeatObserver(int screeningId, SeatUpdateObserver observer) {
        seatObservers.computeIfAbsent(screeningId, k -> new CopyOnWriteArrayList<>()).add(observer);
        NetworkMessage message = new NetworkMessage("REGISTER_SEAT_OBSERVER", null, null);
        message.setScreeningId(screeningId);
        sendMessage(message);
    }

    public void unregisterSeatObserver(int screeningId, SeatUpdateObserver observer) {
        List<SeatUpdateObserver> observers = seatObservers.get(screeningId);
        if (observers != null) {
            observers.remove(observer);
            if (observers.isEmpty()) {
                seatObservers.remove(screeningId);
            }
        }
        NetworkMessage message = new NetworkMessage("UNREGISTER_SEAT_OBSERVER", null, null);
        message.setScreeningId(screeningId);
        sendMessage(message);
    }

    public void registerChatObserver(ChatMessageObserver observer) {
        chatObservers.add(observer);
        sendMessage(new NetworkMessage("REGISTER_CHAT_OBSERVER", null, null));
    }

    public void unregisterChatObserver(ChatMessageObserver observer) {
        chatObservers.remove(observer);
        sendMessage(new NetworkMessage("UNREGISTER_CHAT_OBSERVER", null, null));
    }

    public void registerTypingObserver(TypingIndicatorObserver observer) {
        typingObservers.add(observer);
        sendMessage(new NetworkMessage("REGISTER_TYPING_OBSERVER", null, null));
    }

    public void unregisterTypingObserver(TypingIndicatorObserver observer) {
        typingObservers.remove(observer);
        sendMessage(new NetworkMessage("UNREGISTER_TYPING_OBSERVER", null, null));
    }

    public void setUserId(int userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        sendMessage(new NetworkMessage("SET_USER_ID", null, data));
    }

    // Notification methods to broadcast updates
    public void notifySeatLocked(int screeningId, String seatNumber, int userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("seatNumber", seatNumber);
        data.put("userId", userId);
        NetworkMessage message = new NetworkMessage("SEAT_LOCKED", "SEAT_LOCKED", data);
        message.setScreeningId(screeningId);
        sendMessage(message);
    }

    public void notifySeatUnlocked(int screeningId, String seatNumber) {
        Map<String, Object> data = new HashMap<>();
        data.put("seatNumber", seatNumber);
        NetworkMessage message = new NetworkMessage("SEAT_UNLOCKED", "SEAT_UNLOCKED", data);
        message.setScreeningId(screeningId);
        sendMessage(message);
    }

    public void notifySeatBooked(int screeningId, String seatNumber) {
        Map<String, Object> data = new HashMap<>();
        data.put("seatNumber", seatNumber);
        NetworkMessage message = new NetworkMessage("SEAT_BOOKED", "SEAT_BOOKED", data);
        message.setScreeningId(screeningId);
        sendMessage(message);
    }

    public void notifySeatUpdated(int screeningId, List<Seat> updatedSeats) {
        NetworkMessage message = new NetworkMessage("SEAT_UPDATED", "SEAT_UPDATED", updatedSeats);
        message.setScreeningId(screeningId);
        sendMessage(message);
    }

    public void notifyMessageReceived(ChatMessage message) {
        sendMessage(new NetworkMessage("CHAT_MESSAGE_SENT", "CHAT_MESSAGE_SENT", message));
    }

    public void notifyMessageRead(int messageId, int userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("messageId", messageId);
        data.put("userId", userId);
        sendMessage(new NetworkMessage("CHAT_MESSAGE_READ", "CHAT_MESSAGE_READ", data));
    }

    public void notifyTypingStarted(int userId, int chatWithUserId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("chatWithUserId", chatWithUserId);
        sendMessage(new NetworkMessage("TYPING_STARTED", "TYPING_STARTED", data));
    }

    public void notifyTypingStopped(int userId, int chatWithUserId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("chatWithUserId", chatWithUserId);
        sendMessage(new NetworkMessage("TYPING_STOPPED", "TYPING_STOPPED", data));
    }

    public boolean isConnected() {
        return isConnected;
    }
}
