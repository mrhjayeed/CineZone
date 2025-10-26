# User Guide - CineZone Movie Ticket Booking System

## Table of Contents

1. [Getting Started](#getting-started)
2. [User Registration and Login](#user-registration-and-login)
3. [Browsing Movies](#browsing-movies)
4. [Booking Tickets](#booking-tickets)
5. [Payment Process](#payment-process)
6. [Managing Your Profile](#managing-your-profile)
7. [Writing Reviews](#writing-reviews)
8. [Using Chat Feature](#using-chat-feature)
9. [Viewing Booking History](#viewing-booking-history)
10. [Troubleshooting](#troubleshooting)

---

## Getting Started

### First Time Setup

1. **Launch the Application**: Double-click the CineZone application icon or run from command line
2. **Verify Server Connection**: Ensure the socket server is running for real-time features
3. **Create an Account**: Click "Sign Up" on the login screen

### System Requirements

- Stable internet connection for real-time features
- Modern web browser (for trailer viewing)
- Supported payment methods

---

## User Registration and Login

### Creating a New Account

1. Click **"Sign Up"** on the login screen
2. Fill in the registration form:
   - **Username**: Choose a unique username (3-50 characters)
   - **Email**: Enter a valid email address
   - **Full Name**: Your complete name
   - **Password**: Create a strong password (minimum 8 characters)
   - **Confirm Password**: Re-enter your password
3. Click **"Register"**
4. Upon successful registration, you'll be redirected to the login screen

### Logging In

1. Enter your **username** or **email**
2. Enter your **password**
3. Click **"Login"**
4. You'll be directed to your dashboard

### Forgot Password?

1. Click **"Forgot Password?"** on the login screen
2. Enter your registered **email address**
3. Follow the password reset instructions
4. Create a new password
5. Login with your new credentials

---

## Browsing Movies

### Movie Catalog

The main dashboard displays all currently available movies:

- **Movie Posters**: Visual representation of each movie
- **Movie Title**: Name of the movie
- **Genre**: Category (Action, Drama, Comedy, etc.)
- **Rating**: Average user rating (1-5 stars)
- **Duration**: Movie length in minutes

### Viewing Movie Details

1. Click on any **movie card** to view details
2. Movie details include:
   - Title, Director, Release Year
   - Genre and Duration
   - Plot Description
   - User Ratings
   - Available Screenings
   - Watch Trailer button

### Watching Trailers

1. Open movie details
2. Click **"Watch Trailer"** button
3. Trailer opens in your default web browser

---

## Booking Tickets

### Step 1: Select a Movie

1. Browse the movie catalog on your dashboard
2. Click on your preferred movie

### Step 2: Choose a Screening

1. View available show times in the movie details dialog
2. Each screening shows:
   - **Screen Name**: Theater room number
   - **Show Time**: Date and time
   - **Ticket Price**: Price per seat
   - **Available Seats**: Number of seats remaining
3. Click **"Book Seats"** for your preferred screening

### Step 3: Select Your Seats

The seat selection interface displays:

- **Interactive Seat Map**: 10 rows (A-J) × 10 columns (1-10)
- **Color Coding**:
  - 🟢 **Green**: Available seats
  - 🔴 **Red**: Already booked
  - 🔵 **Blue**: Your selected seats
  - 🟡 **Yellow**: Temporarily locked by other users

**To Select Seats:**

1. Click on **green (available)** seats
2. Selected seats turn **blue**
3. You can select multiple seats
4. Click a selected seat again to deselect
5. View total amount at the bottom
6. Click **"Proceed to Payment"**

**Important Notes:**

- Seats are temporarily locked for **5 minutes** once selected
- Complete payment within this time or seats will be released
- You cannot select locked or booked seats

---

## Payment Process

### Payment Methods

CineZone supports multiple payment options:

1. **Credit Card**
2. **Debit Card**
3. **UPI** (Unified Payments Interface)
4. **Net Banking**

### Making a Payment

1. After selecting seats, the payment dialog opens
2. Review booking details:
   - Movie name
   - Show time
   - Selected seats
   - Total amount
3. Choose your **payment method**
4. Enter payment details:
   - **For Cards**: Card number, CVV, expiry date, cardholder name
   - **For UPI**: UPI ID
   - **For Net Banking**: Bank selection and account details
5. Click **"Process Payment"**

### Payment Confirmation

Upon successful payment:

- Booking confirmation dialog appears
- Booking ID is displayed
- Seats are permanently reserved
- Confirmation email is sent (if configured)

### Failed Payment

If payment fails:

- Error message is displayed
- Seats remain locked for remaining time
- You can retry payment
- If time expires, seats are released

---

## Managing Your Profile

### Viewing Your Profile

1. Click **"Profile"** tab in the dashboard
2. View your current information:
   - Profile picture
   - Username
   - Email
   - Full name
   - Account creation date

### Editing Profile

1. Click **"Edit Profile"** button
2. Update your information:
   - Full name
   - Email address
   - Profile picture (upload new image)
3. Click **"Save Changes"**

### Uploading Profile Picture

1. Click **"Edit Profile"**
2. Click on the profile picture area or **"Choose File"**
3. Select an image from your computer
4. Supported formats: JPG, PNG, GIF
5. Image is automatically resized and cropped
6. Click **"Save Changes"**

### Changing Password

1. Go to Profile section
2. Click **"Change Password"**
3. Enter:
   - Current password
   - New password
   - Confirm new password
4. Click **"Update Password"**
5. You'll need to login again with new password

---

## Writing Reviews

### Creating a Review

1. Navigate to **"Community Reviews"** tab
2. Click **"Write Review"** button
3. Fill in the review form:
   - **Rating**: Select 1-5 stars
   - **Title**: Brief review headline
   - **Review Type**: Theater Experience, Movie Review, or Service Feedback
   - **Comment**: Detailed review (minimum 10 characters)
4. Click **"Submit Review"**

### Viewing Reviews

1. Click **"Community Reviews"** tab
2. Browse all user reviews
3. Each review shows:
   - Reviewer name and profile picture
   - Rating (stars)
   - Review title and content
   - Date posted

### Managing Your Reviews

1. Go to **"My Reviews"** section
2. View all your submitted reviews
3. Options:
   - **Edit**: Modify your review
   - **Delete**: Remove your review

---

## Using Chat Feature

### Starting a Chat

1. Click **"Chat"** tab in the dashboard
2. Click **"New Chat"** or **"Select User"**
3. Choose a user from the user list
4. Chat window opens

### Sending Messages

1. Type your message in the text field at the bottom
2. Press **Enter** or click **"Send"** button
3. Message appears in the chat history

### Real-time Communication

- Messages are delivered instantly (if server is connected)
- You can see when messages are read
- Chat history is automatically saved

### Contacting Support

1. Open chat feature
2. Select **"Admin"** or **"Support"** from user list
3. Send your query or concern
4. Admin will respond in real-time

---

## Viewing Booking History

### My Bookings

1. Click **"My Bookings"** tab
2. View all your bookings:
   - **Upcoming Bookings**: Future show times
   - **Past Bookings**: Completed shows
3. Each booking displays:
   - Movie name and poster
   - Show time and screen
   - Seat numbers
   - Booking status
   - Total amount paid

### Booking Status Types

- ✅ **CONFIRMED**: Booking successful, seats reserved
- ⏳ **PENDING**: Payment processing
- ❌ **CANCELLED**: Booking cancelled

### Cancelling a Booking

1. Go to **"My Bookings"**
2. Select the booking to cancel
3. Click **"Cancel Booking"** button
4. Confirm cancellation
5. Refund will be processed (if applicable)

**Cancellation Policy:**

- Cancellations allowed up to 2 hours before show time
- Full refund for cancellations made 24+ hours in advance
- 50% refund for cancellations made 2-24 hours in advance
- No refund for cancellations within 2 hours

---

## Troubleshooting

### Common Issues and Solutions

#### Cannot Login

**Problem**: Login fails with incorrect credentials

**Solutions:**
- Verify username and password are correct
- Check Caps Lock is off
- Use "Forgot Password" feature to reset
- Contact admin if issue persists

#### Real-time Features Not Working

**Problem**: Chat not sending, seats not updating

**Solutions:**
- Ensure socket server is running
- Check internet connection
- Restart the application
- Contact support if issue continues

#### Seats Not Available

**Problem**: Seats appear booked immediately after selection

**Solutions:**
- Another user may have booked simultaneously
- Wait 5 minutes for locks to expire
- Choose alternative seats
- Try a different show time

#### Payment Failed

**Problem**: Payment doesn't process

**Solutions:**
- Verify payment details are correct
- Check sufficient balance/credit limit
- Try a different payment method
- Contact your bank if issue persists
- Retry within the 5-minute window

#### Profile Picture Not Uploading

**Problem**: Image upload fails

**Solutions:**
- Use supported formats (JPG, PNG, GIF)
- Ensure file size is under 5MB
- Check file permissions
- Try a different image

#### Application Crashes

**Problem**: App closes unexpectedly

**Solutions:**
- Ensure Java 21+ is installed
- Check database connection
- Review system requirements
- Check error logs
- Reinstall if necessary

### Getting Help

If you encounter issues not covered here:

1. **Check Documentation**: Review detailed guides
2. **Contact Support**: Use in-app chat feature
3. **Email Support**: support@cinezone.com
4. **Report Bug**: Submit issue via GitHub

### Error Messages

#### "Database Connection Failed"
- MySQL server is not running
- Check database credentials
- Verify network connectivity

#### "Server Not Available"
- Socket server is not running
- Start MovieTicketServer application
- Check port 8888 is not blocked

#### "Session Expired"
- Login session timed out
- Login again with your credentials

---

## Tips and Best Practices

### For Better Experience

1. **Book Early**: Popular shows fill up quickly
2. **Enable Notifications**: Stay updated on bookings
3. **Complete Payment Quickly**: Don't let seat locks expire
4. **Write Reviews**: Help others choose movies
5. **Keep Profile Updated**: Ensure contact information is current
6. **Use Strong Passwords**: Protect your account
7. **Check Show Times**: Arrive at theater on time

### Security Tips

1. **Never Share Password**: Keep credentials confidential
2. **Logout After Use**: Especially on shared computers
3. **Verify Bookings**: Check confirmation emails
4. **Report Suspicious Activity**: Contact support immediately

---

## Frequently Asked Questions

**Q: Can I book tickets for multiple movies at once?**
A: Yes, complete one booking, then start another.

**Q: How do I get a refund?**
A: Cancel booking through "My Bookings" within cancellation window.

**Q: Can I change my seat after booking?**
A: No, cancel and rebook with new seats (subject to cancellation policy).

**Q: Is my payment information secure?**
A: Yes, we use industry-standard encryption.

**Q: Can I transfer my booking to someone else?**
A: No, bookings are non-transferable.

**Q: What if I lose my booking confirmation?**
A: View in "My Bookings" section with your booking ID.

**Q: How do I contact customer support?**
A: Use in-app chat, email, or phone support.

---

## Conclusion

Thank you for choosing CineZone! We hope this guide helps you enjoy a seamless movie booking experience. For additional assistance, please don't hesitate to contact our support team.

**Enjoy your movie! 🎬🍿**

