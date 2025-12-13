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

    // AID applet
    private static final byte[] APPLET_AID = new byte[] {
        (byte)0x11, (byte)0x22, (byte)0x33,
        (byte)0x44, (byte)0x55, (byte)0x00
    };

    private static final byte CLA = (byte)0x80;

    private static final byte INS_INIT_CARD      = (byte)0x10;
    private static final byte INS_VERIFY_PIN     = (byte)0x20;
    private static final byte INS_CHANGE_PIN     = (byte)0x21;
    private static final byte INS_UNLOCK         = (byte)0x22;
    private static final byte INS_ADMIN_SET_PIN  = (byte)0x23;
    private static final byte INS_WRITE_PERSONAL = (byte)0x30;
    private static final byte INS_READ_PERSONAL  = (byte)0x31;
    private static final byte INS_GET_TRIES      = (byte)0x32;

    private static final byte INS_GET_CARD_PUB   = (byte)0x40;
private static final byte INS_SIGN_CHALLENGE = (byte)0x41;
    private static final byte INS_WRITE_SECURE   = (byte)0x42;
    private static final byte INS_READ_SECURE    = (byte)0x43;

    // Field IDs
    public static final byte FIELD_NAME    = (byte)0x00;
    public static final byte FIELD_DOB     = (byte)0x01;
    public static final byte FIELD_PHONE   = (byte)0x02;
    public static final byte FIELD_ADDRESS = (byte)0x03;
    public static final byte FIELD_PACKAGE = (byte)0x04;
    public static final byte FIELD_CARDID  = (byte)0x05;
    public static final byte FIELD_AVATAR  = (byte)0x06;

    private Card card;
    private CardChannel channel;

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
                    return;
                } catch (CardException e) {
                    lastEx = e;
                    System.out.println("[CARD]   -> Kết nối thất bại với " + proto + ": " + e.getMessage());
                }
            }
        }

        if (lastEx != null) {
            throw lastEx;
        }
        throw new IllegalStateException("Không tìm thấy reader nào có thẻ kết nối được");
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

    // ========= INIT_CARD =========
public void initCard(String cardId, String userPin, String adminPin) throws Exception {
    byte[] cardIdBytes = cardId.getBytes("UTF-8");
    if (cardIdBytes.length > 32) throw new IllegalArgumentException("CardID quá dài");
    if (userPin == null || !userPin.matches("\\d{6}")) throw new IllegalArgumentException("User PIN phải 6 số");
    if (adminPin == null || !adminPin.matches("\\d{6}")) throw new IllegalArgumentException("Admin PIN phải 6 số");

    byte[] userPinBytes  = userPin.getBytes("ASCII");
    byte[] adminPinBytes = adminPin.getBytes("ASCII");

    byte[] data = new byte[1 + cardIdBytes.length + 6 + 6];
    int off = 0;
    data[off++] = (byte) cardIdBytes.length;
    System.arraycopy(cardIdBytes, 0, data, off, cardIdBytes.length);
    off += cardIdBytes.length;
    System.arraycopy(userPinBytes, 0, data, off, 6);
    off += 6;
    System.arraycopy(adminPinBytes, 0, data, off, 6);

    ResponseAPDU resp = channel.transmit(new CommandAPDU(CLA, INS_INIT_CARD, 0, 0, data));
    checkStatus(resp, "INIT_CARD");
}


    // ========= PIN =========
    public void verifyPin(String pin) throws Exception {
        if (pin == null || pin.length() != 6) {
            throw new IllegalArgumentException("PIN phải 6 ký tự");
        }
        byte[] pinBytes = pin.getBytes("ASCII");

        CommandAPDU cmd = new CommandAPDU(CLA, INS_VERIFY_PIN, 0x00, 0x00, pinBytes);
        ResponseAPDU resp = channel.transmit(cmd);
        checkStatus(resp, "VERIFY_PIN");
    }

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

        CommandAPDU cmd = new CommandAPDU(CLA, INS_CHANGE_PIN, 0x00, 0x00, data);
        ResponseAPDU resp = channel.transmit(cmd);
        checkStatus(resp, "CHANGE_PIN");
    }

    public void unlockByAdmin(String adminPass) throws Exception {
        if (adminPass == null) adminPass = "";
        byte[] bytes = adminPass.getBytes("ASCII");
        CommandAPDU cmd = new CommandAPDU(CLA, INS_UNLOCK, 0x00, 0x00, bytes);
        ResponseAPDU resp = channel.transmit(cmd);
        checkStatus(resp, "UNLOCK");
    }

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

        CommandAPDU cmd = new CommandAPDU(CLA, INS_ADMIN_SET_PIN, 0x00, 0x00, data);
        ResponseAPDU resp = channel.transmit(cmd);
        checkStatus(resp, "ADMIN_SET_PIN");
    }

    public byte getTriesRemaining() throws Exception {
        byte[] cmdBytes = new byte[] {
            (byte)0x80,
            (byte)0x32,
            (byte)0x00,
            (byte)0x00,
            (byte)0x01
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

    // ========= READ / WRITE FIELDS =========
    public void writeField(byte fieldId, byte[] plainData) throws Exception {
        if (plainData == null) plainData = new byte[0];

        byte[] data = new byte[1 + plainData.length];
        data[0] = fieldId;
        System.arraycopy(plainData, 0, data, 1, plainData.length);

        CommandAPDU cmd = new CommandAPDU(CLA, INS_WRITE_PERSONAL, 0x00, 0x00, data);
        ResponseAPDU resp = channel.transmit(cmd);
        checkStatus(resp, "WRITE_PERSONAL");
    }

    public byte[] readField(byte fieldId) throws Exception {
        byte[] data = new byte[] { fieldId };
        CommandAPDU cmd = new CommandAPDU(CLA, INS_READ_PERSONAL, 0x00, 0x00, data);
        ResponseAPDU resp = channel.transmit(cmd);
        checkStatus(resp, "READ_PERSONAL");
        return resp.getData();
    }

    // ========= GET_CARD_PUB (để lưu DB) =========
    public byte[] getCardPublicKey() throws Exception {
        int totalLen = 128;  // RSA-1024
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
                    chunk
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

    public void setAppPublicKeyModulus(byte[] appModulus) throws Exception {
        if (appModulus == null) {
            throw new IllegalArgumentException("appModulus null");
        }
        CommandAPDU cmd = new CommandAPDU(CLA, INS_SIGN_CHALLENGE , 0x00, 0x00, appModulus);
        ResponseAPDU resp = channel.transmit(cmd);
        checkStatus(resp, "SET_APP_PUB");
    }

    public void writeAvatar(byte[] avatarBytes) throws Exception {
        writeField(FIELD_AVATAR, avatarBytes);
    }
}
