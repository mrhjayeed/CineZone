package com.example.movieticket.network;

import com.example.movieticket.model.ChatMessage;
import com.example.movieticket.model.Seat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Socket server for handling real-time communication between multiple clients
 */
public class SocketServer {
    private static final int PORT = 8888;
    private ServerSocket serverSocket;
    private final Map<Integer, Set<ClientHandler>> seatObservers = new ConcurrentHashMap<>();
    private final Set<ClientHandler> chatObservers = ConcurrentHashMap.newKeySet();
    private final Set<ClientHandler> typingObservers = ConcurrentHashMap.newKeySet();
    private final ExecutorService clientPool = Executors.newCachedThreadPool();
    private final ObjectMapper objectMapper;
    private volatile boolean isRunning = false;

    public SocketServer() {
        // Configure ObjectMapper with automatic module discovery for Java 8 time support
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Automatically finds and registers JSR310 module
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        isRunning = true;
        System.out.println("Socket server started on port " + PORT);

        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clientPool.submit(clientHandler);
                System.out.println("New client connected: " + clientSocket.getInetAddress());
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        }
    }

    public void stop() throws IOException {
        isRunning = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        clientPool.shutdown();
        try {
            if (!clientPool.awaitTermination(5, TimeUnit.SECONDS)) {
                clientPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            clientPool.shutdownNow();
        }
        System.out.println("Socket server stopped");
    }

    // Register client for seat updates on specific screening
    public void registerSeatObserver(int screeningId, ClientHandler client) {
        seatObservers.computeIfAbsent(screeningId, k -> ConcurrentHashMap.newKeySet()).add(client);
    }

    // Unregister client from seat updates
    public void unregisterSeatObserver(int screeningId, ClientHandler client) {
        Set<ClientHandler> clients = seatObservers.get(screeningId);
        if (clients != null) {
            clients.remove(client);
            if (clients.isEmpty()) {
                seatObservers.remove(screeningId);
            }
        }
    }

    // Register client for chat messages
    public void registerChatObserver(ClientHandler client) {
        chatObservers.add(client);
    }

    // Unregister client from chat messages
    public void unregisterChatObserver(ClientHandler client) {
        chatObservers.remove(client);
    }

    // Register client for typing indicators
    public void registerTypingObserver(ClientHandler client) {
        typingObservers.add(client);
    }

    // Unregister client from typing indicators
    public void unregisterTypingObserver(ClientHandler client) {
        typingObservers.remove(client);
    }

    // Remove client from all observers when disconnected
    public void removeClient(ClientHandler client) {
        chatObservers.remove(client);
        typingObservers.remove(client);
        seatObservers.values().forEach(clients -> clients.remove(client));
    }

    // Broadcast seat update to all observers of the screening
    public void broadcastSeatUpdate(int screeningId, String eventType, Object data) {
        Set<ClientHandler> clients = seatObservers.get(screeningId);
        if (clients != null) {
            NetworkMessage message = new NetworkMessage("SEAT_UPDATE", eventType, data);
            message.setScreeningId(screeningId);
            broadcastToClients(clients, message);
        }
    }

    // Broadcast chat message to all chat observers
    public void broadcastChatMessage(String eventType, ChatMessage message) {
        NetworkMessage networkMessage = new NetworkMessage("CHAT_MESSAGE", eventType, message);
        broadcastToClients(chatObservers, networkMessage);
    }

    // Broadcast typing indicator to all typing observers
    public void broadcastTypingIndicator(String eventType, Map<String, Object> data) {
        NetworkMessage message = new NetworkMessage("TYPING_INDICATOR", eventType, data);
        broadcastToClients(typingObservers, message);
    }

    private void broadcastToClients(Set<ClientHandler> clients, NetworkMessage message) {
        Set<ClientHandler> disconnectedClients = new HashSet<>();

        for (ClientHandler client : clients) {
            try {
                client.sendMessage(message);
            } catch (Exception e) {
                System.err.println("Failed to send message to client: " + e.getMessage());
                disconnectedClients.add(client);
            }
        }

        // Remove disconnected clients
        clients.removeAll(disconnectedClients);
        disconnectedClients.forEach(this::removeClient);
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
