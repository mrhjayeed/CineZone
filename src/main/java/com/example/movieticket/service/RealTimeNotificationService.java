package com.example.movieticket.service;

import com.example.movieticket.model.ChatMessage;
import com.example.movieticket.model.Seat;
import com.example.movieticket.network.SocketClient;
import javafx.application.Platform;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Real-time notification service using Socket programming
 * Handles seat updates and chat messages across multiple connected clients
 * Falls back to local-only mode if socket server is not available
 */
public class RealTimeNotificationService {
    private static RealTimeNotificationService instance;
    private SocketClient socketClient;
    private boolean socketEnabled = false;

    // Local observers for UI updates
    private final Map<Integer, List<SeatUpdateObserver>> seatObservers = new ConcurrentHashMap<>();
    private final List<ChatMessageObserver> chatObservers = new CopyOnWriteArrayList<>();
    private final List<TypingIndicatorObserver> typingObservers = new CopyOnWriteArrayList<>();

    // Track active typing users
    private final Map<String, Long> typingUsers = new ConcurrentHashMap<>();
    private static final long TYPING_TIMEOUT = 3000; // 3 seconds

    private RealTimeNotificationService() {
        // Initialize socket client but don't connect yet
        socketClient = new SocketClient();
    }

    public static synchronized RealTimeNotificationService getInstance() {
        if (instance == null) {
            instance = new RealTimeNotificationService();
        }
        return instance;
    }

    private void initializeSocketObservers() {
        if (!socketEnabled || socketClient == null) return;

        // Register socket observers to handle incoming messages from server
        socketClient.registerSeatObserver(0, new SocketClient.SeatUpdateObserver() {
            @Override
            public void onSeatUpdated(int screeningId, List<Seat> updatedSeats) {
                notifyLocalSeatObservers(screeningId, (observers) ->
                    observers.forEach(obs -> obs.onSeatUpdated(screeningId, updatedSeats)));
            }

            @Override
            public void onSeatLocked(int screeningId, String seatNumber, int userId) {
                notifyLocalSeatObservers(screeningId, (observers) ->
                    observers.forEach(obs -> obs.onSeatLocked(screeningId, seatNumber, userId)));
            }

            @Override
            public void onSeatUnlocked(int screeningId, String seatNumber) {
                notifyLocalSeatObservers(screeningId, (observers) ->
                    observers.forEach(obs -> obs.onSeatUnlocked(screeningId, seatNumber)));
            }

            @Override
            public void onSeatBooked(int screeningId, String seatNumber) {
                notifyLocalSeatObservers(screeningId, (observers) ->
                    observers.forEach(obs -> obs.onSeatBooked(screeningId, seatNumber)));
            }
        });

        socketClient.registerChatObserver(new SocketClient.ChatMessageObserver() {
            @Override
            public void onMessageReceived(ChatMessage message) {
                Platform.runLater(() -> {
                    for (ChatMessageObserver observer : chatObservers) {
                        try {
                            observer.onMessageReceived(message);
                        } catch (Exception e) {
                            System.err.println("Error notifying chat observer: " + e.getMessage());
                        }
                    }
                });
            }

            @Override
            public void onMessageRead(int messageId, int userId) {
                Platform.runLater(() -> {
                    for (ChatMessageObserver observer : chatObservers) {
                        try {
                            observer.onMessageRead(messageId, userId);
                        } catch (Exception e) {
                            System.err.println("Error notifying chat read observer: " + e.getMessage());
                        }
                    }
                });
            }
        });

        socketClient.registerTypingObserver(new SocketClient.TypingIndicatorObserver() {
            @Override
            public void onTypingStarted(int userId, int chatWithUserId) {
                Platform.runLater(() -> {
                    for (TypingIndicatorObserver observer : typingObservers) {
                        try {
                            observer.onTypingStarted(userId, chatWithUserId);
                        } catch (Exception e) {
                            System.err.println("Error notifying typing start observer: " + e.getMessage());
                        }
                    }
                });
            }

            @Override
            public void onTypingStopped(int userId, int chatWithUserId) {
                Platform.runLater(() -> {
                    for (TypingIndicatorObserver observer : typingObservers) {
                        try {
                            observer.onTypingStopped(userId, chatWithUserId);
                        } catch (Exception e) {
                            System.err.println("Error notifying typing stop observer: " + e.getMessage());
                        }
                    }
                });
            }
        });
    }

