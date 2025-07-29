package Helpers;

public class CafeTeaThread extends Thread {
    private final CafeArea area;

    public CafeTeaThread(CafeArea area) {
        this.area = area;
    }

    @Override
    public void run() {
        System.out.println("Tea thread has started");
        while (!Thread.currentThread().isInterrupted()) {
            area.brewTeaOrders();
        }
    }
}
