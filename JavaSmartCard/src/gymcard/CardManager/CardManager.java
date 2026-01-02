package gymcard.CardManager;

import javax.smartcardio.*;
import java.util.Arrays;

public class CardManager {

    // ================== APPLET ==================
    private static final byte[] APPLET_AID = new byte[] {
            (byte) 0x11, (byte) 0x22, (byte) 0x33,
            (byte) 0x44, (byte) 0x55, (byte) 0x00
    };

    private static final byte CLA = (byte) 0x80;

    // ================== INS ==================
    private static final byte INS_INIT_CARD = (byte) 0x10;
    private static final byte INS_VERIFY_PIN = (byte) 0x20;
    private static final byte INS_CHANGE_PIN = (byte) 0x21;
    private static final byte INS_UNLOCK = (byte) 0x22;
    private static final byte INS_ADMIN_SET_PIN = (byte) 0x23;

    private static final byte INS_WRITE_PERSONAL = (byte) 0x30;
    private static final byte INS_READ_PERSONAL = (byte) 0x31;
    private static final byte INS_GET_TRIES = (byte) 0x32;

    // ===== RSA AUTHENTICATION =====
    private static final byte INS_GET_CARD_PUB = (byte) 0x40;
    private static final byte INS_SIGN_CHALLENGE = (byte) 0x41;
    private static final int RSA_MOD_LEN = 128; // 1024-bit RSA modulus
    private static final int RSA_CHUNK_SIZE = 64; // Chunk size for reading public key

    // ===== AVATAR CHUNK (T=0 SAFE) =====
    private static final byte INS_AVATAR_BEGIN = (byte) 0x50;
    private static final byte INS_AVATAR_CHUNK = (byte) 0x51;
    private static final byte INS_AVATAR_END = (byte) 0x52;
    private static final byte INS_AVATAR_READ_CHUNK = (byte) 0x53;

    private static final int AVATAR_CHUNK = 220; // an toàn T=0 (<=255)
    // ================== FIELD ==================
    public static final byte FIELD_NAME = (byte) 0x00;
    public static final byte FIELD_DOB = (byte) 0x01;
    public static final byte FIELD_PHONE = (byte) 0x02;
    public static final byte FIELD_ADDRESS = (byte) 0x03;
    public static final byte FIELD_PACKAGE = (byte) 0x04;
    public static final byte FIELD_CARDID = (byte) 0x05;
    public static final byte FIELD_AVATAR = (byte) 0x06;
    public static final byte FIELD_CHECKIN = (byte) 0x07; // Check-in data (không mã hóa trên thẻ)
    public static final byte FIELD_BALANCE = (byte) 0x08; // Số dư (có mã hóa trên thẻ)
    public static final byte FIELD_SERVICES = (byte) 0x09; // Dịch vụ đã mua (plaintext)

    // ================== AVATAR CONFIG ==================
    public static final int AVATAR_STORE_LEN = 8192; // 8KB cho ảnh 128x128
    private static final int AVATAR_MAX_DATA = AVATAR_STORE_LEN - 2; // 2 bytes length prefix
    private static final int AVATAR_CHUNK_SIZE = 220; // T=0 safe

    private Card card;
    private CardChannel channel;

    // ===================================================
    // CONNECT
    // ===================================================
    public void connect() throws Exception {
        TerminalFactory factory = TerminalFactory.getDefault();
        for (CardTerminal terminal : factory.terminals().list()) {

            if (!terminal.isCardPresent())
                continue;

            for (String proto : new String[] { "T=1", "T=0", "*" }) {
                try {
                    card = terminal.connect(proto);
                    channel = card.getBasicChannel();
                    selectApplet();
                    System.out.println("[CARD] Connected with " + proto);
                    return;
                } catch (Exception ignore) {
                }
            }
        }
        throw new IllegalStateException("Không kết nối được thẻ");
    }

    public void disconnect() throws Exception {
        if (card != null) {
            card.disconnect(false);
            card = null;
            channel = null;
        }
    }

    private void selectApplet() throws Exception {
        ResponseAPDU r = channel.transmit(
                new CommandAPDU(0x00, 0xA4, 0x04, 0x00, APPLET_AID));
        checkStatus(r, "SELECT");
    }

