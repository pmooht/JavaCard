package gymcard.CardManager;

import java.math.BigInteger;
import javax.smartcardio.*;
import java.security.*;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

public class CardManager {

    // AID applet của bạn
    private static final byte[] APPLET_AID = new byte[] {
        (byte)0x11, (byte)0x22, (byte)0x33,
        (byte)0x44, (byte)0x55, (byte)0x00
    };

    // CLA & INS giống applet ProjectCuoiKy
    private static final byte CLA = (byte)0x80;

    private static final byte INS_INIT_CARD      = (byte)0x10;
    private static final byte INS_VERIFY_PIN     = (byte)0x20;
    private static final byte INS_CHANGE_PIN     = (byte)0x21;
    private static final byte INS_UNLOCK         = (byte)0x22;
    private static final byte INS_ADMIN_SET_PIN = (byte)0x23; // THÊM MỚI
    private static final byte INS_WRITE_PERSONAL = (byte)0x30;
    private static final byte INS_READ_PERSONAL  = (byte)0x31;
    private static final byte INS_GET_TRIES      = (byte)0x32;

    // RSA / secure channel
    private static final byte INS_GET_CARD_PUB   = (byte)0x40;
    private static final byte INS_SET_APP_PUB    = (byte)0x41;
    private static final byte INS_WRITE_SECURE   = (byte)0x42;
    private static final byte INS_READ_SECURE    = (byte)0x43;

    // Field IDs (map với applet)
    public static final byte FIELD_NAME    = (byte)0x00;
    public static final byte FIELD_DOB     = (byte)0x01;
    public static final byte FIELD_PHONE   = (byte)0x02;
    public static final byte FIELD_ADDRESS = (byte)0x03;
    public static final byte FIELD_PACKAGE = (byte)0x04;
    public static final byte FIELD_CARDID  = (byte)0x05;

    private Card card;
    private CardChannel channel;

    private PublicKey cardRSAPublicKey; // JCE public key để encrypt
    // ---------------------------------------------------------
    // KẾT NỐI / NGẮT KẾT NỐI
    // ---------------------------------------------------------

public void connect() throws Exception {
    TerminalFactory factory = TerminalFactory.getDefault();
    java.util.List<CardTerminal> terminals = factory.terminals().list();

    if (terminals.isEmpty()) {
        throw new IllegalStateException("Không tìm thấy đầu đọc thẻ nào");
    }

    CardException lastEx = null;

    for (CardTerminal terminal : terminals) {
        System.out.println("[CARD] Found reader: " + terminal.getName());

        if (!terminal.isCardPresent()) {
            System.out.println("[CARD]   -> Không có thẻ, bỏ qua");
            continue;
        }

        System.out.println("[CARD]   -> Có thẻ, thử kết nối...");

        for (String proto : new String[] { "T=1", "T=0", "*" }) {
            try {
                System.out.println("[CARD]   -> Thử protocol " + proto);
                card = terminal.connect(proto);
                channel = card.getBasicChannel();
                System.out.println("[CARD]   -> Kết nối OK với " + proto);

                // SELECT applet
                selectApplet();
                System.out.println("[CARD] Connected & SELECT applet OK");
                // load public key để dùng cho mã hoá đường truyền (PIN, v.v.)
                ensureCardPublicKeyLoaded();
                return; // thành công, thoát luôn
            } catch (CardException e) {
                lastEx = e;
                System.out.println("[CARD]   -> Kết nối thất bại với " + proto + ": " + e.getMessage());
            }
        }
    }

    // Nếu tới đây mà chưa return tức là fail hết
    if (lastEx != null) {
        throw lastEx;
    }
    throw new IllegalStateException("Không tìm thấy reader nào có thẻ kết nối được");
}
private void ensureCardPublicKeyLoaded() throws Exception {
    if (cardRSAPublicKey != null) return;

    // 1. Lấy modulus từ thẻ (128 bytes)
    byte[] modulusBytes = getCardPublicKey();  // đã implement chunk 64-64

    // 2. Tạo BigInteger modulus & exponent
    BigInteger mod = new BigInteger(1, modulusBytes);
    BigInteger exp = new BigInteger("65537"); // 0x10001

    KeySpec spec = new RSAPublicKeySpec(mod, exp);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    cardRSAPublicKey = kf.generatePublic(spec);

    System.out.println("[CARD] Built RSA public key from modulus, size=" + modulusBytes.length);
}

    public void disconnect() throws Exception {
        if (card != null) {
            card.disconnect(false);
            card = null;
            channel = null;
            System.out.println("[CARD] Disconnected");
        }
    }

