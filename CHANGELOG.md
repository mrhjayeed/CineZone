# Changelog

All notable changes to the CineZone Movie Ticket Booking System will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2025-10-23

### Added

#### User Features
- User registration and authentication system
- Role-based access control (ADMIN/USER)
- Profile management with profile picture upload
- Password change and reset functionality
- Movie browsing with poster and rating display
- Interactive seat selection with real-time availability
- Multiple seat selection in single booking
- Seat locking mechanism (5-minute temporary locks)
- Payment processing with multiple payment methods (Credit Card, Debit Card, UPI, Net Banking)
- Booking confirmation and history
- Review and rating system (1-5 stars)
- Real-time chat system for user-to-user communication
- Community reviews page

#### Admin Features
- Comprehensive admin dashboard
- Movie management (CRUD operations)
- Screening management with show time scheduling
- User management system
- Booking monitoring and status tracking
- Content moderation for reviews
- Statistics and analytics dashboard

#### Technical Features
- JavaFX-based desktop application
- MySQL database integration
- Socket-based real-time communication
- Connection pooling for efficient database access
- Secure password hashing
- Multi-client support via separate socket server
- FXML-based UI architecture
- Custom CSS styling
- Image upload and management system
- Poster and profile picture storage

#### Database
- Complete schema with 9 tables
- Foreign key relationships and constraints
- Automatic timestamp tracking
- Seat lock expiration handling
- Transaction support for bookings

#### Documentation
- Comprehensive README with setup instructions
- User Guide with detailed feature explanations
- Developer Guide with architecture documentation
- API Documentation for all data service methods
- Database Documentation with schema details
- Deployment Guide for production setup
- Installation Guide for all platforms
- Contributing Guidelines for open source collaboration

### Technical Stack
- Java 21
- JavaFX 21.0.6
- MySQL 8.2.0
- Maven for build management
- Jackson 2.14.2 for JSON processing
- Java Socket Programming for real-time features

### Known Issues
- None reported in initial release

### Security
- Password hashing implemented
- SQL injection prevention using PreparedStatements
- Input validation on all forms
- Role-based access control

---

## [Unreleased]

### Planned Features
- Email notifications for bookings
- QR code generation for tickets
- Payment gateway integration
- Movie recommendation system
- Mobile application (iOS/Android)
- Social media login integration
- Multi-language support
- Food and beverage ordering
- Loyalty program
- Advanced analytics dashboard

---

## Version History

- **1.0.0** (2025-10-23) - Initial Release

---

For more information about changes, see the [commit history](https://github.com/yourusername/movieticket/commits/main).
