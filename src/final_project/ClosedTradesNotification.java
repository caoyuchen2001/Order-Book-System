package final_project;

import java.util.List;

/**
 * Represents a UDP notification message for closed trades.
 *
 * Example JSON structure:
 * {
 *   "notification": "closedTrades",
 *   "trades": [
 *     {
 *       "orderId": 123,
 *       "type": "ask",
 *       "orderType": "limit",
 *       "size": 100,
 *       "price": 50,
 *       "timestamp": 1745273695711
 *     },
 *     ...
 *   ]
 * }
 */

public class ClosedTradesNotification {
    private String notification = "closedTrades";
    private List<TradeInfo> trades;

    public ClosedTradesNotification(List<TradeInfo> trades) {
        this.trades = trades;
    }

    public List<TradeInfo> getTrades() {
        return trades;
    }
    
    public String getNotification() {
    	return notification;
    	
    }
}

