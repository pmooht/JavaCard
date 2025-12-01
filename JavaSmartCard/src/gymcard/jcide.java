
// package ProjectCuoiKy;

// import javacard.framework.*;
// import javacard.security.*;
// import javacardx.crypto.*;

// public class ProjectCuoiKy extends Applet {

//    // ========= PIN & POLICY =========
//    private OwnerPIN pin;
//    private static final byte MAX_PIN_TRIES = (byte)3;
//    private static final byte PIN_SIZE      = (byte)6; // 6 số

//    private boolean blocked;

//    // ========= AES / HASH =========
//    private static final short AES_KEY_LEN = 16; // 128-bit

//    /** masterKey: luôn chứa MasterKey (MK) dùng để mã hóa dữ liệu */
//    private AESKey masterKey;

//    /** wrapKey: dùng tạm để bọc/giải bọc MK bằng UPK / AdminKey */
//    private AESKey wrapKey;

//    private Cipher aesCipher;
//    private MessageDigest sha;          // SHA-1 trên JC2.2.1
//    private byte[] tmpKeyBuf;           // 20 bytes cho output SHA-1
//    private byte[] mkBuf;               // buffer 16 bytes chứa MK plaintext

//    /** MK được bọc bởi UserPIN & AdminKey */
//    private byte[] encMK_User;          // AES(MK, UPK)
//    private byte[] encMK_Admin;         // AES(MK, AK)

//    // ========= DỮ LIỆU CÁ NHÂN (đều mã hóa bằng MK) =========
//    private static final short NAME_LEN    = 64;
//    private static final short DOB_LEN     = 16;
//    private static final short PHONE_LEN   = 16;
//    private static final short ADDRESS_LEN = 128;
//    private static final short PACKAGE_LEN = 32;
//    private static final short CARDID_LEN  = 32;

//    private byte[] nameBuf;
//    private byte[] dobBuf;
//    private byte[] phoneBuf;
//    private byte[] addressBuf;
//    private byte[] packageBuf;
//    private byte[] cardIdBuf;

//    // ========= RSA ĐƯỜNG TRUYỀN (giữ nguyên như bạn đang dùng) =========
//    private RSAPublicKey  cardPublicKey;
//    private RSAPrivateKey cardPrivateKey;
//    private RSAPublicKey  appPublicKey;
//    private KeyPair       cardKeyPair;
//    private Cipher        rsaCipher;

//    private static final short RSA_KEY_LEN_BITS = KeyBuilder.LENGTH_RSA_1024;
//    private static final byte[] EXP_F4 = { 0x01, 0x00, 0x01 }; // 65537

//    private static final short RSA_MOD_LEN    = (short)128;   // 1024-bit modulus
//    private static final short GET_PUB_CHUNK = (short)64;     // mỗi lần trả tối đa 64 bytes

//    // ========= APDUs =========
//    private static final byte CLA_GYM          = (byte)0x80;
//    private static final byte INS_INIT_CARD    = (byte)0x10;
//    private static final byte INS_VERIFY_PIN   = (byte)0x20;
//    private static final byte INS_CHANGE_PIN   = (byte)0x21;
//    private static final byte INS_UNLOCK       = (byte)0x22;
//    private static final byte INS_ADMIN_SET_PIN= (byte)0x23;
//    private static final byte INS_WRITE_PERSONAL = (byte)0x30;
//    private static final byte INS_READ_PERSONAL  = (byte)0x31;
//    private static final byte INS_GET_TRIES      = (byte)0x32;
//    private static final byte INS_GET_CARD_PUB   = (byte)0x40;
//    private static final byte INS_SET_APP_PUB    = (byte)0x41;
//    private static final byte INS_WRITE_SECURE   = (byte)0x42;
//    private static final byte INS_READ_SECURE    = (byte)0x43;

//    // field ids
//    private static final byte FIELD_NAME    = (byte)0x00;
//    private static final byte FIELD_DOB     = (byte)0x01;
//    private static final byte FIELD_PHONE   = (byte)0x02;
//    private static final byte FIELD_ADDRESS = (byte)0x03;
//    private static final byte FIELD_PACKAGE = (byte)0x04;
//    private static final byte FIELD_CARDID  = (byte)0x05;