    private void selectApplet() throws Exception {
        CommandAPDU select = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, APPLET_AID);
        ResponseAPDU resp = channel.transmit(select);
        checkStatus(resp, "SELECT APPLET");
    }

    private void checkStatus(ResponseAPDU resp, String where) {
        int sw = resp.getSW();
        if (sw != 0x9000) {
            throw new RuntimeException(where + " thất bại, SW=" + Integer.toHexString(sw));
        }
    }

    // ---------------------------------------------------------
    // INIT_CARD / PIN / UNLOCK
    // ---------------------------------------------------------

    /**
     * INIT_CARD: data = cardIdLen(1) | cardId | PIN(6)
     */
    public void initCard(String cardId, String pin) throws Exception {
        byte[] cardIdBytes = cardId.getBytes("UTF-8");
        if (cardIdBytes.length > 32) {
            throw new IllegalArgumentException("CardID quá dài (max 32 bytes)");
        }
        if (pin == null || pin.length() != 6) {
            throw new IllegalArgumentException("PIN phải 6 ký tự");
        }
        byte[] pinBytes = pin.getBytes("ASCII");

        byte[] data = new byte[1 + cardIdBytes.length + pinBytes.length];
        data[0] = (byte) cardIdBytes.length;
        System.arraycopy(cardIdBytes, 0, data, 1, cardIdBytes.length);
        System.arraycopy(pinBytes, 0, data, 1 + cardIdBytes.length, pinBytes.length);

        CommandAPDU cmd = new CommandAPDU(CLA, INS_INIT_CARD, 0x00, 0x00, data);
        ResponseAPDU resp = channel.transmit(cmd);
        checkStatus(resp, "INIT_CARD");
    }

    /**
     * VERIFY_PIN: data = PIN(6), không trả data, chỉ SW.
     */
    public void verifyPin(String pin) throws Exception {
        if (pin == null || pin.length() != 6) {
            throw new IllegalArgumentException("PIN phải 6 ký tự");
        }
        byte[] pinBytes = pin.getBytes("ASCII");
            // RSA encrypt
    // RSA encrypt
    byte[] enc = rsaEncryptForCard(pinBytes);

    CommandAPDU cmd = new CommandAPDU(CLA, INS_VERIFY_PIN, 0x00, 0x00, enc);
    ResponseAPDU resp = channel.transmit(cmd);
    checkStatus(resp, "VERIFY_PIN");
    }
private byte[] rsaEncryptForCard(byte[] plain) throws Exception {
    ensureCardPublicKeyLoaded();
    Cipher rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    rsa.init(Cipher.ENCRYPT_MODE, cardRSAPublicKey);
    return rsa.doFinal(plain);
}
    /**
     * CHANGE_PIN: data = oldPIN(6) | newPIN(6)
     */
    public void changePin(String oldPin, String newPin) throws Exception {
        if (oldPin == null || newPin == null ||
            oldPin.length() != 6 || newPin.length() != 6) {
            throw new IllegalArgumentException("PIN phải 6 ký tự");
        }
        byte[] oldBytes = oldPin.getBytes("ASCII");
        byte[] newBytes = newPin.getBytes("ASCII");
        byte[] data = new byte[12];
        System.arraycopy(oldBytes, 0, data, 0, 6);
        System.arraycopy(newBytes, 0, data, 6, 6);

//        CommandAPDU cmd = new CommandAPDU(CLA, INS_CHANGE_PIN, 0x00, 0x00, data);
//        ResponseAPDU resp = channel.transmit(cmd);
//        checkStatus(resp, "CHANGE_PIN");
            byte[] enc = rsaEncryptForCard(data);

    CommandAPDU cmd = new CommandAPDU(CLA, INS_CHANGE_PIN, 0x00, 0x00, enc);
    ResponseAPDU resp = channel.transmit(cmd);
    checkStatus(resp, "CHANGE_PIN");
    }

    /**
     * UNLOCK: data = "ADMIN"
     */
    public void unlockByAdmin(String adminPass) throws Exception {
        if (adminPass == null) adminPass = "";
        byte[] bytes = adminPass.getBytes("ASCII");
        CommandAPDU cmd = new CommandAPDU(CLA, INS_UNLOCK, 0x00, 0x00, bytes);
        ResponseAPDU resp = channel.transmit(cmd);
        checkStatus(resp, "UNLOCK");
    }

    /**
     * GET_TRIES: trả về 1 byte triesRemaining
     *
     * Lưu ý: dùng constructor KHÔNG có Le để tránh PC/SC làm chain lung tung.
     */
