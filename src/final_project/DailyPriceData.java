package final_project;




/**
 * DailyPriceData keeps track of the daily price statistics for trades.
 * It calculates and stores the following price information for a single day:
 * 
 * 
 * 1. Open price: the price of the first trade (based on the earliest timestamp).
 * 2. Close price: the price of the last trade (based on the latest timestamp).
 * 3. High price: the highest price among all trades of the day.
 * 4. Low price: the lowest price among all trades of the day.
 *
 * The addTrade() method should be called for each trade to update these values.
 * 
 * Example usage:
 * DailyPriceData data = new DailyPriceData();
 * data.addTrade(100, 1649991111000L);  // Adds a trade with price 100 and its timestamp.
 * 
 * This class is used to prepare price history data (for getPriceHistory operation).
 */
public class DailyPriceData {
	
    private Integer open;
    private Integer close;
    private Integer high;
    private Integer low;
    private long firstTimestamp = Long.MAX_VALUE;
    private long lastTimestamp = Long.MIN_VALUE;

    public void addTrade(int price, long timestamp) {
        if (timestamp < firstTimestamp) {
            firstTimestamp = timestamp;
            open = price;
        }
        if (timestamp > lastTimestamp) {
            lastTimestamp = timestamp;
            close = price;
        }

        if (high == null || price > high) {
            high = price;
        }
        if (low == null || price < low) {
            low = price;
        }
    }

    // Getters
    public int getOpen() { return open; }
    public int getClose() { return close; }
    public int getHigh() { return high; }
    public int getLow() { return low; }
    
    @Override
    public String toString() {
        return String.format("{open=%d, close=%d, high=%d, low=%d}", open, close, high, low);
    }

}

