A simple Java Swing-based chat application that mimics a WhatsApp-like interface for messaging between users and also for video calling. This application connects to a MySQL database for storing and retrieving messages and supports features like real-time message updates and video calling through Jitsi Meet.

Features
User Login: Each user can log in and participate in a chat session.
Real-time Messaging: Messages between users are automatically fetched and displayed in real-time (every 3 seconds).
Recipient Selection: Users can select a recipient from the list to send messages directly.
Video Calling: Integration with Jitsi Meet allows users to initiate video calls by entering a room name.
Clear Chat: A button to clear the chat history on the user's screen.
Custom UI: WhatsApp-like green background with a responsive and easy-to-use UI.

Usage
Login with any valid username and password that exists in the users table.
Start chatting with other users by selecting a recipient from the list and typing your message.
Initiate a video call by clicking the "Start Video Call" button and entering a room name.
Clear the chat screen using the "Clear Chat" button.
Technologies Used
Java Swing: For building the user interface.
MySQL: For the backend database to store users and messages.
JDBC: For database connectivity.
Jitsi Meet API: For enabling video calling functionality.
Timer: To poll and fetch messages every few seconds.

This project is licensed under the MIT License - see the LICENSE file for details.


