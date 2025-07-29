import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

import java.net.Socket;
import java.io.*;

public class Customer implements AutoCloseable {
    private boolean isClosed = false;
    private Socket socket;
    private Scanner readline;
    private PrintWriter writeLine;
    private final int portNumber = 8888;
    private static String customerName;

    // Constructor to initialize Customer with a name
    public Customer(String customerName) throws IOException {
        this(customerName, createDefaultSocket());
    }
    
    // Package-private constructor for testing
    Customer(String customerName, Socket socket) throws IOException {
        Customer.customerName = customerName;
        this.socket = socket;
        this.readline = new Scanner(socket.getInputStream());
        this.writeLine = new PrintWriter(socket.getOutputStream(), true);
    }
    
    // Method to create the default socket (can be overridden in tests)
    private static Socket createDefaultSocket() throws IOException {
        return new Socket("localhost", 8888);
    }

    // Method to place an order
    public void PlaceOrder(String customer_Input) {
        // Convert the entire input string to lowercase for case-insensitive command processing
        String inputLowerCase = customer_Input.toLowerCase();
        String[] numOfCommands = inputLowerCase.split(" ");

        try {
            StringBuilder command = new StringBuilder("MAKE ");
            for (int i = 1; i < numOfCommands.length; i++) {
                if (numOfCommands[i].matches("\\d+")) { // Check if it's a number (quantity)
                    int quantity = Integer.parseInt(numOfCommands[i]);
                    if (i + 1 < numOfCommands.length && numOfCommands[i + 1].matches("(tea|coffee)s?")) {
                        String beverage = numOfCommands[i + 1];
                        command.append(quantity).append(" ").append(beverage).append(" ");
                    }
                }
            }
            if (command.length() > 0) {
                command.setLength(command.length() - 1); // Remove trailing space
            }
            writeLine.println(command.toString());
            System.out.println("Order Placed");
        } catch (NumberFormatException e) {
            System.err.println("Invalid quantity. Please enter a valid order.");
        }
    }


    // Getter for customerName
    public String getCustomerName() {
        return Customer.customerName;
    }

    // Method to retrieve the order status
    public String[] RetrieveOrder() {
        writeLine.println("ORDERS");
        String line = readline.nextLine();
        int numOfOrders;
        try {
            numOfOrders = Integer.parseInt(line); // Expecting an integer
        } catch (NumberFormatException e) {
            System.err.println("Invalid response from server: " + line);
            return new String[0]; // Return empty array on error
        }
        String[] ordersMade = new String[numOfOrders];
        for (int i = 0; i < numOfOrders; i++) {
            ordersMade[i] = readline.nextLine();
        }
        return ordersMade;
    }

    // Method for server authentication
    public void ServerAuthentication(Scanner scan) throws IOException {
        String serverNumber = readline.nextLine(); // Read the number sent by the server
        System.out.println("Please enter the authentication code received: " + serverNumber);
        String userInput = scan.nextLine();
        if (!userInput.equals(serverNumber)) {
            throw new IOException("Authentication failed: Incorrect input.");
        } else {
            writeLine.println(userInput); // Confirm with the server
            System.out.println("Authentication successful.");
        }
    }

    // Method to exit the cafe
    public void exitCafe() throws Exception {
        writeLine.println("exit");
        String response = readline.nextLine(); // Wait for server's confirmation
        if ("exit-ack".equals(response)) {
            System.out.println("Exiting cafe...");
            close(); // Close the connection
        } else {
            System.err.println("Failed to properly exit cafe.");
        }
    }

    
    @Override
    public void close() throws Exception {
        if (!isClosed) {
            System.out.println("Closing Cafe.");
            readline.close();
            writeLine.close();
            socket.close();
            isClosed = true; // Set the flag indicating the client is closed
        }
    }



    public static void main(String[] args) {
        // Prompt for entering the customer's name
        System.out.println("Enter your Name: ");
        Scanner scan = new Scanner(System.in);
        String name = scan.nextLine();
        AtomicReference<Customer> customerRef = new AtomicReference<>();
    
        try {
            // Create a new Customer instance and set up the connection
            Customer customer = new Customer(name); // Initialize with entered name
            customerRef.set(customer);
    
            // Perform server authentication
            customer.ServerAuthentication(scan);
            customer.writeLine.println(name);
    
            // Welcome message and cafe information
            System.out.println("Welcome " + customer.getCustomerName() + " to the Virtual Cafe!");
            System.out.println("We serve only Tea and Coffee.");
            System.out.println("Tea will take 30 seconds.");
            System.out.println("Coffee will take 45 seconds.");
            System.out.println("Enter how many Teas or Coffees you want:");
    
            // Main loop for processing user input
            while (true) {
                String query = scan.nextLine();
                String[] substrings = query.toLowerCase().split(" ");
    
                // Command to exit the cafe
                if ("exit".equals(substrings[0])) {
                    customer.exitCafe(); // Exit the cafe
                    break;    
                }
    
                // Command to check the status of the order
                if ("status".equals(substrings[1])) {
                    String[] listOfOrders = customer.RetrieveOrder();
                    if (listOfOrders.length == 0) {
                        System.out.println("There are currently no orders for " + customer.getCustomerName());
                    } else {
                        // Display all orders
                        for (String order : listOfOrders) {
                            System.out.println(order);
                        }
                    }
                } else {
                    // Process any other input as an order
                    customer.PlaceOrder(query);
                    System.out.println("To check the status of the order, type 'order status'.");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while communicating with the server: " + e.getMessage());
        }
    }
    
    
}