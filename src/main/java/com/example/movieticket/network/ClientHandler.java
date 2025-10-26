package com.example.movieticket.network;

import com.example.movieticket.model.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.net.Socket;
import java.util.Map;

/**
 * Handles individual client connections on the server side
 */
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final SocketServer server;
    private final ObjectMapper objectMapper;
    private PrintWriter out;
    private BufferedReader in;
    private volatile boolean isRunning = true;
    private int userId = -1;

    public ClientHandler(Socket socket, SocketServer server) {
        this.clientSocket = socket;
        this.server = server;
        this.objectMapper = server.getObjectMapper();
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            while (isRunning && (inputLine = in.readLine()) != null) {
                try {
                    NetworkMessage message = objectMapper.readValue(inputLine, NetworkMessage.class);
                    handleMessage(message);
                } catch (Exception e) {
                    System.err.println("Error processing client message: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Client handler error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void handleMessage(NetworkMessage message) {
        switch (message.getType()) {
            case "REGISTER_SEAT_OBSERVER":
                if (message.getScreeningId() != null) {
                    server.registerSeatObserver(message.getScreeningId(), this);
                }
                break;
            case "UNREGISTER_SEAT_OBSERVER":
                if (message.getScreeningId() != null) {
                    server.unregisterSeatObserver(message.getScreeningId(), this);
                }
                break;
            case "REGISTER_CHAT_OBSERVER":
                server.registerChatObserver(this);
                break;
            case "UNREGISTER_CHAT_OBSERVER":
                server.unregisterChatObserver(this);
                break;
            case "REGISTER_TYPING_OBSERVER":
                server.registerTypingObserver(this);
                break;
            case "UNREGISTER_TYPING_OBSERVER":
                server.unregisterTypingObserver(this);
                break;
            case "SET_USER_ID":
                if (message.getData() instanceof Map) {
                    Map<String, Object> data = (Map<String, Object>) message.getData();
                    this.userId = (Integer) data.get("userId");
                }
                break;
            case "SEAT_LOCKED":
            case "SEAT_UNLOCKED":
            case "SEAT_BOOKED":
            case "SEAT_UPDATED":
                if (message.getScreeningId() != null) {
                    server.broadcastSeatUpdate(message.getScreeningId(), message.getEventType(), message.getData());
                }
                break;
            case "CHAT_MESSAGE_SENT":
            case "CHAT_MESSAGE_READ":
                // Convert LinkedHashMap to ChatMessage object using ObjectMapper
                if (message.getData() instanceof Map) {
                    ChatMessage chatMessage = objectMapper.convertValue(message.getData(), ChatMessage.class);
                    server.broadcastChatMessage(message.getEventType(), chatMessage);
                }
                break;
            case "TYPING_STARTED":
            case "TYPING_STOPPED":
                server.broadcastTypingIndicator(message.getEventType(), (Map<String, Object>) message.getData());
                break;
        }
    }

    public void sendMessage(NetworkMessage message) throws IOException {
        if (out != null && !clientSocket.isClosed()) {
            String jsonMessage = objectMapper.writeValueAsString(message);
            out.println(jsonMessage);
        }
    }

    public void close() {
        isRunning = false;
        cleanup();
    }

    private void cleanup() {
        server.removeClient(this);
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing client resources: " + e.getMessage());
        }
    }

    public int getUserId() {
        return userId;
    }
}
