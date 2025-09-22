package final_project;

/**
 * InsertLimitOrderRequest
 *
 * Example JSON request:
 * 
 * {
 *   "operation": "insertLimitOrder",
 *   "values": {
 *     "type": "bid",
 *     "size": 100,
 *     "limitPrice": 105
 *   }
 * }
 */

public class InsertLimitOrderRequest {
    private String operation;
    private Values values;
    
    public InsertLimitOrderRequest(String operation, Values values) {
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
        private int limitPrice;
        
        public Values(String type, int size, int limitPrice) {
            this.type = type;
            this.size = size;
            this.limitPrice = limitPrice;
        }
        
        public String getType() {
            return type;
        }
        
        public int getSize() {
            return size;
        }
        
        public int getlimitPrice() {
            return limitPrice;
        }
    }
}