package com.example.movieticket.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/movieticket_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "jdb@mySQL";
    private static final String SQL_FILE_PATH = "/database_schema.sql";

    private static final int MAX_CONNECTIONS = 10;
    private static final ConcurrentLinkedQueue<Connection> connectionPool = new ConcurrentLinkedQueue<>();
    private static final AtomicInteger currentConnections = new AtomicInteger(0);

    public static Connection getConnection() throws SQLException {
        Connection connection = connectionPool.poll();

        if (connection != null && !connection.isClosed() && connection.isValid(2)) {
            return connection;
        }

        if (currentConnections.get() < MAX_CONNECTIONS) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                currentConnections.incrementAndGet();
                return connection;
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found", e);
            }
        }

        // Wait for a connection to become available
        connection = connectionPool.poll();
        if (connection == null || connection.isClosed() || !connection.isValid(2)) {
            // Create new connection if pool is empty and limit not reached
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                currentConnections.incrementAndGet();
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found", e);
            }
        }

        return connection;
    }

    public static void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed() && connection.isValid(2)) {
                    connectionPool.offer(connection);
                } else {
                    currentConnections.decrementAndGet();
                }
            } catch (SQLException e) {
                currentConnections.decrementAndGet();
                System.err.println("Error checking connection validity: " + e.getMessage());
            }
        }
    }

    public static void initializeDatabase() {
        createDatabase();
        executeSqlFile();
        migrateDatabase(); // Add migration for existing databases
    }

    public static void createDatabase() {
        try {
            // Connect to MySQL server without specifying database
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", USERNAME, PASSWORD);
            Statement stmt = conn.createStatement();

            // Create database if not exists
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS movieticket_db");

            conn.close();
            System.out.println("Database created successfully");
        } catch (SQLException e) {
            System.err.println("Error creating database: " + e.getMessage());
        }
    }

    public static void executeSqlFile() {
        try (Connection conn = getConnection()) {
            InputStream inputStream = DatabaseConnection.class.getResourceAsStream(SQL_FILE_PATH);
            if (inputStream == null) {
                System.err.println("SQL file not found: " + SQL_FILE_PATH);
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sqlBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Skip comments and empty lines
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                sqlBuilder.append(line).append(" ");
            }

            // Split by semicolons to get individual SQL statements
            String[] sqlStatements = sqlBuilder.toString().split(";");

            try (Statement stmt = conn.createStatement()) {
                for (String sql : sqlStatements) {
                    sql = sql.trim();
                    if (!sql.isEmpty()) {
                        // Skip USE database statement since we're already connected to the database
                        if (!sql.toUpperCase().startsWith("USE ")) {
                            stmt.executeUpdate(sql);
                        }
                    }
                }
            }

            System.out.println("Database schema executed successfully from SQL file");

        } catch (SQLException | IOException e) {
            System.err.println("Error executing SQL file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void closeAllConnections() {
        Connection connection;
        while ((connection = connectionPool.poll()) != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
        currentConnections.set(0);
    }

    /**
     * Migrate existing database to add new columns if they don't exist
     */
    private static void migrateDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Check if profile_picture_path column exists, if not add it
            try {
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN profile_picture_path VARCHAR(500)");
                System.out.println("Added profile_picture_path column to users table");
            } catch (SQLException e) {
                // Column already exists, ignore the error
                if (!e.getMessage().contains("Duplicate column name")) {
                    System.err.println("Error during migration: " + e.getMessage());
                }
            }

        } catch (SQLException e) {
            System.err.println("Error migrating database: " + e.getMessage());
        }
    }
}
