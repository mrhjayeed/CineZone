# Contributing to CineZone Movie Ticket Booking System

Thank you for your interest in contributing to CineZone! This document provides guidelines and instructions for contributing to the project.

## Table of Contents

1. [Code of Conduct](#code-of-conduct)
2. [Getting Started](#getting-started)
3. [How to Contribute](#how-to-contribute)
4. [Development Workflow](#development-workflow)
5. [Coding Standards](#coding-standards)
6. [Commit Guidelines](#commit-guidelines)
7. [Pull Request Process](#pull-request-process)
8. [Testing Guidelines](#testing-guidelines)
9. [Documentation](#documentation)
10. [Community](#community)

---

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inspiring community for all. Please be respectful and constructive in your interactions.

### Expected Behavior

- Use welcoming and inclusive language
- Be respectful of differing viewpoints
- Accept constructive criticism gracefully
- Focus on what's best for the community
- Show empathy towards other community members

### Unacceptable Behavior

- Harassment, discrimination, or offensive comments
- Trolling or insulting/derogatory comments
- Publishing others' private information
- Other conduct deemed inappropriate

---

## Getting Started

### Prerequisites

Before contributing, ensure you have:

- JDK 21+ installed
- Maven 3.6+ installed
- MySQL 8.0+ installed
- Git installed
- A GitHub account
- Basic knowledge of Java and JavaFX

### Fork and Clone

1. **Fork the repository** on GitHub
2. **Clone your fork:**
   ```bash
   git clone https://github.com/YOUR_USERNAME/movieticket.git
   cd movieticket
   ```

3. **Add upstream remote:**
   ```bash
   git remote add upstream https://github.com/ORIGINAL_OWNER/movieticket.git
   ```

4. **Verify remotes:**
   ```bash
   git remote -v
   ```

### Set Up Development Environment

1. **Install dependencies:**
   ```bash
   mvn clean install
   ```

2. **Set up database:**
   ```bash
   mysql -u root -p < src/main/resources/database_schema.sql
   ```

3. **Configure database connection:**
   Edit `src/main/java/com/example/movieticket/service/DatabaseConnection.java`

4. **Run tests:**
   ```bash
   mvn test
   ```

---

## How to Contribute

### Types of Contributions

We welcome various types of contributions:

#### 1. Bug Reports

Found a bug? Please report it!

**Before submitting:**
- Check if the bug has already been reported
- Verify it's reproducible
- Collect relevant information

**Submit via GitHub Issues with:**
- Clear, descriptive title
- Steps to reproduce
- Expected vs actual behavior
- Screenshots (if applicable)
- Environment details (OS, Java version, etc.)

**Example:**
```markdown
**Bug Description:** Application crashes when selecting seat A1

**Steps to Reproduce:**
1. Login as user
2. Select movie "Inception"
3. Choose screening at 6:30 PM
4. Click on seat A1
5. Application crashes

**Expected:** Seat should be selected
**Actual:** Application crashes with NullPointerException

**Environment:**
- OS: Windows 11
- Java: 21.0.1
- MySQL: 8.0.35
```

#### 2. Feature Requests

Have an idea for a new feature?

**Before submitting:**
- Check if it's already been suggested
- Consider if it fits the project scope
- Think about implementation

**Include:**
- Clear description of the feature
- Use cases and benefits
- Possible implementation approach
- Mock-ups or diagrams (if applicable)

#### 3. Code Contributions

Want to write code? Great!

**Areas to contribute:**
- Bug fixes
- New features
- Performance improvements
- Refactoring
- Test coverage
- Documentation

#### 4. Documentation

Help improve documentation:
- Fix typos or unclear explanations
- Add examples
- Translate to other languages
- Create tutorials or guides

#### 5. Testing

Help ensure quality:
- Write unit tests
- Perform manual testing
- Test on different platforms
- Report test results

---

## Development Workflow

### Branch Strategy

We use a simplified Git Flow:

```
main
  └── develop
       ├── feature/feature-name
       ├── bugfix/bug-description
       ├── hotfix/critical-fix
       └── docs/documentation-update
```

**Branch Types:**

- `main`: Production-ready code
- `develop`: Integration branch for features
- `feature/*`: New features
- `bugfix/*`: Bug fixes
- `hotfix/*`: Critical production fixes
- `docs/*`: Documentation updates

### Working on a Feature

1. **Sync with upstream:**
   ```bash
   git checkout develop
   git fetch upstream
   git merge upstream/develop
   ```

2. **Create feature branch:**
   ```bash
   git checkout -b feature/seat-color-customization
   ```

3. **Make changes:**
   - Write code
   - Add tests
   - Update documentation

4. **Commit changes:**
   ```bash
   git add .
   git commit -m "feat: add seat color customization option"
   ```

5. **Push to your fork:**
   ```bash
   git push origin feature/seat-color-customization
   ```

6. **Create Pull Request** on GitHub

### Keeping Your Fork Updated

```bash
# Fetch upstream changes
git fetch upstream

# Merge into your local develop
git checkout develop
git merge upstream/develop

# Push to your fork
git push origin develop
```

---

## Coding Standards

### Java Code Style

#### Naming Conventions

```java
// Classes: PascalCase
public class UserDashboardController { }

// Methods: camelCase
public void handleLoginButton() { }

// Variables: camelCase
private String userName;
private int userId;

// Constants: UPPER_SNAKE_CASE
private static final int MAX_RETRY_COUNT = 3;
private static final String DEFAULT_THEME = "dark";

// Packages: lowercase
package com.example.movieticket.controller;
```

#### Code Formatting

```java
// Indentation: 4 spaces (no tabs)
public class Example {
    private String name;
    
    public void method() {
        if (condition) {
            // code
        } else {
            // code
        }
    }
}

// Braces: K&R style
if (condition) {
    doSomething();
} else {
    doSomethingElse();
}

// Line length: max 120 characters
// Break long lines logically
String longString = "This is a very long string that should be broken " +
                   "into multiple lines for better readability";
```

#### JavaDoc Comments

```java
/**
 * Authenticates a user with provided credentials.
 * 
 * @param username the username to authenticate
 * @param password the password to verify
 * @return User object if successful, null otherwise
 * @throws SQLException if database error occurs
 * @since 1.0
 */
public static User authenticateUser(String username, String password) 
        throws SQLException {
    // implementation
}
```

#### Best Practices

```java
// 1. Use meaningful variable names
// Bad
String s = "John";
int x = 25;

// Good
String userName = "John";
int userAge = 25;

// 2. Keep methods short and focused
// Bad - method does too much
public void processEverything() {
    validateUser();
    connectDatabase();
    fetchData();
    processData();
    updateUI();
}

// Good - separate concerns
public void processUserData() {
    User user = validateUser();
    List<Data> data = fetchUserData(user);
    updateUIWithData(data);
}

// 3. Handle exceptions properly
// Bad
try {
    riskyOperation();
} catch (Exception e) {
    // ignore
}

// Good
try {
    riskyOperation();
} catch (SQLException e) {
    LOGGER.error("Database operation failed", e);
    showErrorDialog("Unable to complete operation");
}

// 4. Use Optional for nullable returns
// Bad
public User findUser(int id) {
    return userMap.get(id); // might be null
}

// Good
public Optional<User> findUser(int id) {
    return Optional.ofNullable(userMap.get(id));
}
```

### FXML Style

```xml
<?xml version="1.0" encoding="UTF-8"?>

<!-- Organized imports -->
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>
<?import javafx.geometry.Insets?>

<!-- Proper indentation (2 spaces for XML) -->
<VBox xmlns:fx="http://javafx.com/fxml"
      fx:controller="com.example.movieticket.controller.LoginController"
      spacing="10"
      alignment="CENTER">
  
  <padding>
    <Insets top="20" right="20" bottom="20" left="20"/>
  </padding>
  
  <!-- Descriptive fx:id -->
  <TextField fx:id="usernameTextField" promptText="Username"/>
  <PasswordField fx:id="passwordField" promptText="Password"/>
  
  <!-- Action handlers -->
  <Button text="Login" onAction="#handleLoginAction"/>
  
</VBox>
```

### SQL Style

```sql
-- Use uppercase for keywords
SELECT user_id, username, email
FROM users
WHERE role = 'USER'
  AND created_at > '2024-01-01'
ORDER BY username;

-- Indent for readability
INSERT INTO bookings (
    user_id,
    screening_id,
    seat_ids,
    total_amount,
    status
) VALUES (
    1,
    5,
    '1,2,3',
    37.50,
    'CONFIRMED'
);

-- Use meaningful table aliases
SELECT 
    u.username,
    m.title,
    s.show_time,
    b.total_amount
FROM bookings b
INNER JOIN users u ON b.user_id = u.user_id
INNER JOIN screenings s ON b.screening_id = s.screening_id
INNER JOIN movies m ON s.movie_id = m.movie_id
WHERE b.status = 'CONFIRMED';
```

---

## Commit Guidelines

### Commit Message Format

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Type:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Build process or auxiliary tool changes

**Examples:**

```bash
# Simple feature
git commit -m "feat: add seat color customization"

# Bug fix with scope
git commit -m "fix(booking): resolve double booking issue"

# Breaking change
git commit -m "feat!: change API response format

BREAKING CHANGE: API now returns JSON instead of XML"

# With body
git commit -m "refactor(database): improve connection pooling

- Increase max connections to 20
- Add connection timeout handling
- Improve error logging"
```

### Writing Good Commits

**Do:**
- Write clear, concise commit messages
- Use present tense ("add feature" not "added feature")
- Capitalize first letter
- Don't end with a period
- Explain what and why, not how

**Don't:**
- Write vague messages ("fixed stuff", "updates")
- Commit unrelated changes together
- Commit commented-out code
- Commit temporary files

---

## Pull Request Process

### Before Creating a PR

- [ ] Code follows style guidelines
- [ ] All tests pass
- [ ] Added tests for new features
- [ ] Updated documentation
- [ ] Commit messages follow conventions
- [ ] Branch is up to date with develop
- [ ] No merge conflicts

### Creating a Pull Request

1. **Push your branch:**
   ```bash
   git push origin feature/your-feature-name
   ```

2. **Open PR on GitHub:**
   - Go to repository on GitHub
   - Click "New Pull Request"
   - Select your branch
   - Fill in PR template

3. **PR Template:**
   ```markdown
   ## Description
   Brief description of changes
   
   ## Type of Change
   - [ ] Bug fix
   - [ ] New feature
   - [ ] Breaking change
   - [ ] Documentation update
   
   ## Testing
   - [ ] Unit tests added/updated
   - [ ] Manual testing performed
   - [ ] All tests pass
   
   ## Screenshots (if applicable)
   Add screenshots here
   
   ## Checklist
   - [ ] Code follows style guidelines
   - [ ] Self-review completed
   - [ ] Documentation updated
   - [ ] No new warnings
   ```

### PR Review Process

1. **Automated Checks:**
   - Build passes
   - Tests pass
   - Code style check passes

2. **Code Review:**
   - At least one approval required
   - Address all feedback
   - Make requested changes

3. **Merge:**
   - Squash and merge (preferred)
   - Delete branch after merge

### Responding to Feedback

- Be open to suggestions
- Explain your approach if needed
- Make requested changes promptly
- Thank reviewers for their time

---

## Testing Guidelines

### Writing Tests

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    
    @Test
    public void testUserAuthentication_ValidCredentials_ReturnsUser() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        
        // Act
        User user = DataService.authenticateUser(username, password);
        
        // Assert
        assertNotNull(user);
        assertEquals(username, user.getUsername());
    }
    
    @Test
    public void testUserAuthentication_InvalidCredentials_ReturnsNull() {
        // Arrange
        String username = "invalid";
        String password = "wrong";
        
        // Act
        User user = DataService.authenticateUser(username, password);
        
        // Assert
        assertNull(user);
    }
}
```

### Test Coverage

Aim for:
- Minimum 70% code coverage
- Test all public methods
- Test edge cases
- Test error handling

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest

# Run with coverage
mvn test jacoco:report
```

---

## Documentation

### Code Documentation

- Add JavaDoc for all public classes and methods
- Use inline comments for complex logic
- Keep comments up to date

### User Documentation

When adding features:
- Update USER_GUIDE.md
- Add screenshots if applicable
- Update README.md if needed

### Developer Documentation

- Update DEVELOPER_GUIDE.md for architectural changes
- Document new APIs in API.md
- Update DATABASE.md for schema changes

---

## Community

### Getting Help

- **GitHub Issues:** For bugs and feature requests
- **Discussions:** For questions and general discussion
- **Email:** support@cinezone.com

### Communication Channels

- **GitHub:** Primary communication platform
- **Email:** For sensitive issues
- **Discord/Slack:** (if available) For real-time chat

### Recognition

Contributors will be:
- Listed in CONTRIBUTORS.md
- Mentioned in release notes
- Given credit in commit history

---

## Release Process

### Version Numbers

We use [Semantic Versioning](https://semver.org/):

- MAJOR.MINOR.PATCH
- Example: 1.2.3

**Increment:**
- MAJOR: Breaking changes
- MINOR: New features (backward compatible)
- PATCH: Bug fixes

### Release Checklist

- [ ] All tests passing
- [ ] Documentation updated
- [ ] CHANGELOG.md updated
- [ ] Version number incremented
- [ ] Git tag created
- [ ] Release notes written

---

## License

By contributing, you agree that your contributions will be licensed under the same license as the project (MIT License).

---

## Questions?

If you have questions not covered here:

1. Check existing documentation
2. Search GitHub Issues
3. Create a new issue with "question" label
4. Email: dev@cinezone.com

---

## Thank You!

Thank you for contributing to CineZone! Your efforts help make this project better for everyone.

**Happy Coding! 🚀**

---

**Contributing Guide Version**: 1.0  
**Last Updated**: October 23, 2025

