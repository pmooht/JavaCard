package gymcard.client;

/**
 * Một entry trong lịch sử check-in
 * Mỗi entry đại diện cho 1 ngày tập gym
 */
public class CheckInLogEntry {
    public String date; // dd/MM/yyyy (10 chars)
    public String checkInTime; // HH:mm (5 chars) - lần check-in đầu tiên
    public String checkOutTime; // HH:mm (5 chars) - lần check-out cuối cùng
    public int totalMinutes; // Tổng thời gian tập trong ngày

    public CheckInLogEntry() {
        this.date = "";
        this.checkInTime = "";
        this.checkOutTime = "";
        this.totalMinutes = 0;
    }

    public CheckInLogEntry(String date, String checkInTime, String checkOutTime, int totalMinutes) {
        this.date = date != null ? date : "";
        this.checkInTime = checkInTime != null ? checkInTime : "";
        this.checkOutTime = checkOutTime != null ? checkOutTime : "";
        this.totalMinutes = totalMinutes;
    }

    /**
     * Serialize thành String để lưu trên thẻ
     * Format: date|inTime|outTime|minutes
     */
    public String serialize() {
        return date + "|" + checkInTime + "|" + checkOutTime + "|" + totalMinutes;
    }

    /**
     * Parse từ String đọc từ thẻ
     */
    public static CheckInLogEntry parse(String str) {
        if (str == null || str.isEmpty())
            return null;
        String[] parts = str.split("\\|");
        if (parts.length < 4)
            return null;

        CheckInLogEntry entry = new CheckInLogEntry();
        entry.date = parts[0];
        entry.checkInTime = parts[1];
        entry.checkOutTime = parts[2];
        try {
            entry.totalMinutes = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            entry.totalMinutes = 0;
        }
        return entry;
    }

    /**
     * Lấy thời gian dạng text
     */
    public String getTotalTimeText() {
        if (totalMinutes <= 0)
            return "0 phút";
        int hours = totalMinutes / 60;
        int mins = totalMinutes % 60;
        if (hours == 0)
            return mins + " phút";
        if (mins == 0)
            return hours + " giờ";
        return hours + " giờ " + mins + " phút";
    }

    @Override
    public String toString() {
        return "CheckInLogEntry{date='" + date + "', in='" + checkInTime +
                "', out='" + checkOutTime + "', mins=" + totalMinutes + "}";
    }
}
