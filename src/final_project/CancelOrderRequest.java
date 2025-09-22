package final_project;

/**
 * CancelOrderRequest
 * 
 * {
 *   "operation": "cancelOrder",
 *   "values": {
 *       "orderId": <NUMBER>
 *   }
 * }
 */
public class CancelOrderRequest {
    private String operation;
    private Values values;

    public CancelOrderRequest(String operation, Values values) {
        this.operation = operation;
        this.values = values;
    }

    public String getOperation() {
        return operation;
    }

    public Values getValues() {
        return values;
    }

    public static class Values {
        private int orderId;

        public Values(int orderId) {
            this.orderId = orderId;
        }

        public int getOrderId() {
            return orderId;
        }
    }
}

