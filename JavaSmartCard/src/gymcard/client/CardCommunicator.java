package gymcard.client;

import gymcard.CardManager.CardIdGenerator;
import gymcard.CardManager.CardManager;

/**
 * Card Communicator - Nói chuyện với thẻ JavaCard thật
 * Một số chức năng (check-in, balance, transaction) vẫn còn giả lập trong RAM,
 * nhưng yêu cầu phải kết nối & xác thực PIN.
 */
public class CardCommunicator {

    // Giới hạn kích thước field (phải khớp với applet)
    private static final int NAME_MAX_LEN    = 64;
    private static final int DOB_MAX_LEN     = 16;
    private static final int PHONE_MAX_LEN   = 16;
    private static final int ADDRESS_MAX_LEN = 128;
    private static final int AVATAR_MAX_LEN  = 192; // khớp AVATAR_LEN trên thẻ

    // Trạng thái kết nối & xác thực
    private boolean connected;
    private boolean authenticated;

    // Làm việc với thẻ thật
    private CardManager cardManager;

    // Simulated member state (nhưng dữ liệu core sẽ được sync với thẻ)
    private MemberInfo memberInfo;
    private PackageInfo packageInfo;
    private CheckInInfo lastCheckIn;
    private short balance;
    private int checkInCount;
    private final TransactionInfo[] transactions;
    private int transactionCount;

    public CardCommunicator() {
        connected = false;
        authenticated = false;

        balance = 0;
        checkInCount = 0;
        transactions = new TransactionInfo[10];
        transactionCount = 0;

        initializeDefaultData();
    }

    /**
     * Initialize default in-memory data
     */
    private void initializeDefaultData() {
        memberInfo = new MemberInfo();
        memberInfo.name = "";
        memberInfo.birthDate = "";
        memberInfo.phone = "";
        memberInfo.address = "";
        memberInfo.avatarBytes = null;
        // nếu bạn có field avatarBytes trong MemberInfo thì nhớ set null ở đây

        packageInfo = new PackageInfo();
        packageInfo.type = 0;
        packageInfo.expiry = "";
        packageInfo.registration = "";
        packageInfo.remainingSessions = 0;

        lastCheckIn = new CheckInInfo();
        lastCheckIn.date = "";
        lastCheckIn.checkInTime = "";
        lastCheckIn.checkOutTime = "";
    }

    // ---------------------------------------------------------------------
    // KẾT NỐI / NGẮT KẾT NỐI
    // ---------------------------------------------------------------------

    /**
     * Kết nối tới đầu đọc + thẻ thật
     */
    public void connect() throws Exception {
        if (connected) return;

        cardManager = new CardManager();
        cardManager.connect(); // bên trong: TerminalFactory, SELECT applet

        connected = true;
        authenticated = false;

        System.out.println("[CARD] Connected to real JavaCard");
    }

