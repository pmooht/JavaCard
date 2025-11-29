package gymcard.client;

/**
 * Check-in information model
 */
public class CheckInInfo {
    public String date;
    public String checkInTime;
    public String checkOutTime;
    
    public CheckInInfo() {
        this.date = "";
        this.checkInTime = "";
        this.checkOutTime = "";
    }
    
    @Override
    public String toString() {
        return "CheckInInfo{" +
                "date='" + date + '\'' +
                ", checkInTime='" + checkInTime + '\'' +
                ", checkOutTime='" + checkOutTime + '\'' +
                '}';
    }
}
