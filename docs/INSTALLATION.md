# Installation Guide - CineZone Movie Ticket Booking System

## Table of Contents

1. [Quick Start](#quick-start)
2. [Detailed Installation](#detailed-installation)
3. [Platform-Specific Instructions](#platform-specific-instructions)
4. [Database Setup](#database-setup)
5. [Running the Application](#running-the-application)
6. [Troubleshooting Installation](#troubleshooting-installation)
7. [Uninstallation](#uninstallation)

---

## Quick Start

For experienced developers who want to get started quickly:

```bash
# Clone repository
git clone https://github.com/yourusername/movieticket.git
cd movieticket

# Setup MySQL database
mysql -u root -p
CREATE DATABASE movieticket_db;
USE movieticket_db;
SOURCE src/main/resources/database_schema.sql;
EXIT;

# Update database credentials in DatabaseConnection.java
# Edit: src/main/java/com/example/movieticket/service/DatabaseConnection.java

# Build and run server
mvnw exec:java -Dexec.mainClass="com.example.movieticket.MovieTicketServer"

# In new terminal, run application
mvnw javafx:run
```

**Default Login:**
- Username: `admin`
- Password: `admin123`

---

## Detailed Installation

### Prerequisites

Before installing CineZone, ensure you have the following installed:

#### 1. Java Development Kit (JDK) 21+

**Check if Java is installed:**
```bash
java -version
```

Should show:
```
java version "21.0.x" or higher
```

**Install Java:**

- **Windows:** Download from [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://adoptium.net/)
- **macOS:** 
  ```bash
  brew install openjdk@21
  ```
- **Linux (Ubuntu/Debian):**
  ```bash
  sudo apt update
  sudo apt install openjdk-21-jdk
  ```

**Set JAVA_HOME:**

- **Windows:**
  ```cmd
  setx JAVA_HOME "C:\Program Files\Java\jdk-21"
  setx PATH "%PATH%;%JAVA_HOME%\bin"
  ```

- **macOS/Linux:**
  ```bash
  export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
  export PATH=$JAVA_HOME/bin:$PATH
  ```

#### 2. Apache Maven 3.6+

**Check if Maven is installed:**
```bash
mvn -version
```

**Install Maven:**

- **Windows:** Download from [Maven Downloads](https://maven.apache.org/download.cgi)
- **macOS:**
  ```bash
  brew install maven
  ```
- **Linux:**
  ```bash
  sudo apt install maven
  ```

#### 3. MySQL Server 8.0+

**Check if MySQL is installed:**
```bash
mysql --version
```

**Install MySQL:**

- **Windows:** Download [MySQL Installer](https://dev.mysql.com/downloads/installer/)
- **macOS:**
  ```bash
  brew install mysql
  brew services start mysql
  ```
- **Linux:**
  ```bash
  sudo apt update
  sudo apt install mysql-server
  sudo systemctl start mysql
  sudo systemctl enable mysql
  ```

**Secure MySQL Installation:**
```bash
sudo mysql_secure_installation
```

Follow prompts to:
- Set root password
- Remove anonymous users
- Disallow root login remotely
- Remove test database

#### 4. Git (Optional)

**Install Git:**

- **Windows:** Download from [git-scm.com](https://git-scm.com/)
- **macOS:**
  ```bash
  brew install git
  ```
- **Linux:**
  ```bash
  sudo apt install git
  ```

---

## Platform-Specific Instructions

### Windows Installation

#### Step 1: Install Prerequisites

1. **Install JDK 21:**
   - Download installer from Oracle or Adoptium
   - Run installer, accept defaults
   - Verify installation: `java -version`

2. **Install Maven:**
   - Download binary zip from maven.apache.org
   - Extract to `C:\Program Files\Apache\Maven`
   - Add to PATH: `C:\Program Files\Apache\Maven\bin`
   - Verify: `mvn -version`

3. **Install MySQL:**
   - Download MySQL Installer
   - Choose "Server Only" installation
   - Set root password (remember this!)
   - Start MySQL service

#### Step 2: Clone/Download Project

**Using Git:**
```cmd
cd %USERPROFILE%\Desktop
git clone https://github.com/yourusername/movieticket.git
cd movieticket
```

**Or download ZIP:**
1. Download from GitHub
2. Extract to `C:\Users\YourName\Desktop\movieticket`
3. Open Command Prompt in that folder

#### Step 3: Configure Database

```cmd
mysql -u root -p
```

Enter your root password, then:
```sql
CREATE DATABASE movieticket_db;
EXIT;
```

Import schema:
```cmd
cd src\main\resources
mysql -u root -p movieticket_db < database_schema.sql
cd ..\..\..
```

#### Step 4: Update Configuration

Edit `src\main\java\com\example\movieticket\service\DatabaseConnection.java`:

Find these lines:
```java
private static final String URL = "jdbc:mysql://localhost:3306/movieticket_db";
private static final String USERNAME = "root";
private static final String PASSWORD = "jdb@mySQL";
```

Change `PASSWORD` to your MySQL root password.

#### Step 5: Build Project

```cmd
mvnw.cmd clean install
```

This will:
- Download all dependencies
- Compile the project
- Run tests
- Create JAR files

#### Step 6: Run Application

Open **two** Command Prompt windows:

**Terminal 1 (Server):**
```cmd
mvnw.cmd exec:java -Dexec.mainClass="com.example.movieticket.MovieTicketServer"
```

**Terminal 2 (Application):**
```cmd
mvnw.cmd javafx:run
```

---

### macOS Installation

#### Step 1: Install Prerequisites

```bash
# Install Homebrew if not already installed
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install JDK
brew install openjdk@21

# Add to PATH
echo 'export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# Install Maven
brew install maven

# Install MySQL
brew install mysql
brew services start mysql

# Secure MySQL
mysql_secure_installation
```

#### Step 2: Clone Project

```bash
cd ~/Desktop
git clone https://github.com/yourusername/movieticket.git
cd movieticket
```

#### Step 3: Setup Database

```bash
mysql -u root -p
```

```sql
CREATE DATABASE movieticket_db;
EXIT;
```

```bash
mysql -u root -p movieticket_db < src/main/resources/database_schema.sql
```

#### Step 4: Configure Application

Edit database credentials:
```bash
nano src/main/java/com/example/movieticket/service/DatabaseConnection.java
```

Update the `PASSWORD` constant.

#### Step 5: Build and Run

```bash
# Build
./mvnw clean install

# Run server (in Terminal 1)
./mvnw exec:java -Dexec.mainClass="com.example.movieticket.MovieTicketServer"

# Run application (in Terminal 2)
./mvnw javafx:run
```

---

### Linux Installation (Ubuntu/Debian)

#### Step 1: Install Prerequisites

```bash
# Update package list
sudo apt update

# Install JDK 21
sudo apt install openjdk-21-jdk

# Install Maven
sudo apt install maven

# Install MySQL
sudo apt install mysql-server

# Start MySQL
sudo systemctl start mysql
sudo systemctl enable mysql

# Secure MySQL
sudo mysql_secure_installation

# Install Git
sudo apt install git
```

#### Step 2: Clone Project

```bash
cd ~/Desktop
git clone https://github.com/yourusername/movieticket.git
cd movieticket
```

#### Step 3: Setup Database

```bash
sudo mysql -u root -p
```

```sql
CREATE DATABASE movieticket_db;
EXIT;
```

```bash
sudo mysql -u root -p movieticket_db < src/main/resources/database_schema.sql
```

#### Step 4: Configure Application

```bash
nano src/main/java/com/example/movieticket/service/DatabaseConnection.java
```

Update database credentials.

#### Step 5: Build and Run

```bash
# Make Maven wrapper executable
chmod +x mvnw

# Build
./mvnw clean install

# Run server (Terminal 1)
./mvnw exec:java -Dexec.mainClass="com.example.movieticket.MovieTicketServer"

# Run application (Terminal 2)
./mvnw javafx:run
```

---

## Database Setup

### Creating the Database

#### Method 1: Command Line

```bash
mysql -u root -p
```

```sql
-- Create database
CREATE DATABASE movieticket_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create application user
CREATE USER 'cinezone'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON movieticket_db.* TO 'cinezone'@'localhost';
FLUSH PRIVILEGES;

-- Use the database
USE movieticket_db;

-- Import schema
SOURCE /path/to/movieticket/src/main/resources/database_schema.sql;

-- Verify tables
SHOW TABLES;
EXIT;
```

#### Method 2: MySQL Workbench (GUI)

1. Open MySQL Workbench
2. Connect to MySQL server
3. Click "Create new schema" button
4. Name: `movieticket_db`, Charset: `utf8mb4`
5. Click "Apply"
6. Open `database_schema.sql` file
7. Click "Execute" (lightning bolt icon)
8. Verify all tables created

### Sample Data (Optional)

To populate with sample data for testing:

```sql
USE movieticket_db;

-- Add sample movies
INSERT INTO movies (title, director, release_year, description, duration, genre, rating, poster_url, trailer_url) VALUES
('Inception', 'Christopher Nolan', 2010, 'A thief who steals corporate secrets through dream-sharing technology...', 148, 'Sci-Fi', 4.8, '/posters/inception.jpg', 'https://www.youtube.com/watch?v=YoHD9XEInc0'),
('The Dark Knight', 'Christopher Nolan', 2008, 'When the menace known as the Joker wreaks havoc...', 152, 'Action', 4.9, '/posters/dark-knight.jpg', 'https://www.youtube.com/watch?v=EXeTwQWrcwY'),
('Interstellar', 'Christopher Nolan', 2014, 'A team of explorers travel through a wormhole in space...', 169, 'Sci-Fi', 4.7, '/posters/interstellar.jpg', 'https://www.youtube.com/watch?v=zSWdZVtXT7E');

-- Add screenings
INSERT INTO screenings (movie_id, screen_name, show_time, ticket_price, total_seats, available_seats) VALUES
(1, 'Screen 1', '2024-10-25 18:30:00', 12.50, 100, 100),
(1, 'Screen 2', '2024-10-25 21:00:00', 12.50, 100, 100),
(2, 'Screen 1', '2024-10-26 19:00:00', 15.00, 100, 100);
```

---

## Running the Application

### Starting the Socket Server

The socket server must be running before starting the main application.

**Option 1: Command Line**

Windows:
```cmd
mvnw.cmd exec:java -Dexec.mainClass="com.example.movieticket.MovieTicketServer"
```

macOS/Linux:
```bash
./mvnw exec:java -Dexec.mainClass="com.example.movieticket.MovieTicketServer"
```

**Option 2: IDE (IntelliJ IDEA)**

1. Open project in IntelliJ
2. Find `MovieTicketServer.java`
3. Right-click → Run 'MovieTicketServer.main()'

**Option 3: JAR File**

```bash
java -cp target/movieticket-1.0-SNAPSHOT.jar com.example.movieticket.MovieTicketServer
```

**Expected Output:**
```
Starting Movie Ticket Socket Server...
Server started on port 8888
Waiting for client connections...
```

### Starting the Main Application

**Option 1: Maven**

Windows:
```cmd
mvnw.cmd javafx:run
```

macOS/Linux:
```bash
./mvnw javafx:run
```

**Option 2: IDE**

1. Find `MovieTicketApp.java`
2. Right-click → Run 'MovieTicketApp.main()'

**Option 3: JAR File**

```bash
java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -jar target/movieticket-1.0-SNAPSHOT.jar
```

**Expected Output:**
```
Successfully connected to socket server
Application started
```

### First Login

When the application opens:

1. **Admin Login:**
   - Username: `admin`
   - Password: `admin123`

2. **Create New User:**
   - Click "Sign Up"
   - Fill in registration form
   - Submit

**Important:** Change the admin password after first login!

---

## Troubleshooting Installation

### Common Issues and Solutions

#### Issue 1: "java: command not found"

**Solution:**
- Verify Java is installed: Check installation
- Add Java to PATH
- Restart terminal/command prompt

#### Issue 2: "mvn: command not found"

**Solution:**
- Install Maven
- Add Maven to PATH
- Verify with `mvn -version`

#### Issue 3: MySQL Connection Error

**Error:** `SQLException: Access denied for user 'root'@'localhost'`

**Solution:**
```bash
# Reset MySQL password
sudo mysql
```

```sql
ALTER USER 'root'@'localhost' IDENTIFIED BY 'new_password';
FLUSH PRIVILEGES;
EXIT;
```

Update password in `DatabaseConnection.java`.

#### Issue 4: Port 8888 Already in Use

**Error:** `BindException: Address already in use`

**Solution:**

Find and kill process using port 8888:

**Windows:**
```cmd
netstat -ano | findstr :8888
taskkill /PID <PID> /F
```

**macOS/Linux:**
```bash
lsof -i :8888
kill -9 <PID>
```

#### Issue 5: JavaFX Runtime Components Missing

**Error:** `Error: JavaFX runtime components are missing`

**Solution:**
- Ensure JavaFX is included in dependencies (check `pom.xml`)
- Run with Maven: `mvnw javafx:run`
- Or specify module path manually

#### Issue 6: Build Failures

**Error:** `Build failure: Cannot resolve dependencies`

**Solution:**
```bash
# Clear Maven cache
mvn dependency:purge-local-repository

# Rebuild
mvn clean install -U
```

#### Issue 7: Database Schema Not Created

**Solution:**
```bash
# Manually run schema
mysql -u root -p movieticket_db < src/main/resources/database_schema.sql

# Verify
mysql -u root -p -e "USE movieticket_db; SHOW TABLES;"
```

#### Issue 8: Application Window Doesn't Appear

**Solution:**
- Check console for errors
- Verify FXML files exist
- Check JavaFX version compatibility
- Try running with `--add-opens` flags:
  ```bash
  java --add-opens javafx.graphics/javafx.scene=ALL-UNNAMED -jar app.jar
  ```

---

## Uninstallation

### Removing the Application

#### Windows

**If installed via installer:**
1. Open Control Panel → Programs and Features
2. Find "CineZone"
3. Click "Uninstall"

**If running from source:**
```cmd
cd C:\Users\YourName\Desktop\movieticket
rmdir /s /q target
cd ..
rmdir /s /q movieticket
```

#### macOS/Linux

```bash
cd ~/Desktop
rm -rf movieticket
```

### Removing the Database

```sql
-- Backup first (optional)
mysqldump -u root -p movieticket_db > movieticket_backup.sql

-- Drop database
mysql -u root -p -e "DROP DATABASE movieticket_db;"

-- Remove user
mysql -u root -p -e "DROP USER 'cinezone'@'localhost';"
```

### Removing Dependencies

**Only if no other projects need them:**

```bash
# Remove MySQL
# Windows: Use MySQL Installer
# macOS: brew uninstall mysql
# Linux: sudo apt remove mysql-server

# Remove Maven
# Windows: Delete from Program Files
# macOS: brew uninstall maven
# Linux: sudo apt remove maven

# Remove JDK (not recommended)
# Only if not needed for other projects
```

---

## Next Steps

After successful installation:

1. **Read the User Guide:** Learn how to use all features
2. **Explore Admin Dashboard:** Manage movies, screenings, users
3. **Test Booking Flow:** Make a test booking
4. **Try Chat Feature:** Test real-time communication
5. **Review Documentation:** Check API docs for development

---

## Getting Help

If you encounter issues not covered here:

1. **Check Documentation:**
   - [User Guide](USER_GUIDE.md)
   - [Developer Guide](DEVELOPER_GUIDE.md)
   - [Troubleshooting](DEPLOYMENT.md#troubleshooting)

2. **Search Issues:** Check GitHub Issues for similar problems

3. **Ask for Help:**
   - Email: mrhjayeed@gmail.com
   - GitHub: Create an issue
   - Forum: community.cinezone.com

4. **Provide Information:**
   - Operating System and version
   - Java version
   - MySQL version
   - Error messages (full stack trace)
   - Steps to reproduce

---

## Conclusion

You should now have CineZone installed and running! Enjoy using the application.

**Installation Guide Version**: 1.0  
**Last Updated**: October 23, 2025  
**Next Update**: As needed

Happy Movie Booking! 🎬🍿