public byte getTriesRemaining() throws Exception {
    // APDU case 2S: CLA INS P1 P2 Le
    // 80 32 00 00 01  -> Le = 1 byte
    byte[] cmdBytes = new byte[] {
        (byte)0x80, // CLA
        (byte)0x32, // INS_GET_TRIES
        (byte)0x00, // P1
        (byte)0x00, // P2
        (byte)0x01  // Le = 1
    };

    CommandAPDU cmd = new CommandAPDU(cmdBytes);
    ResponseAPDU resp = channel.transmit(cmd);
    checkStatus(resp, "GET_TRIES");

    byte[] data = resp.getData();
    if (data == null || data.length < 1) {
        throw new RuntimeException("GET_TRIES: card không trả về byte nào");
    }
    return data[0];
}



    // ---------------------------------------------------------
    // READ / WRITE FIELDS (thẻ tự mã hóa AES bên trong)
    // ---------------------------------------------------------

    /**
     * WRITE_PERSONAL: data = fieldId(1) | plainData...
     * Thẻ sẽ AES-ECB-NOPAD + masterKey(PIN) và lưu ciphertext.
     */
    public void writeField(byte fieldId, byte[] plainData) throws Exception {
        if (plainData == null) plainData = new byte[0];

        byte[] data = new byte[1 + plainData.length];
        data[0] = fieldId;
        System.arraycopy(plainData, 0, data, 1, plainData.length);

        CommandAPDU cmd = new CommandAPDU(CLA, INS_WRITE_PERSONAL, 0x00, 0x00, data);
        ResponseAPDU resp = channel.transmit(cmd);
        checkStatus(resp, "WRITE_PERSONAL");
    }

    /**
     * READ_PERSONAL: data = fieldId(1)
     * Thẻ giải mã AES rồi trả plaintext (đã cắt 0x00).
     */
    public byte[] readField(byte fieldId) throws Exception {
        byte[] data = new byte[] { fieldId };
        CommandAPDU cmd = new CommandAPDU(CLA, INS_READ_PERSONAL, 0x00, 0x00, data);
        ResponseAPDU resp = channel.transmit(cmd);
        checkStatus(resp, "READ_PERSONAL");
        return resp.getData();
    }

    // ---------------------------------------------------------
    // RSA: LẤY PUBLIC KEY CỦA THẺ & GỬI PUBLIC KEY CỦA APP
    // ---------------------------------------------------------

    /**
     * INS_GET_CARD_PUB:
     *   Applet trả về modulus của cardPublicKey (big-endian, length = keySize/8).
     *
     * Tuần 1: ta lấy modulus ở dạng byte[] để:
     *   - Lưu DB (hex/Base64)
     *   - Nếu cần thì dựng lại java.security.PublicKey (exponent = 65537).
     */
