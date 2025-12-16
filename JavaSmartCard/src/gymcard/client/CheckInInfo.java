package gymcard.client;

/**
 * Check-in information model
 */
public class CheckInInfo {
    public String date;
    public String checkInTime;
    public String checkOutTime;
    public int count; // Số lần check-in trong tháng

    public CheckInInfo() {
        this.date = "";
        this.checkInTime = "";
        this.checkOutTime = "";
        this.count = 0;
    }

    @Override
    public String toString() {
        return "CheckInInfo{" +
                "date='" + date + '\'' +
                ", checkInTime='" + checkInTime + '\'' +
                ", checkOutTime='" + checkOutTime + '\'' +
                ", count=" + count +
                '}';
    }
}
