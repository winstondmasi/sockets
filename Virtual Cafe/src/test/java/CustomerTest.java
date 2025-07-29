import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerTest {
    private TestSocket testSocket;
    private Customer customer;
    private ByteArrayOutputStream outputStream;
    private PrintWriter writer;
    
    @BeforeEach
    void setUp() throws Exception {
        // Setup test socket with sample input
        String input = "123\n1 coffee\n";
        testSocket = new TestSocket(input);
        outputStream = (ByteArrayOutputStream) testSocket.getOutputStream();
        
        // Create the test customer with test socket using package-private constructor
        customer = new Customer("TestCustomer", testSocket);
        
        // Set up writer to capture output
        writer = new PrintWriter(outputStream, true);
        TestHelper.setInstanceField(customer, "writeLine", writer);
        
        // Set up reader with test input
        TestHelper.setInstanceField(customer, "readline", new Scanner(testSocket.getInputStream()));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (customer != null) {
            try {
                customer.close();
            } catch (Exception e) {
                // Ignore close exceptions in tests
            }
        }
        if (writer != null) {
            writer.close();
        }
        if (testSocket != null) {
            testSocket.close();
        }
    }
    
    @Test
    void testGetCustomerName() {
        // Setup
        String expectedName = "TestCustomer";
        
        // Execute & Verify
        assertEquals(expectedName, customer.getCustomerName());
    }
    
    @Test
    void testPlaceOrder_ValidOrder() throws IOException {
        // Setup
        String order = "MAKE 1 coffee";
        
        // Execute
        customer.PlaceOrder(order);
        
        // Verify the output contains the order
        String output = outputStream.toString();
        assertTrue(output.contains("MAKE 1 coffee"));
    }
    
    @Test
    void testPlaceOrder_InvalidOrder() {
        // Setup
        String invalidOrder = "MAKE coffee";
        
        // Execute & Verify - should not throw exception
        assertDoesNotThrow(() -> customer.PlaceOrder(invalidOrder));
    }
    
    @Test
    void testRetrieveOrder() throws Exception {
        // Setup - simulate server response
        String serverResponse = "1\n1 coffee - READY";
        TestSocket responseSocket = new TestSocket(serverResponse);
        TestHelper.setInstanceField(customer, "socket", responseSocket);
        TestHelper.setInstanceField(customer, "readline", new Scanner(responseSocket.getInputStream()));
        
        // Execute
        String[] orders = customer.RetrieveOrder();
        
        // Verify
        assertNotNull(orders);
        assertEquals(1, orders.length);
        assertEquals("1 coffee - READY", orders[0]);
    }
    
    @Test
    void testServerAuthentication_Success() throws Exception {
        // Setup - create a new test socket for this test
        String authCode = "123456";
        TestSocket authSocket = new TestSocket(authCode + "\n");
        Customer testCustomer = new Customer("TestCustomer", authSocket);
        
        // Execute
        testCustomer.ServerAuthentication(new Scanner(authCode));
        
        // Verify the output contains the auth code
        String output = authSocket.getOutput();
        assertTrue(output.contains(authCode), "Output should contain auth code");
    }
    
    @Test
    void testExitCafe() throws Exception {
        // Setup - create a new test socket with the expected response
        TestSocket exitSocket = new TestSocket("exit-ack\n");
        Customer testCustomer = new Customer("TestCustomer", exitSocket);
        
        // Execute
        testCustomer.exitCafe();
        
        // Verify the output contains exit command
        String output = exitSocket.getOutput();
        assertTrue(output.contains("exit"), "Output should contain exit command");
        assertTrue(exitSocket.isClosed(), "Socket should be closed after exit");
    }
    
    @Test
    void testClose_WhenNotClosed() throws Exception {
        // Setup - set isClosed to false using TestHelper
        TestHelper.setInstanceField(customer, "isClosed", false);
        
        // Execute
        customer.close();
        
        // Verify
        assertTrue(testSocket.isClosed());
        assertTrue((Boolean) TestHelper.getInstanceField(customer, "isClosed"));
    }
}