package gymcard.client;

/**
 * Package information model
 */
public class PackageInfo {
    public byte type; // 1=Monthly, 2=Session, 3=VIP
    public String expiry;
    public String registration;
    public short remainingSessions;
    
    public PackageInfo() {
        this.type = 0;
        this.expiry = "";
        this.registration = "";
        this.remainingSessions = 0;
    }
    
    public String getPackageTypeName() {
        switch (type) {
            case 1: return "Gói Tháng";
            case 2: return "Gói Theo Buổi";
            case 3: return "Gói VIP";
            default: return "Chưa có gói";
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