//    // ========= INSTALL =========
//    public static void install(byte[] bArray, short bOffset, byte bLength) {
//        new ProjectCuoiKy();
//    }

//    protected ProjectCuoiKy() {
//        // OwnerPIN
//        pin = new OwnerPIN(MAX_PIN_TRIES, PIN_SIZE);
//        byte[] defaultPin = { '1','2','3','4','5','6' };
//        pin.update(defaultPin, (short)0, PIN_SIZE);

//        // AES keys
//        masterKey = (AESKey) KeyBuilder.buildKey(
//                KeyBuilder.TYPE_AES,
//                KeyBuilder.LENGTH_AES_128,
//                false);
//        wrapKey = (AESKey) KeyBuilder.buildKey(
//                KeyBuilder.TYPE_AES,
//                KeyBuilder.LENGTH_AES_128,
//                false);

//        aesCipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_ECB_NOPAD, false);
//        sha       = MessageDigest.getInstance(MessageDigest.ALG_SHA, false);
//        tmpKeyBuf = new byte[20];
//        mkBuf     = new byte[AES_KEY_LEN];
//        encMK_User  = new byte[AES_KEY_LEN];
//        encMK_Admin = new byte[AES_KEY_LEN];

//        // Buffers dữ liệu
//        nameBuf    = new byte[NAME_LEN];
//        dobBuf     = new byte[DOB_LEN];
//        phoneBuf   = new byte[PHONE_LEN];
//        addressBuf = new byte[ADDRESS_LEN];
//        packageBuf = new byte[PACKAGE_LEN];
//        cardIdBuf  = new byte[CARDID_LEN];

//        // Lúc mới cài, MK = 0, chỉ dùng tạm; INIT_CARD sẽ sinh MK chuẩn
//        Util.arrayFillNonAtomic(mkBuf, (short)0, AES_KEY_LEN, (byte)0x00);
//        masterKey.setKey(mkBuf, (short)0);

//        blocked = false;

//        // RSA keypair cho thẻ
//        cardPublicKey = (RSAPublicKey) KeyBuilder.buildKey(
//                KeyBuilder.TYPE_RSA_PUBLIC, RSA_KEY_LEN_BITS, false);
//        cardPrivateKey = (RSAPrivateKey) KeyBuilder.buildKey(
//                KeyBuilder.TYPE_RSA_PRIVATE, RSA_KEY_LEN_BITS, false);
//        cardKeyPair = new KeyPair(cardPublicKey, cardPrivateKey);
//        cardKeyPair.genKeyPair();

//        appPublicKey = (RSAPublicKey) KeyBuilder.buildKey(
//                KeyBuilder.TYPE_RSA_PUBLIC, RSA_KEY_LEN_BITS, false);
//        rsaCipher = Cipher.getInstance(Cipher.ALG_RSA_PKCS1, false);

//        register();
//    }

//    // ========= HELPER: derive 16-byte key từ input (PIN / AdminString) =========
//    /**
//     * outKey[outOff..outOff+15] = SHA1(in[off..off+len-1])[0..15]
//     */
//    private void deriveKey16(byte[] in, short off, short len,
//                             byte[] outKey, short outOff) {
//        sha.reset();
//        sha.doFinal(in, off, len, tmpKeyBuf, (short)0);
//        Util.arrayCopyNonAtomic(tmpKeyBuf, (short)0, outKey, outOff, (short)16);
//        Util.arrayFillNonAtomic(tmpKeyBuf, (short)0, (short)tmpKeyBuf.length, (byte)0x00);
//    }

//    // ========= DISPATCH =========
//    public void process(APDU apdu) {
//        if (selectingApplet()) return;

//        byte[] buf = apdu.getBuffer();
//        if (buf[ISO7816.OFFSET_CLA] != CLA_GYM) {
//            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
//        }

//        byte ins = buf[ISO7816.OFFSET_INS];