public byte[] getCardPublicKey() throws Exception {
    // biết trước RSA_MOD_LEN = 128
    int totalLen = 128;
    byte[] result = new byte[totalLen];
    int offset = 0;

    while (offset < totalLen) {
        int chunk = Math.min(64, totalLen - offset);

        byte p1 = (byte)((offset >> 8) & 0xFF);
        byte p2 = (byte)(offset & 0xFF);

        CommandAPDU cmd = new CommandAPDU(
                CLA,
                INS_GET_CARD_PUB,
                p1,
                p2,
                chunk  // Le mong muốn
        );
        ResponseAPDU resp = channel.transmit(cmd);
       checkStatus(resp, "GET_CARD_PUB");


        byte[] data = resp.getData();
        if (data.length == 0) {
            throw new RuntimeException("GET_CARD_PUB: nhận về chunk rỗng");
        }
        System.arraycopy(data, 0, result, offset, data.length);
        offset += data.length;
    }
    return result;
}

    /**
     * INS_SET_APP_PUB:
     *   App gửi modulus của appPublicKey lên thẻ (exponent F4 cố định 0x10001).
     */
    public void setAppPublicKeyModulus(byte[] appModulus) throws Exception {
        if (appModulus == null) {
            throw new IllegalArgumentException("appModulus null");
        }
        CommandAPDU cmd = new CommandAPDU(CLA, INS_SET_APP_PUB, 0x00, 0x00, appModulus);
        ResponseAPDU resp = channel.transmit(cmd);
        checkStatus(resp, "SET_APP_PUB");
    }

    // ---------------------------------------------------------
    // CÁC HÀM SECURE WRITE/READ (RSA + AES) – CHƯA DÙNG TUẦN 1
    // ---------------------------------------------------------
    // Lưu nguyên, nhưng nhớ: chỉ gọi khi bạn đã implement INS_WRITE_SECURE /
    // INS_READ_SECURE ở applet. Nếu chưa support, các hàm này sẽ ném SW=6D00
    // (INS_NOT_SUPPORTED) – không liên quan tới lỗi 256 iterations.

    public void secureWriteField(byte fieldId,
                                 byte[] plainData,
                                 PublicKey cardPublicKey) throws Exception {

        if (plainData == null) plainData = new byte[0];

        // 1. AES session key random
        byte[] aesKey = new byte[16];
        new SecureRandom().nextBytes(aesKey);

        // 2. pack = fieldId | plainData
        byte[] pack = new byte[1 + plainData.length];
        pack[0] = fieldId;
        System.arraycopy(plainData, 0, pack, 1, plainData.length);

        // 3. AES encrypt
        Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        aesCipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey, "AES"));
        byte[] aesData = aesCipher.doFinal(pack);

        // 4. RSA encrypt AES key
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.ENCRYPT_MODE, cardPublicKey);
        byte[] encKey = rsaCipher.doFinal(aesKey);

        // 5. concat: encKey | aesData
        byte[] sendData = new byte[encKey.length + aesData.length];
        System.arraycopy(encKey, 0, sendData, 0, encKey.length);
        System.arraycopy(aesData, 0, sendData, encKey.length, aesData.length);

        // 6. gửi xuống thẻ
        CommandAPDU cmd = new CommandAPDU(CLA, INS_WRITE_SECURE, 0x00, 0x00, sendData);
        ResponseAPDU resp = channel.transmit(cmd);
        checkStatus(resp, "WRITE_SECURE");
    }

    public byte[] secureReadField(byte fieldId,
                                  PrivateKey appPrivateKey) throws Exception {

        byte[] data = new byte[] { fieldId };
        CommandAPDU cmd = new CommandAPDU(CLA, INS_READ_SECURE, 0x00, 0x00, data);
        ResponseAPDU resp = channel.transmit(cmd);
        checkStatus(resp, "READ_SECURE");

        byte[] respData = resp.getData();
        if (respData.length < 129) {
            throw new RuntimeException("Dữ liệu READ_SECURE quá ngắn");
        }

        // Giả sử RSA-1024 => encKey = 128 bytes
        int rsaLen = 128;
        byte[] encKey = Arrays.copyOfRange(respData, 0, rsaLen);
        byte[] aesData = Arrays.copyOfRange(respData, rsaLen, respData.length);

        // 1. RSA decrypt AES key
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.DECRYPT_MODE, appPrivateKey);
        byte[] aesKey = rsaCipher.doFinal(encKey);

        // 2. AES decrypt
        Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        aesCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey, "AES"));
        byte[] pack = aesCipher.doFinal(aesData);

        if (pack.length == 0 || pack[0] != fieldId) {
            throw new RuntimeException("Sai fieldId trong dữ liệu trả về");
        }

        byte[] plain = new byte[pack.length - 1];
        System.arraycopy(pack, 1, plain, 0, plain.length);
        return plain;
    }
    /**
 * Admin đặt PIN mới cho user khi user quên PIN:
 * data = "ADMIN" | newPIN(6)
 */
public void adminSetUserPin(String adminPass, String newPin) throws Exception {
    if (newPin == null || newPin.length() != 6) {
        throw new IllegalArgumentException("PIN mới phải 6 ký tự");
    }
    if (adminPass == null || adminPass.length() == 0) {
        throw new IllegalArgumentException("Admin pass không được rỗng");
    }

    byte[] adminBytes = adminPass.getBytes("ASCII");
    byte[] pinBytes   = newPin.getBytes("ASCII");

    byte[] data = new byte[adminBytes.length + pinBytes.length];
System.arraycopy(adminBytes, 0, data, 0, adminBytes.length);
System.arraycopy(pinBytes, 0, data, adminBytes.length, pinBytes.length);

//    CommandAPDU cmd = new CommandAPDU(CLA, INS_ADMIN_SET_PIN, 0x00, 0x00, data);
//    ResponseAPDU resp = channel.transmit(cmd);
//    checkStatus(resp, "ADMIN_SET_PIN");
        byte[] enc = rsaEncryptForCard(data);

    CommandAPDU cmd = new CommandAPDU(CLA, INS_ADMIN_SET_PIN, 0x00, 0x00, enc);
    ResponseAPDU resp = channel.transmit(cmd);
    checkStatus(resp, "ADMIN_SET_PIN");
}
}