    private void checkStatus(ResponseAPDU resp, String where) {
        if (resp.getSW() != 0x9000) {
            throw new RuntimeException(where + " failed, SW=" + Integer.toHexString(resp.getSW()));
        }
    }

    // ===================================================
    // INIT / PIN
    // ===================================================
    public void initCard(String cardId, String userPin, String adminPin) throws Exception {
        byte[] cid = cardId.getBytes("UTF-8");
        byte[] u = userPin.getBytes("ASCII");
        byte[] a = adminPin.getBytes("ASCII");

        byte[] data = new byte[1 + cid.length + 6 + 6];
        int o = 0;
        data[o++] = (byte) cid.length;
        System.arraycopy(cid, 0, data, o, cid.length);
        o += cid.length;
        System.arraycopy(u, 0, data, o, 6);
        o += 6;
        System.arraycopy(a, 0, data, o, 6);

        transmit(new CommandAPDU(CLA, INS_INIT_CARD, 0, 0, data), "INIT_CARD");
    }

    public void verifyPin(String pin) throws Exception {
        transmit(new CommandAPDU(CLA, INS_VERIFY_PIN, 0, 0, pin.getBytes("ASCII")), "VERIFY_PIN");
    }

    public void changePin(String oldPin, String newPin) throws Exception {
        byte[] d = new byte[12];
        System.arraycopy(oldPin.getBytes("ASCII"), 0, d, 0, 6);
        System.arraycopy(newPin.getBytes("ASCII"), 0, d, 6, 6);
        transmit(new CommandAPDU(CLA, INS_CHANGE_PIN, 0, 0, d), "CHANGE_PIN");
    }

    public void adminSetUserPin(String adminPin, String newPin) throws Exception {
        byte[] d = new byte[12];
        System.arraycopy(adminPin.getBytes("ASCII"), 0, d, 0, 6);
        System.arraycopy(newPin.getBytes("ASCII"), 0, d, 6, 6);
        transmit(new CommandAPDU(CLA, INS_ADMIN_SET_PIN, 0, 0, d), "ADMIN_SET_PIN");
    }

    public byte getTriesRemaining() throws Exception {
        ResponseAPDU r = channel.transmit(
                new CommandAPDU(CLA, INS_GET_TRIES, 0, 0, 1));
        checkStatus(r, "GET_TRIES");
        return r.getData()[0];
    }

    // ===================================================
    // NORMAL FIELD
    // ===================================================
    public void writeField(byte fieldId, byte[] data) throws Exception {
        if (data == null)
            data = new byte[0];
        byte[] d = new byte[1 + data.length];
        d[0] = fieldId;
        System.arraycopy(data, 0, d, 1, data.length);
        transmit(new CommandAPDU(CLA, INS_WRITE_PERSONAL, 0, 0, d), "WRITE_FIELD");
    }

    public byte[] readField(byte fieldId) throws Exception {
        ResponseAPDU r = channel.transmit(
                new CommandAPDU(CLA, INS_READ_PERSONAL, 0, 0, new byte[] { fieldId }));
        checkStatus(r, "READ_FIELD");
        return r.getData();
    }

    // ===================================================
    // AVATAR (4096 BYTES, CHUNKED)
    // ===================================================
    public void writeAvatar(byte[] avatarBytes) throws Exception {
        if (avatarBytes == null)
            avatarBytes = new byte[0];
        if (avatarBytes.length > AVATAR_MAX_DATA)
            throw new IllegalArgumentException("Avatar too large");

        // BEGIN (2 bytes length)
        byte[] len2 = new byte[] {
                (byte) ((avatarBytes.length >> 8) & 0xFF),
                (byte) (avatarBytes.length & 0xFF)
        };
        ResponseAPDU r0 = channel.transmit(new CommandAPDU(CLA, INS_AVATAR_BEGIN, 0, 0, len2));
        checkStatus(r0, "AVATAR_BEGIN");

        // CHUNK
        int off = 0;
        while (off < avatarBytes.length) {
            int n = Math.min(AVATAR_CHUNK, avatarBytes.length - off);
            byte p1 = (byte) ((off >> 8) & 0xFF);
            byte p2 = (byte) (off & 0xFF);

            byte[] part = Arrays.copyOfRange(avatarBytes, off, off + n);
            ResponseAPDU rx = channel.transmit(new CommandAPDU(CLA, INS_AVATAR_CHUNK, p1, p2, part));
            checkStatus(rx, "AVATAR_CHUNK");
            off += n;
        }

        // END (encrypt + finalize)
        ResponseAPDU r2 = channel.transmit(new CommandAPDU(CLA, INS_AVATAR_END, 0, 0));
        checkStatus(r2, "AVATAR_END");
    }

