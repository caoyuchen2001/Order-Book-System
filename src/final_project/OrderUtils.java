package final_project;


/**
 * OrderUtils provides utility functions for working with Order objects.
 * 
 * Method copyOrder(Order order):
 *    Creates and returns a deep copy of the given Order instance (LimitOrder, MarketOrder, or StopOrder)
 *    to store in Order history.
 * 
 * Purpose:
 *    Ensures that the copied order is independent of the original object.
 *    Useful for safely store orders in OrderHistory without changing them.
 */

public class OrderUtils {
	
    public static Order copyOrder(Order order) {
        if (order == null) return null;

        String type = order.getType();
        String username = order.getUsername();
        int orderId = order.getOrderId();
        int size = order.getSize();
        long timestamp = order.getTimestamp();
        String orderType = order.getOrderType();

        switch (orderType.toLowerCase()) {
            case "limit":
                if (order instanceof LimitOrder) {
                    int limitPrice = ((LimitOrder) order).getLimitPrice();
                    return new LimitOrder(orderId, username, type, size, timestamp, limitPrice);
                }
                break;

            case "market":
                return new MarketOrder(orderId, username, type, size, timestamp);

            case "stop":
                if (order instanceof StopOrder) {
                    int stopPrice = ((StopOrder) order).getStopPrice();
                    return new StopOrder(orderId, username, type, size, timestamp, stopPrice);
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown orderType: " + orderType);
        }

        throw new IllegalArgumentException("Unsupported order copy for: " + order.getClass().getSimpleName());
    }
}