//        switch (ins) {
//            case INS_INIT_CARD:
//                initCard(apdu);        return;
//            case INS_VERIFY_PIN:
//                verifyPin(apdu);       return;
//            case INS_CHANGE_PIN:
//                changePin(apdu);       return;
//            case INS_UNLOCK:
//                unblock(apdu);         return;
//            case INS_ADMIN_SET_PIN:
//                adminSetPin(apdu);     return;
//            case INS_WRITE_PERSONAL:
//                writePersonal(apdu);   return;
//            case INS_READ_PERSONAL:
//                readPersonal(apdu);    return;
//            case INS_GET_TRIES:
//                sendTries(apdu);       return;
//            case INS_GET_CARD_PUB:
//                getCardPublicKey(apdu);return;
//            case INS_SET_APP_PUB:
//                setAppPublicKey(apdu); return;
//            default:
//                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
//        }
//    }

//    // ========= INIT_CARD =========
//    /**
//     * INIT_CARD (admin, lúc cấp thẻ lần đầu)
//     * data = cardIdLen(1) | cardId | userPIN(6)
//     *
//     * - Sinh MK random
//     * - encMK_User  = AES(MK, UPK)   với UPK = f(userPIN)
//     * - encMK_Admin = AES(MK, AK)    với AK  = f("ADMINPIN")
//     * - masterKey   = MK  (dùng mã hóa dữ liệu)
//     * - cardId được mã hóa bằng MK
//     */
//    private void initCard(APDU apdu) {
//        byte[] buf = apdu.getBuffer();
//        short len  = apdu.setIncomingAndReceive();
//        if (len < (short)(1 + PIN_SIZE)) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

//        short offset = ISO7816.OFFSET_CDATA;
//        byte idLen   = buf[offset++];
//        if (idLen <= 0 || idLen > CARDID_LEN) ISOException.throwIt(ISO7816.SW_WRONG_DATA);
//        if ((short)(1 + idLen + PIN_SIZE) != len) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

//        // 1. copy cardId (plaintext) vào cardIdBuf
//        Util.arrayFillNonAtomic(cardIdBuf, (short)0, CARDID_LEN, (byte)0x00);
//        Util.arrayCopyNonAtomic(buf, offset, cardIdBuf, (short)0, idLen);
//        offset += idLen;

//        short userPinOff = offset;

//        // 2. set OwnerPIN
//        pin.update(buf, userPinOff, PIN_SIZE);
//        pin.resetAndUnblock();
//        blocked = false;

//        // 3. Sinh MK random
//        RandomData rng = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
//        rng.generateData(mkBuf, (short)0, AES_KEY_LEN); // mkBuf = MK

//        // 4. Bọc MK bằng UserPIN: encMK_User = AES(MK, UPK)
//        // derive UPK
//        deriveKey16(buf, userPinOff, PIN_SIZE, tmpKeyBuf, (short)0); // tmpKeyBuf = UPK
//        wrapKey.setKey(tmpKeyBuf, (short)0);
//        aesCipher.init(wrapKey, Cipher.MODE_ENCRYPT);
//        aesCipher.doFinal(mkBuf, (short)0, AES_KEY_LEN, encMK_User, (short)0);

//        // 5. Bọc MK bằng AdminKey: encMK_Admin = AES(MK, AK)
//        byte[] adminConst = { 'A','D','M','I','N','P','I','N' };
//        deriveKey16(adminConst, (short)0, (short)adminConst.length, tmpKeyBuf, (short)0); // tmpKeyBuf = AK
//        wrapKey.setKey(tmpKeyBuf, (short)0);
//        aesCipher.init(wrapKey, Cipher.MODE_ENCRYPT);
//        aesCipher.doFinal(mkBuf, (short)0, AES_KEY_LEN, encMK_Admin, (short)0);

//        // 6. Nạp MK vào masterKey để dùng mã hóa dữ liệu
//        masterKey.setKey(mkBuf, (short)0);

//        // 7. Mã hóa cardIdBuf bằng MK
//        aesCipher.init(masterKey, Cipher.MODE_ENCRYPT);
//        aesCipher.doFinal(cardIdBuf, (short)0, CARDID_LEN, cardIdBuf, (short)0);

//        // 8. clear tạm (mkBuf có thể xóa hoặc giữ; masterKey đã chứa MK)
//        Util.arrayFillNonAtomic(mkBuf, (short)0, AES_KEY_LEN, (byte)0x00);
//        Util.arrayFillNonAtomic(tmpKeyBuf, (short)0, (short)tmpKeyBuf.length, (byte)0x00);
//    }

