import java.io.*;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class implements the chat server using socket programming.
 * It handles multiple client connections, assigns unique IDs, maintains a list of connected clients,
 * and broadcasts messages to all connected clients.
 * 
 * @author [Anh Tuan Ho]
 */
public class ChatServer {
    private static final int PORT = 12345; // Port on which the server listens
    private static CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>(); // Thread-safe list of clients
    private static int nextId = 1; // Counter for assigning unique client IDs

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server is running and waiting for connections...");

            // Accept incoming client connections indefinitely
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                // Create a new client handler with a unique ID
                ClientHandler clientHandler = new ClientHandler(clientSocket, nextId++);
                clients.add(clientHandler);

                // Start a new thread for the client handler
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Broadcasts a message to all connected clients except the sender.
     * 
     * @param message The message to broadcast.
     * @param sender  The client handler of the sender (to exclude from broadcast).
     */
    public static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

 

    /**
     * Removes a client from the list and prints the updated client list.
     * 
     * @param client The client handler to remove.
     */
    public static void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println(client.getName() + " disconnected, ID " + client.getId() + ", total clients: " + clients.size() + ".");
    }

    /**
     * Inner class to handle individual client connections in a separate thread.
     */
    private static class ClientHandler implements Runnable {
        private Socket clientSocket; // Client's socket
        private PrintWriter out;     // Output stream to client
        private BufferedReader in;   // Input stream from client
        private int id;              // Unique ID for the client
        private String name;         // Client's name

        /**
         * Constructor to initialize the client handler.
         * 
         * @param socket The client's socket.
         * @param id     The unique ID assigned to the client.
         */
        public ClientHandler(Socket socket, int id) {
            this.clientSocket = socket;
            this.id = id;
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Returns the client's ID.
         * 
         * @return The client's ID.
         */
        public int getId() {
            return id;
        }

        /**
         * Returns the client's name.
         * 
         * @return The client's name.
         */
        public String getName() {
            return name != null ? name : "Unknown";
        }

        @Override
        public void run() {
            try {
                // Send connection confirmation and ID to the client
                out.println("Connected to chat server, your ID is " + id + ".");

                // Read the client's name (first input after connection)
                name = in.readLine();
                if (name != null && !name.trim().isEmpty()) {
                    System.out.println(name + " connected, ID " + id + ", total clients: " + clients.size() + ".");
                 }

                String inputLine;
                // Read messages from the client
                while ((inputLine = in.readLine()) != null) {
                    // Print received message on server console
                    System.out.println("New message received from " + name + "(ID " + id + "): " + inputLine);
                    // Broadcast the message to other clients with name and ID prefix
                    broadcast("New message from " + name + "(ID " + id + "): " + inputLine, this);
                }
            } catch (IOException e) {
                // Handle client disconnection
            } finally {
                // Clean up resources and remove client
                try {
                    if (in != null) in.close();
                    if (out != null) out.close();
                    if (clientSocket != null) clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                removeClient(this);
            }
        }

        /**
         * Sends a message to the client.
         * 
         * @param message The message to send.
         */
        public void sendMessage(String message) {
            out.println(message);
        }
    }
}