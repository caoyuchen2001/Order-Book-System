package final_project;


/**
 * General operation response object, corresponding to JSON format：
 * 
 * {
 *   "orderId": NUMBER or -1
 * }
 * 
 * used to represent the responses of operations which 
 * respond with order ID or -1
 */
public class OperationResponse2 {
    private int orderId;
    public OperationResponse2(int orderId) {
        this.orderId = orderId;
    }
    public int getOrderId() {
        return orderId;
    }
}