//    // // ========= VERIFY_PIN =========
//    // /**
//     // * VERIFY_PIN: data = PIN(6)
//     // * - Kiểm tra PIN
//     // * - Nếu đúng:
//     // *   + UPK = f(PIN)
//     // *   + MK  = AES_dec(encMK_User, UPK)
//     // *   + masterKey = MK (dùng cho dữ liệu)
//     // */
//    // private void verifyPin(APDU apdu) {
//        // if (blocked) ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);

//        // byte[] buf = apdu.getBuffer();
//        // short len  = apdu.setIncomingAndReceive();
//        // if (len != PIN_SIZE) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

//        // boolean ok = pin.check(buf, ISO7816.OFFSET_CDATA, PIN_SIZE);
//        // if (!ok) {
//            // if (pin.getTriesRemaining() == 0) {
//                // blocked = true;
//            // }
//            // ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
//        // }

//        // // Derive UPK
//        // short pinOff = ISO7816.OFFSET_CDATA;
//        // deriveKey16(buf, pinOff, PIN_SIZE, tmpKeyBuf, (short)0); // tmpKeyBuf = UPK

//        // // MK = AES_dec(encMK_User, UPK)
//        // wrapKey.setKey(tmpKeyBuf, (short)0);
//        // aesCipher.init(wrapKey, Cipher.MODE_DECRYPT);
//        // aesCipher.doFinal(encMK_User, (short)0, AES_KEY_LEN, mkBuf, (short)0);

//        // // Nạp MK vào masterKey
//        // masterKey.setKey(mkBuf, (short)0);

//        // // Clear RAM tạm
//        // Util.arrayFillNonAtomic(mkBuf, (short)0, AES_KEY_LEN, (byte)0x00);
//        // Util.arrayFillNonAtomic(tmpKeyBuf, (short)0, (short)tmpKeyBuf.length, (byte)0x00);
//    // }
// /**
// * VERIFY_PIN (PIN mã hoá RSA):
// * PC gửi: RSA_PKCS1( PIN(6) )
// * - len = RSA_MOD_LEN (128 với 1024-bit)
// * - Thẻ: RSA decrypt → buf[0..5] = PIN
// * - Check PIN + unwrap MK từ encMK_User
// */
// private void verifyPin(APDU apdu) {
//    if (blocked) ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);

//    byte[] buf = apdu.getBuffer();
//    short len  = apdu.setIncomingAndReceive();

//    // 1. phải đúng size khối RSA
//    if (len != RSA_MOD_LEN) {
//        ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//    }

//    // 2. RSA decrypt vào buf[0..]
//    rsaCipher.init(cardPrivateKey, Cipher.MODE_DECRYPT);
//    short outLen = rsaCipher.doFinal(
//            buf, ISO7816.OFFSET_CDATA, len,   // input = ciphertext
//            buf, (short)0                      // output = plaintext PIN
//    );

//    if (outLen != PIN_SIZE) {
//        ISOException.throwIt(ISO7816.SW_WRONG_DATA);
//    }

//    // 3. check PIN
//    boolean ok = pin.check(buf, (short)0, PIN_SIZE);
//    if (!ok) {
//        if (pin.getTriesRemaining() == 0) blocked = true;
//        ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
//    }

//    // 4. Derive UPK từ PIN plaintext ở buf[0..5]
//    deriveKey16(buf, (short)0, PIN_SIZE, tmpKeyBuf, (short)0); // tmpKeyBuf = UPK

//    // 5. MK = AES_dec(encMK_User, UPK)
//    wrapKey.setKey(tmpKeyBuf, (short)0);
//    aesCipher.init(wrapKey, Cipher.MODE_DECRYPT);
//    aesCipher.doFinal(encMK_User, (short)0, AES_KEY_LEN, mkBuf, (short)0); // mkBuf = MK

//    // 6. masterKey = MK
//    masterKey.setKey(mkBuf, (short)0);

