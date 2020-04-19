/**
 *
 * @author: Palash Jain
 *
 * @version: 1.0
 */

/**
 * This POJO class represent a row in the routing table.
 */

public class TableEntry {
    private String address;
    private String nextHop;
    private int cost;
    private long time;

    public TableEntry(String address, String nextHop, int cost, long time) {
        this.address = address;
        this.nextHop = nextHop;
        this.cost = cost;
        this.time = time;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNextHop() {
        return nextHop;
    }

    public void setNextHop(String nextHop) {
        this.nextHop = nextHop;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
