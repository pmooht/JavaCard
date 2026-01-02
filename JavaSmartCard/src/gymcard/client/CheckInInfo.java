package gymcard.client;

/**
 * Check-in information model
 * Hỗ trợ ra vào nhiều lần trong ngày với cộng dồn thời gian
 */
public class CheckInInfo {
    public String date; // Ngày tập dd/MM/yyyy
    public String checkInTime; // Giờ check-in gần nhất HH:mm:ss
    public String checkOutTime; // Giờ check-out gần nhất HH:mm:ss
    public int count; // Số buổi đã tập (tổng)
    public boolean isCheckedIn; // Đang trong phòng tập?
    public int totalMinutesToday; // Tổng phút đã tập hôm nay

    public CheckInInfo() {
        this.date = "";
        this.checkInTime = "";
        this.checkOutTime = "";
        this.count = 0;
        this.isCheckedIn = false;
        this.totalMinutesToday = 0;
    }

    /**
     * Lấy thời gian đã tập dạng text
     */
    public String getTotalTimeText() {
        if (totalMinutesToday <= 0)
            return "0 phút";
        int hours = totalMinutesToday / 60;
        int mins = totalMinutesToday % 60;
        if (hours == 0) {
            return mins + " phút";
        }
        if (mins == 0) {
            return hours + " giờ";
        }
        return hours + " giờ " + mins + " phút";
    }

    /**
     * Lấy trạng thái hiện tại
     */
    public String getStatusText() {
        if (isCheckedIn) {
            return "Đang trong phòng tập";
        } else if (!checkOutTime.isEmpty() && !checkOutTime.equals("--:--:--")) {
            return "Đã rời phòng tập";
        }
        return "Chưa check-in hôm nay";
    }

    @Override
    public String toString() {
        return "CheckInInfo{" +
                "date='" + date + '\'' +
                ", checkInTime='" + checkInTime + '\'' +
                ", checkOutTime='" + checkOutTime + '\'' +
                ", count=" + count +
                ", isCheckedIn=" + isCheckedIn +
                ", totalMinutesToday=" + totalMinutesToday +
                '}';
    }
}
