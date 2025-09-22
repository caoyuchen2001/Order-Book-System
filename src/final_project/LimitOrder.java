package final_project;

/**
 * limit order class
 */
public class LimitOrder extends Order {
    private int limitPrice;

    public LimitOrder(int orderId, String username, String type, int size, long timestamp, int limitPrice) {
        super(orderId, username, type, size, timestamp, "limit");
        this.limitPrice = limitPrice;
    }

    public int getLimitPrice() {
        return limitPrice;
    }

	public void setSize(int size) {
		this.size=size;
	}
    
   
}

