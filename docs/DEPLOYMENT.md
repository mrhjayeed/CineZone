# Deployment Guide - CineZone Movie Ticket Booking System

## Table of Contents

1. [Pre-Deployment Checklist](#pre-deployment-checklist)
2. [Development Environment](#development-environment)
3. [Production Environment Setup](#production-environment-setup)
4. [Database Deployment](#database-deployment)
5. [Application Deployment](#application-deployment)
6. [Server Deployment](#server-deployment)
7. [Configuration Management](#configuration-management)
8. [Monitoring and Maintenance](#monitoring-and-maintenance)
9. [Troubleshooting](#troubleshooting)
10. [Rollback Procedures](#rollback-procedures)

---

## Pre-Deployment Checklist

### Before Deployment

- [ ] All tests passing
- [ ] Code reviewed and approved
- [ ] Database schema finalized
- [ ] Configuration files prepared
- [ ] Backup strategy in place
- [ ] Rollback plan documented
- [ ] Security audit completed
- [ ] Performance testing done
- [ ] Documentation updated
- [ ] Team trained on new features

### System Requirements Verification

**Hardware Requirements:**
- CPU: Multi-core processor (2+ cores recommended)
- RAM: Minimum 8GB (16GB recommended for production)
- Storage: 10GB+ free space
- Network: Stable connection with adequate bandwidth

**Software Requirements:**
- Java Development Kit (JDK) 21+
- MySQL Server 8.0+
- Maven 3.6+
- Operating System: Windows 10/11, macOS 10.15+, or Linux

---

## Development Environment

### Local Development Setup

1. **Clone Repository**
   ```bash
   git clone https://github.com/yourusername/movieticket.git
   cd movieticket
   ```

2. **Configure Database**
   
   Edit `src/main/java/com/example/movieticket/service/DatabaseConnection.java`:
   ```java
   private static final String URL = "jdbc:mysql://localhost:3306/movieticket_db";
   private static final String USERNAME = "root";
   private static final String PASSWORD = "your_password";
   ```

3. **Build Project**
   ```bash
   mvn clean install
   ```

4. **Run Socket Server**
   ```bash
   mvn exec:java -Dexec.mainClass="com.example.movieticket.MovieTicketServer"
   ```

5. **Run Application**
   ```bash
   mvn javafx:run
   ```

### Development Workflow

```
┌─────────────┐
│   Develop   │
└──────┬──────┘
       │
┌──────▼──────┐
│    Test     │
└──────┬──────┘
       │
┌──────▼──────┐
│   Commit    │
└──────┬──────┘
       │
┌──────▼──────┐
│    Build    │
└──────┬──────┘
       │
┌──────▼──────┐
│   Deploy    │
└─────────────┘
```

---

## Production Environment Setup

### Server Infrastructure

**Recommended Architecture:**

```
                    ┌─────────────┐
                    │ Load Balancer│
                    └──────┬──────┘
                           │
              ┌────────────┴────────────┐
              │                         │
     ┌────────▼────────┐       ┌───────▼────────┐
     │  App Server 1   │       │  App Server 2   │
     │  (JavaFX App)   │       │  (JavaFX App)   │
     └────────┬────────┘       └───────┬─────────┘
              │                         │
              └────────────┬────────────┘
                           │
                  ┌────────▼────────┐
                  │  Socket Server  │
                  │   (Port 8888)   │
                  └────────┬────────┘
                           │
                  ┌────────▼────────┐
                  │  MySQL Server   │
                  │  (Port 3306)    │
                  └─────────────────┘
```

### Server Specifications

**Application Server:**
- OS: Windows Server 2019+ or Ubuntu 20.04+
- RAM: 8GB minimum
- CPU: 4 cores
- Storage: 50GB SSD

**Database Server:**
- OS: Windows Server 2019+ or Ubuntu 20.04+
- RAM: 16GB minimum
- CPU: 4+ cores
- Storage: 100GB+ SSD (RAID configuration recommended)

**Socket Server:**
- Can run on same server as application or dedicated
- RAM: 4GB minimum
- CPU: 2+ cores

---

## Database Deployment

### Step 1: Install MySQL Server

**On Windows:**
```cmd
# Download MySQL Installer from mysql.com
# Run installer and select "Server only" or "Full"
# Set root password during installation
# Start MySQL service
net start MySQL80
```

**On Linux (Ubuntu):**
```bash
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql
sudo systemctl enable mysql
sudo mysql_secure_installation
```

### Step 2: Create Database

```bash
mysql -u root -p
```

```sql
CREATE DATABASE movieticket_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create dedicated user for application
CREATE USER 'cinezone_app'@'localhost' IDENTIFIED BY 'strong_secure_password';
GRANT ALL PRIVILEGES ON movieticket_db.* TO 'cinezone_app'@'localhost';
FLUSH PRIVILEGES;

-- For remote access (if needed)
CREATE USER 'cinezone_app'@'%' IDENTIFIED BY 'strong_secure_password';
GRANT ALL PRIVILEGES ON movieticket_db.* TO 'cinezone_app'@'%';
FLUSH PRIVILEGES;
```

### Step 3: Import Schema

```bash
mysql -u cinezone_app -p movieticket_db < src/main/resources/database_schema.sql
```

### Step 4: Verify Installation

```sql
USE movieticket_db;
SHOW TABLES;
DESCRIBE users;
SELECT COUNT(*) FROM users;
```

### Step 5: Configure Database Backup

**Automated Backup Script (Linux):**

Create `/usr/local/bin/backup_cinezone.sh`:
```bash
#!/bin/bash
BACKUP_DIR="/var/backups/cinezone"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/movieticket_db_$DATE.sql.gz"

mkdir -p $BACKUP_DIR

mysqldump -u cinezone_app -p'password' movieticket_db | gzip > $BACKUP_FILE

# Keep only last 7 days of backups
find $BACKUP_DIR -name "*.sql.gz" -mtime +7 -delete

echo "Backup completed: $BACKUP_FILE"
```

Make executable and schedule:
```bash
chmod +x /usr/local/bin/backup_cinezone.sh

# Add to crontab for daily backup at 2 AM
crontab -e
# Add line:
0 2 * * * /usr/local/bin/backup_cinezone.sh
```

---

## Application Deployment

### Step 1: Build Production Package

```bash
# Clean and build
mvn clean package

# This creates a JAR file in target/ directory
```

### Step 2: Create Executable

**Using jpackage (Java 21+):**

```bash
jpackage --input target \
  --name CineZone \
  --main-jar movieticket-1.0-SNAPSHOT.jar \
  --main-class com.example.movieticket.MovieTicketApp \
  --type exe \
  --icon src/main/resources/icon.png \
  --app-version 1.0 \
  --vendor "CineZone Team" \
  --description "Movie Ticket Booking System"
```

**For Windows:**
```cmd
jpackage --input target ^
  --name CineZone ^
  --main-jar movieticket-1.0-SNAPSHOT.jar ^
  --main-class com.example.movieticket.MovieTicketApp ^
  --type msi ^
  --icon src\main\resources\icon.ico ^
  --win-dir-chooser ^
  --win-menu ^
  --win-shortcut
```

### Step 3: Configure Application

**Production Configuration File** (`config.properties`):

```properties
# Database Configuration
db.host=localhost
db.port=3306
db.name=movieticket_db
db.username=cinezone_app
db.password=encrypted_password
db.pool.size=20

# Socket Server Configuration
socket.host=localhost
socket.port=8888

# Application Settings
app.version=1.0
app.environment=production
app.max.upload.size=5242880

# Logging
log.level=INFO
log.file=/var/log/cinezone/app.log
```

### Step 4: Install Application

**On Windows:**
1. Run the MSI installer
2. Choose installation directory (e.g., `C:\Program Files\CineZone`)
3. Complete installation wizard
4. Application shortcut created on desktop and Start menu

**On Linux:**
```bash
# Copy application files
sudo mkdir -p /opt/cinezone
sudo cp -r target/* /opt/cinezone/

# Create launcher script
sudo nano /usr/local/bin/cinezone
```

Add content:
```bash
#!/bin/bash
cd /opt/cinezone
java -jar movieticket-1.0-SNAPSHOT.jar
```

```bash
sudo chmod +x /usr/local/bin/cinezone
```

### Step 5: Set Up Logging

Create log directory:
```bash
# Windows
mkdir C:\ProgramData\CineZone\logs

# Linux
sudo mkdir -p /var/log/cinezone
sudo chown appuser:appuser /var/log/cinezone
```

---

## Server Deployment

### Step 1: Deploy Socket Server

**Create Server Service (Windows):**

Create `cinezone-server.bat`:
```batch
@echo off
cd /d "C:\Program Files\CineZone"
java -jar movieticket-1.0-SNAPSHOT.jar com.example.movieticket.MovieTicketServer
```

**Using NSSM (Non-Sucking Service Manager):**
```cmd
nssm install CineZoneServer "C:\Program Files\CineZone\cinezone-server.bat"
nssm set CineZoneServer DisplayName "CineZone Socket Server"
nssm set CineZoneServer Description "Real-time communication server for CineZone"
nssm start CineZoneServer
```

**Create Server Service (Linux):**

Create `/etc/systemd/system/cinezone-server.service`:
```ini
[Unit]
Description=CineZone Socket Server
After=network.target mysql.service

[Service]
Type=simple
User=cinezone
WorkingDirectory=/opt/cinezone
ExecStart=/usr/bin/java -jar movieticket-1.0-SNAPSHOT.jar com.example.movieticket.MovieTicketServer
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
sudo systemctl daemon-reload
sudo systemctl enable cinezone-server
sudo systemctl start cinezone-server
sudo systemctl status cinezone-server
```

### Step 2: Configure Firewall

**Windows Firewall:**
```cmd
netsh advfirewall firewall add rule name="CineZone Server" dir=in action=allow protocol=TCP localport=8888
netsh advfirewall firewall add rule name="MySQL" dir=in action=allow protocol=TCP localport=3306
```

**Linux UFW:**
```bash
sudo ufw allow 8888/tcp
sudo ufw allow 3306/tcp
sudo ufw enable
```

### Step 3: Network Configuration

**Port Forwarding (if needed):**
- Forward port 8888 for socket server
- Forward port 3306 for MySQL (if remote access needed)
- Use secure VPN for production database access

---

## Configuration Management

### Environment-Specific Configuration

**Development:**
```java
private static final String URL = "jdbc:mysql://localhost:3306/movieticket_dev";
```

**Staging:**
```java
private static final String URL = "jdbc:mysql://staging-db:3306/movieticket_staging";
```

**Production:**
```java
private static final String URL = "jdbc:mysql://prod-db:3306/movieticket_db";
```

### Configuration Best Practices

1. **Never commit passwords to version control**
2. **Use environment variables for sensitive data**
3. **Keep separate configs for each environment**
4. **Document all configuration options**

### Environment Variables

```bash
# Set environment variables
export CINEZONE_DB_PASSWORD="secure_password"
export CINEZONE_SOCKET_PORT=8888
export CINEZONE_ENV=production
```

Access in Java:
```java
String password = System.getenv("CINEZONE_DB_PASSWORD");
String environment = System.getenv("CINEZONE_ENV");
```

---

## Monitoring and Maintenance

### Health Checks

**Database Health:**
```sql
-- Check connection
SELECT 1;

-- Check active connections
SHOW PROCESSLIST;

-- Check table status
SHOW TABLE STATUS;
```

**Application Health:**
```java
// Implement health check endpoint
public static boolean isHealthy() {
    try {
        Connection conn = DatabaseConnection.getConnection();
        boolean dbHealthy = conn.isValid(5);
        DatabaseConnection.releaseConnection(conn);
        
        boolean socketHealthy = RealTimeNotificationService.getInstance().isConnected();
        
        return dbHealthy && socketHealthy;
    } catch (Exception e) {
        return false;
    }
}
```

### Logging

**Configure Log4j2** (add to `pom.xml`):
```xml
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-core</artifactId>
    <version>2.20.0</version>
</dependency>
```

**log4j2.xml:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="INFO">
    <Appenders>
        <File name="FileAppender" fileName="logs/cinezone.log">
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss} [%t] %-5level %logger{36} - %msg%n"/>
        </File>
        <Console name="ConsoleAppender" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
        </Console>
    </Appenders>
    <Loggers>
        <Root level="info">
            <AppenderRef ref="FileAppender"/>
            <AppenderRef ref="ConsoleAppender"/>
        </Root>
    </Loggers>
</Configuration>
```

### Performance Monitoring

**Monitor Key Metrics:**
- Database query response times
- Connection pool usage
- Socket server connections
- Memory usage
- CPU usage

**MySQL Performance:**
```sql
-- Enable slow query log
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;

-- Monitor queries
SHOW FULL PROCESSLIST;

-- Check query cache
SHOW STATUS LIKE 'Qcache%';
```

### Maintenance Tasks

**Daily:**
- Check application logs for errors
- Monitor server resources
- Verify backups completed

**Weekly:**
- Review slow query log
- Check database growth
- Update statistics

**Monthly:**
- Optimize database tables
- Review and archive old data
- Update dependencies
- Security patches

---

## Troubleshooting

### Common Issues

**Issue: Application Won't Start**

Check:
```bash
# Verify Java version
java -version

# Check if port is in use
netstat -ano | findstr :8888

# Check database connection
mysql -u cinezone_app -p movieticket_db
```

**Issue: Socket Server Connection Failed**

Solutions:
1. Verify server is running:
   ```bash
   # Windows
   tasklist | findstr java
   
   # Linux
   ps aux | grep java
   ```

2. Check firewall rules
3. Verify port 8888 is not blocked

**Issue: Database Connection Errors**

Solutions:
1. Verify MySQL service is running
2. Check credentials in configuration
3. Test connection:
   ```bash
   mysql -u cinezone_app -p -h localhost movieticket_db
   ```

**Issue: High Memory Usage**

Solutions:
1. Increase JVM heap size:
   ```bash
   java -Xmx2G -jar movieticket-1.0-SNAPSHOT.jar
   ```

2. Monitor connection pool
3. Check for memory leaks

### Log Analysis

**Check Application Logs:**
```bash
# Windows
type C:\ProgramData\CineZone\logs\app.log

# Linux
tail -f /var/log/cinezone/app.log
```

**Common Error Patterns:**
```
ERROR: SQLException - Connection refused
ACTION: Check MySQL service status

ERROR: SocketException - Connection reset
ACTION: Verify socket server is running

ERROR: OutOfMemoryError
ACTION: Increase heap size
```

---

## Rollback Procedures

### Database Rollback

**Restore from Backup:**
```bash
# Stop application first
mysql -u cinezone_app -p movieticket_db < /backups/movieticket_db_20241023.sql
```

### Application Rollback

**Revert to Previous Version:**

1. Stop current application
2. Install previous version
3. Verify configuration
4. Start application

**Quick Rollback Script (Linux):**
```bash
#!/bin/bash
echo "Rolling back to previous version..."

# Stop current version
sudo systemctl stop cinezone-server

# Restore previous version
sudo cp /opt/cinezone/backup/movieticket-previous.jar /opt/cinezone/movieticket-1.0-SNAPSHOT.jar

# Start application
sudo systemctl start cinezone-server

echo "Rollback completed"
```

---

## Security Considerations

### Production Security

1. **Database Security:**
   - Use strong passwords
   - Limit remote access
   - Enable SSL connections
   - Regular security updates

2. **Application Security:**
   - Hash all passwords (BCrypt)
   - Validate all inputs
   - Implement rate limiting
   - Use HTTPS for web components

3. **Network Security:**
   - Use VPN for remote access
   - Implement firewall rules
   - Monitor access logs
   - Use secure protocols

### Security Checklist

- [ ] All default passwords changed
- [ ] Unnecessary ports closed
- [ ] SSL/TLS enabled
- [ ] Regular security audits
- [ ] Access logs reviewed
- [ ] Backup encryption enabled
- [ ] Intrusion detection configured

---

## Post-Deployment

### Verification Steps

1. **Functional Testing:**
   - User login works
   - Movie browsing functional
   - Booking process complete
   - Payment processing works
   - Chat feature operational

2. **Performance Testing:**
   - Response times acceptable
   - Concurrent users supported
   - Database queries optimized

3. **User Acceptance Testing:**
   - Get feedback from users
   - Monitor for issues
   - Document any problems

### Going Live Checklist

- [ ] All services running
- [ ] Database populated with data
- [ ] Backups configured and tested
- [ ] Monitoring active
- [ ] Support team briefed
- [ ] Documentation available
- [ ] Contact information updated
- [ ] Marketing team notified

---

## Support and Maintenance

### Support Contacts

- **Technical Support:** support@cinezone.com
- **Database Issues:** dba@cinezone.com
- **Network Issues:** netops@cinezone.com
- **Emergency:** emergency@cinezone.com

### Maintenance Windows

- **Scheduled Maintenance:** Sundays 2:00 AM - 4:00 AM
- **Emergency Maintenance:** As needed, with 1-hour notice

---

## Conclusion

This deployment guide provides comprehensive instructions for deploying the CineZone Movie Ticket Booking System. Follow each step carefully and verify at each stage.

**Deployment Version**: 1.0  
**Last Updated**: October 23, 2025  
**Next Review**: January 2026

For additional support, contact the development team.