//    // Clear tạm
//    Util.arrayFillNonAtomic(mkBuf, (short)0, AES_KEY_LEN, (byte)0x00);
//    Util.arrayFillNonAtomic(tmpKeyBuf, (short)0, (short)tmpKeyBuf.length, (byte)0x00);
// }

// /**
// * CHANGE_PIN (PIN mã hoá RSA):
// * PC gửi: RSA_PKCS1( oldPIN(6) | newPIN(6) )
// * Thẻ:
// *  - RSA decrypt → buf[0..11] = old|new
// *  - UPK_old -> giải encMK_User → MK
// *  - đổi OwnerPIN -> newPIN
// *  - UPK_new -> encMK_User mới
// *  - masterKey = MK
// */
// private void changePin(APDU apdu) {
//    if (blocked) ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);

//    byte[] buf = apdu.getBuffer();
//    short len  = apdu.setIncomingAndReceive();

//    if (len != RSA_MOD_LEN) {
//        ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//    }

//    // 1. RSA decrypt
//    rsaCipher.init(cardPrivateKey, Cipher.MODE_DECRYPT);
//    short outLen = rsaCipher.doFinal(
//            buf, ISO7816.OFFSET_CDATA, len,
//            buf, (short)0
//    );

//    if (outLen != (short)(PIN_SIZE * 2)) {
//        ISOException.throwIt(ISO7816.SW_WRONG_DATA);
//    }

//    short offOld = 0;
//    short offNew = PIN_SIZE;

//    // 2. verify old PIN
//    boolean ok = pin.check(buf, offOld, PIN_SIZE);
//    if (!ok) {
//        if (pin.getTriesRemaining() == 0) blocked = true;
//        ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
//    }

//    // 3. UPK_old
//    deriveKey16(buf, offOld, PIN_SIZE, tmpKeyBuf, (short)0); // tmpKeyBuf = UPK_old

//    // MK = AES_dec(encMK_User, UPK_old)
//    wrapKey.setKey(tmpKeyBuf, (short)0);
//    aesCipher.init(wrapKey, Cipher.MODE_DECRYPT);
//    aesCipher.doFinal(encMK_User, (short)0, AES_KEY_LEN, mkBuf, (short)0); // mkBuf = MK

//    // 4. đổi OwnerPIN -> newPIN
//    pin.update(buf, offNew, PIN_SIZE);
//    pin.reset();

//    // 5. UPK_new
//    deriveKey16(buf, offNew, PIN_SIZE, tmpKeyBuf, (short)0); // tmpKeyBuf = UPK_new

//    // encMK_User = AES_enc(MK, UPK_new)
//    wrapKey.setKey(tmpKeyBuf, (short)0);
//    aesCipher.init(wrapKey, Cipher.MODE_ENCRYPT);
//    aesCipher.doFinal(mkBuf, (short)0, AES_KEY_LEN, encMK_User, (short)0);

//    // 6. masterKey = MK
//    masterKey.setKey(mkBuf, (short)0);

//    // clear
//    Util.arrayFillNonAtomic(mkBuf, (short)0, AES_KEY_LEN, (byte)0x00);
//    Util.arrayFillNonAtomic(tmpKeyBuf, (short)0, (short)tmpKeyBuf.length, (byte)0x00);
// }


//    // ========= UNLOCK (admin chỉ mở khóa tries, không đổi PIN) =========
//    /**
//     * UNLOCK: data = "ADMIN"
//     * - Chỉ reset tries, không động tới MK / PIN / dữ liệu
//     */
//    private void unblock(APDU apdu) {
//        byte[] buf = apdu.getBuffer();
//        short len  = apdu.setIncomingAndReceive();
//        if (len == 5 &&
//            buf[ISO7816.OFFSET_CDATA]     == 'A' &&
//            buf[(short)(ISO7816.OFFSET_CDATA + 1)] == 'D' &&
//            buf[(short)(ISO7816.OFFSET_CDATA + 2)] == 'M' &&
//            buf[(short)(ISO7816.OFFSET_CDATA + 3)] == 'I' &&
//            buf[(short)(ISO7816.OFFSET_CDATA + 4)] == 'N') {

//            pin.resetAndUnblock();
//            blocked = false;
//        } else {
//            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
//        }
//    }

