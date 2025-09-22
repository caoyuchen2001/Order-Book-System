package final_project;


// market order class
public class MarketOrder extends Order {
    public MarketOrder(int orderId, String username, String type, int size, long timestamp) {
        super(orderId, username, type, size, timestamp, "market");
    }
}
