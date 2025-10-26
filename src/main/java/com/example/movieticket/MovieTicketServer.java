package com.example.movieticket;

import com.example.movieticket.network.SocketServer;

/**
 * Standalone server application for handling real-time communication
 * Run this separately from the main application to enable multi-client support
 */
public class MovieTicketServer {
    public static void main(String[] args) {
        System.out.println("Starting Movie Ticket Socket Server...");

        SocketServer server = new SocketServer();

        // Add shutdown hook to gracefully stop the server
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("\nShutting down server...");
                server.stop();
            } catch (Exception e) {
                System.err.println("Error stopping server: " + e.getMessage());
            }
        }));

        try {
            server.start();
        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
