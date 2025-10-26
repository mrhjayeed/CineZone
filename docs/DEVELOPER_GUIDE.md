# Developer Guide - CineZone Movie Ticket Booking System

## Table of Contents

1. [Development Environment Setup](#development-environment-setup)
2. [Project Architecture](#project-architecture)
3. [Code Structure](#code-structure)
4. [Core Components](#core-components)
5. [Database Layer](#database-layer)
6. [Network Layer](#network-layer)
7. [Adding New Features](#adding-new-features)
8. [Testing](#testing)
9. [Debugging](#debugging)
10. [Best Practices](#best-practices)
11. [Code Style Guide](#code-style-guide)

---

## Development Environment Setup

### Prerequisites

```bash
# Check Java version
java -version  # Should be 21+

# Check Maven version
mvn -version   # Should be 3.6+

# Check MySQL version
mysql --version  # Should be 8.0+
```

### IDE Setup

#### IntelliJ IDEA (Recommended)

1. **Import Project**
   - File → Open → Select `pom.xml`
   - Import as Maven project

2. **Configure SDK**
   - File → Project Structure → Project
   - Set SDK to Java 21

3. **Enable JavaFX**
   - File → Project Structure → Libraries
   - Add JavaFX library (Maven will handle this)

4. **Configure Run Configurations**
   
   **For Main Application:**
   ```
   Main class: com.example.movieticket.MovieTicketApp
   VM options: --module-path ${PATH_TO_FX} --add-modules javafx.controls,javafx.fxml
   ```
   
   **For Server:**
   ```
   Main class: com.example.movieticket.MovieTicketServer
   ```

#### Eclipse

1. Install e(fx)clipse plugin
2. Import as Maven project
3. Configure build path with JavaFX
4. Set compiler compliance to Java 21

#### VS Code

1. Install Java Extension Pack
2. Install JavaFX extensions
3. Configure launch.json with JavaFX modules

---

## Project Architecture

### Layered Architecture Pattern

```
┌─────────────────────────────────────────────────────┐
│          Presentation Layer (FXML + CSS)            │
│  - User Interface Components                        │
│  - FXML Layout Files                                │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│              Controller Layer                        │
│  - UI Event Handlers                                │
│  - Input Validation                                 │
│  - View Logic                                       │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│               Service Layer                          │
│  - Business Logic                                   │
│  - Transaction Management                           │
│  - Data Processing                                  │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│          Data Access Layer (DAO)                     │
│  - Database Operations                              │
│  - CRUD Methods                                     │
│  - Connection Management                            │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│              Database (MySQL)                        │
│  - Data Persistence                                 │
│  - Relationships & Constraints                      │
└─────────────────────────────────────────────────────┘
```

### MVC Pattern Implementation

**Model** (`model/` package)
- Represents data entities
- Contains getters/setters
- No business logic

**View** (FXML files in `resources/`)
- UI layout and structure
- CSS styling
- No logic

**Controller** (`controller/` package)
- Handles user interactions
- Updates models
- Refreshes views
- Calls service layer

---

## Code Structure

### Package Organization

```
com.example.movieticket/
│
├── MovieTicketApp.java          # Application entry point
├── MovieTicketServer.java       # Socket server entry point
│
├── controller/                  # UI Controllers (MVC)
│   ├── AdminDashboardController.java
│   ├── UserDashboardController.java
│   ├── LoginController.java
│   ├── SignupController.java
│   ├── SeatSelectionController.java
│   ├── PaymentDialogController.java
│   ├── ChatController.java
│   ├── MovieDialogController.java
│   ├── ScreeningDialogController.java
│   ├── ReviewDialogController.java
│   ├── ProfileEditController.java
│   ├── PasswordChangeController.java
│   ├── PasswordResetController.java
│   └── ... (other controllers)
│
├── model/                       # Data Models (POJOs)
│   ├── User.java               # User entity
│   ├── Movie.java              # Movie entity
│   ├── Screening.java          # Screening entity
│   ├── Booking.java            # Booking entity
│   ├── Seat.java               # Seat entity
│   ├── SeatLock.java           # Seat lock entity
│   ├── Review.java             # Review entity
│   ├── Payment.java            # Payment entity
│   └── ChatMessage.java        # Chat message entity
│
├── service/                     # Business Logic Layer
│   ├── DatabaseConnection.java      # DB connection pool
│   ├── DataService.java             # Main data service
│   └── RealTimeNotificationService.java  # Socket client
│
└── network/                     # Network Communication
    ├── SocketServer.java        # Server implementation
    ├── SocketClient.java        # Client socket wrapper
    ├── ClientHandler.java       # Individual client handler
    └── NetworkMessage.java      # Message protocol
```

---

## Core Components

### 1. MovieTicketApp (Main Application)

**Purpose**: Application entry point and JavaFX initialization

```java
public class MovieTicketApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Initialize real-time service
        RealTimeNotificationService notificationService = 
            RealTimeNotificationService.getInstance();
        
        // Connect to socket server
        notificationService.connect();
        
        // Load FXML and show window
        FXMLLoader loader = new FXMLLoader(
            MovieTicketApp.class.getResource("login-view.fxml")
        );
        Scene scene = new Scene(loader.load(), 1150, 750);
        stage.setScene(scene);
        stage.show();
        
        // Cleanup on close
        stage.setOnCloseRequest(event -> {
            notificationService.disconnect();
            Platform.exit();
        });
    }
}
```

**Key Responsibilities:**
- Initialize JavaFX application
- Establish socket connection
- Load login screen
- Handle application shutdown

### 2. MovieTicketServer (Socket Server)

**Purpose**: Handles real-time communication between clients

```java
public class MovieTicketServer {
    public static void main(String[] args) {
        SocketServer server = new SocketServer();
        
        // Graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
        }));
        
        server.start();  // Blocks and listens on port 8888
    }
}
```

**Key Responsibilities:**
- Accept client connections
- Broadcast messages
- Manage client sessions
- Handle disconnections

### 3. Controllers

Controllers manage UI interactions and coordinate between view and model.

**Example: LoginController**

```java
@FXML
private TextField usernameField;

@FXML
private PasswordField passwordField;

@FXML
private void handleLogin() {
    String username = usernameField.getText();
    String password = passwordField.getText();
    
    // Validate input
    if (username.isEmpty() || password.isEmpty()) {
        showAlert("Please fill in all fields");
        return;
    }
    
    // Authenticate user via service
    User user = DataService.authenticateUser(username, password);
    
    if (user != null) {
        // Navigate to dashboard
        loadDashboard(user);
    } else {
        showAlert("Invalid credentials");
    }
}
```

**Controller Best Practices:**
- Keep controllers thin
- Delegate business logic to service layer
- Use dependency injection where possible
- Handle errors gracefully
- Provide user feedback

### 4. Models

Models are POJOs (Plain Old Java Objects) representing data entities.

**Example: Movie Model**

```java
public class Movie {
    private int movieId;
    private String title;
    private String director;
    private int releaseYear;
    private String description;
    private int duration;
    private String genre;
    private double rating;
    private String posterUrl;
    private String trailerUrl;
    
    // Constructors
    public Movie() {}
    
    public Movie(int movieId, String title, ...) {
        this.movieId = movieId;
        this.title = title;
        // ... set other fields
    }
    
    // Getters and Setters
    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }
    // ... other getters/setters
}
```

**Model Guidelines:**
- Immutable where possible
- Override toString() for debugging
- Implement equals() and hashCode() if needed
- No business logic in models
- Use proper encapsulation

### 5. Services

Services contain business logic and coordinate data operations.

**Example: DataService Methods**

```java
public class DataService {
    
    // User operations
    public static User authenticateUser(String username, String password) {
        // Database query and password verification
    }
    
    public static boolean createUser(User user) {
        // Insert new user into database
    }
    
    // Movie operations
    public static List<Movie> getAllMovies() {
        // Fetch all movies from database
    }
    
    public static boolean addMovie(Movie movie) {
        // Insert movie with poster handling
    }
    
    // Booking operations
    public static boolean createBooking(Booking booking) {
        // Create booking with transaction
    }
    
    // Seat operations
    public static List<Seat> getSeatsForScreening(int screeningId) {
        // Fetch seat layout and availability
    }
    
    public static boolean lockSeats(int screeningId, List<String> seats, 
                                   int userId) {
        // Temporarily lock seats
    }
}
```

---

## Database Layer

### Connection Pooling

**DatabaseConnection.java** implements a connection pool for efficiency:

```java
private static final int MAX_CONNECTIONS = 10;
private static final ConcurrentLinkedQueue<Connection> connectionPool = 
    new ConcurrentLinkedQueue<>();

public static Connection getConnection() throws SQLException {
    // Try to get connection from pool
    Connection connection = connectionPool.poll();
    
    if (connection != null && connection.isValid(2)) {
        return connection;
    }
    
    // Create new connection if needed
    if (currentConnections.get() < MAX_CONNECTIONS) {
        connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        currentConnections.incrementAndGet();
        return connection;
    }
    
    // Wait for available connection
    return connectionPool.poll();
}

public static void releaseConnection(Connection connection) {
    if (connection != null) {
        connectionPool.offer(connection);
    }
}
```

### Database Operations Pattern

**Standard CRUD Pattern:**

```java
// CREATE
public static boolean createEntity(Entity entity) {
    String sql = "INSERT INTO table (field1, field2) VALUES (?, ?)";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, entity.getField1());
        stmt.setString(2, entity.getField2());
        
        int rowsAffected = stmt.executeUpdate();
        return rowsAffected > 0;
        
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

// READ
public static List<Entity> getAllEntities() {
    String sql = "SELECT * FROM table";
    List<Entity> entities = new ArrayList<>();
    
    try (Connection conn = DatabaseConnection.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        
        while (rs.next()) {
            Entity entity = new Entity(
                rs.getInt("id"),
                rs.getString("field1"),
                rs.getString("field2")
            );
            entities.add(entity);
        }
        
    } catch (SQLException e) {
        e.printStackTrace();
    }
    
    return entities;
}

// UPDATE
public static boolean updateEntity(Entity entity) {
    String sql = "UPDATE table SET field1 = ?, field2 = ? WHERE id = ?";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, entity.getField1());
        stmt.setString(2, entity.getField2());
        stmt.setInt(3, entity.getId());
        
        return stmt.executeUpdate() > 0;
        
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

// DELETE
public static boolean deleteEntity(int id) {
    String sql = "DELETE FROM table WHERE id = ?";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setInt(1, id);
        return stmt.executeUpdate() > 0;
        
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}
```

### Transaction Management

For operations requiring multiple database changes:

```java
public static boolean createBookingWithPayment(Booking booking, 
                                               Payment payment) {
    Connection conn = null;
    try {
        conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false);  // Start transaction
        
        // Insert booking
        String bookingSql = "INSERT INTO bookings (...) VALUES (...)";
        PreparedStatement bookingStmt = conn.prepareStatement(bookingSql, 
            Statement.RETURN_GENERATED_KEYS);
        // ... set parameters
        bookingStmt.executeUpdate();
        
        // Get generated booking ID
        ResultSet rs = bookingStmt.getGeneratedKeys();
        int bookingId = rs.next() ? rs.getInt(1) : 0;
        
        // Insert payment
        String paymentSql = "INSERT INTO payments (...) VALUES (...)";
        PreparedStatement paymentStmt = conn.prepareStatement(paymentSql);
        paymentStmt.setInt(1, bookingId);
        // ... set other parameters
        paymentStmt.executeUpdate();
        
        // Update seat status
        String seatSql = "UPDATE seats SET is_booked = true WHERE ...";
        PreparedStatement seatStmt = conn.prepareStatement(seatSql);
        // ... set parameters
        seatStmt.executeUpdate();
        
        conn.commit();  // Commit transaction
        return true;
        
    } catch (SQLException e) {
        if (conn != null) {
            try {
                conn.rollback();  // Rollback on error
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        e.printStackTrace();
        return false;
    } finally {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                DatabaseConnection.releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
```

---

## Network Layer

### Socket Communication Architecture

#### Server Side (SocketServer.java)

```java
public class SocketServer {
    private static final int PORT = 8888;
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();
    
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void broadcast(NetworkMessage message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }
    
    public void stop() {
        // Close all connections
    }
}
```

#### Client Side (SocketClient.java)

```java
public class SocketClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    public void sendMessage(NetworkMessage message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public NetworkMessage receiveMessage() {
        try {
            return (NetworkMessage) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
```

#### Message Protocol (NetworkMessage.java)

```java
public class NetworkMessage implements Serializable {
    private MessageType type;
    private String content;
    private int senderId;
    private int receiverId;
    private LocalDateTime timestamp;
    
    public enum MessageType {
        CHAT_MESSAGE,
        SEAT_LOCK,
        SEAT_UNLOCK,
        BOOKING_NOTIFICATION,
        USER_STATUS,
        SYSTEM_MESSAGE
    }
    
    // Constructors, getters, setters
}
```

### Real-Time Notification Service

**RealTimeNotificationService.java** (Singleton pattern):

```java
public class RealTimeNotificationService {
    private static RealTimeNotificationService instance;
    private SocketClient socketClient;
    private boolean isConnected = false;
    
    private RealTimeNotificationService() {
        socketClient = new SocketClient();
    }
    
    public static RealTimeNotificationService getInstance() {
        if (instance == null) {
            synchronized (RealTimeNotificationService.class) {
                if (instance == null) {
                    instance = new RealTimeNotificationService();
                }
            }
        }
        return instance;
    }
    
    public boolean connect() {
        isConnected = socketClient.connect("localhost", 8888);
        if (isConnected) {
            startMessageListener();
        }
        return isConnected;
    }
    
    private void startMessageListener() {
        new Thread(() -> {
            while (isConnected) {
                NetworkMessage message = socketClient.receiveMessage();
                if (message != null) {
                    handleMessage(message);
                }
            }
        }).start();
    }
    
    private void handleMessage(NetworkMessage message) {
        Platform.runLater(() -> {
            switch (message.getType()) {
                case CHAT_MESSAGE:
                    notifyChatListeners(message);
                    break;
                case SEAT_LOCK:
                    notifySeatListeners(message);
                    break;
                // ... handle other message types
            }
        });
    }
}
```

---

## Adding New Features

### Step-by-Step Guide

#### 1. Add Database Table (if needed)

Edit `database_schema.sql`:

```sql
CREATE TABLE IF NOT EXISTS new_feature (
    feature_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    feature_data VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
```

#### 2. Create Model Class

Create `NewFeature.java` in `model/` package:

```java
package com.example.movieticket.model;

public class NewFeature {
    private int featureId;
    private int userId;
    private String featureData;
    private LocalDateTime createdAt;
    
    // Constructors
    public NewFeature() {}
    
    public NewFeature(int featureId, int userId, String featureData) {
        this.featureId = featureId;
        this.userId = userId;
        this.featureData = featureData;
    }
    
    // Getters and Setters
    // ... implement all getters/setters
}
```

#### 3. Add Service Methods

Add methods to `DataService.java`:

```java
public static List<NewFeature> getAllFeatures() {
    String sql = "SELECT * FROM new_feature";
    List<NewFeature> features = new ArrayList<>();
    
    try (Connection conn = DatabaseConnection.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        
        while (rs.next()) {
            NewFeature feature = new NewFeature(
                rs.getInt("feature_id"),
                rs.getInt("user_id"),
                rs.getString("feature_data")
            );
            features.add(feature);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    
    return features;
}

public static boolean addFeature(NewFeature feature) {
    String sql = "INSERT INTO new_feature (user_id, feature_data) VALUES (?, ?)";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setInt(1, feature.getUserId());
        stmt.setString(2, feature.getFeatureData());
        
        return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}
```

#### 4. Create FXML View

Create `new-feature-dialog.fxml` in `resources/`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>

<DialogPane xmlns:fx="http://javafx.com/fxml"
            fx:controller="com.example.movieticket.controller.NewFeatureController"
            prefHeight="400" prefWidth="600">
    
    <content>
        <VBox spacing="10" style="-fx-padding: 20;">
            <Label text="Feature Data:" style="-fx-font-size: 14px;"/>
            <TextField fx:id="featureDataField" promptText="Enter data"/>
            
            <Button text="Save" onAction="#handleSave" 
                    style="-fx-background-color: #4CAF50; -fx-text-fill: white;"/>
        </VBox>
    </content>
</DialogPane>
```

#### 5. Create Controller

Create `NewFeatureController.java` in `controller/` package:

```java
package com.example.movieticket.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import com.example.movieticket.model.NewFeature;
import com.example.movieticket.service.DataService;

public class NewFeatureController {
    
    @FXML
    private TextField featureDataField;
    
    private int userId;
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    @FXML
    private void handleSave() {
        String data = featureDataField.getText();
        
        if (data.isEmpty()) {
            showAlert("Error", "Please enter feature data");
            return;
        }
        
        NewFeature feature = new NewFeature(0, userId, data);
        boolean success = DataService.addFeature(feature);
        
        if (success) {
            showAlert("Success", "Feature saved successfully");
            closeDialog();
        } else {
            showAlert("Error", "Failed to save feature");
        }
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void closeDialog() {
        featureDataField.getScene().getWindow().hide();
    }
}
```

#### 6. Integrate with Main UI

Add button to dashboard FXML and handle navigation:

```java
@FXML
private void openNewFeature() {
    try {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("new-feature-dialog.fxml")
        );
        DialogPane pane = loader.load();
        
        NewFeatureController controller = loader.getController();
        controller.setUserId(currentUser.getUserId());
        
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setDialogPane(pane);
        dialog.showAndWait();
        
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```

---

## Testing

### Unit Testing Setup

Add JUnit dependency to `pom.xml`:

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <version>5.12.1</version>
    <scope>test</scope>
</dependency>
```

### Example Unit Tests

**Testing Model Classes:**

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MovieTest {
    
    @Test
    public void testMovieCreation() {
        Movie movie = new Movie(1, "Test Movie", "Director", 
                               2024, "Description", 120, "Action", 4.5);
        
        assertEquals("Test Movie", movie.getTitle());
        assertEquals("Director", movie.getDirector());
        assertEquals(4.5, movie.getRating());
    }
    
    @Test
    public void testMovieSetters() {
        Movie movie = new Movie();
        movie.setTitle("New Title");
        movie.setRating(5.0);
        
        assertEquals("New Title", movie.getTitle());
        assertEquals(5.0, movie.getRating());
    }
}
```

**Testing Service Methods:**

```java
public class DataServiceTest {
    
    @Test
    public void testUserAuthentication() {
        // Test valid credentials
        User user = DataService.authenticateUser("admin", "admin123");
        assertNotNull(user);
        assertEquals("admin", user.getUsername());
        
        // Test invalid credentials
        User invalidUser = DataService.authenticateUser("invalid", "wrong");
        assertNull(invalidUser);
    }
    
    @Test
    public void testGetAllMovies() {
        List<Movie> movies = DataService.getAllMovies();
        assertNotNull(movies);
        assertTrue(movies.size() >= 0);
    }
}
```

### Integration Testing

**Testing Database Operations:**

```java
@Test
public void testBookingWorkflow() {
    // 1. Create user
    User user = new User(0, "testuser", "password", 
                        "test@test.com", "Test User", User.UserRole.USER);
    boolean userCreated = DataService.createUser(user);
    assertTrue(userCreated);
    
    // 2. Get movie
    List<Movie> movies = DataService.getAllMovies();
    assertFalse(movies.isEmpty());
    
    // 3. Get screening
    List<Screening> screenings = DataService.getScreeningsForMovie(
        movies.get(0).getMovieId()
    );
    assertFalse(screenings.isEmpty());
    
    // 4. Lock seats
    List<String> seats = Arrays.asList("A1", "A2");
    boolean locked = DataService.lockSeats(
        screenings.get(0).getScreeningId(), seats, user.getUserId()
    );
    assertTrue(locked);
    
    // 5. Create booking
    Booking booking = new Booking(/* ... */);
    boolean booked = DataService.createBooking(booking);
    assertTrue(booked);
}
```

### Manual Testing Checklist

- [ ] User registration and login
- [ ] Movie browsing and filtering
- [ ] Seat selection (single and multiple)
- [ ] Seat locking mechanism
- [ ] Payment processing
- [ ] Booking confirmation
- [ ] Profile editing
- [ ] Password change
- [ ] Review submission
- [ ] Chat messaging
- [ ] Admin movie management
- [ ] Admin user management
- [ ] Admin screening management
- [ ] Real-time updates
- [ ] Error handling
- [ ] Edge cases (network failure, database errors, etc.)

---

## Debugging

### Common Debugging Scenarios

#### 1. Database Connection Issues

```java
// Enable SQL logging
System.setProperty("com.mysql.cj.jdbc.debug", "true");

// Test connection
try {
    Connection conn = DatabaseConnection.getConnection();
    System.out.println("Connection successful: " + !conn.isClosed());
} catch (SQLException e) {
    System.err.println("Connection failed: " + e.getMessage());
    e.printStackTrace();
}
```

#### 2. FXML Loading Errors

```java
try {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("view.fxml"));
    Parent root = loader.load();
} catch (IOException e) {
    System.err.println("Failed to load FXML: " + e.getMessage());
    e.printStackTrace();
    
    // Check if resource exists
    URL resource = getClass().getResource("view.fxml");
    System.out.println("Resource URL: " + resource);
}
```

#### 3. Socket Communication Issues

```java
// Server side debugging
public void broadcast(NetworkMessage message, ClientHandler sender) {
    System.out.println("Broadcasting: " + message.getType());
    System.out.println("Connected clients: " + clients.size());
    
    for (ClientHandler client : clients) {
        try {
            client.sendMessage(message);
            System.out.println("Sent to client: " + client.getClientId());
        } catch (Exception e) {
            System.err.println("Failed to send: " + e.getMessage());
        }
    }
}

// Client side debugging
public void sendMessage(NetworkMessage message) {
    System.out.println("Sending message type: " + message.getType());
    try {
        out.writeObject(message);
        out.flush();
        System.out.println("Message sent successfully");
    } catch (IOException e) {
        System.err.println("Send failed: " + e.getMessage());
        e.printStackTrace();
    }
}
```

#### 4. JavaFX Thread Issues

```java
// Always update UI on JavaFX Application Thread
Platform.runLater(() -> {
    // UI updates here
    label.setText("Updated");
});

// Check if on FX thread
if (Platform.isFxApplicationThread()) {
    // Safe to update UI
} else {
    // Must use Platform.runLater()
}
```

### Logging

Add logging for better debugging:

```java
import java.util.logging.*;

public class DataService {
    private static final Logger LOGGER = 
        Logger.getLogger(DataService.class.getName());
    
    public static User authenticateUser(String username, String password) {
        LOGGER.info("Authenticating user: " + username);
        
        try {
            // ... authentication logic
            LOGGER.info("Authentication successful for: " + username);
            return user;
        } catch (Exception e) {
            LOGGER.severe("Authentication failed: " + e.getMessage());
            return null;
        }
    }
}
```

---

## Best Practices

### Code Organization

1. **Separation of Concerns**
   - Keep controllers focused on UI logic
   - Business logic in service layer
   - Data access in separate methods

2. **DRY Principle (Don't Repeat Yourself)**
   - Extract common code into utility methods
   - Reuse components where possible

3. **Error Handling**
   - Always handle exceptions
   - Provide meaningful error messages
   - Log errors for debugging

4. **Resource Management**
   - Use try-with-resources for connections
   - Release database connections
   - Close sockets properly

### Security

1. **Password Security**
   - Never store plain text passwords
   - Use hashing (BCrypt recommended)
   - Implement password strength requirements

2. **SQL Injection Prevention**
   - Always use PreparedStatement
   - Never concatenate SQL strings
   - Validate and sanitize input

3. **Input Validation**
   - Validate on both client and server
   - Check data types and ranges
   - Prevent XSS and injection attacks

### Performance

1. **Database Optimization**
   - Use connection pooling
   - Create appropriate indexes
   - Optimize queries (use EXPLAIN)
   - Batch operations when possible

2. **UI Responsiveness**
   - Run long operations in background threads
   - Show progress indicators
   - Don't block JavaFX thread

3. **Memory Management**
   - Close resources properly
   - Avoid memory leaks
   - Use weak references where appropriate

### Code Style

1. **Naming Conventions**
   - Classes: PascalCase (MovieController)
   - Methods: camelCase (getUserById)
   - Constants: UPPER_SNAKE_CASE (MAX_CONNECTIONS)
   - Variables: camelCase (userName)

2. **Documentation**
   - JavaDoc for public methods
   - Inline comments for complex logic
   - README for setup instructions

3. **Formatting**
   - Consistent indentation (4 spaces)
   - Maximum line length: 120 characters
   - Blank lines between methods
   - Group related methods

---

## Code Style Guide

### Java Code Formatting

```java
/**
 * Authenticates a user with the provided credentials.
 * 
 * @param username the username to authenticate
 * @param password the password to verify
 * @return User object if authentication successful, null otherwise
 * @throws SQLException if database error occurs
 */
public static User authenticateUser(String username, String password) 
        throws SQLException {
    // Validate input
    if (username == null || username.isEmpty()) {
        throw new IllegalArgumentException("Username cannot be empty");
    }
    
    // Query database
    String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, username);
        stmt.setString(2, hashPassword(password));
        
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        }
    }
    
    return null;
}
```

### FXML Style

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>
<?import javafx.geometry.Insets?>

<VBox xmlns:fx="http://javafx.com/fxml"
      fx:controller="com.example.movieticket.controller.LoginController"
      spacing="20"
      alignment="CENTER"
      styleClass="root-container">
    
    <padding>
        <Insets top="30" right="30" bottom="30" left="30"/>
    </padding>
    
    <Label text="Login" styleClass="title-label"/>
    
    <TextField fx:id="usernameField" 
               promptText="Username"
               styleClass="custom-text-field"/>
    
    <PasswordField fx:id="passwordField" 
                   promptText="Password"
                   styleClass="custom-text-field"/>
    
    <Button text="Login" 
            onAction="#handleLogin"
            styleClass="primary-button"
            prefWidth="200"/>
</VBox>
```

### CSS Style

```css
/* Root container styling */
.root-container {
    -fx-background-color: linear-gradient(to bottom, #1a1a2e, #16213e);
    -fx-padding: 20px;
}

/* Title labels */
.title-label {
    -fx-font-size: 28px;
    -fx-font-weight: bold;
    -fx-text-fill: white;
}

/* Custom text fields */
.custom-text-field {
    -fx-background-color: rgba(255, 255, 255, 0.1);
    -fx-text-fill: white;
    -fx-prompt-text-fill: rgba(255, 255, 255, 0.5);
    -fx-border-color: #0f3460;
    -fx-border-radius: 5px;
    -fx-background-radius: 5px;
    -fx-padding: 10px;
    -fx-font-size: 14px;
}

/* Primary button */
.primary-button {
    -fx-background-color: linear-gradient(to right, #e94560, #c82349);
    -fx-text-fill: white;
    -fx-font-size: 16px;
    -fx-font-weight: bold;
    -fx-border-radius: 5px;
    -fx-background-radius: 5px;
    -fx-cursor: hand;
}

.primary-button:hover {
    -fx-background-color: linear-gradient(to right, #c82349, #a01d3a);
    -fx-scale-y: 1.05;
    -fx-scale-x: 1.05;
}
```

---

## Conclusion

This developer guide provides the foundation for understanding and extending the CineZone Movie Ticket Booking System. For specific implementation details, refer to the source code and inline documentation.

**Happy Coding! 🚀**

