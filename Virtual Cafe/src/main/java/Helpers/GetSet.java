package Helpers;

public class GetSet {
    private String orderStatus;
    private String teaOrCoffee;
    private String custumerName;
    private int orderID;

    public GetSet(int orderID, String customerName, String teaOrCoffee){
        this.orderID = orderID;
        this.custumerName = customerName;
        this.teaOrCoffee = teaOrCoffee;
    }

    public int getID(){
        return orderID;
    }

    public String getStatus(){
        return orderStatus;
    }

    public String setStatus(String newStat){
        orderStatus = newStat;
        return orderStatus;
    }

    public String getName(){
        return custumerName;
    }

    public String getTeaOrCoffee(){
        return teaOrCoffee;
    }

    public void setName(String newClientName) {
        this.custumerName = newClientName;
    }
}
