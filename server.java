//server.java
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class  Server {
    private static final int PORT = 6789;
    private static final Map<String, String> users = new HashMap<>(); //username->password
    private static final Map<String, ClientHandler> onlineClients = new ConcurrentHasMap<>();//username->handler

    static {
        //hardcoded users for simplicity
        users.put("user1", "pass1");
        users.put("user2", "pass2");
        users.put("user3", "pass3");
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Server starting on port" + PORT);
        ServerSocket serverSocket = new ServerSocket(PORT);
        while (true) {
            Socket clientSocket = serverSocket.accept();
            new ClientHandler(clientSocket).start();
        }
    }

    static class ClientHandler extends Thread {
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }
        @Override public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new
                        PrintWriter(socket.getOutputStream(), true);

                //Authentication
                out.println("Enter username:");
                String user = in.readLine();
                out.println("Enter password:");
                String pass = in.readLine();

                if (users.containsKey(user) && users.get(user).equals(pass)) {
                    username = user;
                    onlineClients.put(username, this);
                    out.println("AUTH_SUCCESS");
                    System.out.println("User " + username + " online.");
                    broadcastStatus();
                } else {
                    out.println("AUTH_FAIL");
                    socket.close();
                    return;
                }

                //handle commands
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("MSG ")) {
                        sendMessage(line.substring(4));
                    } else if (line.startsWith("FILE ")) {
                        sendFile(line.substring(5));
                    } else if (line.equals("STATUS")) {
                        sendStatus();
                    } else if (line.equals("LOGOUT")) {
                        break;
                    }
                }
            }   catch (IOException e) {
                System.out.println("Error with client: " + e.getMessage());
            } finally {
                if (username != null) {
                    onlineClients.remove(username);
                    System.out.println("User " + username + " offline.");
                    broadcastStatus();
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
        private void sendMessage(String msg) {
            String[] parts = msg.split(":", 2); if (parts.length == 2) {
                String target = parts[0];
                String text = parts[1];
                ClientHandler targetHandler = onlineClients.get(target);
                if (targetHandler != null) {
                    targetHandler.out.println("MSG " + username + ":" + text);
                } else {
                    out.println("ERROR User " + target + " not online.");
                }
            }
        }
        private void sendFile(String fileInfo) throws IOException {
            String[] parts = fileInfo.split(":", 3); if (parts.length == 3) {
                String target = parts[0];
                String fileName = parts[1];
                int fileSize = Integer.parseInt(parts[2]);

                ClientHandler targetHandler = onlineClients.get(target);
                if (targetHandler != null) {
                    targetHandler.out.println("FILE " + username + ":" + fileName + ":" + fileSize);

                    //relay file bytes
                    byte[] buffer = new byte[4096];
                    InputStream fileIn = socket.getInputStream();
                    OutputStream fileOut = targetHandler.socket.getOutputStream();
                    int bytesRead; int total = 0;
                    while (total < fileSize && (bytesRead = fileIn.read(buffer)) != -1) {
                        fileOut.write(buffer, 0, bytesRead);
                        total += bytesRead;
                    }
                    fileOut.flush();
                } else {
                    out.println("ERROR User " + target + " not online.");

                    //drain the file bytes to avoid blocking
                    byte[] buffer = new byte[4096];
                    InputStream drain = socket.getInputStream();
                    int total = 0;
                    while (total < fileSize) { total += drain.read(buffer);
                }
            }
        }
    }
    private void sendStatus() {
        StringBuilder sb = new StringBuilder("ONLINE:");
        for (String u : onlineClients.keySet())  {
            sb.append(u).append(",");
        }
        out.println(sb.toString());
        }
    }
    private static void broadcastStatus() {
        StringBuilder sb = new StringBuilder("ONLINE:");
        for (String u : onlineClients.keySet()) {
            sb.append(u).append(",");
        }
        String status = sb.toString();
        for (ClientHandler handler : onlineClients.values()) {
            handler.out.println(status);
        }
    }
}
