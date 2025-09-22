package final_project;

/**
 * GetPriceHistoryRequest
 *
 * {
 *   "operation": "getPriceHistory",
 *   "values": {
 *       "month": "MMYYYY"
 *   }
 * }
 */

public class GetPriceHistoryRequest {
    private String operation;
    private Values values;

    public GetPriceHistoryRequest(String operation, Values values) {
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
        private String month; // format "MMYYYY"

        public Values(String month) {
            this.month = month;
        }

        public String getMonth() {
            return month;
        }
    }
}
