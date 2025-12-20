package gymcard.client;

import gymcard.CardManager.CardIdGenerator;
import gymcard.CardManager.CardManager;
import gymcard.Crypto.RSAKeyUtils;
import gymcard.databaseManager.DatabaseManager;

import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;

/**
 * Card Communicator - Nói chuyện với thẻ JavaCard thật
 * Một số chức năng (check-in, balance, transaction) vẫn còn giả lập trong RAM,
 * nhưng yêu cầu phải kết nối & xác thực PIN.
 */
public class CardCommunicator {

    // Gioi han kich thuoc field (phai khop voi applet)
    private static final int NAME_MAX_LEN = 64;
    private static final int DOB_MAX_LEN = 16;
    private static final int PHONE_MAX_LEN = 16;
    private static final int ADDRESS_MAX_LEN = 128;
    private static final int AVATAR_MAX_LEN = 4096; // khop AVATAR_LEN tren the

    // Gioi han so du toi da (8 bytes long) - 999,999,999 VND (khoang 1 ty)
    public static final long MAX_BALANCE = 999_999_999_999L; // 999 ty dong

    // Trạng thái kết nối & xác thực
    private boolean connected;
    private boolean authenticated;

    // Làm việc với thẻ thật
    private CardManager cardManager;

    // Simulated member state (nhưng dữ liệu core sẽ được sync với thẻ)
    private MemberInfo memberInfo;
    private PackageInfo packageInfo;
    private CheckInInfo lastCheckIn;
    private long balance;
    private int checkInCount;
    private final TransactionInfo[] transactions;
    private int transactionCount;

