//client.java
import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
    private static final int PORT = 6789;

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        //prompt for server IP or hostname
        System.out.println("Enter server IP or hostname (e.g., 192.168.1.100 or localhost):");
        String host = scanner.nextLine().trim();

        Socket socket;
        try {
            socket = new Socket(host, PORT); {
        } catch (UnknownHostException e) {
                System.out.println("Invalid host: " + e.getMessage());
                return;
            } catch (IOException e) {
                System.out.println("Connection failed: " + e.getMessage() + " (Check IP/port/firewall)");
                return;
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            //authentication
            System.out.println(in.readLine()); // Enter username
            String username = scanner.nextLine();
            out.println(username); System.out.println(in.readLine()); // Enter password
            String password = scanner.nextLine();
            out.println(password);

            String authResponse = in.readLine();
            if (!"AUTH_SUCCESS".equals(authResponse)) {
                System.out.println("Authentication failed.");
                socket.close();
                return;
            }
            System.out.println("Logged in as " + username);

            //start listener thread
            new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        if (line.startsWith("MSG ")) {
                            System.out.println("Message: " + line.substring(4));
                        } else if (line.startsWith("FILE ")) {
                            receiveFile(line.substring(5), socket);
                        } else if (line.startsWith("ONLINE:")) {
                            System.out.println("Online users: " + line.substring(7).replace(",", ", "));
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Connection lost.");
                }
            }).start();

            //command loop
            while (true) {
                System.out.println("Commands: msg <user>:<message> | file <user>:<file_path> | status | logout");
                String command = scanner.nextLine();
                if (command.startsWith("msg ")) {
                    out.println("MSG " + command.substring(4));
                } else if (command.startsWith("file ")) {
                    sendFile(command.substring(5), out, socket);
                } else if (command.equals("status")) {
                    out.println("STATUS");
                } else if (command.equals("logout")) {
                    out.println("LOGOUT");
                    break;
                }
            }

            socket.close();
    }

        private static void sendFile(String fileInfo, PrintWriter out, Socket socket) throws IOException {
            String[] parts = fileInfo.split(":", 2); if (parts.length == 2) {
                String target = parts[0];
                String filePath = parts[1];
                File file = new File(filePath);
                if (file.exists()) {
                    out.println("FILE " + target + ":" + file.getName() + ":" + file.length());

                    //send file bytes
                    byte[] buffer = new byte[4096];
                    FileInputStream fis = new FileInputStream(file);
                    OutputStream os = socket.getOutputStream();
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                    os.flush();
                    fis.close();
                    System.out.println("File sent.");
                } else {
                    System.out.println("File not found.");
                }
            }
        }

        private static void receiveFile(String fileInfo, Socket socket) throws IOException {
            String[] parts = fileInfo.split(":", 3); if (parts.length == 3) {
                String sender = parts[0];
                String fileName = parts[1];
                int fileSize = Integer.parseInt(parts[2]);

                System.out.println("Receiving file " + fileName + " from " + sender);

                FileOutputStream fos = new FileOutputStream("received_" + fileName);
                byte[] buffer = new byte[4096];
                InputStream is = socket.getInputStream();
                int bytesRead;
                int total = 0;
                while (total < fileSize && (bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    total += bytesRead;
                }
                fos.close();
                System.out.println("File received.");
            }
        }
}
