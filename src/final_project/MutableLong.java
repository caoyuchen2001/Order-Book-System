package final_project;


/**
 * A thread-safe wrapper class for a mutable long value.
 * 
 */
public class MutableLong {
    private long value;

    public MutableLong(long value) {
        this.value = value;
    }

    public synchronized long getValue() {
        return value;
    }

    public synchronized void setValue(long value) {
        this.value = value;
    }
}
