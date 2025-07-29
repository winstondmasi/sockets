import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import Helpers.*;

public class Barista {
    private static int portNumber = 8888;
    private static CafeArea areas = new CafeArea();

    public static void main(String[] args) {
        CafeLog.clearJsonLog();
        serverStartup();
    }

    public static void serverStartup() {
        // Start brewing threads outside of the client connection loop
        Thread teaThread = new Thread(new CafeTeaThread(areas));
        Thread coffeeThread = new Thread(new CafeCoffeeThread(areas));
        teaThread.start();
        coffeeThread.start();

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("Waiting for connections...");
            while (true) {
                Socket socket = serverSocket.accept();  // accept incoming connections (blocks until it does!)
                new Thread(new CafeServer(socket, areas)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
