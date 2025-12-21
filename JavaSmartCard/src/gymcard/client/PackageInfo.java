package gymcard.client;

/**
 * Package information model
 */
public class PackageInfo {
    public byte type; // 1=Day-based package (15, 30, 60, 90 days)
    public String expiry;
    public String registration;
    public short remainingSessions; // Kept for backward compatibility but not used

    public PackageInfo() {
        this.type = 0;
        this.expiry = "";
        this.registration = "";
        this.remainingSessions = 0;
    }

    public String getPackageTypeName() {
        switch (type) {
            case 1:
                return "Gói Ngày";
            default:
                return "Chưa có gói";
        }
    }

    @Override
    public String toString() {
        return "PackageInfo{" +
                "type=" + getPackageTypeName() +
                ", expiry='" + expiry + '\'' +
                ", registration='" + registration + '\'' +
                ", remainingSessions=" + remainingSessions +
                '}';
    }
}