    public byte[] readAvatar() throws Exception {
        // trong applet mình lưu len ở cuối buffer
        int lenPos = AVATAR_STORE_LEN - 2;
        byte[] tail = readAvatarChunk(lenPos, 2);

        int len = ((tail[0] & 0xFF) << 8) | (tail[1] & 0xFF);
        if (len <= 0 || len > AVATAR_MAX_DATA)
            return null;

        byte[] out = new byte[len];
        int off = 0;
        while (off < len) {
            int n = Math.min(AVATAR_CHUNK, len - off);
            byte[] chunk = readAvatarChunk(off, n);
            System.arraycopy(chunk, 0, out, off, chunk.length);
            off += chunk.length;
        }
        return out;
    }

    private byte[] readAvatarChunk(int offset, int le) throws Exception {
        byte p1 = (byte) ((offset >> 8) & 0xFF);
        byte p2 = (byte) (offset & 0xFF);
        CommandAPDU cmd = new CommandAPDU(CLA, INS_AVATAR_READ_CHUNK, p1, p2, le);
        ResponseAPDU resp = channel.transmit(cmd);
        checkStatus(resp, "AVATAR_READ_CHUNK");
        return resp.getData();
    }

    private void transmit(CommandAPDU cmd, String tag) throws Exception {
        ResponseAPDU r = channel.transmit(cmd);
        checkStatus(r, tag);
    }

    public void unlockByAdmin(String adminPass) throws Exception {
        if (adminPass == null)
            adminPass = "";
        byte[] bytes = adminPass.getBytes("ASCII");
        CommandAPDU cmd = new CommandAPDU(CLA, INS_UNLOCK, 0x00, 0x00, bytes);
        ResponseAPDU resp = channel.transmit(cmd);
        checkStatus(resp, "UNLOCK");
    }

    // ===================================================
    // RSA AUTHENTICATION
    // ===================================================

    /**
     * Get card RSA public key modulus (128 bytes for RSA-1024).
     * Reads in chunks of 64 bytes each.
     */
    public byte[] getCardPublicKeyModulus() throws Exception {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();

        for (int offset = 0; offset < RSA_MOD_LEN; offset += RSA_CHUNK_SIZE) {
            byte p1 = (byte) ((offset >> 8) & 0xFF);
            byte p2 = (byte) (offset & 0xFF);

            // Request chunk with Le = expected chunk size
            int remaining = RSA_MOD_LEN - offset;
            int chunkLen = Math.min(RSA_CHUNK_SIZE, remaining);

            CommandAPDU cmd = new CommandAPDU(CLA, INS_GET_CARD_PUB, p1, p2, chunkLen);
            ResponseAPDU resp = channel.transmit(cmd);
            checkStatus(resp, "GET_CARD_PUB");

            baos.write(resp.getData());
        }

        return baos.toByteArray();
    }

    /**
     * Sign a challenge with the card's RSA private key.
     * 
     * @param challenge Random bytes to sign (typically 16-32 bytes)
     * @return Signature (128 bytes for RSA-1024)
     */
    public byte[] signChallenge(byte[] challenge) throws Exception {
        if (challenge == null || challenge.length == 0) {
            throw new IllegalArgumentException("Challenge cannot be empty");
        }

        CommandAPDU cmd = new CommandAPDU(CLA, INS_SIGN_CHALLENGE, 0x00, 0x00, challenge);
        ResponseAPDU resp = channel.transmit(cmd);
        checkStatus(resp, "SIGN_CHALLENGE");

        return resp.getData();
    }
}
