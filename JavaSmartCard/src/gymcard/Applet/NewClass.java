// package ProjectCuoiKy;
//
// import javacard.framework.*;
// import javacard.security.*;
// import javacardx.crypto.*;
//
// public class ProjectCuoiKy extends Applet {
//
// private RandomData rng;
// // ========= PIN CONFIG =========
// private static final byte PIN_SIZE = (byte) 6; // 6 ch s
// private static final byte MAX_PIN_TRIES = (byte) 3;
//
// private byte triesRemaining;
// private boolean blocked;
// private boolean userAuthenticated; // true sau khi VERIFY_PIN ok
//
// // ========= AES / HASH =========
// private static final short AES_KEY_LEN = (short) 16; // 128-bit
// private static final short MK_HASH_LEN = (short) 16; // hash(MK) ct 16 byte
//
// /** MasterKey dùng mã hóa d liu */
// private AESKey masterKey;
// /** wrapKey dùng  bc/gii bc MK di user/admin PIN */
// private AESKey wrapKey;
//
// private Cipher aesCipher;
// private MessageDigest sha; // SHA-1
// private byte[] tmpKeyBuf; // 20 bytes cho output SHA-1
// private byte[] mkBuf; // 16 bytes cha MK plaintext
// private byte[] mkHash; // hash(MK) dùng  verify PIN
//
// /** MK c bc bi user/admin PIN */
// private byte[] encMK_User; // AES(MK, KDF(userPIN))
// private byte[] encMK_Admin; // AES(MK, KDF(adminPIN))
//
// // ========= D LIU CÁ NHÂN =========
// private static final short NAME_LEN = (short) 64;
// private static final short DOB_LEN = (short) 16;
// private static final short PHONE_LEN = (short) 16;
// private static final short ADDRESS_LEN = (short) 128;
// private static final short PACKAGE_LEN = (short) 32;
// private static final short CARDID_LEN = (short) 32;
// private static final short AVATAR_LEN = (short) 4096; // ảnh đã nén,
// thumbnail
// private static final short CHECKIN_LEN = (short) 64; // Check-in data (không
// mã hóa)
// private static final short BALANCE_LEN = (short) 16; // Số dư (có mã hóa AES)
//
// private byte[] nameBuf;
// private byte[] dobBuf;
// private byte[] phoneBuf;
// private byte[] addressBuf;
// private byte[] packageBuf;
// private byte[] cardIdBuf;
// private byte[] avatarBuf;
// private byte[] checkinBuf; // Lưu check-in data (không mã hóa)
// private byte[] balanceBuf; // Lưu số dư (có mã hóa AES)
//
// // ========= RSA CH KÝ (XÁC THC TH) =========
// private RSAPublicKey cardPublicKey;
// private RSAPrivateKey cardPrivateKey;
// private KeyPair cardKeyPair;
// private Signature rsaSign;
//
// private static final short RSA_KEY_LEN_BITS = KeyBuilder.LENGTH_RSA_1024;
// private static final byte[] EXP_F4 = { 0x01, 0x00, 0x01 }; // 65537
//
// private static final short RSA_MOD_LEN = (short) 128; // 1024-bit modulus
// private static final short GET_PUB_CHUNK = (short) 64; // mi ln tr ti a
// 64 bytes
//
// // ========= APDU =========
// private static final byte CLA_GYM = (byte) 0x80;
//
// private static final byte INS_INIT_CARD = (byte) 0x10;
// private static final byte INS_VERIFY_PIN = (byte) 0x20;
// private static final byte INS_CHANGE_PIN = (byte) 0x21;
// private static final byte INS_UNLOCK = (byte) 0x22;
// private static final byte INS_ADMIN_SET_PIN = (byte) 0x23;
//
// private static final byte INS_WRITE_PERSONAL = (byte) 0x30;
// private static final byte INS_READ_PERSONAL = (byte) 0x31;
// private static final byte INS_GET_TRIES = (byte) 0x32;
//
// private static final byte INS_GET_CARD_PUB = (byte) 0x40;
// private static final byte INS_SIGN_CHALLENGE = (byte) 0x41;
// private static final byte INS_AVATAR_BEGIN = (byte)0x50;
// private static final byte INS_AVATAR_CHUNK = (byte)0x51;
// private static final byte INS_AVATAR_END = (byte)0x52;
// private static final byte INS_AVATAR_READ_CHUNK = (byte)0x53;
// private static final byte FIELD_NAME = (byte) 0x00;
// private static final byte FIELD_DOB = (byte) 0x01;
// private static final byte FIELD_PHONE = (byte) 0x02;
// private static final byte FIELD_ADDRESS = (byte) 0x03;
// private static final byte FIELD_PACKAGE = (byte) 0x04;
// private static final byte FIELD_CARDID = (byte) 0x05;
// private static final byte FIELD_AVATAR = (byte) 0x06;
// private static final byte FIELD_CHECKIN = (byte) 0x07; // Check-in data
// (không mã hóa)
// private static final byte FIELD_BALANCE = (byte) 0x08; // Số dư (có mã hóa
// AES)
//
// private byte[] pinKeyBuf; // 16 bytes
// private byte[] mkHashCand; // 16 bytes: hash(MK_candidate)
// private static final byte INS_GET_MEM = (byte) 0x55;
// private short avatarDataLen = 0; // s byte tht s (0..4094)
//
// // ========= INSTALL =========
// public static void install(byte[] bArray, short bOffset, byte bLength) {
// new ProjectCuoiKy();
// }
//
// protected ProjectCuoiKy() {
// rng = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
// pinKeyBuf = new byte[AES_KEY_LEN];
// mkHashCand = new byte[MK_HASH_LEN]; // thêm dòng này
// // --- PIN state ---
// triesRemaining = MAX_PIN_TRIES;
// blocked = false;
// userAuthenticated = false;
//
// // --- AES keys ---
// masterKey = (AESKey) KeyBuilder.buildKey(
// KeyBuilder.TYPE_AES,
// KeyBuilder.LENGTH_AES_128,
// false
// );
// wrapKey = (AESKey) KeyBuilder.buildKey(
// KeyBuilder.TYPE_AES,
// KeyBuilder.LENGTH_AES_128,
// false
// );
//
// aesCipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_ECB_NOPAD, false);
// sha = MessageDigest.getInstance(MessageDigest.ALG_SHA, false);
//
// tmpKeyBuf = new byte[20];
// mkBuf = new byte[AES_KEY_LEN];
// mkHash = new byte[MK_HASH_LEN];
// encMK_User = new byte[AES_KEY_LEN];
// encMK_Admin = new byte[AES_KEY_LEN];
//
// // Tm thi MK = 0...0 cho n khi INIT_CARD
// Util.arrayFillNonAtomic(mkBuf, (short) 0, AES_KEY_LEN, (byte) 0x00);
// masterKey.setKey(mkBuf, (short) 0);
//
// // --- Buffers d liu cá nhân ---
// nameBuf = new byte[NAME_LEN];
// dobBuf = new byte[DOB_LEN];
// phoneBuf = new byte[PHONE_LEN];
// addressBuf = new byte[ADDRESS_LEN];
// packageBuf = new byte[PACKAGE_LEN];
// cardIdBuf = new byte[CARDID_LEN];
// avatarBuf = new byte[AVATAR_LEN];
// checkinBuf = new byte[CHECKIN_LEN]; // Buffer check-in (không mã hóa)
// balanceBuf = new byte[BALANCE_LEN]; // Buffer số dư (có mã hóa AES)
//
// // --- RSA keypair cho th (dùng ký s challenge) ---
// cardPublicKey = (RSAPublicKey) KeyBuilder.buildKey(
// KeyBuilder.TYPE_RSA_PUBLIC, RSA_KEY_LEN_BITS, false);
// cardPrivateKey = (RSAPrivateKey) KeyBuilder.buildKey(
// KeyBuilder.TYPE_RSA_PRIVATE, RSA_KEY_LEN_BITS, false);
// cardKeyPair = new KeyPair(cardPublicKey, cardPrivateKey);
// cardKeyPair.genKeyPair();
//
// // Set exponent = F4 (65537)
// cardPublicKey.setExponent(EXP_F4, (short) 0, (short) EXP_F4.length);
//
// rsaSign = Signature.getInstance(Signature.ALG_RSA_SHA_PKCS1, false);
//
// register();
// }
//
// // ========= KDF & HASH MK =========
//
// /**
// * KDF n gin kiu PBKDF2:
// * keyFromPin = SHA1( PIN || "GYMCARD-KDF" )[0..15]
// */
// private void deriveKeyFromPin(byte[] pinBuf, short pinOff, short pinLen,
// byte[] outKey, short outOff) {
// sha.reset();
// sha.update(pinBuf, pinOff, pinLen);
// byte[] salt = { 'G','Y','M','C','A','R','D','-','K','D','F' };
// sha.doFinal(salt, (short)0, (short)salt.length, tmpKeyBuf, (short)0);
//
// Util.arrayCopyNonAtomic(tmpKeyBuf, (short)0, outKey, outOff, AES_KEY_LEN);
//
// // ch xóa tmpKeyBuf (buffer trung gian), KHÔNG xóa outKey
// Util.arrayFillNonAtomic(tmpKeyBuf, (short)0, (short)tmpKeyBuf.length,
// (byte)0x00);
// }
//
// /**
// * mkHash = SHA1( MK || "GYMCARD-MK" )[0..15]
// * Lu li  dùng verify PIN v sau.
// */
// private void hashMasterKey(byte[] mk, short mkOff,
// byte[] outHash, short outOff) {
// sha.reset();
// sha.update(mk, mkOff, AES_KEY_LEN);
// byte[] salt = { 'G','Y','M','C','A','R','D','-','M','K' };
// sha.doFinal(salt, (short) 0, (short) salt.length, tmpKeyBuf, (short) 0);
//
// Util.arrayCopyNonAtomic(tmpKeyBuf, (short) 0,
// outHash, outOff,
// MK_HASH_LEN);
//
// Util.arrayFillNonAtomic(tmpKeyBuf, (short) 0,
// (short) tmpKeyBuf.length, (byte) 0x00);
// }
//
// private boolean unlockMasterWithUserPin(byte[] buf, short pinOff) {
// // 1) KDF t PIN -> pinKeyBuf (16 bytes)
// deriveKeyFromPin(buf, pinOff, PIN_SIZE, pinKeyBuf, (short) 0);
// wrapKey.setKey(pinKeyBuf, (short) 0);
//
// // 2) Gii mã MK_candidate = AES_dec(encMK_User, keyFromPin)
// aesCipher.init(wrapKey, Cipher.MODE_DECRYPT);
// aesCipher.doFinal(encMK_User, (short) 0, AES_KEY_LEN, mkBuf, (short) 0);
//
// // 3) Hash MK_candidate -> mkHashCand (KHÔNG dùng tmpKeyBuf làm output)
// hashMasterKey(mkBuf, (short) 0, mkHashCand, (short) 0);
//
// // 4) Compare hash
// boolean ok = (Util.arrayCompare(
// mkHashCand, (short) 0,
// mkHash, (short) 0,
// MK_HASH_LEN
// ) == 0);
//
// if (ok) {
// masterKey.setKey(mkBuf, (short) 0);
// }
//
// // 5) Clear sensitive buffers
// Util.arrayFillNonAtomic(mkHashCand, (short) 0, MK_HASH_LEN, (byte) 0x00);
// Util.arrayFillNonAtomic(pinKeyBuf, (short) 0, AES_KEY_LEN, (byte) 0x00);
// Util.arrayFillNonAtomic(mkBuf, (short) 0, AES_KEY_LEN, (byte) 0x00);
//
// return ok;
// }
//
// private boolean unlockMasterWithAdminPin(byte[] buf, short pinOff) {
// // 1) KDF t PIN -> pinKeyBuf
// deriveKeyFromPin(buf, pinOff, PIN_SIZE, pinKeyBuf, (short) 0);
// wrapKey.setKey(pinKeyBuf, (short) 0);
//
// // 2) Gii mã MK_candidate = AES_dec(encMK_Admin, keyFromPin)
// aesCipher.init(wrapKey, Cipher.MODE_DECRYPT);
// aesCipher.doFinal(encMK_Admin, (short) 0, AES_KEY_LEN, mkBuf, (short) 0);
//
// // 3) Hash -> mkHashCand
// hashMasterKey(mkBuf, (short) 0, mkHashCand, (short) 0);
//
// // 4) Compare
// boolean ok = (Util.arrayCompare(
// mkHashCand, (short) 0,
// mkHash, (short) 0,
// MK_HASH_LEN
// ) == 0);
//
// if (ok) {
// masterKey.setKey(mkBuf, (short) 0);
// }
//
// // Clear
// Util.arrayFillNonAtomic(mkHashCand, (short) 0, MK_HASH_LEN, (byte) 0x00);
// Util.arrayFillNonAtomic(pinKeyBuf, (short) 0, AES_KEY_LEN, (byte) 0x00);
// Util.arrayFillNonAtomic(mkBuf, (short) 0, AES_KEY_LEN, (byte) 0x00);
//
// return ok;
// }
// private short avatarPlainLen; //  dài nh tht
// private short avatarBytesWritten;
// private boolean avatarWriting;
// // ========= PROCESS =========
// public void process(APDU apdu) {
// if (selectingApplet()) {
// // Khi select li  reset trng thái login
// userAuthenticated = false;
// return;
// }
//
// byte[] buf = apdu.getBuffer();
// if (buf[ISO7816.OFFSET_CLA] != CLA_GYM) {
// ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
// }
//
// byte ins = buf[ISO7816.OFFSET_INS];
//
// switch (ins) {
// case INS_INIT_CARD:
// initCard(apdu); return;
// case INS_VERIFY_PIN:
// verifyPin(apdu); return;
// case INS_CHANGE_PIN:
// changePin(apdu); return;
// case INS_UNLOCK:
// unlockCard(apdu); return;
// case INS_ADMIN_SET_PIN:
// adminSetUserPin(apdu); return;
// case INS_AVATAR_BEGIN: avatarBegin(apdu); return;
// case INS_AVATAR_CHUNK: avatarChunk(apdu); return;
// case INS_AVATAR_END: avatarEnd(apdu); return;
// case INS_AVATAR_READ_CHUNK: avatarReadChunk(apdu); return;
// case INS_WRITE_PERSONAL:
// writePersonal(apdu); return;
// case INS_READ_PERSONAL:
// readPersonal(apdu); return;
// case INS_GET_TRIES:
// sendTries(apdu); return;
// case INS_GET_MEM:
// getMem(apdu); return;
//
// case INS_GET_CARD_PUB:
// getCardPublicKey(apdu);return;
// case INS_SIGN_CHALLENGE:
// signChallenge(apdu); return;
//
// default:
// ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
// }
// }
// private short avatarWriteOffset = 0;
//
// private void avatarBegin(APDU apdu) {
// if (!userAuthenticated)
// ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
//
// byte[] buf = apdu.getBuffer();
// short len = apdu.setIncomingAndReceive();
// if (len != 2) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//
// short L = Util.makeShort(buf[ISO7816.OFFSET_CDATA],
// buf[(short)(ISO7816.OFFSET_CDATA+1)]);
// if (L < 0 || L > (short)(AVATAR_LEN - 2))
// ISOException.throwIt(ISO7816.SW_WRONG_DATA);
//
// avatarDataLen = L;
// avatarWriteOffset = 0;
// // clear buffer plaintext area (tùy bn)
// Util.arrayFillNonAtomic(avatarBuf, (short)0, AVATAR_LEN, (byte)0x00);
// }
//
// private void avatarChunk(APDU apdu) {
// if (!userAuthenticated)
// ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
//
// byte[] buf = apdu.getBuffer();
// short len = apdu.setIncomingAndReceive();
// if (len <= 0) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//
// // P1P2 = offset trong payload (0..avatarDataLen)
// short offset = Util.makeShort(buf[ISO7816.OFFSET_P1],
// buf[ISO7816.OFFSET_P2]);
// if (offset < 0 || offset >= avatarDataLen)
// ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
//
// // copy plaintext chunk vào avatarBufPlainTemp (chúng ta s encrypt sau 
// END)
// // ->  n gin: ghi plaintext vào avatarBuf luôn, ri END s encrypt toàn
// b 4096
// // avatarBuf ang dùng  lu ciphertext cui cùng; tm thi nó là plaintext
// Util.arrayCopyNonAtomic(buf, ISO7816.OFFSET_CDATA, avatarBuf, offset, len);
// }
//
// private void avatarEnd(APDU apdu) {
// if (!userAuthenticated)
// ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
//
// // ghi header length 2 bytes  cui vùng plaintext trc khi encrypt
// // ( khi decrypt c li bit len)
// short len = avatarDataLen;
// avatarBuf[(short)(AVATAR_LEN - 2)] = (byte)((len >> 8) & 0xFF);
// avatarBuf[(short)(AVATAR_LEN - 1)] = (byte)(len & 0xFF);
//
// // encrypt 4096 bytes (block 16)
// aesCipher.init(masterKey, Cipher.MODE_ENCRYPT);
// for (short i = 0; i < AVATAR_LEN; i += 16) {
// aesCipher.doFinal(avatarBuf, i, (short)16, avatarBuf, i);
// }
// }
//
// private void avatarReadChunk(APDU apdu) {
// if (!userAuthenticated)
// ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
//
// byte[] buf = apdu.getBuffer();
// short offset = Util.makeShort(buf[ISO7816.OFFSET_P1],
// buf[ISO7816.OFFSET_P2]);
//
// short le = (short)(buf[ISO7816.OFFSET_LC] & 0xFF);
// // Vi CommandAPDU(CLA, INS, p1, p2, le) thì JavaCard s set Le, nhng c Le
// chun thì hay dùng:
// // short le = apdu.setOutgoing(); ... (mình dùng cách di cho chc)
//
// short outMax = apdu.setOutgoing(); // nhn Le
// short outLen = outMax;
// if (outLen <= 0) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//
// // decrypt 1 bn sao chunk ra buf ri tr (không decrypt c 4096 1 lúc)
// // ta cn decrypt tng block 16 tng ng.
// if (offset < 0 || offset >= AVATAR_LEN)
// ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
//
// short remain = (short)(AVATAR_LEN - offset);
// if (outLen > remain) outLen = remain;
//
// // làm tròn lên theo block 16  decrypt úng, nhng ch send outLen
// short startBlock = (short)(offset - (short)(offset % 16));
// short end = (short)(offset + outLen);
// short endBlock = (short)(end % 16 == 0 ? end : (short)(end + (short)(16 -
// (end % 16))));
// if (endBlock > AVATAR_LEN) endBlock = AVATAR_LEN;
//
// // copy ciphertext cn thit vào buf[0..]
// short copyLen = (short)(endBlock - startBlock);
// Util.arrayCopyNonAtomic(avatarBuf, startBlock, buf, (short)0, copyLen);
//
// // decrypt blocks in-place in buf
// aesCipher.init(masterKey, Cipher.MODE_DECRYPT);
// for (short i = 0; i < copyLen; i += 16) {
// aesCipher.doFinal(buf, i, (short)16, buf, i);
// }
//
// // shift  tr úng offset trong chunk
// short innerOff = (short)(offset - startBlock);
// Util.arrayCopyNonAtomic(buf, innerOff, buf, (short)0, outLen);
//
// apdu.setOutgoingLength(outLen);
// apdu.sendBytes((short)0, outLen);
// }
//
// private void getMem(APDU apdu) {
// byte[] buf = apdu.getBuffer();
//
// short persistent =
// JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_PERSISTENT);
// short tReset =
// JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_RESET);
// short tDeselect =
// JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT);
//
// // tr v: persistent(2) | tReset(2) | tDeselect(2)
// Util.setShort(buf, (short)0, persistent);
// Util.setShort(buf, (short)2, tReset);
// Util.setShort(buf, (short)4, tDeselect);
//
// apdu.setOutgoing();
// apdu.setOutgoingLength((short)6);
// apdu.sendBytes((short)0, (short)6);
// }
//
// /**
// * INIT_CARD:
// * data = cardIdLen(1) | cardId | userPIN(6) | adminPIN(6)
// *
// * - To MK random
// * - mkHash = hash(MK)
// * - encMK_User = AES(MK, KDF(userPIN))
// * - encMK_Admin = AES(MK, KDF(adminPIN))
// * - masterKey = MK
// * - cardIdBuf = cardId mã hóa bi MK
// */
// private void initCard(APDU apdu) {
//
// if (blocked) {
// ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
// }
//
// byte[] buf = apdu.getBuffer();
//
// // Lc nm  OFFSET_LC (1 byte) trong JavaCard 2.2.1
// short totalLen = (short) (buf[ISO7816.OFFSET_LC] & 0xFF);
//
// // Nhn chunk u
// short received = apdu.setIncomingAndReceive();
// short cdataOff = ISO7816.OFFSET_CDATA;
//
// // Nhn nt cho  totalLen
// while (received < totalLen) {
// received += apdu.receiveBytes((short) (cdataOff + received));
// }
//
// // data = idLen(1) + id + userPIN(6) + adminPIN(6)
// if (totalLen < (short) (1 + (short)(2 * PIN_SIZE))) {
// ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
// }
//
// short off = cdataOff;
//
// byte idLen = buf[off++];
// if (idLen <= 0 || idLen > (byte) CARDID_LEN) {
// ISOException.throwIt(ISO7816.SW_WRONG_DATA);
// }
//
// short expectedLen = (short) (1 + (short) idLen + (short)(2 * PIN_SIZE));
// if (totalLen != expectedLen) {
// ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
// }
//
// short cardIdOff = off;
// short userPinOff = (short) (cardIdOff + idLen);
// short adminPinOff = (short) (userPinOff + PIN_SIZE);
//
// // 1) copy cardId plaintext
// Util.arrayFillNonAtomic(cardIdBuf, (short) 0, CARDID_LEN, (byte) 0x00);
// Util.arrayCopyNonAtomic(buf, cardIdOff, cardIdBuf, (short) 0, (short) idLen);
//
// // 2) gen MK
// rng.generateData(mkBuf, (short) 0, AES_KEY_LEN);
// masterKey.setKey(mkBuf, (short) 0);
//
// // 3) mkHash
// hashMasterKey(mkBuf, (short) 0, mkHash, (short) 0);
//
//// user
// deriveKeyFromPin(buf, userPinOff, PIN_SIZE, pinKeyBuf, (short)0);
// wrapKey.setKey(pinKeyBuf, (short)0);
// aesCipher.init(wrapKey, Cipher.MODE_ENCRYPT);
// aesCipher.doFinal(mkBuf, (short)0, AES_KEY_LEN, encMK_User, (short)0);
// Util.arrayFillNonAtomic(pinKeyBuf, (short)0, AES_KEY_LEN, (byte)0x00);
//
//// admin
// deriveKeyFromPin(buf, adminPinOff, PIN_SIZE, pinKeyBuf, (short)0);
// wrapKey.setKey(pinKeyBuf, (short)0);
// aesCipher.init(wrapKey, Cipher.MODE_ENCRYPT);
// aesCipher.doFinal(mkBuf, (short)0, AES_KEY_LEN, encMK_Admin, (short)0);
// Util.arrayFillNonAtomic(pinKeyBuf, (short)0, AES_KEY_LEN, (byte)0x00);
//
// // 6) encrypt cardIdBuf by MK (32 bytes multiple of 16 OK)
// aesCipher.init(masterKey, Cipher.MODE_ENCRYPT);
// aesCipher.doFinal(cardIdBuf, (short) 0, CARDID_LEN, cardIdBuf, (short) 0);
//
// // 7) reset state
// triesRemaining = MAX_PIN_TRIES;
// blocked = false;
// userAuthenticated = false;
// }
//
// private void verifyPin(APDU apdu) {
// if (blocked || triesRemaining == 0) {
// ISOException.throwIt((short)0x6983); // blocked
// }
//
// byte[] buf = apdu.getBuffer();
// short len = apdu.setIncomingAndReceive();
// if (len != PIN_SIZE) {
// ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
// }
//
// boolean ok = unlockMasterWithUserPin(buf, ISO7816.OFFSET_CDATA);
//
// if (!ok) {
// if (triesRemaining > 0) triesRemaining--;
// if (triesRemaining == 0) blocked = true;
//
// // tr 63C{tries}
// ISOException.throwIt((short)(0x63C0 | (triesRemaining & 0x0F)));
// }
//
// triesRemaining = MAX_PIN_TRIES;
// userAuthenticated = true;
// }
//
// // ========= CHANGE_PIN =========
// /**
// * CHANGE_PIN:
// * PC gi: oldPIN(6) | newPIN(6)
// * - M MK bng oldPIN
// * - Ly MK hin ti t masterKey
// * - Bc li encMK_User = AES(MK, KDF(newPIN))
// * - mkHash gi nguyên (vì MK không i)
// */
// private void changePin(APDU apdu) {
// if (blocked || triesRemaining == 0) {
// ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
// }
//
// byte[] buf = apdu.getBuffer();
// short len = apdu.setIncomingAndReceive();
// if (len != (short) (PIN_SIZE * 2)) {
// ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
// }
//
// short offOld = ISO7816.OFFSET_CDATA;
// short offNew = (short) (offOld + PIN_SIZE);
//
// // 1. M MK bng old PIN
// boolean ok = unlockMasterWithUserPin(buf, offOld);
// if (!ok) {
// if (triesRemaining > 0) {
// triesRemaining--;
// }
// if (triesRemaining == 0) {
// blocked = true;
// }
// ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
// }
//
// // 2. Ly MK hin ti ra mkBuf t masterKey
// masterKey.getKey(mkBuf, (short) 0);
//
// // 3. KDF newPIN và bc li encMK_User
// deriveKeyFromPin(buf, offNew, PIN_SIZE, pinKeyBuf, (short) 0);
// wrapKey.setKey(pinKeyBuf, (short) 0);
// aesCipher.init(wrapKey, Cipher.MODE_ENCRYPT);
// aesCipher.doFinal(mkBuf, (short) 0, AES_KEY_LEN, encMK_User, (short) 0);
//
// // mkHash không i
// triesRemaining = MAX_PIN_TRIES;
// userAuthenticated = true; // ã prove old PIN
//
// Util.arrayFillNonAtomic(mkBuf, (short) 0, AES_KEY_LEN, (byte) 0x00);
// Util.arrayFillNonAtomic(pinKeyBuf, (short) 0, AES_KEY_LEN, (byte) 0x00);
// }
//
// // ========= UNLOCK (admin ch reset tries, không i PIN/MK) =========
// /**
// * UNLOCK_CARD:
// * PC gi: adminPIN(6)
// * - Nu adminPIN úng (ging lung user: KDF  gii encMK_Admin  hash(MK)),
// thì:
// * - triesRemaining = MAX_PIN_TRIES
// * - blocked = false
// * - KHÔNG i MK/PIN
// */
// private void unlockCard(APDU apdu) {
// byte[] buf = apdu.getBuffer();
// short len = apdu.setIncomingAndReceive();
// if (len != PIN_SIZE) {
// ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
// }
//
// short adminOff = ISO7816.OFFSET_CDATA;
// boolean ok = unlockMasterWithAdminPin(buf, adminOff);
// if (!ok) {
// ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
// }
//
// triesRemaining = MAX_PIN_TRIES;
// blocked = false;
// userAuthenticated = false; // unlock xong, user vn cha login
// }
//
// // ========= ADMIN_SET_USER_PIN =========
// /**
// * ADMIN_SET_PIN:
// * PC gi: adminPIN(6) | newUserPIN(6)
// * - Auth adminPIN (lung ging user)
// * - Ly MK t masterKey
// * - Bc li encMK_User = AES(MK, KDF(newUserPIN))
// * - mkHash gi nguyên, encMK_Admin gi nguyên
// */
// private void adminSetUserPin(APDU apdu) {
// byte[] buf = apdu.getBuffer();
// short len = apdu.setIncomingAndReceive();
// if (len != (short) (PIN_SIZE * 2)) {
// ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
// }
//
// short offAdmin = ISO7816.OFFSET_CDATA;
// short offNew = (short) (offAdmin + PIN_SIZE);
//
// // 1. Auth admin bng admin PIN
// boolean ok = unlockMasterWithAdminPin(buf, offAdmin);
// if (!ok) {
// ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
// }
//
// // 2. Ly MK ra t masterKey
// masterKey.getKey(mkBuf, (short) 0);
//
// // 3. Bc li encMK_User di newUserPIN
// deriveKeyFromPin(buf, offNew, PIN_SIZE, pinKeyBuf, (short) 0);
// wrapKey.setKey(pinKeyBuf, (short) 0);
// aesCipher.init(wrapKey, Cipher.MODE_ENCRYPT);
// aesCipher.doFinal(mkBuf, (short) 0, AES_KEY_LEN, encMK_User, (short) 0);
//
// Util.arrayFillNonAtomic(mkBuf, (short) 0, AES_KEY_LEN, (byte) 0x00);
// Util.arrayFillNonAtomic(pinKeyBuf, (short) 0,
// (short) pinKeyBuf.length, (byte) 0x00);
// }
//
// // ========= WRITE / READ PERSONAL =========
// private void writePersonal(APDU apdu) {
// if (blocked || triesRemaining == 0)
// ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
// if (!userAuthenticated)
// ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
//
// byte[] buf = apdu.getBuffer();
// short len = apdu.setIncomingAndReceive();
// if (len < 1) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//
// byte fieldId = buf[ISO7816.OFFSET_CDATA];
// short dataLen = (short) (len - 1);
// short srcOff = (short) (ISO7816.OFFSET_CDATA + 1);
//
// byte[] targetBuf;
// short targetLen;
//
// switch (fieldId) {
// case FIELD_NAME: targetBuf = nameBuf; targetLen = NAME_LEN; break;
// case FIELD_DOB: targetBuf = dobBuf; targetLen = DOB_LEN; break;
// case FIELD_PHONE: targetBuf = phoneBuf; targetLen = PHONE_LEN; break;
// case FIELD_ADDRESS: targetBuf = addressBuf; targetLen = ADDRESS_LEN; break;
// case FIELD_PACKAGE: targetBuf = packageBuf; targetLen = PACKAGE_LEN; break;
// case FIELD_CARDID: targetBuf = cardIdBuf; targetLen = CARDID_LEN; break;
// case FIELD_AVATAR: targetBuf = avatarBuf; targetLen = AVATAR_LEN; break;
// case FIELD_CHECKIN: targetBuf = checkinBuf; targetLen = CHECKIN_LEN; break;
// // Không mã hóa
// case FIELD_BALANCE: targetBuf = balanceBuf; targetLen = BALANCE_LEN; break;
// // Có mã hóa AES
// default:
// ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
// return;
// }
//
// // ====== SPECIAL: AVATAR (binary JPEG with length prefix) ======
// if (fieldId == FIELD_AVATAR) {
// if (dataLen <= 0) {
// // coi nh xóa avatar
// Util.arrayFillNonAtomic(targetBuf, (short)0, targetLen, (byte)0x00);
// } else {
// if (dataLen > (short)(AVATAR_LEN - 2)) {
// ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
// }
// Util.arrayFillNonAtomic(targetBuf, (short)0, targetLen, (byte)0x00);
//
// // write len prefix (big-endian)
// targetBuf[0] = (byte)((dataLen >> 8) & 0xFF);
// targetBuf[1] = (byte)(dataLen & 0xFF);
//
// // copy jpeg bytes
// Util.arrayCopyNonAtomic(buf, srcOff, targetBuf, (short)2, dataLen);
// }
//
// // encrypt full 4096
// aesCipher.init(masterKey, Cipher.MODE_ENCRYPT);
// aesCipher.doFinal(targetBuf, (short)0, targetLen, targetBuf, (short)0);
// return;
// }
//
// // ====== default for text fields ======
// if (dataLen > targetLen) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//
// Util.arrayFillNonAtomic(targetBuf, (short) 0, targetLen, (byte) 0x00);
// Util.arrayCopyNonAtomic(buf, srcOff, targetBuf, (short) 0, dataLen);
//
// // ====== FIELD_CHECKIN: KHÔNG mã hóa (plaintext) ======
// if (fieldId == FIELD_CHECKIN) {
// // Giữ nguyên plaintext, không mã hóa
// return;
// }
//
// // ====== FIELD_BALANCE: CÓ mã hóa AES ======
// // Các field khác cũng mã hóa
// aesCipher.init(masterKey, Cipher.MODE_ENCRYPT);
// aesCipher.doFinal(targetBuf, (short) 0, targetLen, targetBuf, (short) 0);
// }
// private void readPersonal(APDU apdu) {
// if (blocked || triesRemaining == 0)
// ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
// if (!userAuthenticated)
// ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
//
// byte[] buf = apdu.getBuffer();
// short len = apdu.setIncomingAndReceive();
// if (len != 1) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//
// byte fieldId = buf[ISO7816.OFFSET_CDATA];
//
// byte[] src;
// short srcLen;
//
// switch (fieldId) {
// case FIELD_NAME: src = nameBuf; srcLen = NAME_LEN; break;
// case FIELD_DOB: src = dobBuf; srcLen = DOB_LEN; break;
// case FIELD_PHONE: src = phoneBuf; srcLen = PHONE_LEN; break;
// case FIELD_ADDRESS: src = addressBuf; srcLen = ADDRESS_LEN; break;
// case FIELD_PACKAGE: src = packageBuf; srcLen = PACKAGE_LEN; break;
// case FIELD_CARDID: src = cardIdBuf; srcLen = CARDID_LEN; break;
// case FIELD_AVATAR: src = avatarBuf; srcLen = AVATAR_LEN; break;
// case FIELD_CHECKIN: src = checkinBuf; srcLen = CHECKIN_LEN; break; // Không
// mã hóa
// case FIELD_BALANCE: src = balanceBuf; srcLen = BALANCE_LEN; break; // Có mã
// hóa AES
// default:
// ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
// return;
// }
//
// // ====== FIELD_CHECKIN: KHÔNG mã hóa, đọc plaintext trực tiếp ======
// if (fieldId == FIELD_CHECKIN) {
// Util.arrayCopyNonAtomic(src, (short)0, buf, (short)0, srcLen);
// short actualLen = srcLen;
// while (actualLen > 0 && buf[(short)(actualLen - 1)] == (byte)0x00)
// actualLen--;
// apdu.setOutgoing();
// apdu.setOutgoingLength(actualLen);
// if (actualLen > 0) apdu.sendBytes((short)0, actualLen);
// return;
// }
//
// // decrypt -> buf[0..srcLen-1] (cho FIELD_BALANCE và các field khác)
// aesCipher.init(masterKey, Cipher.MODE_DECRYPT);
// aesCipher.doFinal(src, (short)0, srcLen, buf, (short)0);
//
// // ====== SPECIAL: AVATAR ======
// if (fieldId == FIELD_AVATAR) {
// // read prefix
// short jpegLen = (short)(((buf[0] & 0xFF) << 8) | (buf[1] & 0xFF));
// if (jpegLen <= 0 || jpegLen > (short)(AVATAR_LEN - 2)) {
// // không có avatar
// apdu.setOutgoing();
// apdu.setOutgoingLength((short)0);
// return;
// }
//
// apdu.setOutgoing();
// apdu.setOutgoingLength(jpegLen);
// apdu.sendBytes((short)2, jpegLen); // tr úng jpeg bytes
// return;
// }
//
// // ====== default: trim 0x00 for strings ======
// short actualLen = srcLen;
// while (actualLen > 0 && buf[(short)(actualLen - 1)] == (byte)0x00)
// actualLen--;
//
// apdu.setOutgoing();
// apdu.setOutgoingLength(actualLen);
// if (actualLen > 0) apdu.sendBytes((short)0, actualLen);
// }
//
// private void sendTries(APDU apdu) {
// byte[] buf = apdu.getBuffer();
// buf[0] = triesRemaining;
// apdu.setOutgoingAndSend((short) 0, (short) 1);
// }
//
// // ========= RSA PUBLIC KEY & SIGN CHALLENGE =========
//
// /**
// * GET_CARD_PUBLIC_KEY:
// * P1|P2 = offset; tr v 1 chunk modulus (64 bytes max)
// * Exponent luôn là 65537 (F4).
// */
// private void getCardPublicKey(APDU apdu) {
// byte[] buf = apdu.getBuffer();
// byte p1 = buf[ISO7816.OFFSET_P1];
// byte p2 = buf[ISO7816.OFFSET_P2];
//
// short offset = Util.makeShort(p1, p2);
// if (offset < 0 || offset >= RSA_MOD_LEN) {
// ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
// }
//
// // Ly full modulus vào buf[0..modLen-1]
// short modLen = cardPublicKey.getModulus(buf, (short) 0);
// if (modLen != RSA_MOD_LEN) {
// // Nu không úng kích thc thì li
// ISOException.throwIt(ISO7816.SW_WRONG_DATA);
// }
//
// short remain = (short) (RSA_MOD_LEN - offset);
// short outLen = (remain > GET_PUB_CHUNK) ? GET_PUB_CHUNK : remain;
//
// // copy on cn tr v bt u t offset  v buf[0..outLen-1]
// Util.arrayCopyNonAtomic(buf, offset, buf, (short) 0, outLen);
//
// apdu.setOutgoing();
// apdu.setOutgoingLength(outLen);
// apdu.sendBytes((short) 0, outLen);
// }
//
// /**
// * SIGN_CHALLENGE:
// * PC gi: challenge (random bytes)
// * Th: sign(challenge) bng cardPrivateKey + RSA-SHA1-PKCS1
// *  App verify bng cardPublicKey trong DB  xác thc "úng th".
// */
// private void signChallenge(APDU apdu) {
// byte[] buf = apdu.getBuffer();
// short len = apdu.setIncomingAndReceive();
// if (len <= 0) {
// ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
// }
//
// rsaSign.init(cardPrivateKey, Signature.MODE_SIGN);
// short sigLen = rsaSign.sign(buf, ISO7816.OFFSET_CDATA, len,
// buf, (short) 0);
//
// apdu.setOutgoing();
// apdu.setOutgoingLength(sigLen);
// apdu.sendBytes((short) 0, sigLen);
// }
// }