    // Observer interfaces
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

    // Connection management
    public boolean connect() {
        try {
            if (socketClient != null && socketClient.connect()) {
                socketEnabled = true;
                initializeSocketObservers();
                System.out.println("Socket connection established - real-time features enabled");
                return true;
            }
        } catch (Exception e) {
            System.err.println("Failed to connect to socket server: " + e.getMessage());
        }

        socketEnabled = false;
        System.out.println("Running in local mode - real-time features disabled");
        return false;
    }

    public void disconnect() {
        if (socketClient != null && socketEnabled) {
            socketClient.disconnect();
            socketEnabled = false;
        }
    }

    public boolean isConnected() {
        return socketEnabled && socketClient != null && socketClient.isConnected();
    }

    public void setUserId(int userId) {
        if (socketEnabled && socketClient != null) {
            socketClient.setUserId(userId);
        }
    }

    // Local observer registration methods
    public void registerSeatObserver(int screeningId, SeatUpdateObserver observer) {
        seatObservers.computeIfAbsent(screeningId, k -> new CopyOnWriteArrayList<>()).add(observer);

        // If socket is enabled, also register with socket client for this screening
        if (socketEnabled && socketClient != null) {
            socketClient.registerSeatObserver(screeningId, new SocketClient.SeatUpdateObserver() {
                @Override
                public void onSeatUpdated(int sid, List<Seat> updatedSeats) {
                    if (sid == screeningId) {
                        Platform.runLater(() -> observer.onSeatUpdated(sid, updatedSeats));
                    }
                }

                @Override
                public void onSeatLocked(int sid, String seatNumber, int userId) {
                    if (sid == screeningId) {
                        Platform.runLater(() -> observer.onSeatLocked(sid, seatNumber, userId));
                    }
                }

                @Override
                public void onSeatUnlocked(int sid, String seatNumber) {
                    if (sid == screeningId) {
                        Platform.runLater(() -> observer.onSeatUnlocked(sid, seatNumber));
                    }
                }

                @Override
                public void onSeatBooked(int sid, String seatNumber) {
                    if (sid == screeningId) {
                        Platform.runLater(() -> observer.onSeatBooked(sid, seatNumber));
                    }
                }
            });
        }
    }

    public void unregisterSeatObserver(int screeningId, SeatUpdateObserver observer) {
        List<SeatUpdateObserver> observers = seatObservers.get(screeningId);
        if (observers != null) {
            observers.remove(observer);
            if (observers.isEmpty()) {
                seatObservers.remove(screeningId);
            }
        }
    }

    public void registerChatObserver(ChatMessageObserver observer) {
        chatObservers.add(observer);
    }

    public void unregisterChatObserver(ChatMessageObserver observer) {
        chatObservers.remove(observer);
    }

    public void registerTypingObserver(TypingIndicatorObserver observer) {
        typingObservers.add(observer);
    }

    public void unregisterTypingObserver(TypingIndicatorObserver observer) {
        typingObservers.remove(observer);
    }

    // Notification methods that broadcast to all connected clients via socket (if enabled)
    // or handle locally if socket is disabled
    public void notifySeatUpdated(int screeningId, List<Seat> updatedSeats) {
        if (socketEnabled && socketClient != null) {
            socketClient.notifySeatUpdated(screeningId, updatedSeats);
        } else {
            // Local notification fallback
            notifyLocalSeatObservers(screeningId, (observers) ->
                observers.forEach(obs -> obs.onSeatUpdated(screeningId, updatedSeats)));
        }
    }

    public void notifySeatLocked(int screeningId, String seatNumber, int userId) {
        if (socketEnabled && socketClient != null) {
            socketClient.notifySeatLocked(screeningId, seatNumber, userId);
        } else {
            // Local notification fallback
            notifyLocalSeatObservers(screeningId, (observers) ->
                observers.forEach(obs -> obs.onSeatLocked(screeningId, seatNumber, userId)));
        }
    }

