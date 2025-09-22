package final_project;

/**
 * Login request class
 * 
 * example in JSON
 * {
 *   "operation": "login",
 *   "values": {
 *     "username": "xxx",
 *     "password": "xxx"
 *   }
 * }
 */
public class LoginRequest implements Request{
    private String operation;
    private Values values;

    public LoginRequest(String operation, Values values) {
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
