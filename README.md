# Java Instant Messenger (TCP Client–Server)

This project is a Java-based instant messaging application built using TCP socket programming. It was developed as a group project for a networking course and demonstrates core client–server concepts, multithreading, and network communication.

The application allows authenticated users to send private messages and transfer files to other online users through a centralized server.

---

## Features
- TCP client–server architecture
- Username/password authentication (demo users)
- One-to-one private messaging
- File transfer between connected clients
- Online user status tracking
- Multi-client support using threads
- Thread-safe handling of connected clients

---

## Technologies Used
- Java
- TCP Sockets
- Multithreading
- Input/Output Streams
- ConcurrentHashMap

---

## How the Application Works
- The server listens on a fixed port for incoming client connections.
- Each client authenticates using a username and password.
- The server maintains a list of online users.
- Messages and files are routed through the server to the intended recipient.
- Each connected client runs on its own thread to allow concurrent communication.

---

## How to Run the Project

### 1. Start the Server
Open a terminal and run:
```bash
javac Server.java
java Server
```

open a new terminal:
javac Client.java
java Client

When prompted, enter the server IP address.
For local testing, use:
localhost

Demo Login Credentials
Username: user1   Password: pass1
Username: user2   Password: pass2
Username: user3   Password: pass3

Client Commands
msg <username>:<message>      Send a private message
file <username>:<file_path>  Send a file to another user
status                       View online users
logout                       Disconnect from the server

msg user2: Hello!
