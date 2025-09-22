package final_project;

/**
 * InsertMarketOrderRequest
 *
 * Example JSON request:
 * {
 *   "operation": "insertMarketOrder",
 *   "values": {
 *     "type": "bid",
 *     "size": 100
 *   }
 * }
 */
public class InsertMarketOrderRequest {
    private String operation;
    private Values values;

    public InsertMarketOrderRequest(String operation, Values values) {
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
        private String type;  // ask or bid
        private int size;     // number

        public Values(String type, int size) {
            this.type = type;
            this.size = size;
        }

        public String getType() {
            return type;
        }

        public int getSize() {
            return size;
        }
    }
}


