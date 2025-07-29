package Helpers;

public class ThreadManager implements Runnable {
    private final CafeArea area;
    private final String[] sub;
    private final String customerName;

    public ThreadManager(CafeArea area, String[] sub, String customerName) {
        this.area = area;
        this.sub = sub;
        this.customerName = customerName;
    }

    @Override
    public void run() {
        if (sub.length == 3) {
            String beverageType = sub[2]; // Extracting the beverage type
            processOrder(sub[1], beverageType);
        } else if (sub.length == 5) {
            String beverageType1 = sub[2]; // Extracting the first beverage type
            String beverageType2 = sub[4]; // Extracting the second beverage type
            processOrder(sub[1], beverageType1);
            processOrder(sub[3], beverageType2);
        }
        area.Sort();
        area.Delivered(customerName);
        area.logCurrentState(); // Log state after delivering order
    }

    private void processOrder(String quantityString, String beverageType) {
        try {
            int quantity = Integer.parseInt(quantityString);
            for (int i = 0; i < quantity; i++) {
                area.generateAndCreateOrder(customerName, beverageType);
            }
        } catch (NumberFormatException e) {
            System.out.println("Error processing order: " + e.getMessage());
        }
    }

}
