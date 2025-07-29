package Helpers;
import java.io.RandomAccessFile;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CafeLog {

    private static final String JSON_FILE = "cafe_log.json";

    // Method to clear the log file and reinitialize it as an empty JSON array
    public static void clearJsonLog() {
        try (FileWriter file = new FileWriter(JSON_FILE, false)) { // false to overwrite
            file.write("[]"); // Initialize with an empty JSON array
        } catch (IOException e) {
            System.out.println("Error clearing log file: " + e.getMessage());
        }
    }


    // Method to log an event
    public static void logEvent(String event, String extraInfo) {
        String date_and_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String json = String.format("{\"timestamp\": \"%s\", \"event\": \"%s\", \"details\": \"%s\"}\n", 
                                    date_and_time, 
                                    event.replace("\"", "\\\""), 
                                    extraInfo.replace("\"", "\\\""));

        try (RandomAccessFile fl = new RandomAccessFile(JSON_FILE, "rw")) {
            long fl_length = fl.length();
            fl_length = fl_length - 1; // Move to the position before the closing bracket ]
            fl.seek(fl_length); // Set the pointer to that position
            String prefix = fl_length > 1 ? ",\n" : ""; // Add a comma and new line if not the first entry
            fl.writeBytes(prefix + json + "]");
        } catch (IOException e) {
            System.out.println("Error writing to log file: " + e.getMessage());
        }
    }

}
