package final_project;

/**
 * InsertStopOrderRequest 
 * 
 * 
 * Example JSON request:
 * {
 *   "operation": "insertStopOrder",
 *   "values": {
 *     "type": "bid",
 *     "size": 100,
 *     "stopPrice": 105
 *   }
 * }
 */
public class InsertStopOrderRequest {
    private String operation;
    private Values values;

    public InsertStopOrderRequest(String operation, Values values) {
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
        private String type;     
        private int size;         
        private int stopPrice;     

        public Values(String type, int size, int stopPrice) {
            this.type = type;
            this.size = size;
            this.stopPrice = stopPrice;
        }

        public String getType() {
            return type;
        }

        public int getSize() {
            return size;
        }

        public int getStopPrice() {
            return stopPrice;
        }
    }
}
