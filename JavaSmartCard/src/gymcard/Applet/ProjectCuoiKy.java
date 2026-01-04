// package ProjectCuoiKy;
//
// import javacard.framework.*;
// import javacard.security.*;
// import javacardx.crypto.*;
//
// public class ProjectCuoiKy extends Applet {
//
// // ===================== RANDOM =====================
// private RandomData rng;
//
// // ===================== PIN CONFIG =====================
// private static final byte PIN_SIZE = (byte) 6;
// private static final byte MAX_PIN_TRIES = (byte) 3;
//
// private byte triesRemaining;
// private boolean blocked;
// private boolean userAuthenticated;
//
// // NEW: Flag to track if PIN is default
// private boolean isDefaultPin;
//
// // ===================== AES / HASH CONFIG =====================
// private static final short AES_KEY_LEN = (short) 16; // 128-bit AES
// private static final short SHA_256_LEN = (short) 32;
// private static final short MK_HASH_LEN = (short) 32; // Hash full 32 bytes
// private static final short SALT_LEN = (short) 16; // Salt 16 bytes
//
// private AESKey masterKey; // Key Object (RAM) - Chi chua key khi da login
// private AESKey wrapKey; // Key dung de unwrap MK
//
// private Cipher aesCipher;
// private MessageDigest sha;
//
// // Buffers
// private byte[] userSalt; // Salt cho User PIN (Persistent) - Regenerate khi
// doi PIN
// private byte[] adminSalt; // Salt cho Admin PIN (Persistent) - Chi tao 1 lan
// khi init
// private byte[] tmpKeyBuf; // Buffer tam cho SHA-256 (RAM/Transient tot hon
// nhung de array thuong cho don
// // gian)
//
// // Master Key Management Buffers
// private byte[] mkBuf; // 16 bytes MK plaintext (Transient RAM)
// private byte[] mkHash; // 32 bytes hash(MK) de verify (Persistent)
// private byte[] encMK_User; // MK ma hoa boi User PIN (Persistent)
// private byte[] encMK_Admin; // MK ma hoa boi Admin PIN (Persistent)
//
// private byte[] pinKeyBuf; // 16 bytes key derived tu PIN (Transient RAM)
// private byte[] mkHashCand; // 32 bytes hash candidate de so sanh (Transient
// RAM)
//
// // ===================== PERSONAL DATA =====================
// private static final short NAME_LEN = (short) 64;
// private static final short DOB_LEN = (short) 16;
// private static final short PHONE_LEN = (short) 16;
// private static final short ADDRESS_LEN = (short) 128;
// private static final short PACKAGE_LEN = (short) 32;
// private static final short CARDID_LEN = (short) 32;
//
// private static final short AVATAR_LEN = (short) 8192;
// private static final short CHECKIN_LEN = (short) 352; // 10 records x 35
// bytes + header
// private static final short SERVICES_LEN = (short) 256; // Purchased services
// data
//
// private static final short BALANCE_LEN = (short) 16;
// private static final short BALANCE_VALUE_LEN = (short) 8;
//
// private byte[] nameBuf;
// private byte[] dobBuf;
// private byte[] phoneBuf;
// private byte[] addressBuf;
// private byte[] packageBuf;
// private byte[] cardIdBuf;
//
// private byte[] avatarBuf;
// private byte[] checkinBuf;
// private byte[] servicesBuf; // Purchased services (plaintext)
// private byte[] balanceBuf; // Encrypted Balance
// private byte[] balanceTmp; // Transient RAM for Balance processing
//
// // ===================== RSA CONFIG (SECURE STORAGE) =====================
// private RSAPublicKey cardPublicKey;
// private RSAPrivateKey cardPrivateKey; // Object nay chi la "vo", du lieu nap
// vao khi can thiet
// private Signature rsaSign;
//
// private static final short RSA_KEY_LEN_BITS = KeyBuilder.LENGTH_RSA_1024;
// private static final byte[] EXP_F4 = { 0x01, 0x00, 0x01 };
//
// // RSA 1024 bit -> Modulus 128 bytes, Private Exponent 128 bytes
// private static final short RSA_MOD_LEN = (short) 128;
// private static final short GET_PUB_CHUNK = (short) 64;
//
// // STORAGE: Buffer luu tru Private Key da ma hoa (Persistent EEPROM)
// private byte[] encPrivMod; // AES(MK, PrivateModulus)
// private byte[] encPrivExp; // AES(MK, PrivateExponent)
//
// // RAM: Buffer tam de giai ma Private Key truoc khi n p (Transient RAM)
// private byte[] ramPrivMod;
// private byte[] ramPrivExp;
//
// // ===================== APDU INS =====================
// private static final byte CLA_GYM = (byte) 0x80;
//
// private static final byte INS_INIT_CARD = (byte) 0x10;
// private static final byte INS_VERIFY_PIN = (byte) 0x20;
// private static final byte INS_CHANGE_PIN = (byte) 0x21;
// private static final byte INS_UNLOCK = (byte) 0x22;
// private static final byte INS_ADMIN_SET_PIN = (byte) 0x23;
// private static final byte INS_CHECK_DEFAULT_PIN = (byte) 0x24; // NEW
//
// private static final byte INS_WRITE_PERSONAL = (byte) 0x30;
// private static final byte INS_READ_PERSONAL = (byte) 0x31;
// private static final byte INS_GET_TRIES = (byte) 0x32;
//
// private static final byte INS_GET_CARD_PUB = (byte) 0x40;
// private static final byte INS_SIGN_CHALLENGE = (byte) 0x41;
//
// private static final byte INS_AVATAR_BEGIN = (byte) 0x50;
// private static final byte INS_AVATAR_CHUNK = (byte) 0x51;
// private static final byte INS_AVATAR_END = (byte) 0x52;
// private static final byte INS_AVATAR_READ_CHUNK = (byte) 0x53;
//
// private static final byte INS_GET_MEM = (byte) 0x55;
//
// // Field IDs
// private static final byte FIELD_NAME = (byte) 0x00;
// private static final byte FIELD_DOB = (byte) 0x01;
// private static final byte FIELD_PHONE = (byte) 0x02;
// private static final byte FIELD_ADDRESS = (byte) 0x03;
// private static final byte FIELD_PACKAGE = (byte) 0x04;
// private static final byte FIELD_CARDID = (byte) 0x05;
// private static final byte FIELD_AVATAR = (byte) 0x06;
// private static final byte FIELD_CHECKIN = (byte) 0x07;
// private static final byte FIELD_BALANCE = (byte) 0x08;
// private static final byte FIELD_SERVICES = (byte) 0x09;
//
// private short avatarDataLen = 0;
// private static final byte ALG_SHA_256 = (byte) 4;
//
// // =========================================================
// // INSTALL & CONSTRUCTOR
// // =========================================================
// public static void install(byte[] bArray, short bOffset, byte bLength) {
// new ProjectCuoiKy();
// }
//
// protected ProjectCuoiKy() {
// rng = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
//
// triesRemaining = MAX_PIN_TRIES;
// blocked = false;
// userAuthenticated = false;
// isDefaultPin = true; // Default state
//
// // AES & SHA Engines
// masterKey = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES,
// KeyBuilder.LENGTH_AES_128, false);
// wrapKey = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES,
// KeyBuilder.LENGTH_AES_128, false);
// aesCipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_ECB_NOPAD, false);
// try {
// sha = MessageDigest.getInstance(ALG_SHA_256, false);
// } catch (CryptoException e) {
// // N u th quá c không h tr SHA-256, nó s nh y vào ây
// ISOException.throwIt(ISO7816.SW_FUNC_NOT_SUPPORTED);
// }
//
// // Memory Allocation
// userSalt = new byte[SALT_LEN]; // User Salt - co the doi khi change PIN
// adminSalt = new byte[SALT_LEN]; // Admin Salt - chi tao 1 lan
// tmpKeyBuf = new byte[SHA_256_LEN]; // 32 bytes
// mkHash = new byte[MK_HASH_LEN]; // 32 bytes
// encMK_User = new byte[AES_KEY_LEN];
// encMK_Admin = new byte[AES_KEY_LEN];
//
// // Transient RAM buffers (Security crucial)
// mkBuf = JCSystem.makeTransientByteArray(AES_KEY_LEN,
// JCSystem.CLEAR_ON_DESELECT);
// pinKeyBuf = JCSystem.makeTransientByteArray(AES_KEY_LEN,
// JCSystem.CLEAR_ON_DESELECT);
// mkHashCand = JCSystem.makeTransientByteArray(MK_HASH_LEN,
// JCSystem.CLEAR_ON_DESELECT);
// balanceTmp = JCSystem.makeTransientByteArray(BALANCE_LEN,
// JCSystem.CLEAR_ON_DESELECT);
//
// // Sinh Salt ngau nhien cho ca User va Admin
// rng.generateData(userSalt, (short) 0, SALT_LEN);
// rng.generateData(adminSalt, (short) 0, SALT_LEN);
//
// // Init Master Key (dummy)
// masterKey.setKey(mkBuf, (short) 0);
//
// // Personal buffers (Persistent)
// nameBuf = new byte[NAME_LEN];
// dobBuf = new byte[DOB_LEN];
// phoneBuf = new byte[PHONE_LEN];
// addressBuf = new byte[ADDRESS_LEN];
// packageBuf = new byte[PACKAGE_LEN];
// cardIdBuf = new byte[CARDID_LEN];
//
// avatarBuf = new byte[AVATAR_LEN];
// checkinBuf = new byte[CHECKIN_LEN];
// servicesBuf = new byte[SERVICES_LEN];
// balanceBuf = new byte[BALANCE_LEN];
//
// // RSA Buffers Allocation
// encPrivMod = new byte[RSA_MOD_LEN]; // Persistent
// encPrivExp = new byte[RSA_MOD_LEN]; // Persistent
//
// ramPrivMod = JCSystem.makeTransientByteArray(RSA_MOD_LEN,
// JCSystem.CLEAR_ON_DESELECT); // RAM
// ramPrivExp = JCSystem.makeTransientByteArray(RSA_MOD_LEN,
// JCSystem.CLEAR_ON_DESELECT); // RAM
//
// // RSA Objects
// cardPublicKey = (RSAPublicKey)
// KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PUBLIC, RSA_KEY_LEN_BITS, false);
// // Type Private thuong, ta se tu quan ly viec xoa key (clearKey)
// cardPrivateKey = (RSAPrivateKey)
// KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PRIVATE, RSA_KEY_LEN_BITS, false);
//
// cardPublicKey.setExponent(EXP_F4, (short) 0, (short) EXP_F4.length);
// rsaSign = Signature.getInstance(Signature.ALG_RSA_SHA_PKCS1, false);
//
// register();
// }
//
// // =========================================================
// // HELPER: KDF & HASH
// // =========================================================
//
// // KDF: SHA-256(PIN || Salt) -> Lay 16 bytes lam AES Key
// // Nhan salt lam tham so de ho tro ca userSalt va adminSalt
// private void deriveKeyFromPinWithSalt(byte[] pinBuf, short pinOff, short
// pinLen,
// byte[] salt, byte[] outKey, short outOff) {
// sha.reset();
// sha.update(pinBuf, pinOff, pinLen);
// sha.doFinal(salt, (short) 0, SALT_LEN, tmpKeyBuf, (short) 0);
// Util.arrayCopyNonAtomic(tmpKeyBuf, (short) 0, outKey, outOff, AES_KEY_LEN);
// // Clean temp buffer
// Util.arrayFillNonAtomic(tmpKeyBuf, (short) 0, SHA_256_LEN, (byte) 0x00);
// }
//
// // Hash MK: SHA-256(MK || Salt) -> Full 32 bytes
// // Nhan salt lam tham so de ho tro ca userSalt va adminSalt
// private void hashMasterKeyWithSalt(byte[] mk, short mkOff, byte[] salt,
// byte[] outHash, short outOff) {
// sha.reset();
// sha.update(mk, mkOff, AES_KEY_LEN);
// sha.doFinal(salt, (short) 0, SALT_LEN, tmpKeyBuf, (short) 0);
// Util.arrayCopyNonAtomic(tmpKeyBuf, (short) 0, outHash, outOff, MK_HASH_LEN);
// Util.arrayFillNonAtomic(tmpKeyBuf, (short) 0, SHA_256_LEN, (byte) 0x00);
// }
//
// // =========================================================
// // UNLOCK MASTER KEY LOGIC
// // =========================================================
// private boolean unlockMasterWithUserPin(byte[] buf, short pinOff) {
// // 1. Derive AES Key from PIN using USER SALT
// deriveKeyFromPinWithSalt(buf, pinOff, PIN_SIZE, userSalt, pinKeyBuf, (short)
// 0);
// wrapKey.setKey(pinKeyBuf, (short) 0);
//
// // 2. Decrypt Master Key from EEPROM into RAM
// aesCipher.init(wrapKey, Cipher.MODE_DECRYPT);
// aesCipher.doFinal(encMK_User, (short) 0, AES_KEY_LEN, mkBuf, (short) 0);
//
// // 3. Verify Integrity (Hash) using USER SALT
// hashMasterKeyWithSalt(mkBuf, (short) 0, userSalt, mkHashCand, (short) 0);
// boolean ok = (Util.arrayCompare(mkHashCand, (short) 0, mkHash, (short) 0,
// MK_HASH_LEN) == 0);
//
// // 4. If OK, set to Key Object
// if (ok)
// masterKey.setKey(mkBuf, (short) 0);
//
// // 5. Clean RAM
// Util.arrayFillNonAtomic(mkHashCand, (short) 0, MK_HASH_LEN, (byte) 0x00);
// Util.arrayFillNonAtomic(pinKeyBuf, (short) 0, AES_KEY_LEN, (byte) 0x00);
// // Note: mkBuf van giu trong RAM neu OK de dung, se xoa khi deselect
// if (!ok)
// Util.arrayFillNonAtomic(mkBuf, (short) 0, AES_KEY_LEN, (byte) 0x00);
//
// return ok;
// }
//
// private boolean unlockMasterWithAdminPin(byte[] buf, short pinOff) {
// // Derive key using ADMIN SALT
// deriveKeyFromPinWithSalt(buf, pinOff, PIN_SIZE, adminSalt, pinKeyBuf, (short)
// 0);
// wrapKey.setKey(pinKeyBuf, (short) 0);
//
// aesCipher.init(wrapKey, Cipher.MODE_DECRYPT);
// aesCipher.doFinal(encMK_Admin, (short) 0, AES_KEY_LEN, mkBuf, (short) 0);
//
// // Hash with ADMIN SALT for admin-side verification
// hashMasterKeyWithSalt(mkBuf, (short) 0, adminSalt, mkHashCand, (short) 0);
//
// // Compare with adminMkHash (will be added) or use raw MK comparison
// // For now, we verify by checking if decrypted MK can decrypt known data
// // Alternative: store separate adminMkHash
// // Simplified: Use same mkHash but with userSalt (admin verifies via their
// own
// // path)
//
// // Actually, simpler approach: Admin uses adminSalt for everything
// // Admin's encMK_Admin is encrypted with key derived from adminSalt
// // We need separate hash for admin verification
// // For now, let's verify by re-encrypting and comparing
//
// // Re-derive user key from mkBuf to verify integrity
// // This is a simplification - in production, store separate admin hash
// hashMasterKeyWithSalt(mkBuf, (short) 0, userSalt, mkHashCand, (short) 0);
// boolean ok = (Util.arrayCompare(mkHashCand, (short) 0, mkHash, (short) 0,
// MK_HASH_LEN) == 0);
//
// if (ok)
// masterKey.setKey(mkBuf, (short) 0);
//
// Util.arrayFillNonAtomic(mkHashCand, (short) 0, MK_HASH_LEN, (byte) 0x00);
// Util.arrayFillNonAtomic(pinKeyBuf, (short) 0, AES_KEY_LEN, (byte) 0x00);
// if (!ok)
// Util.arrayFillNonAtomic(mkBuf, (short) 0, AES_KEY_LEN, (byte) 0x00);
//
// return ok;
// }
//
// // =========================================================
// // MAIN PROCESS
// // =========================================================
// public void process(APDU apdu) {
// if (selectingApplet()) {
// userAuthenticated = false;
// masterKey.clearKey(); // Security: Xoa MK khoi RAM khi doi applet
// return;
// }
//
// byte[] buf = apdu.getBuffer();
// if (buf[ISO7816.OFFSET_CLA] != CLA_GYM)
// ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
//
// switch (buf[ISO7816.OFFSET_INS]) {
// case INS_INIT_CARD:
// initCard(apdu);
// return;
// case INS_VERIFY_PIN:
// verifyPin(apdu);
// return;
// case INS_CHANGE_PIN:
// changePin(apdu);
// return;
// case INS_UNLOCK:
// unlockCard(apdu);
// return;
// case INS_ADMIN_SET_PIN:
// adminSetUserPin(apdu);
// return;
// case INS_CHECK_DEFAULT_PIN:
// checkDefaultPin(apdu);
// return;
//
// case INS_AVATAR_BEGIN:
// avatarBegin(apdu);
// return;
// case INS_AVATAR_CHUNK:
// avatarChunk(apdu);
// return;
// case INS_AVATAR_END:
// avatarEnd(apdu);
// return;
// case INS_AVATAR_READ_CHUNK:
// avatarReadChunk(apdu);
// return;
//
// case INS_WRITE_PERSONAL:
// writePersonal(apdu);
// return;
// case INS_READ_PERSONAL:
// readPersonal(apdu);
// return;
// case INS_GET_TRIES:
// sendTries(apdu);
// return;
//
// case INS_GET_MEM:
// getMem(apdu);
// return;
// case INS_GET_CARD_PUB:
// getCardPublicKey(apdu);
// return;
// case INS_SIGN_CHALLENGE:
// signChallenge(apdu);
// return;
//
// default:
// ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
// }
// }
//
// // =========================================================
// // INIT CARD
// // =========================================================
// private void initCard(APDU apdu) {
// if (blocked)
// ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
//
// byte[] buf = apdu.getBuffer();
// short totalLen = (short) (buf[ISO7816.OFFSET_LC] & 0xFF);
// short received = apdu.setIncomingAndReceive();
// short cdataOff = ISO7816.OFFSET_CDATA;
// while (received < totalLen)
// received += apdu.receiveBytes((short) (cdataOff + received));
//
// // Check length: ID_Len(1) + ID + UserPIN(6) + AdminPIN(6)
// if (totalLen < (short) (1 + (2 * PIN_SIZE)))
// ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//
// short off = cdataOff;
// byte idLen = buf[off++];
// if (idLen <= 0 || idLen > (byte) CARDID_LEN)
// ISOException.throwIt(ISO7816.SW_WRONG_DATA);
//
// short cardIdOff = off;
// short userPinOff = (short) (cardIdOff + idLen);
// short adminPinOff = (short) (userPinOff + PIN_SIZE);
//
// // 1. Store CardID (Plaintext tam thoi, lat nua encrypt)
// Util.arrayFillNonAtomic(cardIdBuf, (short) 0, CARDID_LEN, (byte) 0x00);
// Util.arrayCopyNonAtomic(buf, cardIdOff, cardIdBuf, (short) 0, (short) idLen);
//
// // 2. Generate Master Key (MK) -> RAM
// rng.generateData(mkBuf, (short) 0, AES_KEY_LEN);
// masterKey.setKey(mkBuf, (short) 0);
//
// // 2.5. Generate NEW salts for this card initialization
// rng.generateData(userSalt, (short) 0, SALT_LEN);
// rng.generateData(adminSalt, (short) 0, SALT_LEN);
//
// // 3. Hash MK for verification using userSalt -> EEPROM
// hashMasterKeyWithSalt(mkBuf, (short) 0, userSalt, mkHash, (short) 0);
//
// // 4. Encrypt MK with User PIN using userSalt -> EEPROM
// deriveKeyFromPinWithSalt(buf, userPinOff, PIN_SIZE, userSalt, pinKeyBuf,
// (short) 0);
// wrapKey.setKey(pinKeyBuf, (short) 0);
// aesCipher.init(wrapKey, Cipher.MODE_ENCRYPT);
// aesCipher.doFinal(mkBuf, (short) 0, AES_KEY_LEN, encMK_User, (short) 0);
//
// // 5. Encrypt MK with Admin PIN using adminSalt -> EEPROM
// deriveKeyFromPinWithSalt(buf, adminPinOff, PIN_SIZE, adminSalt, pinKeyBuf,
// (short) 0);
// wrapKey.setKey(pinKeyBuf, (short) 0);
// aesCipher.init(wrapKey, Cipher.MODE_ENCRYPT);
// aesCipher.doFinal(mkBuf, (short) 0, AES_KEY_LEN, encMK_Admin, (short) 0);
//
// // 6. Encrypt CardID with MK
// aesCipher.init(masterKey, Cipher.MODE_ENCRYPT);
// aesCipher.doFinal(cardIdBuf, (short) 0, CARDID_LEN, cardIdBuf, (short) 0);
//
// // ==================================================
// // GENERATE & PROTECT RSA PRIVATE KEY
// // ==================================================
// // Tao KeyPair tam thoi
// KeyPair tempKP = new KeyPair(KeyPair.ALG_RSA, RSA_KEY_LEN_BITS);
// tempKP.genKeyPair();
//
// RSAPrivateKey priv = (RSAPrivateKey) tempKP.getPrivate();
// RSAPublicKey pub = (RSAPublicKey) tempKP.getPublic();
//
// // Luu Public Key Modulus vao the (de xuat ra ngoai verify)
// // Dung ramPrivMod lam buffer trung gian
// pub.getModulus(ramPrivMod, (short) 0);
// cardPublicKey.setModulus(ramPrivMod, (short) 0, RSA_MOD_LEN);
//
// // --- MA HOA PRIVATE KEY ---
// aesCipher.init(masterKey, Cipher.MODE_ENCRYPT);
//
// // A. Lay & Ma hoa Modulus
// priv.getModulus(ramPrivMod, (short) 0);
// aesCipher.doFinal(ramPrivMod, (short) 0, RSA_MOD_LEN, encPrivMod, (short) 0);
//
// // B. Lay & Ma hoa Exponent
// priv.getExponent(ramPrivExp, (short) 0);
// aesCipher.doFinal(ramPrivExp, (short) 0, RSA_MOD_LEN, encPrivExp, (short) 0);
//
// // Xoa du lieu nhay cam
// Util.arrayFillNonAtomic(ramPrivMod, (short) 0, RSA_MOD_LEN, (byte) 0x00);
// Util.arrayFillNonAtomic(ramPrivExp, (short) 0, RSA_MOD_LEN, (byte) 0x00);
// tempKP = null; // Garbage collection
// // ==================================================
//
// // Init State
// triesRemaining = MAX_PIN_TRIES;
// blocked = false;
// userAuthenticated = false;
// isDefaultPin = true; // Set to true on Init (Admin creates card)
//
// resetBalanceZero();
// Util.arrayFillNonAtomic(checkinBuf, (short) 0, CHECKIN_LEN, (byte) 0x00);
// Util.arrayFillNonAtomic(servicesBuf, (short) 0, SERVICES_LEN, (byte) 0x00);
// // Clear services
// Util.arrayFillNonAtomic(packageBuf, (short) 0, PACKAGE_LEN, (byte) 0x00); //
// Clear package
// Util.arrayFillNonAtomic(avatarBuf, (short) 0, AVATAR_LEN, (byte) 0x00);
// avatarDataLen = 0;
//
// // Xoa Master Key khoi RAM sau khi init
// masterKey.clearKey();
// Util.arrayFillNonAtomic(mkBuf, (short) 0, AES_KEY_LEN, (byte) 0x00);
// Util.arrayFillNonAtomic(pinKeyBuf, (short) 0, AES_KEY_LEN, (byte) 0x00);
// }
//
// // =========================================================
// // PIN & ACCESS
// // =========================================================
// private void verifyPin(APDU apdu) {
// if (blocked || triesRemaining == 0)
// ISOException.throwIt((short) 0x6983);
// byte[] buf = apdu.getBuffer();
// short len = apdu.setIncomingAndReceive();
// if (len != PIN_SIZE)
// ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//
// boolean ok = unlockMasterWithUserPin(buf, ISO7816.OFFSET_CDATA);
// if (!ok) {
// if (triesRemaining > 0)
// triesRemaining--;
// if (triesRemaining == 0)
// blocked = true;
// ISOException.throwIt((short) (0x63C0 | (triesRemaining & 0x0F)));
// }
//
// triesRemaining = MAX_PIN_TRIES;
// userAuthenticated = true;
// }
//
// private void changePin(APDU apdu) {
// if (blocked || triesRemaining == 0)
// ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
// byte[] buf = apdu.getBuffer();
// short len = apdu.setIncomingAndReceive();
// if (len != (short) (PIN_SIZE * 2))
// ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//
// short offOld = ISO7816.OFFSET_CDATA;
// short offNew = (short) (offOld + PIN_SIZE);
//
// boolean ok = unlockMasterWithUserPin(buf, offOld);
// if (!ok) {
// if (triesRemaining > 0)
// triesRemaining--;
// if (triesRemaining == 0)
// blocked = true;
// ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
// }
//
// // === SALT ROTATION: Sinh userSalt moi de tang cuong bao mat ===
// // 1. Get raw MK from RAM
// masterKey.getKey(mkBuf, (short) 0);
//
// // 2. Generate NEW userSalt (KEY SECURITY FEATURE)
// rng.generateData(userSalt, (short) 0, SALT_LEN);
//
// // 3. Derive new TempKey from new PIN + NEW Salt
// deriveKeyFromPinWithSalt(buf, offNew, PIN_SIZE, userSalt, pinKeyBuf, (short)
// 0);
// wrapKey.setKey(pinKeyBuf, (short) 0);
//
// // 4. Re-encrypt MK with new key
// aesCipher.init(wrapKey, Cipher.MODE_ENCRYPT);
// aesCipher.doFinal(mkBuf, (short) 0, AES_KEY_LEN, encMK_User, (short) 0);
//
// // 5. Update mkHash with NEW Salt (CRITICAL for verification)
// hashMasterKeyWithSalt(mkBuf, (short) 0, userSalt, mkHash, (short) 0);
//
// triesRemaining = MAX_PIN_TRIES;
// userAuthenticated = true;
// isDefaultPin = false; // PIN changed successfully
//
// Util.arrayFillNonAtomic(mkBuf, (short) 0, AES_KEY_LEN, (byte) 0x00);
// Util.arrayFillNonAtomic(pinKeyBuf, (short) 0, AES_KEY_LEN, (byte) 0x00);
// }
//
// private void checkDefaultPin(APDU apdu) {
// if (!userAuthenticated)
// ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
//
// byte[] buf = apdu.getBuffer();
// buf[0] = isDefaultPin ? (byte) 1 : (byte) 0;
// apdu.setOutgoingAndSend((short) 0, (short) 1);
// }
//
// private void unlockCard(APDU apdu) {
// byte[] buf = apdu.getBuffer();
// short len = apdu.setIncomingAndReceive();
// if (len != PIN_SIZE)
// ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//
// boolean ok = unlockMasterWithAdminPin(buf, ISO7816.OFFSET_CDATA);
// if (!ok)
// ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
//
// triesRemaining = MAX_PIN_TRIES;
// blocked = false;
// userAuthenticated = false;
// // Admin unlock thi khong set userAuthenticated = true de tranh admin doc du
// // lieu user
// // Admin chi mo khoa de user nhap PIN lai
// }
//
// private void adminSetUserPin(APDU apdu) {
// byte[] buf = apdu.getBuffer();
// short len = apdu.setIncomingAndReceive();
// if (len != (short) (PIN_SIZE * 2))
// ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//
// short offAdmin = ISO7816.OFFSET_CDATA;
// short offNew = (short) (offAdmin + PIN_SIZE);
//
// boolean ok = unlockMasterWithAdminPin(buf, offAdmin);
// if (!ok)
// ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
//
// // Get raw MK from RAM (unlocked by admin)
// masterKey.getKey(mkBuf, (short) 0);
//
// // === SALT ROTATION khi Admin reset PIN ===
// // Sinh userSalt moi de dam bao an toan
// rng.generateData(userSalt, (short) 0, SALT_LEN);
//
// // Derive key tu new PIN + NEW userSalt
// deriveKeyFromPinWithSalt(buf, offNew, PIN_SIZE, userSalt, pinKeyBuf, (short)
// 0);
// wrapKey.setKey(pinKeyBuf, (short) 0);
//
// aesCipher.init(wrapKey, Cipher.MODE_ENCRYPT);
// aesCipher.doFinal(mkBuf, (short) 0, AES_KEY_LEN, encMK_User, (short) 0);
//
// // Update mkHash voi userSalt moi
// hashMasterKeyWithSalt(mkBuf, (short) 0, userSalt, mkHash, (short) 0);
//
// Util.arrayFillNonAtomic(mkBuf, (short) 0, AES_KEY_LEN, (byte) 0x00);
// Util.arrayFillNonAtomic(pinKeyBuf, (short) 0, AES_KEY_LEN, (byte) 0x00);
//
// // Khi Admin reset PIN, user buoc phai doi PIN lan dau
// isDefaultPin = true;
// }
//
// // =========================================================
// // RSA: SIGNATURE WITH ENCRYPTED PRIVATE KEY
// // =========================================================
//
// private void getCardPublicKey(APDU apdu) {
// byte[] buf = apdu.getBuffer();
// short offset = Util.makeShort(buf[ISO7816.OFFSET_P1],
// buf[ISO7816.OFFSET_P2]);
// if (offset < 0 || offset >= RSA_MOD_LEN)
// ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
//
// short modLen = cardPublicKey.getModulus(buf, (short) 0);
// short remain = (short) (RSA_MOD_LEN - offset);
// short outLen = (remain > GET_PUB_CHUNK) ? GET_PUB_CHUNK : remain;
//
// Util.arrayCopyNonAtomic(buf, offset, buf, (short) 0, outLen);
// apdu.setOutgoing();
// apdu.setOutgoingLength(outLen);
// apdu.sendBytes((short) 0, outLen);
// }
//
// // --- LOGIC KY AN TOAN ---
// private void signChallenge(APDU apdu) {
// // 1. Kiem tra Authenticated (tuc la Master Key da co trong RAM)
// if (!userAuthenticated)
// ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
//
// byte[] buf = apdu.getBuffer();
// short len = apdu.setIncomingAndReceive();
// if (len <= 0)
// ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//
// // 2. Giai ma Private Key tu EEPROM vao RAM
// aesCipher.init(masterKey, Cipher.MODE_DECRYPT);
//
// // Modulus
// aesCipher.doFinal(encPrivMod, (short) 0, RSA_MOD_LEN, ramPrivMod, (short) 0);
// // Exponent
// aesCipher.doFinal(encPrivExp, (short) 0, RSA_MOD_LEN, ramPrivExp, (short) 0);
//
// // 3. Nap Key vao Object
// cardPrivateKey.setModulus(ramPrivMod, (short) 0, RSA_MOD_LEN);
// cardPrivateKey.setExponent(ramPrivExp, (short) 0, RSA_MOD_LEN);
//
// // 4. Ky
// rsaSign.init(cardPrivateKey, Signature.MODE_SIGN);
// short sigLen = rsaSign.sign(buf, ISO7816.OFFSET_CDATA, len, buf, (short) 0);
//
// // 5. CLEANUP NGAY LAP TUC
// cardPrivateKey.clearKey();
// Util.arrayFillNonAtomic(ramPrivMod, (short) 0, RSA_MOD_LEN, (byte) 0x00);
// Util.arrayFillNonAtomic(ramPrivExp, (short) 0, RSA_MOD_LEN, (byte) 0x00);
//
// apdu.setOutgoing();
// apdu.setOutgoingLength(sigLen);
// apdu.sendBytes((short) 0, sigLen);
// }
//
// // =========================================================
// // PERSONAL DATA & AVATAR
// // =========================================================
//
// private void writePersonal(APDU apdu) {
// if (blocked || triesRemaining == 0)
// ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
// if (!userAuthenticated)
// ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
//
// byte[] buf = apdu.getBuffer();
// short lc = apdu.setIncomingAndReceive();
// if (lc < 1)
// ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//
// byte fieldId = buf[ISO7816.OFFSET_CDATA];
// short dataLen = (short) (lc - 1);
// short srcOff = (short) (ISO7816.OFFSET_CDATA + 1);
//
// if (fieldId == FIELD_AVATAR)
// ISOException.throwIt(ISO7816.SW_FUNC_NOT_SUPPORTED);
//
// if (fieldId == FIELD_CHECKIN) {
// if (dataLen > CHECKIN_LEN)
// ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
// Util.arrayFillNonAtomic(checkinBuf, (short) 0, CHECKIN_LEN, (byte) 0x00);
// if (dataLen > 0)
// Util.arrayCopyNonAtomic(buf, srcOff, checkinBuf, (short) 0, dataLen);
// return;
// }
//
// if (fieldId == FIELD_BALANCE) {
// if (dataLen != BALANCE_VALUE_LEN)
// ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
// setBalanceInternalFrom8(buf, srcOff);
// return;
// }
//
// if (fieldId == FIELD_SERVICES) {
// if (dataLen > SERVICES_LEN)
// ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
// Util.arrayFillNonAtomic(servicesBuf, (short) 0, SERVICES_LEN, (byte) 0x00);
// if (dataLen > 0)
// Util.arrayCopyNonAtomic(buf, srcOff, servicesBuf, (short) 0, dataLen);
// return;
// }
//
// byte[] targetBuf;
// short targetLen;
//
// switch (fieldId) {
// case FIELD_NAME:
// targetBuf = nameBuf;
// targetLen = NAME_LEN;
// break;
// case FIELD_DOB:
// targetBuf = dobBuf;
// targetLen = DOB_LEN;
// break;
// case FIELD_PHONE:
// targetBuf = phoneBuf;
// targetLen = PHONE_LEN;
// break;
// case FIELD_ADDRESS:
// targetBuf = addressBuf;
// targetLen = ADDRESS_LEN;
// break;
// case FIELD_PACKAGE:
// targetBuf = packageBuf;
// targetLen = PACKAGE_LEN;
// break;
// case FIELD_CARDID:
// targetBuf = cardIdBuf;
// targetLen = CARDID_LEN;
// break;
// default:
// ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
// return;
// }
//
// if (dataLen > targetLen)
// ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
// Util.arrayFillNonAtomic(targetBuf, (short) 0, targetLen, (byte) 0x00);
// if (dataLen > 0)
// Util.arrayCopyNonAtomic(buf, srcOff, targetBuf, (short) 0, dataLen);
//
// aesCipher.init(masterKey, Cipher.MODE_ENCRYPT);
// aesCipher.doFinal(targetBuf, (short) 0, targetLen, targetBuf, (short) 0);
// }
//
// private void readPersonal(APDU apdu) {
// if (blocked || triesRemaining == 0)
// ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
// if (!userAuthenticated)
// ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
//
// byte[] buf = apdu.getBuffer();
// short lc = apdu.setIncomingAndReceive();
// if (lc != 1)
// ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//
// byte fieldId = buf[ISO7816.OFFSET_CDATA];
// if (fieldId == FIELD_AVATAR)
// ISOException.throwIt(ISO7816.SW_FUNC_NOT_SUPPORTED);
//
// if (fieldId == FIELD_CHECKIN) {
// // Calculate actual data length from checkinBuf first
// short actualLen = CHECKIN_LEN;
// while (actualLen > 0 && checkinBuf[(short) (actualLen - 1)] == (byte) 0x00)
// actualLen--;
// // Limit to APDU buffer size (max 255)
// if (actualLen > (short) 255)
// actualLen = (short) 255;
// // Copy only actual data to APDU buffer
// if (actualLen > 0)
// Util.arrayCopyNonAtomic(checkinBuf, (short) 0, buf, (short) 0, actualLen);
// apdu.setOutgoing();
// apdu.setOutgoingLength(actualLen);
// if (actualLen > 0)
// apdu.sendBytes((short) 0, actualLen);
// return;
// }
//
// if (fieldId == FIELD_BALANCE) {
// getBalanceInternalTo8(buf, (short) 0);
// apdu.setOutgoing();
// apdu.setOutgoingLength(BALANCE_VALUE_LEN);
// apdu.sendBytes((short) 0, BALANCE_VALUE_LEN);
// return;
// }
//
// if (fieldId == FIELD_SERVICES) {
// // Calculate actual data length from servicesBuf first
// short actualLen = SERVICES_LEN;
// while (actualLen > 0 && servicesBuf[(short) (actualLen - 1)] == (byte) 0x00)
// actualLen--;
// // Limit to APDU buffer size (max 255)
// if (actualLen > (short) 255)
// actualLen = (short) 255;
// // Copy only actual data to APDU buffer
// if (actualLen > 0)
// Util.arrayCopyNonAtomic(servicesBuf, (short) 0, buf, (short) 0, actualLen);
// apdu.setOutgoing();
// apdu.setOutgoingLength(actualLen);
// if (actualLen > 0)
// apdu.sendBytes((short) 0, actualLen);
// return;
// }
//
// byte[] src;
// short srcLen;
// switch (fieldId) {
// case FIELD_NAME:
// src = nameBuf;
// srcLen = NAME_LEN;
// break;
// case FIELD_DOB:
// src = dobBuf;
// srcLen = DOB_LEN;
// break;
// case FIELD_PHONE:
// src = phoneBuf;
// srcLen = PHONE_LEN;
// break;
// case FIELD_ADDRESS:
// src = addressBuf;
// srcLen = ADDRESS_LEN;
// break;
// case FIELD_PACKAGE:
// src = packageBuf;
// srcLen = PACKAGE_LEN;
// break;
// case FIELD_CARDID:
// src = cardIdBuf;
// srcLen = CARDID_LEN;
// break;
// default:
// ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
// return;
// }
//
// aesCipher.init(masterKey, Cipher.MODE_DECRYPT);
// aesCipher.doFinal(src, (short) 0, srcLen, buf, (short) 0);
//
// short actualLen = srcLen;
// while (actualLen > 0 && buf[(short) (actualLen - 1)] == (byte) 0x00)
// actualLen--;
//
// apdu.setOutgoing();
// apdu.setOutgoingLength(actualLen);
// if (actualLen > 0)
// apdu.sendBytes((short) 0, actualLen);
// }
//
// private void avatarBegin(APDU apdu) {
// if (!userAuthenticated)
// ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
// byte[] buf = apdu.getBuffer();
// short len = apdu.setIncomingAndReceive();
// if (len != 2)
// ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
// short L = Util.makeShort(buf[ISO7816.OFFSET_CDATA], buf[(short)
// (ISO7816.OFFSET_CDATA + 1)]);
// if (L < 0 || L > (short) (AVATAR_LEN - 2))
// ISOException.throwIt(ISO7816.SW_WRONG_DATA);
// avatarDataLen = L;
// Util.arrayFillNonAtomic(avatarBuf, (short) 0, AVATAR_LEN, (byte) 0x00);
// }
//
// private void avatarChunk(APDU apdu) {
// if (!userAuthenticated)
// ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
// byte[] buf = apdu.getBuffer();
// short lc = apdu.setIncomingAndReceive();
// if (lc <= 0)
// ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
// short offset = Util.makeShort(buf[ISO7816.OFFSET_P1],
// buf[ISO7816.OFFSET_P2]);
// if (offset < 0 || offset >= avatarDataLen)
// ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
// if ((short) (offset + lc) > avatarDataLen)
// ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
// Util.arrayCopyNonAtomic(buf, ISO7816.OFFSET_CDATA, avatarBuf, offset, lc);
// }
//
// private void avatarEnd(APDU apdu) {
// if (!userAuthenticated)
// ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
// short L = avatarDataLen;
// avatarBuf[(short) (AVATAR_LEN - 2)] = (byte) ((L >> 8) & 0xFF);
// avatarBuf[(short) (AVATAR_LEN - 1)] = (byte) (L & 0xFF);
// aesCipher.init(masterKey, Cipher.MODE_ENCRYPT);
// for (short i = 0; i < AVATAR_LEN; i += 16)
// aesCipher.doFinal(avatarBuf, i, (short) 16, avatarBuf, i);
// }
//
// private void avatarReadChunk(APDU apdu) {
// if (!userAuthenticated)
// ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
// byte[] buf = apdu.getBuffer();
// short offset = Util.makeShort(buf[ISO7816.OFFSET_P1],
// buf[ISO7816.OFFSET_P2]);
// if (offset < 0 || offset >= AVATAR_LEN)
// ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
// short outMax = apdu.setOutgoing();
// if (outMax <= 0)
// ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//
// short remain = (short) (AVATAR_LEN - offset);
// short outLen = outMax;
// if (outLen > remain)
// outLen = remain;
//
// short startBlock = (short) (offset - (short) (offset % 16));
// short end = (short) (offset + outLen);
// short endBlock = (short) ((end % 16 == 0) ? end : (short) (end + (short) (16
// - (end % 16))));
// if (endBlock > AVATAR_LEN)
// endBlock = AVATAR_LEN;
//
// short copyLen = (short) (endBlock - startBlock);
// Util.arrayCopyNonAtomic(avatarBuf, startBlock, buf, (short) 0, copyLen);
//
// aesCipher.init(masterKey, Cipher.MODE_DECRYPT);
// for (short i = 0; i < copyLen; i += 16)
// aesCipher.doFinal(buf, i, (short) 16, buf, i);
//
// short innerOff = (short) (offset - startBlock);
// Util.arrayCopyNonAtomic(buf, innerOff, buf, (short) 0, outLen);
// apdu.setOutgoingLength(outLen);
// apdu.sendBytes((short) 0, outLen);
// }
//
// private void setBalanceInternalFrom8(byte[] src, short srcOff) {
// Util.arrayFillNonAtomic(balanceTmp, (short) 0, BALANCE_LEN, (byte) 0x00);
// Util.arrayCopyNonAtomic(src, srcOff, balanceTmp, (short) 0,
// BALANCE_VALUE_LEN);
// aesCipher.init(masterKey, Cipher.MODE_ENCRYPT);
// aesCipher.doFinal(balanceTmp, (short) 0, BALANCE_LEN, balanceBuf, (short) 0);
// Util.arrayFillNonAtomic(balanceTmp, (short) 0, BALANCE_LEN, (byte) 0x00);
// }
//
// private void getBalanceInternalTo8(byte[] out, short outOff) {
// Util.arrayCopyNonAtomic(balanceBuf, (short) 0, balanceTmp, (short) 0,
// BALANCE_LEN);
// aesCipher.init(masterKey, Cipher.MODE_DECRYPT);
// aesCipher.doFinal(balanceTmp, (short) 0, BALANCE_LEN, balanceTmp, (short) 0);
// Util.arrayCopyNonAtomic(balanceTmp, (short) 0, out, outOff,
// BALANCE_VALUE_LEN);
// Util.arrayFillNonAtomic(balanceTmp, (short) 0, BALANCE_LEN, (byte) 0x00);
// }
//
// private void resetBalanceZero() {
// Util.arrayFillNonAtomic(balanceTmp, (short) 0, BALANCE_LEN, (byte) 0x00);
// aesCipher.init(masterKey, Cipher.MODE_ENCRYPT);
// aesCipher.doFinal(balanceTmp, (short) 0, BALANCE_LEN, balanceBuf, (short) 0);
// Util.arrayFillNonAtomic(balanceTmp, (short) 0, BALANCE_LEN, (byte) 0x00);
// }
//
// private void sendTries(APDU apdu) {
// byte[] buf = apdu.getBuffer();
// buf[0] = triesRemaining;
// apdu.setOutgoingAndSend((short) 0, (short) 1);
// }
//
// private void getMem(APDU apdu) {
// byte[] buf = apdu.getBuffer();
// short persistent =
// JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_PERSISTENT);
// short tReset =
// JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_RESET);
// short tDeselect =
// JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT);
// Util.setShort(buf, (short) 0, persistent);
// Util.setShort(buf, (short) 2, tReset);
// Util.setShort(buf, (short) 4, tDeselect);
// apdu.setOutgoing();
// apdu.setOutgoingLength((short) 6);
// apdu.sendBytes((short) 0, (short) 6);
// }
// }