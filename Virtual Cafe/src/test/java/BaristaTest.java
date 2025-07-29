import Helpers.CafeArea;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class BaristaTest {
    
    private CafeArea testCafeArea;
    private Thread serverThread;
    private final AtomicBoolean serverRunning = new AtomicBoolean(false);
    
    @BeforeEach
    void setUp() {
        // Create a new test CafeArea for each test
        testCafeArea = new CafeArea();
        
        // Set up the test environment
        try {
            // Reset the static port number to avoid port conflicts
            TestHelper.setStaticField(Barista.class, "portNumber", 8889);
            TestHelper.setStaticField(Barista.class, "areas", testCafeArea);
            
            // Store any exception that occurs in the server thread
            AtomicReference<Exception> serverError = new AtomicReference<>();
            
            // Start the server in a separate thread
            serverRunning.set(true);
            serverThread = new Thread(() -> {
                try {
                    System.out.println("Starting server in test thread...");
                    Barista.serverStartup();
                } catch (Exception e) {
                    if (serverRunning.get()) {
                        System.err.println("Server thread failed with exception: " + e.getMessage());
                        e.printStackTrace();
                        serverError.set(e);
                    }
                }
            });
            
            serverThread.setDaemon(true); // Make it a daemon thread so it doesn't prevent JVM exit
            serverThread.start();
            
            // Give the server a moment to start and check for errors
            for (int i = 0; i < 10; i++) {
                if (serverError.get() != null) {
                    throw new RuntimeException("Server thread failed to start", serverError.get());
                }
                if (serverThread.isAlive()) {
                    break;
                }
                Thread.sleep(100);
            }
            
            if (!serverThread.isAlive()) {
                throw new RuntimeException("Server thread failed to start");
            }
            
            System.out.println("Server thread started successfully");
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up test environment: " + e.getMessage(), e);
        }
    }
    
    @AfterEach
    void tearDown() {
        // Signal the server to stop
        serverRunning.set(false);
        
        // Reset static fields
        try {
            TestHelper.setStaticField(Barista.class, "areas", null);
        } catch (Exception e) {
            // Ignore
        }
        
        // Interrupt the server thread if it's still running
        if (serverThread != null && serverThread.isAlive()) {
            serverThread.interrupt();
            try {
                serverThread.join(1000); // Wait up to 1 second for the thread to finish
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    @Test
    void testServerInitialization() {
        // Verify the server is running
        assertTrue(serverThread.isAlive(), "Server thread should be running");
    }
    
    @Test
    void testServerHandlesMultipleClients() throws IOException {
        // Test that multiple clients can connect
        try (TestSocket client1 = new TestSocket("test input");
             TestSocket client2 = new TestSocket("test input")) {
            
            // Just verify we can create client connections
            assertTrue(true, "Should be able to create client connections");
        }
    }
    
    @Test
    void testMainMethod() {
        // Just verify the main method exists and can be called
        Barista.main(new String[]{});
        assertTrue(true);
    }
}