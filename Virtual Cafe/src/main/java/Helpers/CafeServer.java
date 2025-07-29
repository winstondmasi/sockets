package Helpers;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class CafeServer implements Runnable {
    private static final String hidden_key = null;
    private Socket clientSocket;
    private CafeArea areas;
    private String customerName;

    // Constructor to initialize the CafeServer with client socket and cafe area
    public CafeServer(Socket clientSocket, CafeArea areas) {
        this.clientSocket = clientSocket;
        this.areas = areas;
    }

    // Converts a byte array to a hexadecimal string
    private static String convertByteToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    // Generates a hash using SHA-256 algorithm, combining a number with a secret key
    private static String makeSecretKey(String number, String key) {
        try {
            MessageDigest code = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = code.digest((number + key).getBytes());
            return convertByteToHex(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // Authenticates the client by sending a hashed number and expecting a matching response
    private boolean authenticateClient(Scanner scanner, PrintWriter writer) {
        Random random = new Random();
        int authNumber = 1000 + random.nextInt(9000);
        String madeHash = makeSecretKey(String.valueOf(authNumber), hidden_key);
        writer.println(madeHash);
        String clientResponse = scanner.nextLine();
        String verifyHash = makeSecretKey(String.valueOf(authNumber), hidden_key);
        return verifyHash.equals(clientResponse);
    }

    // Main execution method for the server thread
    @Override
    public void run() {
        try (Scanner scanner = new Scanner(clientSocket.getInputStream());
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            if (!authenticateClient(scanner, writer)) {
                writer.println("Authentication failed. Connection will be terminated.");
                return;
            }

            if (scanner.hasNextLine()) {
                customerName = scanner.nextLine();
            } else {
                System.err.println("Failed to receive the customer's name. Connection may have been closed.");
                return; // Stop further execution
            }

            areas.addNewClient();
            System.out.println("New connection with Customer: " + customerName);
            areas.logCurrentState();

            // Processing client commands
            while (true) {
                if (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] substrings = line.split(" ");
                    switch (substrings[0].toLowerCase()) {
                        case "make":
                            System.out.println("Received order from " + customerName + ": " + Arrays.toString(substrings));
                            new Thread(new ThreadManager(areas, substrings, customerName)).start();
                            break;
                        case "orders":
                            List<String> ordersList = areas.retrieveOrderList(customerName);
                            writer.println(ordersList.size()); // Number of orders
                            ordersList.forEach(writer::println); // Each order status
                            break;
                        case "exit":
                            areas.repurposeOrders(customerName);
                            areas.removeOrders(customerName);
                            writer.println("exit-ack"); // Acknowledge exit
                            return; // End thread on exit
                        default:
                            writer.println("ERROR Unknown command: " + substrings[0]);
                    }
                } else {
                    System.err.println("Client " + customerName + " connection was closed or interrupted.");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error with client " + customerName + ": " + e.getMessage());
        } finally {
            System.out.println("Customer " + customerName + " left the shop.");
            areas.removeClient();
            areas.logCurrentState();
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket for customer " + customerName);
            }
        }
    }
}
