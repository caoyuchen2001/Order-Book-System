package final_project;


//Order base class (abstract class)
public abstract class Order {
 protected int orderId;       
 protected String username;    
 protected String type;        
 protected int size;      
 protected long timestamp;     
 protected String orderType;

 public Order(int orderId, String username, String type, int size, long timestamp, String orderType) {
     this.orderId = orderId;
     this.username = username;
     this.type = type;
     this.size = size;
     this.timestamp = timestamp;
     this.orderType = orderType;
 }
 
 public int getOrderId() {
     return orderId;
 }
 
 public String getUsername() {
     return username;
 }
 
 public String getType() {
     return type;
 }
 
 public int getSize() {
     return size;
 }
 
 public long getTimestamp() {
     return timestamp;
 }
 
 public String getOrderType() {
     return orderType;
 }
 
 public void reduceSize(int i) {
	 this.size -=i;
 }
 public void setSize(int i) {
	 this.size=i;
 }
 
 @Override
 public boolean equals(Object obj) {
     if (this == obj) return true;
     if (obj == null || getClass() != obj.getClass()) return false;
     Order order = (Order) obj;
     return this.orderId == order.orderId;
 }
}