    /**
     * Ngắt kết nối
     */
    public void disconnect() throws Exception {
        if (!connected) return;

        try {
            cardManager.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        connected = false;
        authenticated = false;

        System.out.println("[CARD] Disconnected");
    }

    // ---------------------------------------------------------------------
    // KHỞI TẠO THẺ MỚI (INIT_CARD) - dùng cho AdminPanel.registerMember()
    // ---------------------------------------------------------------------

    /**
     * Khởi tạo thẻ mới:
     *  - Sinh CardID tự động (GYM000001, GYM000002, ...)
     *  - Gửi APDU INIT_CARD(cardId, pin) xuống thẻ
     *  - Trên thẻ: set PIN mới + sinh masterKey từ PIN
     *
     * @param pin PIN 6 chữ số do admin nhập
     * @return CardID đã gán cho thẻ (để hiển thị cho admin / lưu DB)
     */
public String initNewCard(String cardId, String userPin) throws Exception {
    if (!connected) throw new Exception("Chưa kết nối thẻ");
    if (cardId == null || cardId.isBlank()) throw new Exception("CardID rỗng");
    if (!userPin.matches("\\d{6}")) throw new Exception("PIN phải 6 số");

    String adminPin = "123456";
    cardManager.initCard(cardId, userPin, adminPin);

    System.out.println("[CARD] triesAfterInit=" + (cardManager.getTriesRemaining() & 0xFF));

    authenticated = false;
    initializeDefaultData();
    return cardId;
}

    /**
     * Kiểm tra đã kết nối thẻ chưa
     */
    public boolean isConnected() {
        return connected;
    }

    // ---------------------------------------------------------------------
    // PIN / BẢO MẬT
    // ---------------------------------------------------------------------

    /**
     * Verify PIN với thẻ (APDU INS_VERIFY_PIN)
     */
    public boolean verifyPin(String pin) throws Exception {
        if (!connected) {
            throw new Exception("Chưa kết nối thẻ");
        }
        if (pin == null || pin.length() != 6) {
            throw new Exception("PIN phải gồm đúng 6 ký tự");
        }

        try {
            cardManager.verifyPin(pin);   // nếu sai sẽ ném RuntimeException với SW != 9000
            authenticated = true;
            System.out.println("[CARD] PIN verified OK");
            return true;
        } catch (RuntimeException ex) {
            // card đã tự trừ số lần thử trong OwnerPIN
            authenticated = false;
            System.out.println("[CARD] PIN verify fail: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Đổi PIN: old -> new (INS_CHANGE_PIN)
     */
    public boolean changePin(String oldPin, String newPin) throws Exception {
        if (!connected) throw new Exception("Chưa kết nối thẻ");
        if (!authenticated) throw new Exception("Chưa xác thực PIN");

        if (oldPin == null || newPin == null || oldPin.length() != 6 || newPin.length() != 6) {
            throw new Exception("PIN phải 6 ký tự");
        }

        try {
            cardManager.changePin(oldPin, newPin);
            System.out.println("[CARD] PIN changed successfully");
            return true;
        } catch (RuntimeException ex) {
            System.out.println("[CARD] Change PIN failed: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Mở khóa thẻ bằng mật khẩu admin ("ADMIN" theo applet)
     */
    public boolean unlockPin(String adminPass) throws Exception {
        if (!connected) throw new Exception("Chưa kết nối thẻ");

        try {
            cardManager.unlockByAdmin(adminPass);
            authenticated = false;
            System.out.println("[CARD] Card unlocked by admin");
            return true;
        } catch (RuntimeException ex) {
            System.out.println("[CARD] Unlock failed: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Số lần nhập PIN còn lại trên thẻ
     */
    public int getPinTries() throws Exception {
        if (!connected) throw new Exception("Chưa kết nối thẻ");

        byte tries = cardManager.getTriesRemaining();
        return tries & 0xFF;
    }

    /**
     * Admin đổi PIN cho hội viên khi hội viên quên PIN.
     * Chỉ cần mật khẩu admin + PIN mới.
     */
    public boolean adminResetMemberPin(String adminPass, String newPin) throws Exception {
        if (!connected) throw new Exception("Chưa kết nối thẻ");
        // KHÔNG yêu cầu authenticated, vì applet tự kiểm tra adminPass

        if (newPin == null || newPin.length() != 6) {
            throw new Exception("PIN mới phải 6 ký tự");
        }
        if (adminPass == null || adminPass.isEmpty()) {
            throw new Exception("Mật khẩu admin không được rỗng");
        }

        try {
            cardManager.adminSetUserPin(adminPass, newPin);
            System.out.println("[CARD] Admin reset member PIN OK");
            // Sau khi đổi PIN, masterKey AES trên thẻ đã đổi theo PIN mới.
            // Ở phía client không cần làm gì thêm.
            return true;
        } catch (RuntimeException ex) {
            System.out.println("[CARD] Admin reset member PIN FAIL: " + ex.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------------------
    // VALIDATE INPUT & CHUYỂN UTF-8 + GIỚI HẠN ĐỘ DÀI
    // ---------------------------------------------------------------------

    private byte[] toUtf8AndLimit(String value, int maxBytes, String fieldName) throws Exception {
        if (value == null) {
            return null;
        }
        byte[] bytes = value.getBytes("UTF-8");
        if (bytes.length > maxBytes) {
            throw new Exception(String.format(
                    "%s quá dài (tối đa %d bytes UTF-8, hiện tại %d)",
                    fieldName, maxBytes, bytes.length));
        }
        return bytes;
    }

    private void validateBirthDate(String birthDate) throws Exception {
        if (birthDate == null || birthDate.trim().isEmpty()) {
            throw new Exception("Ngày sinh không được để trống");
        }
        if (!birthDate.matches("\\d{2}/\\d{2}/\\d{4}")) {
            throw new Exception("Ngày sinh phải theo định dạng dd/MM/yyyy (ví dụ: 01/10/2000)");
        }
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);
        try {
            sdf.parse(birthDate);
        } catch (java.text.ParseException e) {
            throw new Exception("Ngày sinh không hợp lệ (ngày/tháng không tồn tại)");
        }
    }

    private void validatePhone(String phone) throws Exception {
        if (phone == null || phone.trim().isEmpty()) {
            throw new Exception("Số điện thoại không được để trống");
        }
        String normalized = phone.trim().replaceAll("\\s+", "");
        // Ví dụ: 0xxxxxxxxx (10–11 số)
        if (!normalized.matches("0\\d{9,10}")) {
            throw new Exception("Số điện thoại không hợp lệ (phải bắt đầu bằng 0, dài 10–11 số)");
        }
    }

    // ---------------------------------------------------------------------
    // THÔNG TIN HỘI VIÊN – map với các FIELD trên thẻ
    // ---------------------------------------------------------------------

    /**
     * Ghi thông tin người dùng xuống thẻ (kèm avatar):
     * - name → FIELD_NAME
     * - birthDate → FIELD_DOB
     * - phone → FIELD_PHONE
     * - address → FIELD_ADDRESS
     * - avatarBytes → FIELD_AVATAR
     *
     * avatarBytes là byte[] đã nén sẵn ở UI (ví dụ JPEG chất lượng thấp + resize).
     */
    public boolean setMemberInfo(String name,
                                 String birthDate,
                                 String phone,
                                 String address,
                                 byte[] avatarBytes)
            throws Exception {

        if (!connected || !authenticated) {
            throw new Exception("Chưa xác thực PIN");
        }

        // Validate logic
        if (name == null || name.trim().isEmpty()) {
            throw new Exception("Họ và tên không được để trống");
        }
        validateBirthDate(birthDate);
        validatePhone(phone);

        if (address != null && address.trim().isEmpty()) {
            address = null;
        }

        // Chuyển UTF-8 + giới hạn độ dài
        byte[] nameBytes    = toUtf8AndLimit(name.trim(),      NAME_MAX_LEN,    "Họ và tên");
        byte[] dobBytes     = toUtf8AndLimit(birthDate.trim(), DOB_MAX_LEN,     "Ngày sinh");
        byte[] phoneBytes   = toUtf8AndLimit(phone.trim(),     PHONE_MAX_LEN,   "Số điện thoại");
        byte[] addressBytes = (address != null)
                ? toUtf8AndLimit(address.trim(), ADDRESS_MAX_LEN, "Địa chỉ")
                : null;

        // Avatar: đã nén sẵn ở UI, ở đây chỉ check size
        if (avatarBytes != null && avatarBytes.length > AVATAR_MAX_LEN) {
            throw new Exception("Ảnh đại diện sau khi nén vẫn quá lớn (>"
                    + AVATAR_MAX_LEN + " bytes)");
        }

        // Ghi xuống thẻ (thẻ tự AES bên trong)
        if (nameBytes != null) {
            cardManager.writeField(CardManager.FIELD_NAME, nameBytes);
            memberInfo.name = name;
        }
        if (dobBytes != null) {
            cardManager.writeField(CardManager.FIELD_DOB, dobBytes);
            memberInfo.birthDate = birthDate;
        }
        if (phoneBytes != null) {
            cardManager.writeField(CardManager.FIELD_PHONE, phoneBytes);
            memberInfo.phone = phone;
        }
        if (addressBytes != null) {
            cardManager.writeField(CardManager.FIELD_ADDRESS, addressBytes);
            memberInfo.address = address;
        }
        if (avatarBytes != null) {
            cardManager.writeField(CardManager.FIELD_AVATAR, avatarBytes);
                memberInfo.avatarBytes = avatarBytes; // thêm dòng này
            // nếu MemberInfo có field avatarBytes:
            // memberInfo.avatarBytes = avatarBytes;
        }

        System.out.println("[CARD] Member info written to card (with avatar)");
        return true;
    }

    /**
     * Overload cũ: không truyền avatar -> avatar = null
     * Để bạn khỏi phải sửa quá nhiều code đang dùng hàm này.
     */
    public boolean setMemberInfo(String name,
                                 String birthDate,
                                 String phone,
                                 String address)
            throws Exception {
        return setMemberInfo(name, birthDate, phone, address, null);
    }

    /**
     * Đọc thông tin hội viên từ thẻ
     */
public MemberInfo getMemberInfo() throws Exception {
    if (!connected || !authenticated) {
        throw new Exception("Chưa xác thực PIN");
    }

    MemberInfo info = new MemberInfo();

    byte[] nameBytes  = cardManager.readField(CardManager.FIELD_NAME);
    byte[] dobBytes   = cardManager.readField(CardManager.FIELD_DOB);
    byte[] phoneBytes = cardManager.readField(CardManager.FIELD_PHONE);
    byte[] addrBytes  = cardManager.readField(CardManager.FIELD_ADDRESS);

    // ✅ đọc avatar
    byte[] avatarBytes = null;
    try {
        avatarBytes = cardManager.readField(CardManager.FIELD_AVATAR);
        // nếu thẻ chưa có avatar thì thường toàn 0x00 hoặc length=0
        if (avatarBytes != null) {
            boolean allZero = true;
            for (byte b : avatarBytes) {
                if (b != 0x00) { allZero = false; break; }
            }
            if (allZero) avatarBytes = null;
        }
    } catch (Exception ignore) {
        // tùy bạn: nếu applet chưa hỗ trợ/field không có thì bỏ qua
        avatarBytes = null;
    }

    info.name      = new String(nameBytes,  "UTF-8");
    info.birthDate = new String(dobBytes,   "UTF-8");
    info.phone     = new String(phoneBytes, "UTF-8");
    info.address   = new String(addrBytes,  "UTF-8");
    info.avatarBytes = avatarBytes; // ✅ gán

    this.memberInfo = info;
    return info;
}


    // ---------------------------------------------------------------------
    // GÓI TẬP – mã hóa đơn giản vào 1 field trên thẻ (FIELD_PACKAGE)
    // ---------------------------------------------------------------------

    /**
     * Set gói tập:
     * - type: 0=chưa, 1=tháng, 2=buổi, 3=VIP...
     * - expiry, registration: dd/MM/yyyy
     * - sessions: số buổi còn lại (nếu gói theo buổi)
     *
     * Lưu xuống thẻ dạng string ngắn:
     *   "type|expiry|registration|sessions"
     * rồi writeField(FIELD_PACKAGE, bytes).
     */
    public boolean setPackage(byte type, String expiry, String registration, short sessions)
            throws Exception {

        if (!connected || !authenticated) {
            throw new Exception("Chưa xác thực PIN");
        }

        String packStr = (type & 0xFF) + "|" +
                (expiry == null ? "" : expiry) + "|" +
                (registration == null ? "" : registration) + "|" +
                sessions;

        byte[] data = packStr.getBytes("UTF-8");
        cardManager.writeField(CardManager.FIELD_PACKAGE, data);

        packageInfo.type = type;
        packageInfo.expiry = expiry;
        packageInfo.registration = registration;
        packageInfo.remainingSessions = sessions;

        System.out.println("[CARD] Package info written: " + packStr);
        return true;
    }

    /**
     * Đọc gói tập từ thẻ
     */
    public PackageInfo getPackage() throws Exception {
        if (!connected || !authenticated) {
            throw new Exception("Chưa xác thực PIN");
        }

        byte[] data = cardManager.readField(CardManager.FIELD_PACKAGE);
        String packStr = new String(data, "UTF-8");
        // Format: type|expiry|registration|sessions
        String[] parts = packStr.split("\\|");
        PackageInfo p = new PackageInfo();

        if (parts.length >= 1) {
            try {
                p.type = (byte) Integer.parseInt(parts[0]);
            } catch (NumberFormatException e) {
                p.type = 0;
            }
        }
        if (parts.length >= 2) {
            p.expiry = parts[1];
        }
        if (parts.length >= 3) {
            p.registration = parts[2];
        }
        if (parts.length >= 4) {
            try {
                p.remainingSessions = (short) Integer.parseInt(parts[3]);
            } catch (NumberFormatException e) {
                p.remainingSessions = 0;
            }
        }

        this.packageInfo = p;
        return p;
    }

    // ---------------------------------------------------------------------
    // CHECK-IN / CHECK-OUT / BALANCE / TRANSACTION (mock trong RAM)
    // ---------------------------------------------------------------------

    public boolean checkIn(String date, String time) throws Exception {
        if (!connected || !authenticated) {
            throw new Exception("Chưa xác thực PIN");
        }

        lastCheckIn.date = date;
        lastCheckIn.checkInTime = time;
        lastCheckIn.checkOutTime = ""; // Clear checkout

        checkInCount++;

        // Trừ buổi nếu gói theo buổi (type == 2)
        if (packageInfo.type == 2 && packageInfo.remainingSessions > 0) {
            packageInfo.remainingSessions--;
        }

        System.out.println("[MOCK] Checked in at " + time);
        return true;
    }

    public boolean checkOut(String time) throws Exception {
        if (!connected || !authenticated) {
            throw new Exception("Chưa xác thực PIN");
        }

        lastCheckIn.checkOutTime = time;
        System.out.println("[MOCK] Checked out at " + time);
        return true;
    }

    public int getCheckInCount() throws Exception {
        if (!connected || !authenticated) {
            throw new Exception("Chưa xác thực PIN");
        }
        return checkInCount;
    }

    public CheckInInfo getLastCheckIn() throws Exception {
        if (!connected || !authenticated) {
            throw new Exception("Chưa xác thực PIN");
        }
        return lastCheckIn;
    }

    public short getBalance() throws Exception {
        if (!connected || !authenticated) {
            throw new Exception("Chưa xác thực PIN");
        }
        return balance;
    }

    public boolean addBalance(short amount) throws Exception {
        if (!connected || !authenticated) {
            throw new Exception("Chưa xác thực PIN");
        }

        balance += amount;
        recordTransaction((byte) 1, amount);

        System.out.println("[MOCK] Add balance: " + amount + "k, new=" + balance);
        return true;
    }

    public boolean deductBalance(short amount) throws Exception {
        if (!connected || !authenticated) {
            throw new Exception("Chưa xác thực PIN");
        }

        if (balance < amount) {
            System.out.println("[MOCK] Not enough balance");
            return false;
        }

        balance -= amount;
        recordTransaction((byte) 2, amount);

        System.out.println("[MOCK] Deduct balance: " + amount + "k, new=" + balance);
        return true;
    }

    public TransactionInfo getTransaction(byte index) throws Exception {
        if (!connected || !authenticated) {
            throw new Exception("Chưa xác thực PIN");
        }

        if (index < 0 || index >= transactionCount) return null;
        return transactions[index];
    }

    private void recordTransaction(byte type, short amount) {
        if (transactionCount >= 10) {
            for (int i = 1; i < 10; i++) {
                transactions[i - 1] = transactions[i];
            }
            transactionCount = 9;
        }

        TransactionInfo trans = new TransactionInfo();
        java.util.Date now = new java.util.Date();
        trans.date = new java.text.SimpleDateFormat("dd/MM/yyyy").format(now);
        trans.time = new java.text.SimpleDateFormat("HH:mm:ss").format(now);
        trans.amount = amount;
        trans.type = type;

        transactions[transactionCount++] = trans;
    }

    // ---------------------------------------------------------------------
    // TIỆN ÍCH RESET / DEMO (TÙY BẠN CÓ DÙNG HAY KHÔNG)
    // ---------------------------------------------------------------------

    public void resetCardMockState() {
        authenticated = false;
        balance = 0;
        checkInCount = 0;
        transactionCount = 0;
        initializeDefaultData();
        System.out.println("[MOCK] Reset in-memory state");
    }

    public void loadDemoDataLocal() {
        memberInfo.name = "Nguyễn Văn Demo";
        memberInfo.birthDate = "01/01/1990";
        memberInfo.phone = "0987654321";
        memberInfo.address = "123 Đường ABC, Quận XYZ, Hà Nội";

        packageInfo.type = 2;
        packageInfo.expiry = "31/12/2026";
        packageInfo.registration = "01/01/2025";
        packageInfo.remainingSessions = 50;

        lastCheckIn.date = "29/11/2025";
        lastCheckIn.checkInTime = "08:00:00";
        lastCheckIn.checkOutTime = "10:00:00";

        balance = 500;
        checkInCount = 15;

        for (int i = 0; i < 5; i++) {
            recordTransaction((byte) (i % 2 + 1), (short) (50 + i * 10));
        }

        System.out.println("[MOCK] Local demo data loaded (RAM only)");
    }
}
