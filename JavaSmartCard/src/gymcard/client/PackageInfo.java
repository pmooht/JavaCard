package gymcard.client;

/**
 * Package information model
 * Hỗ trợ gói theo ngày và gói theo lượt với giới hạn thời gian
 */
public class PackageInfo {
    public byte type; // 0=chưa có, 1+ = ID gói trong database
    public String expiry; // Ngày hết hạn dd/MM/yyyy
    public String registration; // Ngày đăng ký dd/MM/yyyy
    public int remainingSessions; // Số buổi còn lại (gói theo lượt)
    public int maxDurationMinutes; // Thời lượng tối đa/buổi (phút), 0 = không giới hạn
    public int usedMinutesToday; // Số phút đã tập hôm nay
    public String packageName; // Tên gói (lưu từ database khi mua)

    public PackageInfo() {
        this.type = 0;
        this.expiry = "";
        this.registration = "";
        this.remainingSessions = 0;
        this.maxDurationMinutes = 0;
        this.usedMinutesToday = 0;
        this.packageName = "";
    }

    public String getPackageTypeName() {
        // Return stored package name if available
        if (packageName != null && !packageName.isEmpty()) {
            return packageName;
        }
        // Fallback to generic names
        if (type > 0) {
            return "Gói Tập #" + type;
        }
        return "Chưa có gói";
    }

    /**
     * Kiểm tra gói có giới hạn thời gian không
     */
    public boolean hasTimeLimit() {
        return maxDurationMinutes > 0;
    }

    /**
     * Kiểm tra đã vượt thời gian chưa
     */
    public boolean isOvertime() {
        return hasTimeLimit() && usedMinutesToday > maxDurationMinutes;
    }

    /**
     * Lấy số phút còn lại trong buổi
     */
    public int getRemainingMinutes() {
        if (!hasTimeLimit())
            return Integer.MAX_VALUE;
        return Math.max(0, maxDurationMinutes - usedMinutesToday);
    }

    /**
     * Lấy thời lượng tối đa dạng text
     */
    public String getMaxDurationText() {
        if (maxDurationMinutes <= 0) {
            return "Không giới hạn";
        }
        int hours = maxDurationMinutes / 60;
        int mins = maxDurationMinutes % 60;
        if (mins == 0) {
            return hours + " giờ";
        }
        return hours + "h" + mins + "p";
    }

    @Override
    public String toString() {
        return "PackageInfo{" +
                "type=" + getPackageTypeName() +
                ", expiry='" + expiry + '\'' +
                ", registration='" + registration + '\'' +
                ", remainingSessions=" + remainingSessions +
                ", maxDuration=" + maxDurationMinutes + "min" +
                ", usedToday=" + usedMinutesToday + "min" +
                '}';
    }
}
