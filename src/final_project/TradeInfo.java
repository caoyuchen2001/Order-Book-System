package final_project;



/**
 * TradeInfo represents a executed trade result.
 * It stores details of the trade, including the order ID, order side ("ask" or "bid"),
 * order type ("limit", "market", or "stop"), size, price, and timestamp.
 *
 * This class is used for recording, persisting, and notifying users about trade results.
 */

public class TradeInfo {
	
    private int orderId;
    private String type;        // "ask" or "bid"
    private String orderType;   // "limit", "market", "stop"
    private int size;
    private int price;
    private long timestamp;

    public TradeInfo(int orderId, String type, String orderType, int size, int price, long timestamp) {
    	
        this.orderId = orderId;
        this.type = type;
        this.orderType = orderType;
        this.size = size;
        this.price = price;
        this.timestamp = timestamp;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getType() {
        return type;
    }

    public String getOrderType() {
        return orderType;
    }
    public void setOrderType(String s) {
         orderType = s;
    }

    public int getSize() {
        return size;
    }

    public int getPrice() {
        return price;
    }

    public long getTimestamp() {
        return timestamp;
    }
}


