package final_project;

/**
 * LogoutRequest class
 * 
 * example in JSON
 * {
 *   "operation": "logout",
 *   "values": {}
 * }
 */
public class LogoutRequest {
    private String operation;
    private Values values;

    public LogoutRequest(String operation, Values values) {
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
        public Values() {
            ;// no need for parameter
        }
    }
}

