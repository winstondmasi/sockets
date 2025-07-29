package Helpers;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CafeArea {
    // Data structures to manage orders and areas in the cafe
    private final ConcurrentHashMap<Integer, GetSet> order = new ConcurrentHashMap<>();
    private final Queue<Integer> orderSequence = new ConcurrentLinkedQueue<>();
    private final Queue<Integer> waitingArea = new LinkedList<>();
    private final Queue<Integer> trayArea = new LinkedList<>();
    private final Set<Integer> teaBrewArea = new HashSet<>();
    private final Set<Integer> coffeeBrewArea = new HashSet<>();
    private AtomicInteger clientCount = new AtomicInteger(0);
    private AtomicInteger trayAreaCount = new AtomicInteger(0);
    private AtomicInteger deliveredOrdersCount = new AtomicInteger(0);

    // Create a new order with a specific ID
    public void createNewOrder(String customerName, String teaOrCoffee, int orderID) {
        GetSet getSet = new GetSet(orderID, customerName, teaOrCoffee);
        getSet.setStatus("In Waiting");
        waitingArea.add(orderID);
        order.put(orderID, getSet);
        Sort();
    }

    // Generate and create a new order
    public void generateAndCreateOrder(String customerName, String teaOrCoffee) {
        int newOrderID = order.isEmpty() ? 1 : Collections.max(order.keySet()) + 1;
        orderSequence.add(newOrderID);
        createNewOrder(customerName, teaOrCoffee, newOrderID);
        CafeLog.logEvent("New Order Placed", "OrderID: " + newOrderID + ", Customer: " + customerName + ", Beverage: " + teaOrCoffee); // JSON LOG
    }

    // Sort and process orders for brewing
    public void Sort() {
        processOrdersForBrewing(teaBrewArea, "tea", 2);
        processOrdersForBrewing(coffeeBrewArea, "coffee", 2);
    }

    // Process orders for brewing in specific areas
    private void processOrdersForBrewing(Set<Integer> brewArea, String type, int capacity) {
        synchronized (brewArea) {
            Iterator<Integer> iterator = waitingArea.iterator();
            while (iterator.hasNext() && brewArea.size() < capacity) {
                int orderID = iterator.next();
                GetSet order = this.order.get(orderID);
                if (order != null && order.getTeaOrCoffee().toLowerCase().startsWith(type)) {
                    order.setStatus("Brewing Area");
                    brewArea.add(orderID);
                    CafeLog.logEvent("Order Moved to Brewing", "Order ID " + orderID + ", Customer: " + order.getName() + ", Beverage: " + type);
                    iterator.remove();
                }
            }
        }
    }

    // Method to move orders to the tray after brewing
    private void traying(int orderID, int brewTime) throws InterruptedException {
        GetSet making = this.order.get(orderID);
        if (making != null) {
            Thread.sleep(brewTime);
            synchronized (this) {
                if (teaBrewArea.contains(orderID) || coffeeBrewArea.contains(orderID)) {
                    making.setStatus("Tray Area");
                    CafeLog.logEvent("Order Moved to Tray", "Order ID " + orderID + ", Customer: " + making.getName() + ", Beverage: " + making.getTeaOrCoffee());
                    teaBrewArea.remove(orderID);
                    coffeeBrewArea.remove(orderID);
                    trayArea.add(orderID);
                    trayAreaCount.incrementAndGet();
                    logCurrentState();
                }
            }
        }
    }

    // Retrieve a list of orders for a specific customer
    public List<String> retrieveOrderList(String customerName) {
        List<String> query = new ArrayList<>();
        for (GetSet list : order.values()) {
            if (customerName.equals(list.getName())) {
                String orderDetails = "Order for " + list.getName() +
                                      " Order Number: " + list.getID() +
                                      " Beverage: " + list.getTeaOrCoffee() +
                                      " Status: " + list.getStatus();
                query.add(orderDetails);
            }
        }
        return query;
    }

    // Increment client count when a new client is added
    public void addNewClient() {
        clientCount.incrementAndGet();
        CafeLog.logEvent("New Client Added", "Total Clients: " + clientCount.get()); // JSON LOG
    }

    // Decrement client count when a client is removed
    public void removeClient() {
        clientCount.decrementAndGet();
        CafeLog.logEvent("Client Removed", "Total Clients: " + clientCount.get());
    }

    // Find a new customer for an order if the original customer leaves
    private String findNewCustomerForOrder(String teaOrCoffee, String leavingClientName) {
        for (Map.Entry<Integer, GetSet> entry : order.entrySet()) {
            GetSet currentOrder = entry.getValue();
            // Return new customer's name if found
            if (!currentOrder.getName().equals(leavingClientName) && currentOrder.getTeaOrCoffee().equals(teaOrCoffee)
                && !"Delivered".equals(currentOrder.getStatus())) {
                return currentOrder.getName();
            }
        }
        return null; // No suitable customer found
    }
    
    // Repurpose orders of a leaving customer to other customers
    public void repurposeOrders(String customerName) {
        System.out.println("Attempting to repurpose orders for: " + customerName);
        boolean made_repurposed = false;
    
        // Iterate through all orders of the leaving customer
        for (Iterator<Map.Entry<Integer, GetSet>> it = order.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Integer, GetSet> entry = it.next();
            GetSet curr_order = entry.getValue();
    
            // Check if the order belongs to the leaving customer and is not delivered
            if (curr_order.getName().equals(customerName) && !"Delivered".equals(curr_order.getStatus())) {
                String newCustName = findNewCustomerForOrder(curr_order.getTeaOrCoffee(), customerName);
                if (newCustName != null) {
                    curr_order.setName(newCustName); // Reassign order to new customer
                    made_repurposed = true;
                    System.out.println("Order ID " + curr_order.getID() + " repurposed to " + newCustName);
                    String JSON_details = "OrderID: " + curr_order.getID() + ", From: " + customerName + ", To: " + newCustName;
                    CafeLog.logEvent("Order Repurposed", JSON_details); //JSON LOG
                }
            }
        }
    
        System.out.println(made_repurposed ? "Some orders were repurposed for other customers." : "No orders were repurposed.");
    }

    // Remove orders of a customer who has left the cafe
    public void removeOrders(String customerName) {
        synchronized (order) {
            Iterator<Map.Entry<Integer, GetSet>> loop = order.entrySet().iterator();
            while (loop.hasNext()) {
                Map.Entry<Integer, GetSet> entry = loop.next();
                GetSet curr_order = entry.getValue();
                if (curr_order.getName().equals(customerName)) {
                    // Remove the order from all areas and order map
                    teaBrewArea.remove(curr_order.getID());
                    coffeeBrewArea.remove(curr_order.getID());
                    trayArea.remove(curr_order.getID());
                    loop.remove();
                }
            }
        }
        Sort(); // Re-sort the orders after removal
        CafeLog.logEvent("Orders Removed", "Customer: " + customerName + " left, Orders Removed"); //JSON LOG
    }

    // Methods to brew tea and coffee orders
    public void brewTeaOrders() {
        // Continuously brew tea orders
        while (true) {
            try {
                brewOrders(teaBrewArea, 30000); // 30 seconds for tea
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
    
    public void brewCoffeeOrders() {
        while (true) {
            try {
                brewOrders(coffeeBrewArea, 45000); // 45 seconds for coffee
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    // Helper method to brew orders 
    private void brewOrders(Set<Integer> brewArea, int brewTime) throws InterruptedException {
        Integer orderID = null;
        synchronized (brewArea) {
            if (!brewArea.isEmpty()) {
                orderID = brewArea.iterator().next();
            }
        }
        if (orderID != null) {
            traying(orderID, brewTime); // Move order to tray after brewing
            synchronized (brewArea) {
                brewArea.remove(orderID);
            }
            Sort(); // Re-sort orders after brewing
            GetSet orderDetails = order.get(orderID);
            if (orderDetails != null && areAllOrdersReady(orderDetails.getName())) {
                Delivered(orderDetails.getName()); // Deliver order if all items are ready
            }
        }
    }

    // Check if all orders for a customer are ready (in the tray area)
    public boolean areAllOrdersReady(String customerName) {
        for (GetSet all : order.values()) {
            if (customerName.equals(all.getName()) && !"Tray Area".equals(all.getStatus())) {
                return false; // Return false if any order is not ready
            }
        }
        return true; // All orders are ready
    }
    
    

    public void Delivered(String customerName) {
        Map<String, Integer> bev_count = new HashMap<>();
    
        synchronized (trayArea) {
            Iterator<Integer> deliver = trayArea.iterator();
            while (deliver.hasNext()) {
                int orderID = deliver.next();
                GetSet gSet = order.get(orderID);
                if (gSet != null && customerName.equals(gSet.getName())) {
                    bev_count.merge(gSet.getTeaOrCoffee(), 1, Integer::sum);
                    gSet.setStatus("Delivered"); // Update the status to Delivered
                    deliver.remove();
                    order.remove(orderID);
                    deliveredOrdersCount.incrementAndGet();
                }
            }
            trayAreaCount.set(trayArea.size()); // Update the tray area count based on current size
        }
    
    // Building a summary of what is being delivered for console output
    StringBuilder buildDetails = new StringBuilder("\nOrders for " + customerName + " have been delivered: ");
    for (Map.Entry<String, Integer> enter : bev_count.entrySet()) {
        buildDetails.append(enter.getValue()).append(" ").append(enter.getKey()).append("(s) ");
    }
    System.out.println(buildDetails.toString());

    // Building a summary of what is being delivered for JSON log
    StringBuilder logDetails = new StringBuilder();
    for (Map.Entry<String, Integer> enter : bev_count.entrySet()) {
        logDetails.append(enter.getValue()).append(" ").append(enter.getKey()).append("(s) ");
    }

    // Log the event
    CafeLog.logEvent("Order Delivered", "Orders for " + customerName + " have been delivered: " + logDetails.toString().trim());

    Sort(); // Re-sort the orders after delivering
    logCurrentState(); // Log the current state of the cafe
    }
    
    

    // Method to output the current state in the terminal
    public void logCurrentState() {
        System.out.println("\nNumber of clients in the café: " + clientCount.get());
        System.out.println("Number of clients waiting for orders: " + waitingArea.size());
        System.out.println("Number of items in waiting area: " + waitingArea.stream().mapToInt(id -> order.get(id) != null ? 1 : 0).sum());
        System.out.println("Number of teas brewing: " + teaBrewArea.size());
        System.out.println("Number of coffees brewing: " + coffeeBrewArea.size());
        System.out.println("Number of items in tray area: " + trayAreaCount.get());
        System.out.println("Total number of orders delivered: " + deliveredOrdersCount.get());
    }
    
}
