package Helpers;

public class CafeCoffeeThread extends Thread {
    private final CafeArea area;

    public CafeCoffeeThread(CafeArea area) {
        this.area = area;
    }

    @Override
    public void run() {
        System.out.println("Coffee thread has started");
        while (!Thread.currentThread().isInterrupted()) {
            area.brewCoffeeOrders();
        }
    }
}