    public void notifySeatUnlocked(int screeningId, String seatNumber) {
        if (socketEnabled && socketClient != null) {
            socketClient.notifySeatUnlocked(screeningId, seatNumber);
        } else {
            // Local notification fallback
            notifyLocalSeatObservers(screeningId, (observers) ->
                observers.forEach(obs -> obs.onSeatUnlocked(screeningId, seatNumber)));
        }
    }

    public void notifySeatBooked(int screeningId, String seatNumber) {
        if (socketEnabled && socketClient != null) {
            socketClient.notifySeatBooked(screeningId, seatNumber);
        } else {
            // Local notification fallback
            notifyLocalSeatObservers(screeningId, (observers) ->
                observers.forEach(obs -> obs.onSeatBooked(screeningId, seatNumber)));
        }
    }

    public void notifyMessageReceived(ChatMessage message) {
        if (socketEnabled && socketClient != null) {
            socketClient.notifyMessageReceived(message);
        } else {
            // Local notification fallback
            Platform.runLater(() -> {
                for (ChatMessageObserver observer : chatObservers) {
                    try {
                        observer.onMessageReceived(message);
                    } catch (Exception e) {
                        System.err.println("Error notifying chat observer: " + e.getMessage());
                    }
                }
            });
        }
    }

    public void notifyMessageRead(int messageId, int userId) {
        if (socketEnabled && socketClient != null) {
            socketClient.notifyMessageRead(messageId, userId);
        } else {
            // Local notification fallback
            Platform.runLater(() -> {
                for (ChatMessageObserver observer : chatObservers) {
                    try {
                        observer.onMessageRead(messageId, userId);
                    } catch (Exception e) {
                        System.err.println("Error notifying chat read observer: " + e.getMessage());
                    }
                }
            });
        }
    }

    public void notifyTypingStarted(int userId, int chatWithUserId) {
        // Update local typing tracking
        String key = userId + ":" + chatWithUserId;
        typingUsers.put(key, System.currentTimeMillis());

        if (socketEnabled && socketClient != null) {
            socketClient.notifyTypingStarted(userId, chatWithUserId);
        } else {
            // Local notification fallback
            Platform.runLater(() -> {
                for (TypingIndicatorObserver observer : typingObservers) {
                    try {
                        observer.onTypingStarted(userId, chatWithUserId);
                    } catch (Exception e) {
                        System.err.println("Error notifying typing start observer: " + e.getMessage());
                    }
                }
            });
        }
    }

    public void notifyTypingStopped(int userId, int chatWithUserId) {
        // Update local typing tracking
        String key = userId + ":" + chatWithUserId;
        typingUsers.remove(key);

        if (socketEnabled && socketClient != null) {
            socketClient.notifyTypingStopped(userId, chatWithUserId);
        } else {
            // Local notification fallback
            Platform.runLater(() -> {
                for (TypingIndicatorObserver observer : typingObservers) {
                    try {
                        observer.onTypingStopped(userId, chatWithUserId);
                    } catch (Exception e) {
                        System.err.println("Error notifying typing stop observer: " + e.getMessage());
                    }
                }
            });
        }
    }

    private void notifyLocalSeatObservers(int screeningId, java.util.function.Consumer<List<SeatUpdateObserver>> notifier) {
        List<SeatUpdateObserver> observers = seatObservers.get(screeningId);
        if (observers != null) {
            Platform.runLater(() -> {
                try {
                    notifier.accept(observers);
                } catch (Exception e) {
                    System.err.println("Error notifying local seat observers: " + e.getMessage());
                }
            });
        }
    }

    // Typing indicator management
    public void startTypingCleanupTask() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                cleanupExpiredTyping();
            }
        }, TYPING_TIMEOUT, TYPING_TIMEOUT);
    }

    public void cleanupExpiredTyping() {
        long now = System.currentTimeMillis();
        typingUsers.entrySet().removeIf(entry -> {
            if (now - entry.getValue() > TYPING_TIMEOUT) {
                String[] parts = entry.getKey().split(":");
                int userId = Integer.parseInt(parts[0]);
                int chatWithUserId = Integer.parseInt(parts[1]);
                notifyTypingStopped(userId, chatWithUserId);
                return true;
            }
            return false;
        });
    }
}
