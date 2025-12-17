//package ProjectCuoiKy;
//
//import javacard.framework.*;
//import javacard.security.*;
//import javacardx.crypto.*;
//
//public class ProjectCuoiKy extends Applet {
//
//    // ===================== RANDOM =====================
//    private RandomData rng;
//
//    // ===================== PIN CONFIG =====================
//    private static final byte PIN_SIZE      = (byte) 6;
//    private static final byte MAX_PIN_TRIES = (byte) 3;
//
//    private byte    triesRemaining;
//    private boolean blocked;
//    private boolean userAuthenticated;
//
//    // ===================== AES / HASH =====================
//    private static final short AES_KEY_LEN = (short) 16;
//    private static final short MK_HASH_LEN = (short) 16;
//
//    private AESKey masterKey;
//    private AESKey wrapKey;
//
//    private Cipher        aesCipher;
//    private MessageDigest sha;
//
//    private byte[] tmpKeyBuf;   // 20 bytes SHA-1 output
//    private byte[] mkBuf;       // 16 bytes MK plaintext
//    private byte[] mkHash;      // 16 bytes hash(MK)
//    private byte[] encMK_User;  // AES(MK, KDF(userPIN))
//    private byte[] encMK_Admin; // AES(MK, KDF(adminPIN))
//
//    private byte[] pinKeyBuf;   // 16 bytes
//    private byte[] mkHashCand;  // 16 bytes
//
//    // ===================== PERSONAL DATA =====================
//    private static final short NAME_LEN    = (short) 64;
//    private static final short DOB_LEN     = (short) 16;
//    private static final short PHONE_LEN   = (short) 16;
//    private static final short ADDRESS_LEN = (short) 128;
//    private static final short PACKAGE_LEN = (short) 32;
//    private static final short CARDID_LEN  = (short) 32;
//
//    private static final short AVATAR_LEN  = (short) 4096; // ciphertext 4096
//    private static final short CHECKIN_LEN = (short) 64;   // plaintext
//
//    // balance luu ciphertext 16 bytes
//    // plaintext: 8 bytes so tien (big-endian) + 8 bytes padding 0
//    private static final short BALANCE_LEN = (short) 16;
//    private static final short BALANCE_VALUE_LEN = (short) 8;
//
//    private byte[] nameBuf;
//    private byte[] dobBuf;
//    private byte[] phoneBuf;
//    private byte[] addressBuf;
//    private byte[] packageBuf;
//    private byte[] cardIdBuf;
//
//    private byte[] avatarBuf;
//    private byte[] checkinBuf;
//    private byte[] balanceBuf;
//
//    // transient tmp cho decrypt/encrypt balance
//    private byte[] balanceTmp;
//
//    // ===================== RSA SIGN =====================
//    private RSAPublicKey  cardPublicKey;
//    private RSAPrivateKey cardPrivateKey;
//    private KeyPair       cardKeyPair;
//    private Signature     rsaSign;
//
//    private static final short RSA_KEY_LEN_BITS = KeyBuilder.LENGTH_RSA_1024;
//    private static final byte[] EXP_F4 = { 0x01, 0x00, 0x01 };
//
//    private static final short RSA_MOD_LEN   = (short) 128;
//    private static final short GET_PUB_CHUNK = (short) 64;
//
//    // ===================== APDU =====================
//    private static final byte CLA_GYM = (byte) 0x80;
//
//    private static final byte INS_INIT_CARD      = (byte) 0x10;
//    private static final byte INS_VERIFY_PIN     = (byte) 0x20;
//    private static final byte INS_CHANGE_PIN     = (byte) 0x21;
//    private static final byte INS_UNLOCK         = (byte) 0x22;
//    private static final byte INS_ADMIN_SET_PIN  = (byte) 0x23;
//
//    private static final byte INS_WRITE_PERSONAL = (byte) 0x30;
//    private static final byte INS_READ_PERSONAL  = (byte) 0x31;
//    private static final byte INS_GET_TRIES      = (byte) 0x32;
//
//    private static final byte INS_GET_CARD_PUB   = (byte) 0x40;
//    private static final byte INS_SIGN_CHALLENGE = (byte) 0x41;
//
//    // Avatar chunking
//    private static final byte INS_AVATAR_BEGIN      = (byte) 0x50;
//    private static final byte INS_AVATAR_CHUNK      = (byte) 0x51;
//    private static final byte INS_AVATAR_END        = (byte) 0x52;
//    private static final byte INS_AVATAR_READ_CHUNK = (byte) 0x53;
//
//    private static final byte INS_GET_MEM = (byte) 0x55;
//
//    // Field ids
//    private static final byte FIELD_NAME    = (byte) 0x00;
//    private static final byte FIELD_DOB     = (byte) 0x01;
//    private static final byte FIELD_PHONE   = (byte) 0x02;
//    private static final byte FIELD_ADDRESS = (byte) 0x03;
//    private static final byte FIELD_PACKAGE = (byte) 0x04;
//    private static final byte FIELD_CARDID  = (byte) 0x05;
//    private static final byte FIELD_AVATAR  = (byte) 0x06; // chi dung chunking
//    private static final byte FIELD_CHECKIN = (byte) 0x07; // plaintext
//    private static final byte FIELD_BALANCE = (byte) 0x08; // encrypted, 8 bytes value
//
//    // Avatar state
//    private short avatarDataLen = 0;
//
//    // =========================================================
//    // INSTALL
//    // =========================================================
//
//    // Ham install applet
//    public static void install(byte[] bArray, short bOffset, byte bLength) {
//        new ProjectCuoiKy();
//    }
//
//    // Constructor
//    protected ProjectCuoiKy() {
//        rng = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
//
//        triesRemaining    = MAX_PIN_TRIES;
//        blocked           = false;
//        userAuthenticated = false;
//
//        masterKey = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
//        wrapKey   = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
//
//        aesCipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_ECB_NOPAD, false);
//        sha       = MessageDigest.getInstance(MessageDigest.ALG_SHA, false);
//
//        tmpKeyBuf   = new byte[20];
//        mkBuf       = new byte[AES_KEY_LEN];
//        mkHash      = new byte[MK_HASH_LEN];
//        encMK_User  = new byte[AES_KEY_LEN];
//        encMK_Admin = new byte[AES_KEY_LEN];
//
//        pinKeyBuf   = new byte[AES_KEY_LEN];
//        mkHashCand  = new byte[MK_HASH_LEN];
//
//        // MK tam thoi = 0
//        Util.arrayFillNonAtomic(mkBuf, (short)0, AES_KEY_LEN, (byte)0x00);
//        masterKey.setKey(mkBuf, (short)0);
//
//        // Persistent buffers
//        nameBuf    = new byte[NAME_LEN];
//        dobBuf     = new byte[DOB_LEN];
//        phoneBuf   = new byte[PHONE_LEN];
//        addressBuf = new byte[ADDRESS_LEN];
//        packageBuf = new byte[PACKAGE_LEN];
//        cardIdBuf  = new byte[CARDID_LEN];
//
//        avatarBuf  = new byte[AVATAR_LEN];
//        checkinBuf = new byte[CHECKIN_LEN];
//        balanceBuf = new byte[BALANCE_LEN];
//
//        // transient tmp for balance
//        balanceTmp = JCSystem.makeTransientByteArray(BALANCE_LEN, JCSystem.CLEAR_ON_DESELECT);
//
//        // RSA keypair
//        cardPublicKey  = (RSAPublicKey)  KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PUBLIC,  RSA_KEY_LEN_BITS, false);
//        cardPrivateKey = (RSAPrivateKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PRIVATE, RSA_KEY_LEN_BITS, false);
//        cardKeyPair    = new KeyPair(cardPublicKey, cardPrivateKey);
//        cardKeyPair.genKeyPair();
//        cardPublicKey.setExponent(EXP_F4, (short)0, (short)EXP_F4.length);
//
//        rsaSign = Signature.getInstance(Signature.ALG_RSA_SHA_PKCS1, false);
//
//        register();
//    }
//
//    // =========================================================
//    // KDF / HASH
//    // =========================================================
//
//    // KDF: SHA1(PIN || "GYMCARD-KDF")[0..15]
//    private void deriveKeyFromPin(byte[] pinBuf, short pinOff, short pinLen,
//                                  byte[] outKey, short outOff) {
//        sha.reset();
//        sha.update(pinBuf, pinOff, pinLen);
//        byte[] salt = { 'G','Y','M','C','A','R','D','-','K','D','F' };
//        sha.doFinal(salt, (short)0, (short)salt.length, tmpKeyBuf, (short)0);
//
//        Util.arrayCopyNonAtomic(tmpKeyBuf, (short)0, outKey, outOff, AES_KEY_LEN);
//        Util.arrayFillNonAtomic(tmpKeyBuf, (short)0, (short)tmpKeyBuf.length, (byte)0x00);
//    }
//
//    // Hash MK: SHA1(MK || "GYMCARD-MK")[0..15]
//    private void hashMasterKey(byte[] mk, short mkOff, byte[] outHash, short outOff) {
//        sha.reset();
//        sha.update(mk, mkOff, AES_KEY_LEN);
//        byte[] salt = { 'G','Y','M','C','A','R','D','-','M','K' };
//        sha.doFinal(salt, (short)0, (short)salt.length, tmpKeyBuf, (short)0);
//
//        Util.arrayCopyNonAtomic(tmpKeyBuf, (short)0, outHash, outOff, MK_HASH_LEN);
//        Util.arrayFillNonAtomic(tmpKeyBuf, (short)0, (short)tmpKeyBuf.length, (byte)0x00);
//    }
//
//    // Mo MK bang user PIN
//    private boolean unlockMasterWithUserPin(byte[] buf, short pinOff) {
//        deriveKeyFromPin(buf, pinOff, PIN_SIZE, pinKeyBuf, (short)0);
//        wrapKey.setKey(pinKeyBuf, (short)0);
//
//        aesCipher.init(wrapKey, Cipher.MODE_DECRYPT);
//        aesCipher.doFinal(encMK_User, (short)0, AES_KEY_LEN, mkBuf, (short)0);
//
//        hashMasterKey(mkBuf, (short)0, mkHashCand, (short)0);
//
//        boolean ok = (Util.arrayCompare(mkHashCand, (short)0, mkHash, (short)0, MK_HASH_LEN) == 0);
//        if (ok) masterKey.setKey(mkBuf, (short)0);
//
//        Util.arrayFillNonAtomic(mkHashCand, (short)0, MK_HASH_LEN, (byte)0x00);
//        Util.arrayFillNonAtomic(pinKeyBuf,  (short)0, AES_KEY_LEN, (byte)0x00);
//        Util.arrayFillNonAtomic(mkBuf,      (short)0, AES_KEY_LEN, (byte)0x00);
//
//        return ok;
//    }
//
//    // Mo MK bang admin PIN
//    private boolean unlockMasterWithAdminPin(byte[] buf, short pinOff) {
//        deriveKeyFromPin(buf, pinOff, PIN_SIZE, pinKeyBuf, (short)0);
//        wrapKey.setKey(pinKeyBuf, (short)0);
//
//        aesCipher.init(wrapKey, Cipher.MODE_DECRYPT);
//        aesCipher.doFinal(encMK_Admin, (short)0, AES_KEY_LEN, mkBuf, (short)0);
//
//        hashMasterKey(mkBuf, (short)0, mkHashCand, (short)0);
//
//        boolean ok = (Util.arrayCompare(mkHashCand, (short)0, mkHash, (short)0, MK_HASH_LEN) == 0);
//        if (ok) masterKey.setKey(mkBuf, (short)0);
//
//        Util.arrayFillNonAtomic(mkHashCand, (short)0, MK_HASH_LEN, (byte)0x00);
//        Util.arrayFillNonAtomic(pinKeyBuf,  (short)0, AES_KEY_LEN, (byte)0x00);
//        Util.arrayFillNonAtomic(mkBuf,      (short)0, AES_KEY_LEN, (byte)0x00);
//
//        return ok;
//    }
//
//    // =========================================================
//    // PROCESS APDU
//    // =========================================================
//
//    // Router APDU
//    public void process(APDU apdu) {
//        if (selectingApplet()) {
//            userAuthenticated = false;
//            return;
//        }
//
//        byte[] buf = apdu.getBuffer();
//        if (buf[ISO7816.OFFSET_CLA] != CLA_GYM) ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
//
//        switch (buf[ISO7816.OFFSET_INS]) {
//            case INS_INIT_CARD:          initCard(apdu);        return;
//            case INS_VERIFY_PIN:         verifyPin(apdu);       return;
//            case INS_CHANGE_PIN:         changePin(apdu);       return;
//            case INS_UNLOCK:             unlockCard(apdu);      return;
//            case INS_ADMIN_SET_PIN:      adminSetUserPin(apdu); return;
//
//            case INS_AVATAR_BEGIN:       avatarBegin(apdu);     return;
//            case INS_AVATAR_CHUNK:       avatarChunk(apdu);     return;
//            case INS_AVATAR_END:         avatarEnd(apdu);       return;
//            case INS_AVATAR_READ_CHUNK:  avatarReadChunk(apdu); return;
//
//            case INS_WRITE_PERSONAL:     writePersonal(apdu);   return;
//            case INS_READ_PERSONAL:      readPersonal(apdu);    return;
//            case INS_GET_TRIES:          sendTries(apdu);       return;
//
//            case INS_GET_MEM:            getMem(apdu);          return;
//
//            case INS_GET_CARD_PUB:       getCardPublicKey(apdu);return;
//            case INS_SIGN_CHALLENGE:     signChallenge(apdu);   return;
//
//            default:
//                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
//        }
//    }
//
//    // =========================================================
//    // INIT / PIN
//    // =========================================================
//
//    // INIT_CARD
//    private void initCard(APDU apdu) {
//        if (blocked) ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
//
//        byte[] buf = apdu.getBuffer();
//        short totalLen = (short)(buf[ISO7816.OFFSET_LC] & 0xFF);
//
//        short received = apdu.setIncomingAndReceive();
//        short cdataOff = ISO7816.OFFSET_CDATA;
//        while (received < totalLen) received += apdu.receiveBytes((short)(cdataOff + received));
//
//        if (totalLen < (short)(1 + (2 * PIN_SIZE))) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//
//        short off = cdataOff;
//        byte idLen = buf[off++];
//        if (idLen <= 0 || idLen > (byte)CARDID_LEN) ISOException.throwIt(ISO7816.SW_WRONG_DATA);
//
//        short expectedLen = (short)(1 + (short)idLen + (short)(2 * PIN_SIZE));
//        if (totalLen != expectedLen) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//
//        short cardIdOff   = off;
//        short userPinOff  = (short)(cardIdOff + idLen);
//        short adminPinOff = (short)(userPinOff + PIN_SIZE);
//
//        Util.arrayFillNonAtomic(cardIdBuf, (short)0, CARDID_LEN, (byte)0x00);
//        Util.arrayCopyNonAtomic(buf, cardIdOff, cardIdBuf, (short)0, (short)idLen);
//
//        rng.generateData(mkBuf, (short)0, AES_KEY_LEN);
//        masterKey.setKey(mkBuf, (short)0);
//
//        hashMasterKey(mkBuf, (short)0, mkHash, (short)0);
//
//        deriveKeyFromPin(buf, userPinOff, PIN_SIZE, pinKeyBuf, (short)0);
//        wrapKey.setKey(pinKeyBuf, (short)0);
//        aesCipher.init(wrapKey, Cipher.MODE_ENCRYPT);
//        aesCipher.doFinal(mkBuf, (short)0, AES_KEY_LEN, encMK_User, (short)0);
//        Util.arrayFillNonAtomic(pinKeyBuf, (short)0, AES_KEY_LEN, (byte)0x00);
//
//        deriveKeyFromPin(buf, adminPinOff, PIN_SIZE, pinKeyBuf, (short)0);
//        wrapKey.setKey(pinKeyBuf, (short)0);
//        aesCipher.init(wrapKey, Cipher.MODE_ENCRYPT);
//        aesCipher.doFinal(mkBuf, (short)0, AES_KEY_LEN, encMK_Admin, (short)0);
//        Util.arrayFillNonAtomic(pinKeyBuf, (short)0, AES_KEY_LEN, (byte)0x00);
//
//        aesCipher.init(masterKey, Cipher.MODE_ENCRYPT);
//        aesCipher.doFinal(cardIdBuf, (short)0, CARDID_LEN, cardIdBuf, (short)0);
//
//        triesRemaining    = MAX_PIN_TRIES;
//        blocked           = false;
//        userAuthenticated = false;
//
//        // balance = 0 (8 bytes = 0)
//        resetBalanceZero();
//
//        Util.arrayFillNonAtomic(checkinBuf, (short)0, CHECKIN_LEN, (byte)0x00);
//        Util.arrayFillNonAtomic(avatarBuf, (short)0, AVATAR_LEN, (byte)0x00);
//        avatarDataLen = 0;
//    }
//
//    // VERIFY_PIN
//    private void verifyPin(APDU apdu) {
//        if (blocked || triesRemaining == 0) ISOException.throwIt((short)0x6983);
//
//        byte[] buf = apdu.getBuffer();
//        short len  = apdu.setIncomingAndReceive();
//        if (len != PIN_SIZE) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//
//        boolean ok = unlockMasterWithUserPin(buf, ISO7816.OFFSET_CDATA);
//        if (!ok) {
//            if (triesRemaining > 0) triesRemaining--;
//            if (triesRemaining == 0) blocked = true;
//            ISOException.throwIt((short)(0x63C0 | (triesRemaining & 0x0F)));
//        }
//
//        triesRemaining    = MAX_PIN_TRIES;
//        userAuthenticated = true;
//    }
//
//    // CHANGE_PIN
//    private void changePin(APDU apdu) {
//        if (blocked || triesRemaining == 0) ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
//
//        byte[] buf = apdu.getBuffer();
//        short len  = apdu.setIncomingAndReceive();
//        if (len != (short)(PIN_SIZE * 2)) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//
//        short offOld = ISO7816.OFFSET_CDATA;
//        short offNew = (short)(offOld + PIN_SIZE);
//
//        boolean ok = unlockMasterWithUserPin(buf, offOld);
//        if (!ok) {
//            if (triesRemaining > 0) triesRemaining--;
//            if (triesRemaining == 0) blocked = true;
//            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
//        }
//
//        masterKey.getKey(mkBuf, (short)0);
//
//        deriveKeyFromPin(buf, offNew, PIN_SIZE, pinKeyBuf, (short)0);
//        wrapKey.setKey(pinKeyBuf, (short)0);
//
//        aesCipher.init(wrapKey, Cipher.MODE_ENCRYPT);
//        aesCipher.doFinal(mkBuf, (short)0, AES_KEY_LEN, encMK_User, (short)0);
//
//        triesRemaining    = MAX_PIN_TRIES;
//        userAuthenticated = true;
//
//        Util.arrayFillNonAtomic(mkBuf, (short)0, AES_KEY_LEN, (byte)0x00);
//        Util.arrayFillNonAtomic(pinKeyBuf, (short)0, AES_KEY_LEN, (byte)0x00);
//    }
//
//    // UNLOCK
//    private void unlockCard(APDU apdu) {
//        byte[] buf = apdu.getBuffer();
//        short len  = apdu.setIncomingAndReceive();
//        if (len != PIN_SIZE) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//
//        boolean ok = unlockMasterWithAdminPin(buf, ISO7816.OFFSET_CDATA);
//        if (!ok) ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
//
//        triesRemaining    = MAX_PIN_TRIES;
//        blocked           = false;
//        userAuthenticated = false;
//    }
//
//    // ADMIN_SET_PIN
//    private void adminSetUserPin(APDU apdu) {
//        byte[] buf = apdu.getBuffer();
//        short len  = apdu.setIncomingAndReceive();
//        if (len != (short)(PIN_SIZE * 2)) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//
//        short offAdmin = ISO7816.OFFSET_CDATA;
//        short offNew   = (short)(offAdmin + PIN_SIZE);
//
//        boolean ok = unlockMasterWithAdminPin(buf, offAdmin);
//        if (!ok) ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
//
//        masterKey.getKey(mkBuf, (short)0);
//
//        deriveKeyFromPin(buf, offNew, PIN_SIZE, pinKeyBuf, (short)0);
//        wrapKey.setKey(pinKeyBuf, (short)0);
//
//        aesCipher.init(wrapKey, Cipher.MODE_ENCRYPT);
//        aesCipher.doFinal(mkBuf, (short)0, AES_KEY_LEN, encMK_User, (short)0);
//
//        Util.arrayFillNonAtomic(mkBuf, (short)0, AES_KEY_LEN, (byte)0x00);
//        Util.arrayFillNonAtomic(pinKeyBuf, (short)0, AES_KEY_LEN, (byte)0x00);
//    }
//
//    // =========================================================
//    // AVATAR CHUNKING
//    // =========================================================
//
//    // AVATAR_BEGIN
//    private void avatarBegin(APDU apdu) {
//        if (!userAuthenticated) ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
//
//        byte[] buf = apdu.getBuffer();
//        short len = apdu.setIncomingAndReceive();
//        if (len != 2) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//
//        short L = Util.makeShort(buf[ISO7816.OFFSET_CDATA], buf[(short)(ISO7816.OFFSET_CDATA + 1)]);
//        if (L < 0 || L > (short)(AVATAR_LEN - 2)) ISOException.throwIt(ISO7816.SW_WRONG_DATA);
//
//        avatarDataLen = L;
//        Util.arrayFillNonAtomic(avatarBuf, (short)0, AVATAR_LEN, (byte)0x00);
//    }
//
//    // AVATAR_CHUNK
//    private void avatarChunk(APDU apdu) {
//        if (!userAuthenticated) ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
//
//        byte[] buf = apdu.getBuffer();
//        short lc = apdu.setIncomingAndReceive();
//        if (lc <= 0) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//
//        short offset = Util.makeShort(buf[ISO7816.OFFSET_P1], buf[ISO7816.OFFSET_P2]);
//        if (offset < 0 || offset >= avatarDataLen) ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
//        if ((short)(offset + lc) > avatarDataLen) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//
//        Util.arrayCopyNonAtomic(buf, ISO7816.OFFSET_CDATA, avatarBuf, offset, lc);
//    }
//
//    // AVATAR_END
//    private void avatarEnd(APDU apdu) {
//        if (!userAuthenticated) ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
//
//        short L = avatarDataLen;
//        avatarBuf[(short)(AVATAR_LEN - 2)] = (byte)((L >> 8) & 0xFF);
//        avatarBuf[(short)(AVATAR_LEN - 1)] = (byte)(L & 0xFF);
//
//        aesCipher.init(masterKey, Cipher.MODE_ENCRYPT);
//        for (short i = 0; i < AVATAR_LEN; i += 16) {
//            aesCipher.doFinal(avatarBuf, i, (short)16, avatarBuf, i);
//        }
//    }
//
//    // AVATAR_READ_CHUNK
//    private void avatarReadChunk(APDU apdu) {
//        if (!userAuthenticated) ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
//
//        byte[] buf = apdu.getBuffer();
//        short offset = Util.makeShort(buf[ISO7816.OFFSET_P1], buf[ISO7816.OFFSET_P2]);
//        if (offset < 0 || offset >= AVATAR_LEN) ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
//
//        short outMax = apdu.setOutgoing();
//        if (outMax <= 0) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//
//        short remain = (short)(AVATAR_LEN - offset);
//        short outLen = outMax;
//        if (outLen > remain) outLen = remain;
//
//        short startBlock = (short)(offset - (short)(offset % 16));
//        short end = (short)(offset + outLen);
//        short endBlock = (short)((end % 16 == 0) ? end : (short)(end + (short)(16 - (end % 16))));
//        if (endBlock > AVATAR_LEN) endBlock = AVATAR_LEN;
//
//        short copyLen = (short)(endBlock - startBlock);
//        Util.arrayCopyNonAtomic(avatarBuf, startBlock, buf, (short)0, copyLen);
//
//        aesCipher.init(masterKey, Cipher.MODE_DECRYPT);
//        for (short i = 0; i < copyLen; i += 16) {
//            aesCipher.doFinal(buf, i, (short)16, buf, i);
//        }
//
//        short innerOff = (short)(offset - startBlock);
//        Util.arrayCopyNonAtomic(buf, innerOff, buf, (short)0, outLen);
//
//        apdu.setOutgoingLength(outLen);
//        apdu.sendBytes((short)0, outLen);
//    }
//
//    // =========================================================
//    // PERSONAL WRITE/READ
//    // =========================================================
//
//    // WRITE_PERSONAL
//    private void writePersonal(APDU apdu) {
//        if (blocked || triesRemaining == 0) ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
//        if (!userAuthenticated) ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
//
//        byte[] buf = apdu.getBuffer();
//        short lc = apdu.setIncomingAndReceive();
//        if (lc < 1) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//
//        byte fieldId  = buf[ISO7816.OFFSET_CDATA];
//        short dataLen = (short)(lc - 1);
//        short srcOff  = (short)(ISO7816.OFFSET_CDATA + 1);
//
//        // Avatar chi dung chunking
//        if (fieldId == FIELD_AVATAR) ISOException.throwIt(ISO7816.SW_FUNC_NOT_SUPPORTED);
//
//        // CHECKIN plaintext
//        if (fieldId == FIELD_CHECKIN) {
//            if (dataLen > CHECKIN_LEN) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//            Util.arrayFillNonAtomic(checkinBuf, (short)0, CHECKIN_LEN, (byte)0x00);
//            if (dataLen > 0) Util.arrayCopyNonAtomic(buf, srcOff, checkinBuf, (short)0, dataLen);
//            return;
//        }
//
//        // BALANCE: nhan 8 bytes (khong parse long tren the)
//        if (fieldId == FIELD_BALANCE) {
//            if (dataLen != BALANCE_VALUE_LEN) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//            setBalanceInternalFrom8(buf, srcOff);
//            return;
//        }
//
//        // Text fields encrypt fixed-size
//        byte[] targetBuf;
//        short  targetLen;
//
//        switch (fieldId) {
//            case FIELD_NAME:    targetBuf = nameBuf;    targetLen = NAME_LEN;    break;
//            case FIELD_DOB:     targetBuf = dobBuf;     targetLen = DOB_LEN;     break;
//            case FIELD_PHONE:   targetBuf = phoneBuf;   targetLen = PHONE_LEN;   break;
//            case FIELD_ADDRESS: targetBuf = addressBuf; targetLen = ADDRESS_LEN; break;
//            case FIELD_PACKAGE: targetBuf = packageBuf; targetLen = PACKAGE_LEN; break;
//            case FIELD_CARDID:  targetBuf = cardIdBuf;  targetLen = CARDID_LEN;  break;
//            default:
//                ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
//                return;
//        }
//
//        if (dataLen > targetLen) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//
//        Util.arrayFillNonAtomic(targetBuf, (short)0, targetLen, (byte)0x00);
//        if (dataLen > 0) Util.arrayCopyNonAtomic(buf, srcOff, targetBuf, (short)0, dataLen);
//
//        aesCipher.init(masterKey, Cipher.MODE_ENCRYPT);
//        aesCipher.doFinal(targetBuf, (short)0, targetLen, targetBuf, (short)0);
//    }
//
//    // READ_PERSONAL
//    private void readPersonal(APDU apdu) {
//        if (blocked || triesRemaining == 0) ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
//        if (!userAuthenticated) ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
//
//        byte[] buf = apdu.getBuffer();
//        short lc = apdu.setIncomingAndReceive();
//        if (lc != 1) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//
//        byte fieldId = buf[ISO7816.OFFSET_CDATA];
//
//        // Avatar chi dung chunking
//        if (fieldId == FIELD_AVATAR) ISOException.throwIt(ISO7816.SW_FUNC_NOT_SUPPORTED);
//
//        // CHECKIN plaintext
//        if (fieldId == FIELD_CHECKIN) {
//            Util.arrayCopyNonAtomic(checkinBuf, (short)0, buf, (short)0, CHECKIN_LEN);
//            short actualLen = CHECKIN_LEN;
//            while (actualLen > 0 && buf[(short)(actualLen - 1)] == (byte)0x00) actualLen--;
//
//            apdu.setOutgoing();
//            apdu.setOutgoingLength(actualLen);
//            if (actualLen > 0) apdu.sendBytes((short)0, actualLen);
//            return;
//        }
//
//        // BALANCE: tra ve 8 bytes
//        if (fieldId == FIELD_BALANCE) {
//            getBalanceInternalTo8(buf, (short)0);
//            apdu.setOutgoing();
//            apdu.setOutgoingLength(BALANCE_VALUE_LEN);
//            apdu.sendBytes((short)0, BALANCE_VALUE_LEN);
//            return;
//        }
//
//        byte[] src;
//        short  srcLen;
//
//        switch (fieldId) {
//            case FIELD_NAME:    src = nameBuf;    srcLen = NAME_LEN;    break;
//            case FIELD_DOB:     src = dobBuf;     srcLen = DOB_LEN;     break;
//            case FIELD_PHONE:   src = phoneBuf;   srcLen = PHONE_LEN;   break;
//            case FIELD_ADDRESS: src = addressBuf; srcLen = ADDRESS_LEN; break;
//            case FIELD_PACKAGE: src = packageBuf; srcLen = PACKAGE_LEN; break;
//            case FIELD_CARDID:  src = cardIdBuf;  srcLen = CARDID_LEN;  break;
//            default:
//                ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
//                return;
//        }
//
//        aesCipher.init(masterKey, Cipher.MODE_DECRYPT);
//        aesCipher.doFinal(src, (short)0, srcLen, buf, (short)0);
//
//        short actualLen = srcLen;
//        while (actualLen > 0 && buf[(short)(actualLen - 1)] == (byte)0x00) actualLen--;
//
//        apdu.setOutgoing();
//        apdu.setOutgoingLength(actualLen);
//        if (actualLen > 0) apdu.sendBytes((short)0, actualLen);
//    }
//
//    // =========================================================
//    // BALANCE INTERNAL (8 bytes value, khong long)
//    // =========================================================
//
//    // set balance tu 8 bytes, encrypt vao balanceBuf
//    private void setBalanceInternalFrom8(byte[] src, short srcOff) {
//        Util.arrayFillNonAtomic(balanceTmp, (short)0, BALANCE_LEN, (byte)0x00);
//        Util.arrayCopyNonAtomic(src, srcOff, balanceTmp, (short)0, BALANCE_VALUE_LEN);
//
//        aesCipher.init(masterKey, Cipher.MODE_ENCRYPT);
//        aesCipher.doFinal(balanceTmp, (short)0, BALANCE_LEN, balanceBuf, (short)0);
//
//        Util.arrayFillNonAtomic(balanceTmp, (short)0, BALANCE_LEN, (byte)0x00);
//    }
//
//    // get balance -> decrypt balanceBuf, copy 8 bytes ra out
//    private void getBalanceInternalTo8(byte[] out, short outOff) {
//        Util.arrayCopyNonAtomic(balanceBuf, (short)0, balanceTmp, (short)0, BALANCE_LEN);
//
//        aesCipher.init(masterKey, Cipher.MODE_DECRYPT);
//        aesCipher.doFinal(balanceTmp, (short)0, BALANCE_LEN, balanceTmp, (short)0);
//
//        Util.arrayCopyNonAtomic(balanceTmp, (short)0, out, outOff, BALANCE_VALUE_LEN);
//        Util.arrayFillNonAtomic(balanceTmp, (short)0, BALANCE_LEN, (byte)0x00);
//    }
//
//    // reset balance = 0
//    private void resetBalanceZero() {
//        Util.arrayFillNonAtomic(balanceTmp, (short)0, BALANCE_LEN, (byte)0x00);
//        aesCipher.init(masterKey, Cipher.MODE_ENCRYPT);
//        aesCipher.doFinal(balanceTmp, (short)0, BALANCE_LEN, balanceBuf, (short)0);
//        Util.arrayFillNonAtomic(balanceTmp, (short)0, BALANCE_LEN, (byte)0x00);
//    }
//
//    // =========================================================
//    // MISC
//    // =========================================================
//
//    // Tra ve tries
//    private void sendTries(APDU apdu) {
//        byte[] buf = apdu.getBuffer();
//        buf[0] = triesRemaining;
//        apdu.setOutgoingAndSend((short)0, (short)1);
//    }
//
//    // Debug mem
//    private void getMem(APDU apdu) {
//        byte[] buf = apdu.getBuffer();
//
//        short persistent = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_PERSISTENT);
//        short tReset     = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_RESET);
//        short tDeselect  = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT);
//
//        Util.setShort(buf, (short)0, persistent);
//        Util.setShort(buf, (short)2, tReset);
//        Util.setShort(buf, (short)4, tDeselect);
//
//        apdu.setOutgoing();
//        apdu.setOutgoingLength((short)6);
//        apdu.sendBytes((short)0, (short)6);
//    }
//
//    // =========================================================
//    // RSA PUBLIC KEY + SIGN
//    // =========================================================
//
//    // GET_CARD_PUBLIC_KEY
//    private void getCardPublicKey(APDU apdu) {
//        byte[] buf = apdu.getBuffer();
//        short offset = Util.makeShort(buf[ISO7816.OFFSET_P1], buf[ISO7816.OFFSET_P2]);
//        if (offset < 0 || offset >= RSA_MOD_LEN) ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
//
//        short modLen = cardPublicKey.getModulus(buf, (short)0);
//        if (modLen != RSA_MOD_LEN) ISOException.throwIt(ISO7816.SW_WRONG_DATA);
//
//        short remain = (short)(RSA_MOD_LEN - offset);
//        short outLen = (remain > GET_PUB_CHUNK) ? GET_PUB_CHUNK : remain;
//
//        Util.arrayCopyNonAtomic(buf, offset, buf, (short)0, outLen);
//
//        apdu.setOutgoing();
//        apdu.setOutgoingLength(outLen);
//        apdu.sendBytes((short)0, outLen);
//    }
//
//    // SIGN_CHALLENGE
//    private void signChallenge(APDU apdu) {
//        byte[] buf = apdu.getBuffer();
//        short len = apdu.setIncomingAndReceive();
//        if (len <= 0) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//
//        rsaSign.init(cardPrivateKey, Signature.MODE_SIGN);
//        short sigLen = rsaSign.sign(buf, ISO7816.OFFSET_CDATA, len, buf, (short)0);
//
//        apdu.setOutgoing();
//        apdu.setOutgoingLength(sigLen);
//        apdu.sendBytes((short)0, sigLen);
//    }
//}