// /**
// * ADMIN_SET_PIN (payload mã hoá RSA):
// * PC gửi: RSA_PKCS1( 'A','D','M','I','N', newPIN(6) )
// */
// private void adminSetPin(APDU apdu) {
//    byte[] buf = apdu.getBuffer();
//    short len  = apdu.setIncomingAndReceive();

//    if (len != RSA_MOD_LEN) {
//        ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//    }

//    // 1. RSA decrypt
//    rsaCipher.init(cardPrivateKey, Cipher.MODE_DECRYPT);
//    short outLen = rsaCipher.doFinal(
//            buf, ISO7816.OFFSET_CDATA, len,
//            buf, (short)0
//    );

//    if (outLen != (short)(5 + PIN_SIZE)) {
//        ISOException.throwIt(ISO7816.SW_WRONG_DATA);
//    }

//    short off = 0;

//    // 2. check "ADMIN"
//    if (buf[off]     != 'A' ||
//        buf[(short)(off+1)] != 'D' ||
//        buf[(short)(off+2)] != 'M' ||
//        buf[(short)(off+3)] != 'I' ||
//        buf[(short)(off+4)] != 'N') {
//        ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
//    }

//    short newPinOff = (short)(off + 5);

//    // 3. AdminKey từ "ADMINPIN"
//    byte[] adminConst = { 'A','D','M','I','N','P','I','N' };
//    deriveKey16(adminConst, (short)0, (short)adminConst.length, tmpKeyBuf, (short)0); // tmpKeyBuf = AK

//    // MK = AES_dec(encMK_Admin, AK)
//    wrapKey.setKey(tmpKeyBuf, (short)0);
//    aesCipher.init(wrapKey, Cipher.MODE_DECRYPT);
//    aesCipher.doFinal(encMK_Admin, (short)0, AES_KEY_LEN, mkBuf, (short)0); // mkBuf = MK

//    // 4. đổi OwnerPIN sang newUserPIN
//    pin.update(buf, newPinOff, PIN_SIZE);
//    pin.resetAndUnblock();
//    blocked = false;

//    // 5. UPK_new = f(newUserPIN)
//    deriveKey16(buf, newPinOff, PIN_SIZE, tmpKeyBuf, (short)0); // tmpKeyBuf = UPK_new

//    // 6. encMK_User = AES_enc(MK, UPK_new)
//    wrapKey.setKey(tmpKeyBuf, (short)0);
//    aesCipher.init(wrapKey, Cipher.MODE_ENCRYPT);
//    aesCipher.doFinal(mkBuf, (short)0, AES_KEY_LEN, encMK_User, (short)0);

//    // 7. masterKey = MK
//    masterKey.setKey(mkBuf, (short)0);

//    // clear
//    Util.arrayFillNonAtomic(mkBuf, (short)0, AES_KEY_LEN, (byte)0x00);
//    Util.arrayFillNonAtomic(tmpKeyBuf, (short)0, (short)tmpKeyBuf.length, (byte)0x00);
// }


//    // ========= WRITE / READ PERSONAL =========
//    private void writePersonal(APDU apdu) {
//        if (blocked) ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
//        if (!pin.isValidated()) ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);

//        byte[] buf = apdu.getBuffer();
//        short len  = apdu.setIncomingAndReceive();
//        if (len < 1) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

//        byte fieldId   = buf[ISO7816.OFFSET_CDATA];
//        short dataLen  = (short)(len - 1);
//        short srcOff   = (short)(ISO7816.OFFSET_CDATA + 1);

//        byte[] targetBuf;
//        short targetLen;

//        switch (fieldId) {
//            case FIELD_NAME:
//                targetBuf = nameBuf;    targetLen = NAME_LEN;    break;
//            case FIELD_DOB:
//                targetBuf = dobBuf;     targetLen = DOB_LEN;     break;
//            case FIELD_PHONE:
//                targetBuf = phoneBuf;   targetLen = PHONE_LEN;   break;
//            case FIELD_ADDRESS:
//                targetBuf = addressBuf; targetLen = ADDRESS_LEN; break;
//            case FIELD_PACKAGE:
//                targetBuf = packageBuf; targetLen = PACKAGE_LEN; break;
//            case FIELD_CARDID:
//                targetBuf = cardIdBuf;  targetLen = CARDID_LEN;  break;
//            default:
//                ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
//                return;
//        }

