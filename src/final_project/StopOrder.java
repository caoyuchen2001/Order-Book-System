package final_project;

/*
 * StopOrder class
 */

public class StopOrder extends Order {
    private int stopPrice;

    public StopOrder(int orderId, String username, String type, int size, long timestamp, int stopPrice) {
        super(orderId, username, type, size, timestamp, "stop");
        this.stopPrice = stopPrice;
    }

    public int getStopPrice() {
        return stopPrice;
    }
}
