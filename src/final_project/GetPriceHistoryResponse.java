package final_project;

import java.util.Map;

/**
 * GetPriceHistoryResponse
 *
 * It contains:
 * 
 * 1. response: an integer status code (e.g., 100 for success, 101 for error).
 * 2. errorMessage: a string describing the error if the request failed, or "OK" if successful.
 * 3. data: a map where the key is the day of the month (e.g., "01", "02", ..., "31"),
 *         and the value is a DailyPriceData object containing open, close, high, and low prices
 *         for that day.
 *
 * Example JSON output:
 * {
 *   "response": 100,
 *   "errorMessage": "OK",
 *   "data": {
 *     "01": { "open": 100, "close": 105, "high": 110, "low": 95 },
 *     "02": { "open": 106, "close": 108, "high": 112, "low": 104 }
 *   }
 * }
 */


public class GetPriceHistoryResponse {
    private int response;
    private String errorMessage;
    private Map<String, DailyPriceData> data;

    public GetPriceHistoryResponse(int response, String errorMessage, Map<String, DailyPriceData> data) {
        this.response = response;
        this.errorMessage = errorMessage;
        this.data = data;
    }

    public int getResponse() { return response; }
    public String getErrorMessage() { return errorMessage; }
    public Map<String, DailyPriceData> getData() { return data; }
}



