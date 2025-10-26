# CineZone - Movie Ticket Booking System  
  
[![Java](https://img.shields.io/badge/Java-23.0.1-orange.svg)](https://www.oracle.com/java/)  
[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.6-blue.svg)](https://openjfx.io/)  
[![MySQL](https://img.shields.io/badge/MySQL-8.2.0-blue.svg)](https://www.mysql.com/)  
[![Maven](https://img.shields.io/badge/Maven-3.6+-red.svg)](https://maven.apache.org/)  
  
A comprehensive, feature-rich movie ticket booking application built with JavaFX and MySQL. CineZone provides a complete solution for movie theaters to manage their operations and for users to book tickets with an intuitive, modern interface.  
  
## 📋 Table of Contents  
  
- [Features](#features)  
- [Technology Stack](#technology-stack)  
- [System Requirements](#system-requirements)  
- [Installation](#installation)  
- [Usage](#usage)  
- [Project Structure](#project-structure)  
- [Database Schema](#database-schema)  
- [Architecture](#architecture)  
- [Contributing](#contributing)  
- [License](#license)  
  
## ✨ Features  
  
### User Features  
- 🎬 **Movie Browsing**: Browse movies with posters, ratings, and detailed information  
- 🎫 **Seat Selection**: Interactive seat map with real-time availability  
- 💳 **Payment Processing**: Multiple payment methods (Credit/Debit Card, UPI, Net Banking)  
- ⭐ **Reviews & Ratings**: Write reviews and rate movies (5-star system)  
- 💬 **Real-time Chat**: Direct messaging with other users and admin support  
- 👤 **Profile Management**: Edit profile, upload profile pictures, change passwords  
- 📜 **Booking History**: View past and upcoming bookings  
  
### Admin Features  
- 📊 **Dashboard**: Comprehensive admin dashboard with statistics  
- 🎥 **Movie Management**: Add, edit, delete movies with poster and trailer URLs  
- 📅 **Screening Management**: Create and manage show times across multiple screens  
- 👥 **User Management**: Manage user accounts and roles  
- 📖 **Booking Monitoring**: View and track all bookings with status management  
- 🗑️ **Content Moderation**: Review and remove inappropriate user content  
  
### Technical Features  
- 🔐 **Secure Authentication**: Role-based access control (ADMIN/USER)  
- 🔄 **Real-time Updates**: Socket-based notification system  
- 🔒 **Seat Locking**: Temporary seat locks (5 minutes) to prevent double booking  
- 🗄️ **Connection Pooling**: Efficient database connection management  
- 🎨 **Modern UI**: Custom CSS styling with responsive design  
- 📱 **Multi-client Support**: Separate server for handling multiple concurrent clients  
  
## 🛠️ Technology Stack  
  
### Frontend  
- **JavaFX 21.0.6**: UI framework for rich client applications  
- **FXML**: XML-based UI markup  
- **CSS**: Custom styling for enhanced UX  
  
### Backend  
- **Java 23.0.1**: Core programming language  
- **MySQL 8.2.0**: Relational database  
- **Jackson 2.14.2**: JSON serialization/deserialization  
- **Java Socket Programming**: Real-time communication  
  
### Build & Dependency Management  
- **Maven**: Project build and dependency management  
  
### Additional Libraries  
- **ControlsFX 11.2.1**: Additional JavaFX controls  
- **FormsFX 11.6.0**: Form validation  
- **ValidatorFX 0.6.1**: Input validation  
- **Ikonli 12.3.1**: Icon library for JavaFX  
  
## 💻 System Requirements  
  
- **Java Development Kit (JDK)**: Version 21 or higher  
- **MySQL Server**: Version 8.0 or higher  
- **Maven**: Version 3.6 or higher  
- **Operating System**: Windows, macOS, or Linux  
- **Memory**: Minimum 4GB RAM (8GB recommended)  
- **Disk Space**: At least 500MB free space  
  
## 📥 Installation  
  
### 1. Clone the Repository  
  
```bash  
git clone https://github.com/yourusername/movieticket.gitcd movieticket```  
  
### 2. Set Up MySQL Database  
  
1. Start MySQL server  
2. Create the database:  
  
```sql  
CREATE DATABASE movieticket_db;  
```  
  
3. Update database credentials in `DatabaseConnection.java`:  
  
```java  
private static final String URL = "jdbc:mysql://localhost:3306/movieticket_db";  
private static final String USERNAME = "your_username";  
private static final String PASSWORD = "your_password";  
```  
  
4. The application will automatically create tables on first run using `database_schema.sql`  
  
### 3. Build the Project  
  
```bash  
mvnw clean install```  
  
Or on Windows:  
  
```cmd  
mvnw.cmd clean install  
```  
  
### 4. Run the Socket Server  
  
First, start the socket server for real-time features:  
  
```bash  
mvnw exec:java -Dexec.mainClass="com.example.movieticket.MovieTicketServer"```  
  
Or on Windows:  
  
```cmd  
mvnw.cmd exec:java -Dexec.mainClass="com.example.movieticket.MovieTicketServer"  
```  
  
### 5. Run the Application  
  
In a new terminal, start the main application:  
  
```bash  
mvnw javafx:run```  
  
Or on Windows:  
  
```cmd  
mvnw.cmd javafx:run  
```  
  
## 🚀 Usage  
  
### Default Admin Credentials  
  
```  
Username: admin  
Password: admin123  
Email: admin@movieticket.com  
```  
  
### User Workflow  
  
1. **Registration**: Create a new account or login with existing credentials  
2. **Browse Movies**: View available movies in the movie catalog  
3. **Select Screening**: Choose a convenient show time  
4. **Select Seats**: Pick your preferred seats from the interactive seat map  
5. **Payment**: Complete payment using your preferred method  
6. **Confirmation**: Receive booking confirmation  
7. **Reviews**: Share your experience and rate the movie  
  
### Admin Workflow  
  
1. **Login**: Use admin credentials  
2. **Manage Movies**: Add new movies with details, posters, and trailers  
3. **Create Screenings**: Schedule show times for movies  
4. **Monitor Bookings**: Track all bookings and their status  
5. **User Management**: Create, edit, or remove user accounts  
6. **Content Moderation**: Review and manage user reviews  
  
## 📁 Project Structure  
  
```  
movieticket/  
├── src/  
│   ├── main/  
│   │   ├── java/  
│   │   │   ├── module-info.java  
│   │   │   └── com/example/movieticket/  
│   │   │       ├── MovieTicketApp.java          # Main application entry  
│   │   │       ├── MovieTicketServer.java       # Socket server  
│   │   │       ├── controller/                  # UI Controllers  
│   │   │       │   ├── AdminDashboardController.java  
│   │   │       │   ├── UserDashboardController.java  
│   │   │       │   ├── LoginController.java  
│   │   │       │   ├── SeatSelectionController.java  
│   │   │       │   ├── PaymentDialogController.java  
│   │   │       │   ├── ChatController.java  
│   │   │       │   └── ... (other controllers)  
│   │   │       ├── model/                       # Data Models  
│   │   │       │   ├── User.java  
│   │   │       │   ├── Movie.java  
│   │   │       │   ├── Screening.java  
│   │   │       │   ├── Booking.java  
│   │   │       │   ├── Seat.java  
│   │   │       │   ├── Review.java  
│   │   │       │   ├── Payment.java  
│   │   │       │   ├── ChatMessage.java  
│   │   │       │   └── SeatLock.java  
│   │   │       ├── service/                     # Business Logic  
│   │   │       │   ├── DatabaseConnection.java  
│   │   │       │   ├── DataService.java  
│   │   │       │   └── RealTimeNotificationService.java  
│   │   │       └── network/                     # Network Layer  
│   │   │           ├── SocketServer.java  
│   │   │           ├── SocketClient.java  
│   │   │           ├── ClientHandler.java  
│   │   │           └── NetworkMessage.java  
│   │   └── resources/  
│   │       ├── database_schema.sql              # Database schema  
│   │       ├── icon.png                         # App icon  
│   │       ├── placeholder-poster.png           # Default poster  
│   │       └── com/example/movieticket/         # FXML & CSS files  
│   │           ├── login-view.fxml  
│   │           ├── signup-view.fxml  
│   │           ├── admin-dashboard.fxml  
│   │           ├── user-dashboard.fxml  
│   │           ├── seat-selection.fxml  
│   │           ├── payment-dialog.fxml  
│   │           ├── chat-window.fxml  
│   │           └── ... (other FXML files)  
├── posters/                                     # Movie poster storage  
├── profile_pictures/                            # User profile pictures  
├── pom.xml                                      # Maven configuration  
 report  
└── README.md                                    # This file  
```  
  
## 🗄️ Database Schema  
  
The application uses 10 main database tables:  
  
- **users**: User accounts and authentication  
- **movies**: Movie catalog with details  
- **screenings**: Show times and screening information  
- **seats**: Seat inventory for each screening  
- **bookings**: Booking records  
- **payments**: Payment transactions  
- **reviews**: User reviews and ratings  
- **messages**: Chat message history  
- **seat_locks**: Temporary seat reservations  
  
For detailed schema, see [database_schema.sql](src/main/resources/database_schema.sql)  
  
## 🏗️ Architecture  
  
### Layered Architecture  
  
```  
┌─────────────────────────────────────┐  
│     Presentation Layer (FXML/CSS)   │  
├─────────────────────────────────────┤  
│     Controller Layer (JavaFX)       │  
├─────────────────────────────────────┤  
│     Service Layer (Business Logic)  │  
├─────────────────────────────────────┤  
│     Data Access Layer (JDBC)        │  
├─────────────────────────────────────┤  
│     Database (MySQL)                │  
└─────────────────────────────────────┘  
```  
  
### Network Architecture  
  
```  
┌──────────────┐         ┌──────────────┐  
│   Client 1   │◄───────►│              │  
├──────────────┤         │    Socket    │  
│   Client 2   │◄───────►│    Server    │  
├──────────────┤         │   (Port 8888)│  
│   Client N   │◄───────►│              │  
└──────────────┘         └──────────────┘  
```  
  
### Key Design Patterns  
  
- **MVC (Model-View-Controller)**: Separation of concerns  
- **Singleton**: Database connection pool, notification service  
- **Observer**: Real-time updates via socket communication  
- **Factory**: Object creation for models  
- **DAO (Data Access Object)**: Database operations abstraction  
  
## 🔑 Key Features Explained  
  
### Real-time Seat Locking  
  
The system implements a sophisticated seat locking mechanism:  
  
1. When a user selects seats, they are temporarily locked for 5 minutes  
2. Other users see these seats as "locked" and cannot select them  
3. If payment is not completed within 5 minutes, locks automatically expire  
4. Database triggers ensure data consistency  
  
### Socket-based Communication  
  
- Separate server (`MovieTicketServer`) handles all real-time operations  
- Uses JSON serialization for message passing  
- Supports multiple concurrent client connections  
- Enables real-time chat and notifications  
  
### Connection Pooling  
  
- Efficient database connection management  
- Maximum 10 concurrent connections  
- Automatic connection recycling  
- Improved performance under load  
  
## 📚 Additional Documentation  
  
- [API Documentation](docs/API.md) - Detailed API reference  
- [User Guide](docs/USER_GUIDE.md) - Complete user manual  
- [Developer Guide](docs/DEVELOPER_GUIDE.md) - Development guidelines  
- [Database Documentation](docs/DATABASE.md) - Database design details  
- [Deployment Guide](docs/DEPLOYMENT.md) - Deployment instructions  
  
## 🤝 Contributing  
  
Contributions are welcome! Please follow these steps:  
  
1. Fork the repository  
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)  
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)  
4. Push to the branch (`git push origin feature/AmazingFeature`)  
5. Open a Pull Request  
  
## 📝 License  
  
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.  
  
## 👥 Authors  
  
- [M. R. Haque Jayeed](https://github.com/mrhjayeed)
  
## 🙏 Acknowledgments  
  
- JavaFX community for excellent documentation  
- OpenJFX for the UI framework  
- MySQL team for the robust database system  
- All contributors who help improve this project  
  
## 📞 Support  
  
For support, email mrhjayeed@gmail.com or create an issue in the repository.  
  
## 🔮 Future Enhancements  
  
- [ ] Mobile application (iOS/Android)  
- [ ] Email notifications for bookings  
- [ ] QR code generation for tickets  
- [ ] Integration with payment gateways  
- [ ] Movie recommendation system  
- [ ] Social media integration  
- [ ] Multi-language support  
- [ ] Analytics dashboard  
- [ ] Loyalty program  
- [ ] Food & beverage ordering  
  
---  
  
**Made with ❤️ by the CineZone Team**