//        if (dataLen > targetLen) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

//        // copy plaintext, pad 0
//        Util.arrayFillNonAtomic(targetBuf, (short)0, targetLen, (byte)0x00);
//        Util.arrayCopyNonAtomic(buf, srcOff, targetBuf, (short)0, dataLen);

//        // encrypt bằng MK (masterKey)
//        aesCipher.init(masterKey, Cipher.MODE_ENCRYPT);
//        aesCipher.doFinal(targetBuf, (short)0, targetLen, targetBuf, (short)0);
//    }

//    private void readPersonal(APDU apdu) {
//        if (blocked) ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
//        if (!pin.isValidated()) ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);

//        byte[] buf = apdu.getBuffer();
//        short len  = apdu.setIncomingAndReceive();
//        if (len != 1) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

//        byte fieldId = buf[ISO7816.OFFSET_CDATA];

//        byte[] src;
//        short  srcLen;

//        switch (fieldId) {
//            case FIELD_NAME:
//                src = nameBuf;    srcLen = NAME_LEN;    break;
//            case FIELD_DOB:
//                src = dobBuf;     srcLen = DOB_LEN;     break;
//            case FIELD_PHONE:
//                src = phoneBuf;   srcLen = PHONE_LEN;   break;
//            case FIELD_ADDRESS:
//                src = addressBuf; srcLen = ADDRESS_LEN; break;
//            case FIELD_PACKAGE:
//                src = packageBuf; srcLen = PACKAGE_LEN; break;
//            case FIELD_CARDID:
//                src = cardIdBuf;  srcLen = CARDID_LEN;  break;
//            default:
//                ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
//                return;
//        }

//        // decrypt bằng MK
//        aesCipher.init(masterKey, Cipher.MODE_DECRYPT);
//        aesCipher.doFinal(src, (short)0, srcLen, buf, (short)0);

//        // cắt 0x00 cuối
//        short actualLen = srcLen;
//        while (actualLen > 0 && buf[(short)(actualLen - 1)] == (byte)0x00) {
//            actualLen--;
//        }

//        apdu.setOutgoing();
//        apdu.setOutgoingLength(actualLen);
//        if (actualLen > 0) {
//            apdu.sendBytes((short)0, actualLen);
//        }
//    }

//    private void sendTries(APDU apdu) {
//        byte[] buf = apdu.getBuffer();
//        buf[0] = pin.getTriesRemaining();
//        apdu.setOutgoingAndSend((short)0, (short)1);
//    }

//    // ========= RSA PUBLIC KEY (chunked) =========
//    private void getCardPublicKey(APDU apdu) {
//        byte[] buf = apdu.getBuffer();
//        byte p1    = buf[ISO7816.OFFSET_P1];
//        byte p2    = buf[ISO7816.OFFSET_P2];

//        short offset = Util.makeShort(p1, p2);
//        if (offset < 0 || offset >= RSA_MOD_LEN) {
//            ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
//        }

//        short remain = (short)(RSA_MOD_LEN - offset);
//        short outLen = (remain > GET_PUB_CHUNK) ? GET_PUB_CHUNK : remain;

//        // ghi modulus đầy đủ vào buf[0..modLen-1]
//        short modLen = cardPublicKey.getModulus(buf, (short)0);
//        // (option) có thể kiểm tra modLen == RSA_MOD_LEN

//        // copy 1 đoạn từ buf[offset] về lại buf[0..outLen-1]
//        Util.arrayCopyNonAtomic(buf, offset, buf, (short)0, outLen);

//        apdu.setOutgoing();
//        apdu.setOutgoingLength(outLen);
//        apdu.sendBytes((short)0, outLen);
//    }

//    private void setAppPublicKey(APDU apdu) {
//        byte[] buf = apdu.getBuffer();
//        short len  = apdu.setIncomingAndReceive();
//        appPublicKey.setModulus(buf, ISO7816.OFFSET_CDATA, len);
//        appPublicKey.setExponent(EXP_F4, (short)0, (short)EXP_F4.length);
//    }
// }
