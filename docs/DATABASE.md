# Database Documentation - CineZone Movie Ticket Booking System

## Table of Contents

1. [Overview](#overview)
2. [Database Schema](#database-schema)
3. [Entity Relationship Diagram](#entity-relationship-diagram)
4. [Table Specifications](#table-specifications)
5. [Relationships](#relationships)
6. [Indexes and Constraints](#indexes-and-constraints)
7. [Sample Queries](#sample-queries)
8. [Database Optimization](#database-optimization)
9. [Backup and Recovery](#backup-and-recovery)

---

## Overview

### Database Management System

- **Type**: Relational Database (MySQL)
- **Version**: MySQL 8.0+
- **Character Set**: UTF-8
- **Collation**: utf8mb4_unicode_ci
- **Engine**: InnoDB (default)

### Database Name

```sql
movieticket_db
```

### Connection Details

```
Host: localhost
Port: 3306
Database: movieticket_db
Username: root (configurable)
Password: (configured in DatabaseConnection.java)
```

---

## Database Schema

### Complete Schema Visualization

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│   users     │       │   movies    │       │ screenings  │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ user_id (PK)│       │ movie_id(PK)│       │screening_id │
│ username    │       │ title       │◄──────┤ movie_id(FK)│
│ password    │       │ director    │       │ screen_name │
│ email       │       │ release_year│       │ show_time   │
│ full_name   │       │ description │       │ ticket_price│
│ role        │       │ duration    │       │ total_seats │
│ profile_pic │       │ genre       │       │ available   │
│ created_at  │       │ rating      │       └──────┬──────┘
└──────┬──────┘       │ poster_url  │              │
       │              │ trailer_url │              │
       │              └─────────────┘              │
       │                                           │
       │              ┌─────────────┐              │
       │              │   seats     │              │
       │              ├─────────────┤              │
       │              │ seat_id (PK)│              │
       │              │screening_id │◄─────────────┘
       │              │ seat_number │
       │              │ row_number  │
       │              │ is_booked   │
       │              └──────┬──────┘
       │                     │
       │              ┌──────▼──────┐
       │              │  bookings   │
       ├─────────────►├─────────────┤
       │              │booking_id   │
       │              │ user_id (FK)│
       │              │screening_id │
       │              │ seat_ids    │
       │              │ total_amount│
       │              │ booking_date│
       │              │ status      │
       │              └──────┬──────┘
       │                     │
       │              ┌──────▼──────┐
       │              │  payments   │
       │              ├─────────────┤
       │              │ payment_id  │
       │              │ booking_id  │
       │              │ pay_method  │
       │              │ amount      │
       │              │ status      │
       │              └─────────────┘
       │
       │              ┌─────────────┐
       ├─────────────►│  reviews    │
       │              ├─────────────┤
       │              │ review_id   │
       │              │ user_id (FK)│
       │              │ rating      │
       │              │ title       │
       │              │ comment     │
       │              │ review_type │
       │              └─────────────┘
       │
       │              ┌─────────────┐
       ├─────────────►│  messages   │
       ├─────────────►├─────────────┤
       │              │ message_id  │
       │              │ sender_id   │
       │              │ receiver_id │
       │              │ content     │
       │              │ timestamp   │
       │              │ is_read     │
       │              └─────────────┘
       │
       │              ┌─────────────┐
       └─────────────►│ seat_locks  │
                      ├─────────────┤
                      │ lock_id (PK)│
                      │screening_id │
                      │ seat_number │
                      │ user_id (FK)│
                      │ lock_time   │
                      │ expires_at  │
                      └─────────────┘
```

---

## Table Specifications

### 1. users

**Purpose**: Stores user account information and authentication credentials

```sql
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'USER') NOT NULL DEFAULT 'USER',
    profile_picture_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Columns:**

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| user_id | INT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| username | VARCHAR(50) | UNIQUE, NOT NULL | Login username |
| password | VARCHAR(255) | NOT NULL | Hashed password |
| email | VARCHAR(100) | UNIQUE, NOT NULL | User email |
| full_name | VARCHAR(100) | NOT NULL | User's full name |
| role | ENUM | NOT NULL, DEFAULT 'USER' | User role (ADMIN/USER) |
| profile_picture_path | VARCHAR(500) | NULLABLE | Path to profile image |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Account creation date |

**Indexes:**
- Primary Key: `user_id`
- Unique Index: `username`, `email`

---

### 2. movies

**Purpose**: Stores movie catalog information

```sql
CREATE TABLE IF NOT EXISTS movies (
    movie_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    director VARCHAR(255) NOT NULL,
    release_year INT NOT NULL,
    description TEXT,
    duration INT NOT NULL,
    genre VARCHAR(100),
    rating DECIMAL(3,1),
    poster_url VARCHAR(500),
    trailer_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Columns:**

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| movie_id | INT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| title | VARCHAR(255) | NOT NULL | Movie title |
| director | VARCHAR(255) | NOT NULL | Director name |
| release_year | INT | NOT NULL | Year of release |
| description | TEXT | NULLABLE | Plot description |
| duration | INT | NOT NULL | Length in minutes |
| genre | VARCHAR(100) | NULLABLE | Movie genre |
| rating | DECIMAL(3,1) | NULLABLE | Average rating (0.0-5.0) |
| poster_url | VARCHAR(500) | NULLABLE | Poster image path |
| trailer_url | VARCHAR(500) | NULLABLE | Trailer video URL |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation date |

**Indexes:**
- Primary Key: `movie_id`
- Index: `title`, `genre`, `release_year`

---

### 3. screenings

**Purpose**: Stores movie screening/showtime information

```sql
CREATE TABLE IF NOT EXISTS screenings (
    screening_id INT AUTO_INCREMENT PRIMARY KEY,
    movie_id INT NOT NULL,
    screen_name VARCHAR(50) NOT NULL,
    show_time DATETIME NOT NULL,
    ticket_price DECIMAL(10,2) NOT NULL,
    total_seats INT NOT NULL DEFAULT 100,
    available_seats INT NOT NULL DEFAULT 100,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (movie_id) REFERENCES movies(movie_id) ON DELETE CASCADE
);
```

**Columns:**

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| screening_id | INT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| movie_id | INT | FOREIGN KEY, NOT NULL | Reference to movie |
| screen_name | VARCHAR(50) | NOT NULL | Theater screen/hall name |
| show_time | DATETIME | NOT NULL | Date and time of screening |
| ticket_price | DECIMAL(10,2) | NOT NULL | Price per ticket |
| total_seats | INT | NOT NULL, DEFAULT 100 | Total seat capacity |
| available_seats | INT | NOT NULL, DEFAULT 100 | Currently available seats |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation date |

**Indexes:**
- Primary Key: `screening_id`
- Foreign Key: `movie_id` → `movies(movie_id)`
- Index: `show_time`, `screen_name`

---

### 4. seats

**Purpose**: Stores individual seat information for each screening

```sql
CREATE TABLE IF NOT EXISTS seats (
    seat_id INT AUTO_INCREMENT PRIMARY KEY,
    screening_id INT NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    row_number VARCHAR(5) NOT NULL,
    is_booked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (screening_id) REFERENCES screenings(screening_id) ON DELETE CASCADE,
    UNIQUE KEY unique_seat (screening_id, seat_number, row_number)
);
```

**Columns:**

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| seat_id | INT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| screening_id | INT | FOREIGN KEY, NOT NULL | Reference to screening |
| seat_number | VARCHAR(10) | NOT NULL | Seat column number (1-10) |
| row_number | VARCHAR(5) | NOT NULL | Seat row letter (A-J) |
| is_booked | BOOLEAN | DEFAULT FALSE | Booking status |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation date |

**Indexes:**
- Primary Key: `seat_id`
- Foreign Key: `screening_id` → `screenings(screening_id)`
- Unique Key: `(screening_id, seat_number, row_number)`

---

### 5. bookings

**Purpose**: Stores ticket booking records

```sql
CREATE TABLE IF NOT EXISTS bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    screening_id INT NOT NULL,
    seat_ids TEXT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('CONFIRMED', 'CANCELLED', 'PENDING') DEFAULT 'CONFIRMED',
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (screening_id) REFERENCES screenings(screening_id) ON DELETE CASCADE
);
```

**Columns:**

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| booking_id | INT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| user_id | INT | FOREIGN KEY, NOT NULL | Reference to user |
| screening_id | INT | FOREIGN KEY, NOT NULL | Reference to screening |
| seat_ids | TEXT | NOT NULL | Comma-separated seat IDs |
| total_amount | DECIMAL(10,2) | NOT NULL | Total payment amount |
| booking_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Booking creation time |
| status | ENUM | DEFAULT 'CONFIRMED' | Booking status |

**Indexes:**
- Primary Key: `booking_id`
- Foreign Keys: `user_id`, `screening_id`
- Index: `status`, `booking_date`

---

### 6. payments

**Purpose**: Stores payment transaction information

```sql
CREATE TABLE IF NOT EXISTS payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    payment_method ENUM('CREDIT_CARD', 'DEBIT_CARD', 'MOBILE_BANKING') NOT NULL,
    mobile_banking_provider ENUM('BKASH', 'NAGAD', 'ROCKET', 'NONE') DEFAULT 'NONE',
    amount DECIMAL(10,2) NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',
    transaction_id VARCHAR(100) UNIQUE,
    card_last_four_digits VARCHAR(4),
    mobile_number VARCHAR(15),
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE
);
```

**Columns:**

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| payment_id | INT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| booking_id | INT | FOREIGN KEY, NOT NULL | Reference to booking |
| payment_method | ENUM | NOT NULL | Payment method type |
| mobile_banking_provider | ENUM | DEFAULT 'NONE' | Mobile banking service |
| amount | DECIMAL(10,2) | NOT NULL | Payment amount |
| payment_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Payment timestamp |
| status | ENUM | DEFAULT 'PENDING' | Payment status |
| transaction_id | VARCHAR(100) | UNIQUE | Transaction reference |
| card_last_four_digits | VARCHAR(4) | NULLABLE | Last 4 digits of card |
| mobile_number | VARCHAR(15) | NULLABLE | Mobile payment number |

---

### 7. reviews

**Purpose**: Stores user reviews and ratings

```sql
CREATE TABLE IF NOT EXISTS reviews (
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title VARCHAR(255) NOT NULL,
    comment TEXT NOT NULL,
    review_type ENUM('THEATER_EXPERIENCE', 'MOVIE_REVIEW', 'SERVICE_FEEDBACK') 
        NOT NULL DEFAULT 'THEATER_EXPERIENCE',
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
```

**Columns:**

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| review_id | INT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| user_id | INT | FOREIGN KEY, NOT NULL | Reference to user |
| rating | INT | NOT NULL, CHECK (1-5) | Star rating (1-5) |
| title | VARCHAR(255) | NOT NULL | Review headline |
| comment | TEXT | NOT NULL | Detailed review |
| review_type | ENUM | NOT NULL | Type of review |
| review_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Review submission date |

---

### 8. messages

**Purpose**: Stores chat messages between users

```sql
CREATE TABLE IF NOT EXISTS messages (
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    content TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(user_id) ON DELETE CASCADE
);
```

**Columns:**

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| message_id | INT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| sender_id | INT | FOREIGN KEY, NOT NULL | Message sender |
| receiver_id | INT | FOREIGN KEY, NOT NULL | Message recipient |
| content | TEXT | NOT NULL | Message text |
| timestamp | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Send time |
| is_read | BOOLEAN | DEFAULT FALSE | Read status |

---

### 9. seat_locks

**Purpose**: Stores temporary seat reservations during booking process

```sql
CREATE TABLE IF NOT EXISTS seat_locks (
    lock_id INT AUTO_INCREMENT PRIMARY KEY,
    screening_id INT NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    user_id INT NOT NULL,
    lock_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (screening_id) REFERENCES screenings(screening_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_seat_lock (screening_id, seat_number)
);
```

**Columns:**

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| lock_id | INT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| screening_id | INT | FOREIGN KEY, NOT NULL | Reference to screening |
| seat_number | VARCHAR(10) | NOT NULL | Locked seat identifier |
| user_id | INT | FOREIGN KEY, NOT NULL | User who locked seat |
| lock_timestamp | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Lock creation time |
| expires_at | TIMESTAMP | NOT NULL | Lock expiration time |

**Special Features:**
- Locks automatically expire after 5 minutes
- Unique constraint prevents double-locking same seat

---

## Relationships

### Primary Relationships

1. **users → bookings** (One-to-Many)
   - One user can have multiple bookings
   - `users.user_id` → `bookings.user_id`

2. **users → reviews** (One-to-Many)
   - One user can write multiple reviews
   - `users.user_id` → `reviews.user_id`

3. **users → messages** (One-to-Many, bidirectional)
   - One user can send/receive multiple messages
   - `users.user_id` → `messages.sender_id`
   - `users.user_id` → `messages.receiver_id`

4. **movies → screenings** (One-to-Many)
   - One movie can have multiple screenings
   - `movies.movie_id` → `screenings.movie_id`

5. **screenings → seats** (One-to-Many)
   - One screening has multiple seats
   - `screenings.screening_id` → `seats.screening_id`

6. **screenings → bookings** (One-to-Many)
   - One screening can have multiple bookings
   - `screenings.screening_id` → `bookings.screening_id`

7. **bookings → payments** (One-to-One/One-to-Many)
   - One booking has one or more payments
   - `bookings.booking_id` → `payments.booking_id`

8. **screenings → seat_locks** (One-to-Many)
   - One screening can have multiple seat locks
   - `screenings.screening_id` → `seat_locks.screening_id`

---

## Indexes and Constraints

### Primary Keys

All tables have an auto-incrementing integer primary key for efficient lookups.

### Foreign Keys

All foreign keys use `ON DELETE CASCADE` to maintain referential integrity:

```sql
-- Example
FOREIGN KEY (movie_id) REFERENCES movies(movie_id) ON DELETE CASCADE
```

This means:
- Deleting a movie deletes all its screenings
- Deleting a user deletes all their bookings, reviews, messages
- Deleting a screening deletes all its seats and bookings

### Unique Constraints

```sql
-- Username and email must be unique
UNIQUE (username)
UNIQUE (email)

-- Each seat in a screening must be unique
UNIQUE KEY unique_seat (screening_id, seat_number, row_number)

-- Only one lock per seat per screening
UNIQUE KEY unique_seat_lock (screening_id, seat_number)

-- Transaction IDs must be unique
UNIQUE (transaction_id)
```

### Check Constraints

```sql
-- Rating must be between 1 and 5
CHECK (rating >= 1 AND rating <= 5)
```

### Recommended Indexes

```sql
-- Performance indexes
CREATE INDEX idx_username ON users(username);
CREATE INDEX idx_email ON users(email);
CREATE INDEX idx_movie_title ON movies(title);
CREATE INDEX idx_movie_genre ON movies(genre);
CREATE INDEX idx_show_time ON screenings(show_time);
CREATE INDEX idx_booking_date ON bookings(booking_date);
CREATE INDEX idx_message_timestamp ON messages(timestamp);
CREATE INDEX idx_seat_lock_expires ON seat_locks(expires_at);
```

---

## Sample Queries

### User Management

```sql
-- Create new user
INSERT INTO users (username, password, email, full_name, role)
VALUES ('john_doe', '$hashed_password', 'john@example.com', 'John Doe', 'USER');

-- Authenticate user
SELECT user_id, username, email, full_name, role, profile_picture_path
FROM users
WHERE username = 'john_doe' AND password = '$hashed_password';

-- Update user profile
UPDATE users
SET full_name = 'John Smith', email = 'john.smith@example.com'
WHERE user_id = 1;

-- Get all users (admin)
SELECT user_id, username, email, full_name, role, created_at
FROM users
ORDER BY created_at DESC;
```

### Movie Operations

```sql
-- Add new movie
INSERT INTO movies (title, director, release_year, description, duration, genre, rating, poster_url, trailer_url)
VALUES ('Inception', 'Christopher Nolan', 2010, 'A thief...', 148, 'Sci-Fi', 4.8, '/posters/inception.jpg', 'https://youtube.com/...');

-- Get all movies
SELECT * FROM movies ORDER BY release_year DESC;

-- Search movies by genre
SELECT * FROM movies WHERE genre = 'Action' ORDER BY rating DESC;

-- Update movie rating
UPDATE movies
SET rating = (SELECT AVG(rating) FROM reviews WHERE movie_id = 1)
WHERE movie_id = 1;

-- Delete movie (cascades to screenings)
DELETE FROM movies WHERE movie_id = 1;
```

### Screening Management

```sql
-- Add screening
INSERT INTO screenings (movie_id, screen_name, show_time, ticket_price, total_seats, available_seats)
VALUES (1, 'Screen 1', '2024-10-25 18:30:00', 12.50, 100, 100);

-- Get screenings for a movie
SELECT s.screening_id, s.screen_name, s.show_time, s.ticket_price, s.available_seats,
       m.title, m.duration
FROM screenings s
JOIN movies m ON s.movie_id = m.movie_id
WHERE s.movie_id = 1 AND s.show_time > NOW()
ORDER BY s.show_time;

-- Update available seats
UPDATE screenings
SET available_seats = available_seats - 2
WHERE screening_id = 1;
```

### Seat Operations

```sql
-- Initialize seats for a screening
INSERT INTO seats (screening_id, seat_number, row_number)
SELECT 1, seat_num, row_letter
FROM 
    (SELECT 1 as n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 
     UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) nums
CROSS JOIN
    (SELECT 'A' as letter UNION SELECT 'B' UNION SELECT 'C' UNION SELECT 'D' UNION SELECT 'E'
     UNION SELECT 'F' UNION SELECT 'G' UNION SELECT 'H' UNION SELECT 'I' UNION SELECT 'J') rows;

-- Get seat layout for screening
SELECT seat_id, seat_number, row_number, is_booked
FROM seats
WHERE screening_id = 1
ORDER BY row_number, seat_number;

-- Book seats
UPDATE seats
SET is_booked = TRUE
WHERE seat_id IN (1, 2, 3);
```

### Seat Locking

```sql
-- Lock seats (5-minute expiry)
INSERT INTO seat_locks (screening_id, seat_number, user_id, expires_at)
VALUES (1, 'A1', 1, DATE_ADD(NOW(), INTERVAL 5 MINUTE));

-- Check if seat is locked
SELECT * FROM seat_locks
WHERE screening_id = 1 AND seat_number = 'A1' AND expires_at > NOW();

-- Remove expired locks
DELETE FROM seat_locks WHERE expires_at < NOW();

-- Release user's locks
DELETE FROM seat_locks WHERE user_id = 1 AND screening_id = 1;
```

### Booking Operations

```sql
-- Create booking
INSERT INTO bookings (user_id, screening_id, seat_ids, total_amount, status)
VALUES (1, 1, '1,2,3', 37.50, 'CONFIRMED');

-- Get user's bookings
SELECT b.booking_id, b.seat_ids, b.total_amount, b.booking_date, b.status,
       m.title, m.poster_url, s.show_time, s.screen_name
FROM bookings b
JOIN screenings s ON b.screening_id = s.screening_id
JOIN movies m ON s.movie_id = m.movie_id
WHERE b.user_id = 1
ORDER BY b.booking_date DESC;

-- Get upcoming bookings
SELECT * FROM bookings b
JOIN screenings s ON b.screening_id = s.screening_id
WHERE b.user_id = 1 AND s.show_time > NOW() AND b.status = 'CONFIRMED';

-- Cancel booking
UPDATE bookings SET status = 'CANCELLED' WHERE booking_id = 1;
```

### Payment Processing

```sql
-- Record payment
INSERT INTO payments (booking_id, payment_method, amount, status, transaction_id, card_last_four_digits)
VALUES (1, 'CREDIT_CARD', 37.50, 'COMPLETED', 'TXN123456789', '4532');

-- Get payment history
SELECT p.payment_id, p.amount, p.payment_method, p.payment_date, p.status,
       b.booking_id, m.title
FROM payments p
JOIN bookings b ON p.booking_id = b.booking_id
JOIN screenings s ON b.screening_id = s.screening_id
JOIN movies m ON s.movie_id = m.movie_id
WHERE b.user_id = 1
ORDER BY p.payment_date DESC;
```

### Reviews

```sql
-- Add review
INSERT INTO reviews (user_id, rating, title, comment, review_type)
VALUES (1, 5, 'Amazing Experience!', 'The theater was great...', 'THEATER_EXPERIENCE');

-- Get all reviews with user info
SELECT r.review_id, r.rating, r.title, r.comment, r.review_type, r.review_date,
       u.username, u.full_name, u.profile_picture_path
FROM reviews r
JOIN users u ON r.user_id = u.user_id
ORDER BY r.review_date DESC;

-- Get user's reviews
SELECT * FROM reviews WHERE user_id = 1;

-- Calculate average theater rating
SELECT AVG(rating) as avg_rating
FROM reviews
WHERE review_type = 'THEATER_EXPERIENCE';
```

### Chat Messages

```sql
-- Send message
INSERT INTO messages (sender_id, receiver_id, content)
VALUES (1, 2, 'Hello, how are you?');

-- Get conversation between two users
SELECT m.message_id, m.content, m.timestamp, m.is_read,
       s.username as sender_name, r.username as receiver_name
FROM messages m
JOIN users s ON m.sender_id = s.user_id
JOIN users r ON m.receiver_id = r.user_id
WHERE (m.sender_id = 1 AND m.receiver_id = 2)
   OR (m.sender_id = 2 AND m.receiver_id = 1)
ORDER BY m.timestamp;

-- Mark message as read
UPDATE messages SET is_read = TRUE WHERE message_id = 1;

-- Get unread message count
SELECT COUNT(*) as unread_count
FROM messages
WHERE receiver_id = 1 AND is_read = FALSE;
```

### Complex Queries

```sql
-- Get most popular movies (by bookings)
SELECT m.movie_id, m.title, m.poster_url, COUNT(b.booking_id) as booking_count
FROM movies m
LEFT JOIN screenings s ON m.movie_id = s.movie_id
LEFT JOIN bookings b ON s.screening_id = b.screening_id
WHERE b.status = 'CONFIRMED'
GROUP BY m.movie_id
ORDER BY booking_count DESC
LIMIT 10;

-- Get revenue by movie
SELECT m.title, SUM(b.total_amount) as total_revenue
FROM movies m
JOIN screenings s ON m.movie_id = s.movie_id
JOIN bookings b ON s.screening_id = b.screening_id
WHERE b.status = 'CONFIRMED'
GROUP BY m.movie_id
ORDER BY total_revenue DESC;

-- Get screening occupancy
SELECT s.screening_id, m.title, s.show_time, s.screen_name,
       (s.total_seats - s.available_seats) as booked_seats,
       s.total_seats,
       ROUND(((s.total_seats - s.available_seats) / s.total_seats) * 100, 2) as occupancy_percentage
FROM screenings s
JOIN movies m ON s.movie_id = m.movie_id
ORDER BY s.show_time;

-- Get user booking statistics
SELECT u.user_id, u.username, u.full_name,
       COUNT(b.booking_id) as total_bookings,
       SUM(b.total_amount) as total_spent,
       AVG(b.total_amount) as avg_booking_amount
FROM users u
LEFT JOIN bookings b ON u.user_id = b.user_id AND b.status = 'CONFIRMED'
GROUP BY u.user_id;
```

---

## Database Optimization

### Performance Tips

1. **Use Indexes Wisely**
   ```sql
   -- Add composite indexes for frequently queried combinations
   CREATE INDEX idx_screening_movie_time ON screenings(movie_id, show_time);
   CREATE INDEX idx_booking_user_status ON bookings(user_id, status);
   ```

2. **Optimize JOIN Queries**
   - Use INNER JOIN instead of WHERE for joins
   - Select only needed columns
   - Use EXPLAIN to analyze query performance

3. **Connection Pooling**
   - Reuse connections (implemented in DatabaseConnection.java)
   - Close connections properly
   - Set appropriate pool size (max 10 in current implementation)

4. **Query Caching**
   ```sql
   -- Enable query cache (MySQL 5.7 and earlier)
   SET GLOBAL query_cache_size = 67108864; -- 64MB
   SET GLOBAL query_cache_type = 1;
   ```

5. **Regular Maintenance**
   ```sql
   -- Analyze tables
   ANALYZE TABLE users, movies, bookings;
   
   -- Optimize tables
   OPTIMIZE TABLE users, movies, bookings;
   
   -- Check table integrity
   CHECK TABLE users, movies, bookings;
   ```

### Cleanup Procedures

```sql
-- Remove expired seat locks (run periodically)
DELETE FROM seat_locks WHERE expires_at < NOW();

-- Archive old bookings (older than 1 year)
INSERT INTO bookings_archive SELECT * FROM bookings WHERE booking_date < DATE_SUB(NOW(), INTERVAL 1 YEAR);
DELETE FROM bookings WHERE booking_date < DATE_SUB(NOW(), INTERVAL 1 YEAR);

-- Clean up old messages (optional)
DELETE FROM messages WHERE timestamp < DATE_SUB(NOW(), INTERVAL 6 MONTH) AND is_read = TRUE;
```

---

## Backup and Recovery

### Backup Strategies

1. **Full Backup**
   ```bash
   mysqldump -u root -p movieticket_db > backup_full_$(date +%Y%m%d).sql
   ```

2. **Table-Specific Backup**
   ```bash
   mysqldump -u root -p movieticket_db users movies > backup_critical_$(date +%Y%m%d).sql
   ```

3. **Structure Only**
   ```bash
   mysqldump -u root -p --no-data movieticket_db > schema_only.sql
   ```

4. **Data Only**
   ```bash
   mysqldump -u root -p --no-create-info movieticket_db > data_only.sql
   ```

### Restore Procedures

```bash
# Restore full backup
mysql -u root -p movieticket_db < backup_full_20241023.sql

# Restore specific tables
mysql -u root -p movieticket_db < backup_critical_20241023.sql
```

### Automated Backup Script (Windows)

```batch
@echo off
set TIMESTAMP=%date:~-4%%date:~-10,2%%date:~-7,2%
set BACKUP_FILE=backup_%TIMESTAMP%.sql
mysqldump -u root -p movieticket_db > C:\backups\%BACKUP_FILE%
echo Backup completed: %BACKUP_FILE%
```

### Backup Schedule Recommendations

- **Daily**: Full database backup (keep last 7 days)
- **Weekly**: Archived backup (keep last 4 weeks)
- **Monthly**: Long-term archive (keep last 12 months)
- **Before major updates**: Manual backup

---

## Database Security

### Best Practices

1. **Use Strong Passwords**
   - Change default root password
   - Use complex passwords for database users

2. **Create Limited-Privilege Users**
   ```sql
   -- Create application user with limited privileges
   CREATE USER 'movieticket_app'@'localhost' IDENTIFIED BY 'strong_password';
   GRANT SELECT, INSERT, UPDATE, DELETE ON movieticket_db.* TO 'movieticket_app'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. **Encrypt Sensitive Data**
   - Hash passwords (never store plain text)
   - Encrypt card numbers (if storing)
   - Use SSL for database connections

4. **Regular Updates**
   - Keep MySQL updated
   - Apply security patches
   - Monitor security advisories

5. **Access Control**
   - Limit database access by IP
   - Use firewall rules
   - Monitor login attempts

---

## Troubleshooting

### Common Issues

**Issue: Connection Refused**
```sql
-- Check MySQL status
systemctl status mysql  # Linux
net start MySQL80  # Windows

-- Verify connection parameters
mysql -u root -p -h localhost -P 3306
```

**Issue: Table Doesn't Exist**
```sql
-- Run schema initialization
SOURCE /path/to/database_schema.sql;

-- Verify tables
SHOW TABLES;
DESCRIBE users;
```

**Issue: Foreign Key Constraint Fails**
```sql
-- Check foreign key constraints
SELECT * FROM information_schema.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = 'movieticket_db';

-- Temporarily disable checks (use carefully)
SET FOREIGN_KEY_CHECKS = 0;
-- ... perform operation ...
SET FOREIGN_KEY_CHECKS = 1;
```

**Issue: Slow Queries**
```sql
-- Enable slow query log
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;  -- 2 seconds

-- Analyze slow queries
EXPLAIN SELECT ...;
```

---

## Conclusion

This database documentation provides comprehensive information about the CineZone database structure, relationships, and operations. For application-level database interactions, refer to the Developer Guide and source code documentation.

**Database Version**: 1.0  
**Last Updated**: October 23, 2025  
**Maintained By**: CineZone Development Team

