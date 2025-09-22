package final_project;



/**
 * Register request object, corresponding to JSON format:
 * 
 * {
 *   "operation": "register",
 *   "values": {
 *     "username": "...",
 *     "password": "..."
 *   }
 * }
 */

public class RegisterRequest implements Request {
    private String operation;
    private Values values;

   
    public RegisterRequest(String operation, Values values) {
        this.operation = operation;
        this.values = values;
    }

    public String getOperation() {
        return operation;
    }
    public Values getValues() {
        return values;
    }

    static class Values {
        private String username;
        private String password;

        public Values(String username, String password) {
            this.username = username;
            this.password = password;
        }
        public String getUsername() {
            return username;
        }
        public String getPassword() {
            return password;
        }
    }
}