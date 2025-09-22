package final_project;

/**
 * Update credential request object, corresponding to JSON format:
 * 
 * {
 *   "operation": "updateCredentials",
 *   "values": {
 *     "username": "xxx",
 *     "currentPassword": "xxx",
 *     "newPassword": "xxx"
 *   }
 * }
 */
public class UpdateCredentialsRequest {
    private String operation;
    private Values values;
    
    public UpdateCredentialsRequest(String operation, Values values) {
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
        private String old_password;
        private String new_password;
        
        public Values(String username, String currentPassword, String newPassword) {
            this.username = username;
            this.old_password = currentPassword;
            this.new_password = newPassword;
        }
        
        public String getUsername() {
            return username;
        }
        
        public String getCurrentPassword() {
            return old_password;
        }
        
        public String getNewPassword() {
            return new_password;
        }
    }
}