    public CardCommunicator() {
        connected = false;
        authenticated = false;

        balance = 0L;
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
        if (connected)
            return;

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
        if (!connected)
            return;

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
     * - Sinh CardID tự động (GYM000001, GYM000002, ...)
     * - Gửi APDU INIT_CARD(cardId, pin) xuống thẻ
     * - Trên thẻ: set PIN mới + sinh masterKey từ PIN
     *
     * @param pin PIN 6 chữ số do admin nhập
     * @return CardID đã gán cho thẻ (để hiển thị cho admin / lưu DB)
     */
    public String initNewCard(String cardId, String userPin) throws Exception {
        if (!connected)
            throw new Exception("Chưa kết nối thẻ");
        if (cardId == null || cardId.isBlank())
            throw new Exception("CardID rỗng");
        if (!userPin.matches("\\d{6}"))
            throw new Exception("PIN phải 6 số");

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
            cardManager.verifyPin(pin); // nếu sai sẽ ném RuntimeException với SW != 9000
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
     * Verify PIN + Xác thực thẻ bằng RSA (nếu có public key trong DB).
     * 
     * Luồng:
     * 1. Verify PIN với thẻ
     * 2. Nếu PIN đúng, đọc CardID từ thẻ (không dùng phone vì có thể thay đổi)
     * 3. Thực hiện RSA challenge-response để xác thực thẻ
     * 
     * @param pin Mã PIN 6 chữ số
     * @return AuthResult chứa kết quả PIN và RSA
     */
    public AuthResult verifyPinWithCardAuth(String pin) throws Exception {
        System.out.println("============================================");
        System.out.println("[AUTH] === BAT DAU XAC THUC PIN + RSA ===");
        System.out.println("============================================");

        AuthResult result = new AuthResult();

        // Bước 1: Verify PIN
        System.out.println("[AUTH] Buoc 1: Dang xac thuc PIN...");
        result.pinVerified = verifyPin(pin);
        System.out.println("[AUTH] Buoc 1: Ket qua PIN = " + (result.pinVerified ? "THANH CONG" : "THAT BAI"));

        if (!result.pinVerified) {
            System.out.println("[AUTH] PIN sai - Dung xac thuc.");
            return result;
        }

        // Bước 2: Đọc CardID từ thẻ để tra cứu public key
        System.out.println("[AUTH] Buoc 2: Dang doc CardID tu the...");
        try {
            byte[] cardIdBytes = cardManager.readField(CardManager.FIELD_CARDID);
            if (cardIdBytes != null && cardIdBytes.length > 0) {
                result.cardId = new String(cardIdBytes, "UTF-8").trim();
                System.out.println("[AUTH] Buoc 2: Da doc duoc CardID = '" + result.cardId + "'");
            } else {
                System.out.println("[AUTH] Buoc 2: CardID rong hoac null");
            }
        } catch (Exception e) {
            System.out.println("[AUTH] Buoc 2: LOI doc CardID: " + e.getMessage());
        }

        // Nếu không có cardId, skip RSA auth
        if (result.cardId == null || result.cardId.isEmpty()) {
            System.out.println("[AUTH] Khong co CardID => BO QUA xac thuc RSA");
            System.out.println("[AUTH] (The chua duoc dang ky trong he thong)");
            result.rsaSkipped = true;
            System.out.println("[AUTH] === KET THUC: PIN OK, RSA SKIPPED ===");
            return result;
        }

        // Bước 3: Xác thực RSA
        System.out.println("[AUTH] Buoc 3: Bat dau xac thuc RSA cho CardID = " + result.cardId);
        try {
            result.rsaVerified = authenticateCard(result.cardId);
            System.out.println("[AUTH] Buoc 3: Ket qua RSA = " + (result.rsaVerified ? "THANH CONG" : "THAT BAI"));
        } catch (Exception e) {
            System.out.println("[AUTH] Buoc 3: LOI RSA: " + e.getMessage());
            result.rsaError = e.getMessage();
            result.rsaVerified = false;
        }

        System.out.println("============================================");
        System.out.println("[AUTH] === KET THUC XAC THUC ===");
        System.out.println("[AUTH] PIN: " + (result.pinVerified ? "OK" : "FAIL"));
        System.out.println("[AUTH] RSA: " + (result.rsaVerified ? "OK" : (result.rsaSkipped ? "SKIPPED" : "FAIL")));
        System.out.println("[AUTH] CardID: " + result.cardId);
        System.out.println("============================================");

        return result;
    }

    /**
     * Kết quả xác thực kết hợp PIN + RSA
     */
    public static class AuthResult {
        public boolean pinVerified = false;
        public boolean rsaVerified = false;
        public boolean rsaSkipped = false;
        public String cardId = null;
        public String rsaError = null;

        public boolean isFullyAuthenticated() {
            return pinVerified && (rsaVerified || rsaSkipped);
        }

        @Override
        public String toString() {
            return "AuthResult{pin=" + pinVerified + ", rsa=" + rsaVerified +
                    ", skipped=" + rsaSkipped + ", cardId=" + cardId + "}";
        }
    }

    /**
     * Đổi PIN: old -> new (INS_CHANGE_PIN)
     * Sau khi đổi PIN thành công, cập nhật public key trong DB để xác thực RSA vẫn
     * hoạt động.
     */
    public boolean changePin(String oldPin, String newPin) throws Exception {
        if (!connected)
            throw new Exception("Chưa kết nối thẻ");
        if (!authenticated)
            throw new Exception("Chưa xác thực PIN");

        if (oldPin == null || newPin == null || oldPin.length() != 6 || newPin.length() != 6) {
            throw new Exception("PIN phải 6 ký tự");
        }

        try {
            cardManager.changePin(oldPin, newPin);
            System.out.println("[CARD] PIN changed successfully");

            // Sau khi đổi PIN, cập nhật public key trong DB
            try {
                byte[] cardIdBytes = cardManager.readField(CardManager.FIELD_CARDID);
                String cardId = (cardIdBytes != null && cardIdBytes.length > 0)
                        ? new String(cardIdBytes, "UTF-8").trim()
                        : null;
                if (cardId != null && !cardId.isEmpty()) {
                    saveCardPublicKeyToDb(cardId);
                    System.out.println("[CARD] Public key updated in DB after PIN change for: " + cardId);
                }
            } catch (Exception e) {
                System.out.println("[CARD] Warning: Could not update public key after PIN change: " + e.getMessage());
                // Không throw exception vì PIN đã đổi thành công
            }

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
        if (!connected)
            throw new Exception("Chưa kết nối thẻ");

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
        if (!connected)
            throw new Exception("Chưa kết nối thẻ");

        byte tries = cardManager.getTriesRemaining();
        return tries & 0xFF;
    }

    /**
     * Admin đổi PIN cho hội viên khi hội viên quên PIN.
     * Chỉ cần mật khẩu admin + PIN mới.
     */
    public boolean adminResetMemberPin(String adminPass, String newPin) throws Exception {
        if (!connected)
            throw new Exception("Chưa kết nối thẻ");
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
    // RSA CARD AUTHENTICATION
    // ---------------------------------------------------------------------

    /**
     * Lấy RSA public key của thẻ (dạng RSAPublicKey object).
     */
    public RSAPublicKey getCardPublicKey() throws Exception {
        if (!connected)
            throw new Exception("Chưa kết nối thẻ");

        byte[] modulus = cardManager.getCardPublicKeyModulus();
        System.out.println("[CARD] Got card public key modulus: " + modulus.length + " bytes");

        return RSAKeyUtils.importFromModulus(modulus);
    }

    /**
     * Lấy RSA public key modulus dạng byte[] (để lưu vào DB).
     */
    public byte[] getCardPublicKeyModulus() throws Exception {
        if (!connected)
            throw new Exception("Chưa kết nối thẻ");

        return cardManager.getCardPublicKeyModulus();
    }

    /**
     * Lưu card public key vào database.
     * Nếu user đã tồn tại, UPDATE public key mới (fix issue: RSA key phải được cập
     * nhật khi re-register).
     * 
     * @param userCode Mã người dùng (thường là số điện thoại hoặc CardID)
     * @return ID của user trong DB, hoặc 0 nếu đã update
     */
    public long saveCardPublicKeyToDb(String userCode) throws Exception {
        if (!connected)
            throw new Exception("Chưa kết nối thẻ");

        byte[] modulus = cardManager.getCardPublicKeyModulus();

        DatabaseManager db = DatabaseManager.getInstance();

        // Kiểm tra user đã tồn tại chưa - nếu có thì UPDATE public key thay vì skip
        if (db.userExists(userCode)) {
            System.out.println("[CARD] User " + userCode + " already exists, UPDATING public key...");
            boolean updated = db.updateCardPublicKey(userCode, modulus);
            if (updated) {
                System.out.println("[CARD] Public key UPDATED for user: " + userCode);
            } else {
                System.out.println("[CARD] WARNING: Failed to update public key for user: " + userCode);
            }
            return 0; // Already exists, updated
        }

        long userId = db.insertUser(userCode, modulus);
        System.out.println("[CARD] Saved NEW card public key to DB for user: " + userCode + ", userId=" + userId);
        return userId;
    }

    /**
     * Xác thực thẻ bằng challenge-response RSA.
     * 
     * Luồng:
     * 1. Lấy stored public key từ DB theo userCode
     * 2. Sinh challenge ngẫu nhiên
     * 3. Gửi challenge xuống thẻ để ký
     * 4. Verify signature bằng stored public key
     * 
     * @param userCode Mã người dùng để tra cứu public key trong DB
     * @return true nếu thẻ authentic, false nếu thẻ giả hoặc không khớp
     */
    public boolean authenticateCard(String userCode) throws Exception {
        System.out.println("--------------------------------------------");
        System.out.println("[RSA] === BAT DAU XAC THUC RSA ===");
        System.out.println("[RSA] CardID/UserCode: " + userCode);
        System.out.println("--------------------------------------------");

        if (!connected) {
            System.out.println("[RSA] LOI: Chua ket noi the!");
            throw new Exception("Chua ket noi the");
        }

        // 1. Lấy stored public key từ DB
        System.out.println("[RSA] Buoc 1: Lay public key tu database...");
        DatabaseManager db = DatabaseManager.getInstance();
        byte[] storedModulus = db.getCardPublicKeyBytes(userCode);

        if (storedModulus == null) {
            System.out.println("[RSA] Buoc 1: KHONG TIM THAY public key trong DB!");
            System.out.println("[RSA] => The chua duoc dang ky hoac CardID sai");
            return false;
        }
        System.out.println("[RSA] Buoc 1: Da tim thay public key (" + storedModulus.length + " bytes)");

        RSAPublicKey storedPubKey = RSAKeyUtils.importFromModulus(storedModulus);
        System.out.println("[RSA] Buoc 1: Da import thanh cong RSA PublicKey");

        // 2. Sinh challenge ngẫu nhiên (32 bytes)
        System.out.println("[RSA] Buoc 2: Tao challenge ngau nhien...");
        byte[] challenge = new byte[32];
        new SecureRandom().nextBytes(challenge);
        System.out.println("[RSA] Buoc 2: Da tao challenge " + challenge.length + " bytes");

        // 3. Gửi challenge xuống thẻ để ký
        System.out.println("[RSA] Buoc 3: Gui challenge xuong the de ky...");
        byte[] signature;
        try {
            signature = cardManager.signChallenge(challenge);
            System.out.println("[RSA] Buoc 3: The da ky thanh cong! Signature: " + signature.length + " bytes");
        } catch (Exception e) {
            System.out.println("[RSA] Buoc 3: LOI - The khong ky duoc challenge!");
            System.out.println("[RSA] Chi tiet loi: " + e.getMessage());
            return false;
        }

        // 4. Verify signature bằng stored public key
        System.out.println("[RSA] Buoc 4: Verify signature bang public key tu DB...");
        try {
            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initVerify(storedPubKey);
            sig.update(challenge);
            boolean valid = sig.verify(signature);

            System.out.println("--------------------------------------------");
            if (valid) {
                System.out.println("[RSA] *** KET QUA: XAC THUC THANH CONG ***");
                System.out.println("[RSA] The la CHINH CHU - Signature hop le!");
            } else {
                System.out.println("[RSA] !!! KET QUA: XAC THUC THAT BAI !!!");
                System.out.println("[RSA] The co the la GIA MAO - Signature sai!");
            }
            System.out.println("--------------------------------------------");

            return valid;
        } catch (Exception e) {
            System.out.println("[RSA] Buoc 4: LOI verify signature: " + e.getMessage());
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
    private static String norm(String s) {
        return s == null ? "" : s.trim();
    }

    public boolean setMemberInfo(String name, String birthDate, String phone, String address, byte[] avatarBytes)
            throws Exception {

        if (!connected || !authenticated)
            throw new Exception("Chưa xác thực PIN");

        name = norm(name);
        birthDate = norm(birthDate);
        phone = norm(phone);
        address = norm(address); // ✅ rỗng vẫn là "" chứ không null

        if (name.isEmpty())
            throw new Exception("Họ và tên không được để trống");
        validateBirthDate(birthDate);
        validatePhone(phone);

        byte[] nameBytes = toUtf8AndLimit(name, NAME_MAX_LEN, "Họ và tên");
        byte[] dobBytes = toUtf8AndLimit(birthDate, DOB_MAX_LEN, "Ngày sinh");
        byte[] phoneBytes = toUtf8AndLimit(phone, PHONE_MAX_LEN, "Số điện thoại");
        byte[] addressBytes = toUtf8AndLimit(address, ADDRESS_MAX_LEN, "Địa chỉ"); // ✅ có thể length=0

        // ✅ luôn write xuống thẻ kể cả rỗng (clear field)
        cardManager.writeField(CardManager.FIELD_NAME, nameBytes);
        cardManager.writeField(CardManager.FIELD_DOB, dobBytes);
        cardManager.writeField(CardManager.FIELD_PHONE, phoneBytes);
        cardManager.writeField(CardManager.FIELD_ADDRESS, addressBytes);

        memberInfo.name = name;
        memberInfo.birthDate = birthDate;
        memberInfo.phone = phone;
        memberInfo.address = address;

        // Avatar: null => không đổi, muốn xóa avatar thì truyền byte[0]
        if (avatarBytes != null) {
            if (avatarBytes.length > AVATAR_MAX_LEN)
                throw new Exception("Avatar quá lớn > " + AVATAR_MAX_LEN);

            System.out.println("[CARD][AVATAR] about to write avatar len=" + avatarBytes.length);
            // ✅ nếu avatarBytes.length==0 -> thẻ sẽ clear avatar (applet bạn đã support)
            cardManager.writeAvatar(avatarBytes);
            System.out.println("[CARD][AVATAR] write avatar OK");

            memberInfo.avatarBytes = (avatarBytes.length == 0 ? null : avatarBytes);
        }

        System.out.println("[CARD] Member info written to card");
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
    private static String safeUtf8(byte[] b) throws Exception {
        if (b == null || b.length == 0)
            return "";
        return new String(b, "UTF-8");
    }

    public MemberInfo getMemberInfo() throws Exception {
        if (!connected || !authenticated)
            throw new Exception("Chưa xác thực PIN");

        MemberInfo info = new MemberInfo();

        byte[] nameBytes = cardManager.readField(CardManager.FIELD_NAME);
        byte[] dobBytes = cardManager.readField(CardManager.FIELD_DOB);
        byte[] phoneBytes = cardManager.readField(CardManager.FIELD_PHONE);
        byte[] addrBytes = cardManager.readField(CardManager.FIELD_ADDRESS);

        info.name = safeUtf8(nameBytes);
        info.birthDate = safeUtf8(dobBytes);
        info.phone = safeUtf8(phoneBytes);
        info.address = safeUtf8(addrBytes);

        // Avatar:
        byte[] avatarBytes = null;
        try {
            avatarBytes = cardManager.readAvatar(); // phải là hàm đọc kiểu AVATAR của applet
        } catch (Exception ignore) {
        }

        info.avatarBytes = (avatarBytes == null || avatarBytes.length == 0) ? null : avatarBytes;

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
     * "type|expiry|registration|sessions"
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
    // CHECK-IN / CHECK-OUT / BALANCE - TẠM LƯU TRONG RAM
    // (TODO: Chuyển sang thẻ thật khi applet hỗ trợ FIELD_CHECKIN/FIELD_BALANCE)
    // ---------------------------------------------------------------------

    /**
     * Check-in: ghi lên thẻ với FIELD_CHECKIN (plaintext, không mã hóa)
     * Mỗi ngày chỉ được check-in 1 lần
     */
    public boolean checkIn(String date, String time) throws Exception {
        if (!connected || !authenticated) {
            throw new Exception("Chưa xác thực PIN");
        }

        // Đọc check-in từ thẻ trước để kiểm tra
        getLastCheckIn();

        // Kiểm tra nếu đã check-in cùng ngày
        if (lastCheckIn.date.equals(date) && !lastCheckIn.checkInTime.isEmpty()) {
            if (lastCheckIn.checkOutTime.isEmpty()) {
                throw new Exception(
                        "Bạn đã check-in hôm nay lúc " + lastCheckIn.checkInTime + ". Vui lòng check-out trước!");
            } else {
                throw new Exception("Bạn đã tập xong hôm nay (" + lastCheckIn.checkInTime + " - "
                        + lastCheckIn.checkOutTime + "). Mỗi ngày chỉ tập 1 buổi!");
            }
        }

        lastCheckIn.date = date;
        lastCheckIn.checkInTime = time;
        lastCheckIn.checkOutTime = "";
        // So buoi khong tang o day - chi tang khi checkout xong
        lastCheckIn.count = checkInCount;

        // Ghi lên thẻ
        boolean writtenToCard = writeCheckInToCard();

        // Trừ buổi nếu gói theo buổi (type == 2)
        if (packageInfo.type == 2 && packageInfo.remainingSessions > 0) {
            packageInfo.remainingSessions--;
            try {
                setPackage(packageInfo.type, packageInfo.expiry, packageInfo.registration,
                        packageInfo.remainingSessions);
            } catch (Exception e) {
                System.out.println("[WARN] Could not update package on card: " + e.getMessage());
            }
        }

        System.out.println(
                "[" + (writtenToCard ? "CARD" : "RAM") + "] Checked in at " + time + ", count=" + checkInCount);
        return true;
    }

    /**
     * Check-out: ghi len the voi FIELD_CHECKIN (plaintext)
     * Chi cho phep checkout neu da check-in va chua checkout trong ngay
     */
    public boolean checkOut(String time) throws Exception {
        if (!connected || !authenticated) {
            throw new Exception("Chua xac thuc PIN");
        }

        // Doc check-in tu the de kiem tra
        getLastCheckIn();

        // Kiem tra da check-in chua
        if (lastCheckIn.date.isEmpty() || lastCheckIn.checkInTime.isEmpty()) {
            throw new Exception("Ban chua check-in! Vui long check-in truoc.");
        }

        // Kiem tra xem co phai hom nay khong
        String today = new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date());
        if (!lastCheckIn.date.equals(today)) {
            throw new Exception("Ban chua check-in hom nay! Check-in truoc khi checkout.");
        }

        // Kiem tra da checkout chua
        if (!lastCheckIn.checkOutTime.isEmpty()) {
            throw new Exception(
                    "Ban da checkout hom nay luc " + lastCheckIn.checkOutTime + ". Moi ngay chi checkout 1 lan!");
        }

        lastCheckIn.checkOutTime = time;

        // Tang so buoi tap khi checkout hoan tat
        checkInCount++;
        lastCheckIn.count = checkInCount;

        // Ghi len the
        boolean writtenToCard = writeCheckInToCard();

        System.out.println("[" + (writtenToCard ? "CARD" : "RAM") + "] Checked out at " + time + ", total sessions = "
                + checkInCount);
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
        // Thử đọc từ thẻ trước
        try {
            byte[] data = cardManager.readField(CardManager.FIELD_CHECKIN);
            if (data != null && data.length > 0) {
                String str = new String(data, "UTF-8");
                String[] parts = str.split("\\|");
                if (parts.length >= 1)
                    lastCheckIn.date = parts[0];
                if (parts.length >= 2)
                    lastCheckIn.checkInTime = parts[1];
                if (parts.length >= 3)
                    lastCheckIn.checkOutTime = parts[2];
                if (parts.length >= 4) {
                    try {
                        lastCheckIn.count = Integer.parseInt(parts[3]);
                        checkInCount = lastCheckIn.count; // Sync RAM voi the
                    } catch (NumberFormatException e) {
                        lastCheckIn.count = 0;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[CARD] Could not read check-in from card: " + e.getMessage());
        }
        return lastCheckIn;
    }

    /**
     * Doc so du tu the (FIELD_BALANCE: 8 bytes long, ma hoa AES)
     */
    public long getBalance() throws Exception {
        if (!connected || !authenticated) {
            throw new Exception("Chua xac thuc PIN");
        }
        // Thu doc tu the (applet se tu decrypt)
        try {
            byte[] data = cardManager.readField(CardManager.FIELD_BALANCE);
            if (data != null && data.length >= 8) {
                // Doc 8 bytes big-endian thanh long
                long cardBalance = 0;
                for (int i = 0; i < 8; i++) {
                    cardBalance = (cardBalance << 8) | (data[i] & 0xFF);
                }
                // Kiem tra gia tri hop le
                if (cardBalance >= 0 && cardBalance <= MAX_BALANCE) {
                    balance = cardBalance;
                    System.out.println("[CARD] Read balance from card: " + balance + " VND");
                } else {
                    System.out.println("[CARD] Invalid balance from card: " + cardBalance + ", using RAM: " + balance);
                }
            }
        } catch (Exception e) {
            System.out.println("[CARD] Could not read balance from card, using RAM: " + e.getMessage());
        }
        return balance;
    }

    /**
     * Nap tien - ghi len the (FIELD_BALANCE: 8 bytes long, ma hoa AES)
     * 
     * @param amount So tien nap (VND)
     * @return true neu thanh cong, false neu that bai (vuot qua max)
     */
    public boolean addBalance(long amount) throws Exception {
        if (!connected || !authenticated) {
            throw new Exception("Chua xac thuc PIN");
        }
        if (amount <= 0) {
            throw new Exception("So tien nap phai lon hon 0");
        }

        // Doc so du hien tai
        getBalance();

        // Kiem tra neu tong so tien vuot qua MAX_BALANCE
        long newBalance = balance + amount;
        if (newBalance > MAX_BALANCE) {
            System.out.println(
                    "[CARD] Balance overflow! current=" + balance + ", add=" + amount + ", max=" + MAX_BALANCE);
            throw new Exception("So tien nap qua lon! Tong so du se vuot qua gioi han " + MAX_BALANCE + " VND");
        }

        balance = newBalance;

        // Ghi len the
        boolean writtenToCard = writeBalanceToCard();

        recordTransaction((byte) 1, (short) (amount / 1000)); // log theo don vi 1000 VND
        System.out
                .println("[" + (writtenToCard ? "CARD" : "RAM") + "] Add balance: " + amount + " VND, new=" + balance);
        return true;
    }

    /**
     * Tru tien - ghi len the (FIELD_BALANCE: 8 bytes long, ma hoa AES)
     * 
     * @param amount So tien tru (VND)
     * @return true neu thanh cong, false neu khong du so du
     */
    public boolean deductBalance(long amount) throws Exception {
        if (!connected || !authenticated) {
            throw new Exception("Chua xac thuc PIN");
        }
        if (amount <= 0) {
            throw new Exception("So tien tru phai lon hon 0");
        }

        // Doc so du hien tai
        getBalance();

        if (balance < amount) {
            System.out.println("[CARD] Not enough balance: " + balance + " < " + amount);
            return false;
        }

        balance -= amount;

        // Ghi len the
        boolean writtenToCard = writeBalanceToCard();

        recordTransaction((byte) 2, (short) (amount / 1000)); // log theo don vi 1000 VND
        System.out.println(
                "[" + (writtenToCard ? "CARD" : "RAM") + "] Deduct balance: " + amount + " VND, new=" + balance);
        return true;
    }

    /**
     * Ghi so du len the (8 bytes big-endian, applet se tu ma hoa)
     */
    private boolean writeBalanceToCard() {
        try {
            byte[] data = new byte[8];
            // Chuyen long thanh 8 bytes big-endian
            for (int i = 7; i >= 0; i--) {
                data[i] = (byte) (balance & 0xFF);
                balance >>= 8;
            }
            // Khoi phuc balance (vi da shift)
            balance = 0;
            for (int i = 0; i < 8; i++) {
                balance = (balance << 8) | (data[i] & 0xFF);
            }

            cardManager.writeField(CardManager.FIELD_BALANCE, data);
            System.out.println("[CARD] Wrote balance to card: " + balance + " VND");
            return true;
        } catch (Exception e) {
            System.out.println("[WARN] Could not write balance to card: " + e.getMessage());
            return false;
        }
    }

    /**
     * Ghi check-in lên thẻ (plaintext, không mã hóa)
     */
    private boolean writeCheckInToCard() {
        try {
            String str = (lastCheckIn.date == null ? "" : lastCheckIn.date) + "|" +
                    (lastCheckIn.checkInTime == null ? "" : lastCheckIn.checkInTime) + "|" +
                    (lastCheckIn.checkOutTime == null ? "" : lastCheckIn.checkOutTime) + "|" +
                    lastCheckIn.count;
            byte[] data = str.getBytes("UTF-8");
            cardManager.writeField(CardManager.FIELD_CHECKIN, data);
            System.out.println("[CARD] Wrote check-in to card: " + str);
            return true;
        } catch (Exception e) {
            System.out.println("[WARN] Could not write check-in to card: " + e.getMessage());
            return false;
        }
    }

    public TransactionInfo getTransaction(byte index) throws Exception {
        if (!connected || !authenticated) {
            throw new Exception("Chưa xác thực PIN");
        }

        if (index < 0 || index >= transactionCount)
            return null;
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
