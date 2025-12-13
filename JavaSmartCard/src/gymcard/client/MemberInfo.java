package gymcard.client;

/**
 * Member information model
 */
public class MemberInfo {
    public String name;
    public String birthDate;
    public String phone;
    public String address;
    public byte[] avatarBytes;
    
    public MemberInfo() {
        this.name = "";
        this.birthDate = "";
        this.phone = "";
        this.address = "";
    }
    
    @Override
    public String toString() {
        return "MemberInfo{" +
                "name='" + name + '\'' +
                ", birthDate='" + birthDate + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
