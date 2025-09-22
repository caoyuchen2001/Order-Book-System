package final_project;

/**
 * General operation response object, corresponding to JSON format：
 * 
 * {
 *   "response": NUMBER,
 *   "errorMessage": "..."
 * }
 * 
 * used to represent the responses of operations which 
 * respond with a code and a message
 */
public class OperationResponse1 {
    private int response;
    private String errorMessage;
    public OperationResponse1(int response, String errorMessage) {
        this.response = response;
        this.errorMessage = errorMessage;
    }
    public int getResponse() {
        return response;
    }
    public String getErrorMessage() {
        return errorMessage;
    }
}