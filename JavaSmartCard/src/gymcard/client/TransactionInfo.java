package gymcard.client;

/**
 * Transaction information model
 */
public class TransactionInfo {
    public String date;
    public String time;
    public short amount;
    public byte type; // 1=Add, 2=Deduct
    
    public TransactionInfo() {
        this.date = "";
        this.time = "";
        this.amount = 0;
        this.type = 0;
    }
    
    public String getTypeName() {
        switch (type) {
            case 1: return "Nạp tiền";
            case 2: return "Thanh toán";
            default: return "Không rõ";
        }
    }
    
    @Override
    public String toString() {
        return "TransactionInfo{" +
                "date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", amount=" + amount +
                ", type=" + getTypeName() +
                '}';
    }
}
