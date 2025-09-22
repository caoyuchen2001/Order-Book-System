package final_project;

/**
 * A thread-safe wrapper class for a mutable string value.
 * 
 */
public class MutableString {
    private String value=null;
    
    
    public MutableString(String value) {
        this.value = value;
    }
    

	public synchronized String getValue() {
        return value;
    }
    
    public synchronized void setValue(String newValue) {
        this.value = newValue;
    }
    
    @Override
    public String toString() {
        return value;
    }
}