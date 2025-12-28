import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * This class implements the chat client that connects to the server,
 * sends messages, and receives broadcasted messages from other clients.
 * 
 * @author [Anh Tuan Ho]
 */
public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost"; // Server IP (localhost for testing)
    private static final int SERVER_PORT = 12345;             // Server port
    private static final String PROMPT = "Enter message (/q to quit program): ";

    public static void main(String[] args) {
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        Scanner scanner = new Scanner(System.in);

        try {
            // Establish connection to the server
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);

            // Set up input and output streams
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Read and print the initial connection message from the server (plain, no color or clearing)
            String connectionMsg = in.readLine();
            if (connectionMsg != null) {
                System.out.println(connectionMsg);
            }

            // Prompt for user's name and send it to the server as the first message
            System.out.print("Enter your name: ");
            String name = scanner.nextLine();
            out.println(name);

            // Display hello message after entering name
            System.out.println("Hello " + name + " !");

            // Start a thread to handle incoming broadcast messages from the server
            BufferedReader finalIn = in; // For use in lambda
            Thread receiverThread = new Thread(() -> {
                try {
                    String serverResponse;
                    while ((serverResponse = finalIn.readLine()) != null) {
                        // Clear the current prompt line using ANSI escape code for better compatibility
                        System.out.print("\u001B[2K\r");
                        // Print the message in cyan
                        System.out.println("\u001B[36m" + serverResponse + "\u001B[0m");
                        // Reprint the prompt
                        System.out.print(PROMPT);
                    }
                } catch (IOException e) {
                    // Suppress stack trace for clean exit
                }
            });
            receiverThread.setDaemon(true); // Set as daemon so JVM exits when main thread ends
            receiverThread.start();

            // Read user input from console and send to server
            System.out.print(PROMPT); // Initial prompt after hello message
            while (scanner.hasNextLine()) {
                String userInput = scanner.nextLine();
                if (userInput.equals("/q")) {
                    // Shutdown input/output to unblock the receiving thread
                    socket.shutdownInput();
                    socket.shutdownOutput();
                    break; // Exit the loop to quit the program
                }
                out.println(userInput);
                System.out.print(PROMPT); // Reprint prompt after sending
            }
        } catch (IOException e) {
            System.out.println("Unable to connect to server.");
        } finally {
            // Clean up resources
            try {
                if (scanner != null) scanner.close();
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                // Suppress stack trace for clean exit
            }
        }
    }
}