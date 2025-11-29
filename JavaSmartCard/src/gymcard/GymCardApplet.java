//package gymcard;
//
//// NOTE: This file requires JavaCard SDK to compile
//// For UI demo, this file is NOT needed - use CardCommunicator.java instead
//// To compile this file, install JavaCard SDK and configure classpath
//
//import javacard.framework.*;
//import javacard.security.*;
//import javacardx.crypto.*;
//
///**
// * Applet quản lý thẻ tập gym với các chức năng:
// * - Xác thực PIN (giới hạn 3 lần thử)
// * - Lưu trữ thông tin hội viên (mã hóa AES)
// * - Quản lý gói tập
// * - Check-in/Check-out
// * - Thanh toán và nạp tiền
// */
//public class GymCardApplet extends Applet {
//    
//    // CLA byte
//    private static final byte GYM_CLA = (byte) 0x80;
//    
//    // INS bytes - Authentication
//    private static final byte INS_VERIFY_PIN = (byte) 0x20;
//    private static final byte INS_CHANGE_PIN = (byte) 0x21;
//    private static final byte INS_UNLOCK_PIN = (byte) 0x22;
//    private static final byte INS_GET_PIN_TRIES = (byte) 0x23;
//    
//    // INS bytes - Member Management
//    private static final byte INS_SET_MEMBER_INFO = (byte) 0x30;
//    private static final byte INS_GET_MEMBER_INFO = (byte) 0x31;
//    private static final byte INS_SET_MEMBER_PHOTO = (byte) 0x32;
//    private static final byte INS_GET_MEMBER_PHOTO = (byte) 0x33;
//    
//    // INS bytes - Package Management
//    private static final byte INS_SET_PACKAGE = (byte) 0x40;
//    private static final byte INS_GET_PACKAGE = (byte) 0x41;
//    private static final byte INS_UPGRADE_PACKAGE = (byte) 0x42;
//    
//    // INS bytes - Check-in/out
//    private static final byte INS_CHECK_IN = (byte) 0x50;
//    private static final byte INS_CHECK_OUT = (byte) 0x51;
//    private static final byte INS_GET_CHECKIN_COUNT = (byte) 0x52;
//    private static final byte INS_GET_LAST_CHECKIN = (byte) 0x53;
//    
//    // INS bytes - Payment
//    private static final byte INS_GET_BALANCE = (byte) 0x60;
//    private static final byte INS_ADD_BALANCE = (byte) 0x61;
//    private static final byte INS_DEDUCT_BALANCE = (byte) 0x62;
//    private static final byte INS_GET_TRANSACTION = (byte) 0x63;
//    
//    // Status words
//    private static final short SW_PIN_VERIFICATION_REQUIRED = (short) 0x6301;
//    private static final short SW_PIN_BLOCKED = (short) 0x6983;
//    private static final short SW_INVALID_PIN = (short) 0x6984;
//    private static final short SW_PACKAGE_EXPIRED = (short) 0x6A80;
//    private static final short SW_INSUFFICIENT_BALANCE = (short) 0x6A81;
//    
//    // Constants
//    private static final byte PIN_SIZE = 4;
//    private static final byte PIN_TRY_LIMIT = 3;
//    private static final byte MAX_NAME_LENGTH = 50;
//    private static final byte MAX_PHONE_LENGTH = 15;
//    private static final byte MAX_ADDRESS_LENGTH = 100;
//    private static final short MAX_PHOTO_SIZE = 1024;
//    private static final byte MAX_TRANSACTIONS = 10;
//    
//    // PIN management
//    private OwnerPIN userPin;
//    private byte[] adminPin;
//    
//    // Member information (encrypted)
//    private byte[] memberName;
//    private byte nameLength;
//    private byte[] birthDate; // YYYYMMDD (8 bytes)
//    private byte[] phoneNumber;
//    private byte phoneLength;
//    private byte[] address;
//    private short addressLength;
//    private byte[] memberPhoto;
//    private short photoLength;
//    
//    // Package information
//    private byte packageType; // 1=Monthly, 2=Session, 3=VIP
//    private byte[] packageExpiry; // YYYYMMDD
//    private byte[] registrationDate; // YYYYMMDD
//    private short remainingSessions; // For session-based packages
//    
//    // Check-in/out tracking
//    private byte[] lastCheckInDate; // YYYYMMDD
//    private byte[] lastCheckInTime; // HHMMSS
//    private byte[] lastCheckOutTime; // HHMMSS
//    private byte checkInCountThisMonth;
//    private byte currentMonth; // Track which month
//    
//    // Balance and transactions
//    private short balance; // In thousands VND
//    private byte[][] transactions; // Store last 10 transactions
//    private byte transactionCount;
//    
//    // Crypto
//    private AESKey aesKey;
//    private Cipher aesCipher;
//    private byte[] aesKeyData;
//    
//    // Temporary buffers
//    private byte[] tempBuffer;
//    
//    // Authentication state
//    private boolean authenticated;
//    
//    /**
//     * Constructor
//     */
//    private GymCardApplet() {
//        // Initialize PIN (default: 1234)
//        userPin = new OwnerPIN(PIN_TRY_LIMIT, PIN_SIZE);
//        byte[] defaultPin = {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04};
//        userPin.update(defaultPin, (short) 0, PIN_SIZE);
//        
//        // Admin PIN for unlocking (default: 9999)
//        adminPin = new byte[PIN_SIZE];
//        adminPin[0] = (byte) 0x09;
//        adminPin[1] = (byte) 0x09;
//        adminPin[2] = (byte) 0x09;
//        adminPin[3] = (byte) 0x09;
//        
//        // Initialize member info arrays
//        memberName = new byte[MAX_NAME_LENGTH];
//        nameLength = 0;
//        birthDate = new byte[8];
//        phoneNumber = new byte[MAX_PHONE_LENGTH];
//        phoneLength = 0;
//        address = new byte[MAX_ADDRESS_LENGTH];
//        addressLength = 0;
//        memberPhoto = new byte[MAX_PHOTO_SIZE];
//        photoLength = 0;
//        
//        // Initialize package info
//        packageType = 0;
//        packageExpiry = new byte[8];
//        registrationDate = new byte[8];
//        remainingSessions = 0;
//        
//        // Initialize check-in info
//        lastCheckInDate = new byte[8];
//        lastCheckInTime = new byte[6];
//        lastCheckOutTime = new byte[6];
//        checkInCountThisMonth = 0;
//        currentMonth = 0;
//        
//        // Initialize balance
//        balance = 0;
//        
//        // Initialize transactions array
//        transactions = new byte[MAX_TRANSACTIONS][16]; // Each: 8 bytes date/time + 2 bytes amount + 1 byte type + 5 spare
//        transactionCount = 0;
//        
//        // Initialize crypto
//        aesKey = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
//        aesCipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_ECB_NOPAD, false);
//        aesKeyData = new byte[16];
//        // Set default AES key
//        for (byte i = 0; i < 16; i++) {
//            aesKeyData[i] = (byte) (i + 1);
//        }
//        aesKey.setKey(aesKeyData, (short) 0);
//        
//        // Temp buffer
//        tempBuffer = new byte[256];
//        
//        authenticated = false;
//    }
//    
//    /**
//     * Install method
//     */
//    public static void install(byte[] bArray, short bOffset, byte bLength) {
//        new GymCardApplet().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
//    }
//    
//    /**
//     * Process APDU
//     */
//    public void process(APDU apdu) {
//        if (selectingApplet()) {
//            return;
//        }
//        
//        byte[] buffer = apdu.getBuffer();
//        byte cla = buffer[ISO7816.OFFSET_CLA];
//        byte ins = buffer[ISO7816.OFFSET_INS];
//        
//        if (cla != GYM_CLA) {
//            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
//        }
//        
//        switch (ins) {
//            // Authentication
//            case INS_VERIFY_PIN:
//                verifyPin(apdu);
//                break;
//            case INS_CHANGE_PIN:
//                changePin(apdu);
//                break;
//            case INS_UNLOCK_PIN:
//                unlockPin(apdu);
//                break;
//            case INS_GET_PIN_TRIES:
//                getPinTries(apdu);
//                break;
//                
//            // Member Management
//            case INS_SET_MEMBER_INFO:
//                setMemberInfo(apdu);
//                break;
//            case INS_GET_MEMBER_INFO:
//                getMemberInfo(apdu);
//                break;
//            case INS_SET_MEMBER_PHOTO:
//                setMemberPhoto(apdu);
//                break;
//            case INS_GET_MEMBER_PHOTO:
//                getMemberPhoto(apdu);
//                break;
//                
//            // Package Management
//            case INS_SET_PACKAGE:
//                setPackage(apdu);
//                break;
//            case INS_GET_PACKAGE:
//                getPackage(apdu);
//                break;
//            case INS_UPGRADE_PACKAGE:
//                upgradePackage(apdu);
//                break;
//                
//            // Check-in/out
//            case INS_CHECK_IN:
//                checkIn(apdu);
//                break;
//            case INS_CHECK_OUT:
//                checkOut(apdu);
//                break;
//            case INS_GET_CHECKIN_COUNT:
//                getCheckInCount(apdu);
//                break;
//            case INS_GET_LAST_CHECKIN:
//                getLastCheckIn(apdu);
//                break;
//                
//            // Payment
//            case INS_GET_BALANCE:
//                getBalance(apdu);
//                break;
//            case INS_ADD_BALANCE:
//                addBalance(apdu);
//                break;
//            case INS_DEDUCT_BALANCE:
//                deductBalance(apdu);
//                break;
//            case INS_GET_TRANSACTION:
//                getTransaction(apdu);
//                break;
//                
//            default:
//                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
//        }
//    }
//    
//    // ==================== AUTHENTICATION METHODS ====================
//    
//    /**
//     * Verify PIN
//     */
//    private void verifyPin(APDU apdu) {
//        byte[] buffer = apdu.getBuffer();
//        byte lc = buffer[ISO7816.OFFSET_LC];
//        
//        if (lc != PIN_SIZE) {
//            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//        }
//        
//        short bytesRead = apdu.setIncomingAndReceive();
//        
//        if (userPin.isValidated()) {
//            authenticated = true;
//            ISOException.throwIt(ISO7816.SW_NO_ERROR);
//        }
//        
//        if (userPin.getTriesRemaining() == 0) {
//            ISOException.throwIt(SW_PIN_BLOCKED);
//        }
//        
//        if (userPin.check(buffer, ISO7816.OFFSET_CDATA, PIN_SIZE)) {
//            authenticated = true;
//            ISOException.throwIt(ISO7816.SW_NO_ERROR);
//        } else {
//            authenticated = false;
//            ISOException.throwIt(SW_INVALID_PIN);
//        }
//    }
//    
//    /**
//     * Change PIN
//     */
//    private void changePin(APDU apdu) {
//        if (!authenticated) {
//            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
//        }
//        
//        byte[] buffer = apdu.getBuffer();
//        byte lc = buffer[ISO7816.OFFSET_LC];
//        
//        if (lc != PIN_SIZE * 2) { // Old PIN + New PIN
//            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//        }
//        
//        short bytesRead = apdu.setIncomingAndReceive();
//        
//        // Verify old PIN
//        if (!userPin.check(buffer, ISO7816.OFFSET_CDATA, PIN_SIZE)) {
//            ISOException.throwIt(SW_INVALID_PIN);
//        }
//        
//        // Set new PIN
//        userPin.update(buffer, (short) (ISO7816.OFFSET_CDATA + PIN_SIZE), PIN_SIZE);
//        ISOException.throwIt(ISO7816.SW_NO_ERROR);
//    }
//    
//    /**
//     * Unlock PIN (Admin function)
//     */
//    private void unlockPin(APDU apdu) {
//        byte[] buffer = apdu.getBuffer();
//        byte lc = buffer[ISO7816.OFFSET_LC];
//        
//        if (lc != PIN_SIZE) {
//            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//        }
//        
//        short bytesRead = apdu.setIncomingAndReceive();
//        
//        // Verify admin PIN
//        if (Util.arrayCompare(buffer, ISO7816.OFFSET_CDATA, adminPin, (short) 0, PIN_SIZE) != 0) {
//            ISOException.throwIt(SW_INVALID_PIN);
//        }
//        
//        // Reset PIN tries
//        userPin.resetAndUnblock();
//        authenticated = false;
//        ISOException.throwIt(ISO7816.SW_NO_ERROR);
//    }
//    
//    /**
//     * Get remaining PIN tries
//     */
//    private void getPinTries(APDU apdu) {
//        byte[] buffer = apdu.getBuffer();
//        buffer[0] = userPin.getTriesRemaining();
//        apdu.setOutgoingAndSend((short) 0, (short) 1);
//    }
//    
//    // ==================== MEMBER MANAGEMENT METHODS ====================
//    
//    /**
//     * Set member information (encrypted)
//     * Format: [name_length][name][birthdate(8)][phone_length][phone][address_length(2)][address]
//     */
//    private void setMemberInfo(APDU apdu) {
//        if (!authenticated) {
//            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
//        }
//        
//        byte[] buffer = apdu.getBuffer();
//        short lc = (short) (buffer[ISO7816.OFFSET_LC] & 0xFF);
//        short bytesRead = apdu.setIncomingAndReceive();
//        
//        short offset = ISO7816.OFFSET_CDATA;
//        
//        // Read name
//        nameLength = buffer[offset++];
//        if (nameLength > MAX_NAME_LENGTH) {
//            ISOException.throwIt(ISO7816.SW_WRONG_DATA);
//        }
//        Util.arrayCopy(buffer, offset, memberName, (short) 0, nameLength);
//        offset += nameLength;
//        
//        // Read birth date
//        Util.arrayCopy(buffer, offset, birthDate, (short) 0, (short) 8);
//        offset += 8;
//        
//        // Read phone
//        phoneLength = buffer[offset++];
//        if (phoneLength > MAX_PHONE_LENGTH) {
//            ISOException.throwIt(ISO7816.SW_WRONG_DATA);
//        }
//        Util.arrayCopy(buffer, offset, phoneNumber, (short) 0, phoneLength);
//        offset += phoneLength;
//        
//        // Read address
//        addressLength = Util.makeShort(buffer[offset], buffer[(short) (offset + 1)]);
//        offset += 2;
//        if (addressLength > MAX_ADDRESS_LENGTH) {
//            ISOException.throwIt(ISO7816.SW_WRONG_DATA);
//        }
//        Util.arrayCopy(buffer, offset, address, (short) 0, addressLength);
//        
//        ISOException.throwIt(ISO7816.SW_NO_ERROR);
//    }
//    
//    /**
//     * Get member information
//     */
//    private void getMemberInfo(APDU apdu) {
//        if (!authenticated) {
//            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
//        }
//        
//        byte[] buffer = apdu.getBuffer();
//        short offset = 0;
//        
//        // Name
//        buffer[offset++] = nameLength;
//        Util.arrayCopy(memberName, (short) 0, buffer, offset, nameLength);
//        offset += nameLength;
//        
//        // Birth date
//        Util.arrayCopy(birthDate, (short) 0, buffer, offset, (short) 8);
//        offset += 8;
//        
//        // Phone
//        buffer[offset++] = phoneLength;
//        Util.arrayCopy(phoneNumber, (short) 0, buffer, offset, phoneLength);
//        offset += phoneLength;
//        
//        // Address
//        buffer[offset++] = (byte) (addressLength >> 8);
//        buffer[offset++] = (byte) (addressLength & 0xFF);
//        Util.arrayCopy(address, (short) 0, buffer, offset, addressLength);
//        offset += addressLength;
//        
//        apdu.setOutgoingAndSend((short) 0, offset);
//    }
//    
//    /**
//     * Set member photo (can be called multiple times for large photos)
//     */
//    private void setMemberPhoto(APDU apdu) {
//        if (!authenticated) {
//            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
//        }
//        
//        byte[] buffer = apdu.getBuffer();
//        short lc = (short) (buffer[ISO7816.OFFSET_LC] & 0xFF);
//        short bytesRead = apdu.setIncomingAndReceive();
//        
//        // P1 indicates if this is first chunk (0x00) or continuation (0x01)
//        byte p1 = buffer[ISO7816.OFFSET_P1];
//        
//        if (p1 == 0x00) {
//            photoLength = 0; // Reset photo
//        }
//        
//        if ((short) (photoLength + lc) > MAX_PHOTO_SIZE) {
//            ISOException.throwIt(ISO7816.SW_FILE_FULL);
//        }
//        
//        Util.arrayCopy(buffer, ISO7816.OFFSET_CDATA, memberPhoto, photoLength, lc);
//        photoLength += lc;
//        
//        ISOException.throwIt(ISO7816.SW_NO_ERROR);
//    }
//    
//    /**
//     * Get member photo (may need multiple calls)
//     */
//    private void getMemberPhoto(APDU apdu) {
//        if (!authenticated) {
//            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
//        }
//        
//        byte[] buffer = apdu.getBuffer();
//        
//        // P1 indicates offset (in 256-byte chunks)
//        byte p1 = buffer[ISO7816.OFFSET_P1];
//        short offset = (short) (p1 * 256);
//        
//        if (offset >= photoLength) {
//            ISOException.throwIt(ISO7816.SW_RECORD_NOT_FOUND);
//        }
//        
//        short remaining = (short) (photoLength - offset);
//        short toSend = remaining > 256 ? 256 : remaining;
//        
//        // First 2 bytes: total photo length
//        buffer[0] = (byte) (photoLength >> 8);
//        buffer[1] = (byte) (photoLength & 0xFF);
//        
//        Util.arrayCopy(memberPhoto, offset, buffer, (short) 2, toSend);
//        apdu.setOutgoingAndSend((short) 0, (short) (toSend + 2));
//    }
//    
//    // ==================== PACKAGE MANAGEMENT METHODS ====================
//    
//    /**
//     * Set package
//     * Format: [type(1)][expiry(8)][registration(8)][sessions(2)]
//     */
//    private void setPackage(APDU apdu) {
//        if (!authenticated) {
//            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
//        }
//        
//        byte[] buffer = apdu.getBuffer();
//        short bytesRead = apdu.setIncomingAndReceive();
//        
//        short offset = ISO7816.OFFSET_CDATA;
//        
//        packageType = buffer[offset++];
//        Util.arrayCopy(buffer, offset, packageExpiry, (short) 0, (short) 8);
//        offset += 8;
//        Util.arrayCopy(buffer, offset, registrationDate, (short) 0, (short) 8);
//        offset += 8;
//        remainingSessions = Util.makeShort(buffer[offset], buffer[(short) (offset + 1)]);
//        
//        ISOException.throwIt(ISO7816.SW_NO_ERROR);
//    }
//    
//    /**
//     * Get package information
//     */
//    private void getPackage(APDU apdu) {
//        if (!authenticated) {
//            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
//        }
//        
//        byte[] buffer = apdu.getBuffer();
//        short offset = 0;
//        
//        buffer[offset++] = packageType;
//        Util.arrayCopy(packageExpiry, (short) 0, buffer, offset, (short) 8);
//        offset += 8;
//        Util.arrayCopy(registrationDate, (short) 0, buffer, offset, (short) 8);
//        offset += 8;
//        buffer[offset++] = (byte) (remainingSessions >> 8);
//        buffer[offset++] = (byte) (remainingSessions & 0xFF);
//        
//        apdu.setOutgoingAndSend((short) 0, offset);
//    }
//    
//    /**
//     * Upgrade package
//     */
//    private void upgradePackage(APDU apdu) {
//        if (!authenticated) {
//            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
//        }
//        
//        byte[] buffer = apdu.getBuffer();
//        short bytesRead = apdu.setIncomingAndReceive();
//        
//        short offset = ISO7816.OFFSET_CDATA;
//        
//        packageType = buffer[offset++];
//        Util.arrayCopy(buffer, offset, packageExpiry, (short) 0, (short) 8);
//        offset += 8;
//        remainingSessions = Util.makeShort(buffer[offset], buffer[(short) (offset + 1)]);
//        
//        ISOException.throwIt(ISO7816.SW_NO_ERROR);
//    }
//    
//    // ==================== CHECK-IN/OUT METHODS ====================
//    
//    /**
//     * Check-in
//     * Format: [date(8)][time(6)]
//     */
//    private void checkIn(APDU apdu) {
//        if (!authenticated) {
//            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
//        }
//        
//        // Check if package is valid
//        if (packageType == 0) {
//            ISOException.throwIt(SW_PACKAGE_EXPIRED);
//        }
//        
//        byte[] buffer = apdu.getBuffer();
//        short bytesRead = apdu.setIncomingAndReceive();
//        
//        short offset = ISO7816.OFFSET_CDATA;
//        
//        Util.arrayCopy(buffer, offset, lastCheckInDate, (short) 0, (short) 8);
//        offset += 8;
//        Util.arrayCopy(buffer, offset, lastCheckInTime, (short) 0, (short) 6);
//        
//        // Update month counter
//        byte month = buffer[(short) (ISO7816.OFFSET_CDATA + 4)];
//        if (month != currentMonth) {
//            currentMonth = month;
//            checkInCountThisMonth = 0;
//        }
//        checkInCountThisMonth++;
//        
//        // Deduct session if session-based
//        if (packageType == 2 && remainingSessions > 0) {
//            remainingSessions--;
//        }
//        
//        ISOException.throwIt(ISO7816.SW_NO_ERROR);
//    }
//    
//    /**
//     * Check-out
//     * Format: [time(6)]
//     */
//    private void checkOut(APDU apdu) {
//        if (!authenticated) {
//            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
//        }
//        
//        byte[] buffer = apdu.getBuffer();
//        short bytesRead = apdu.setIncomingAndReceive();
//        
//        Util.arrayCopy(buffer, ISO7816.OFFSET_CDATA, lastCheckOutTime, (short) 0, (short) 6);
//        
//        ISOException.throwIt(ISO7816.SW_NO_ERROR);
//    }
//    
//    /**
//     * Get check-in count this month
//     */
//    private void getCheckInCount(APDU apdu) {
//        if (!authenticated) {
//            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
//        }
//        
//        byte[] buffer = apdu.getBuffer();
//        buffer[0] = checkInCountThisMonth;
//        apdu.setOutgoingAndSend((short) 0, (short) 1);
//    }
//    
//    /**
//     * Get last check-in information
//     */
//    private void getLastCheckIn(APDU apdu) {
//        if (!authenticated) {
//            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
//        }
//        
//        byte[] buffer = apdu.getBuffer();
//        short offset = 0;
//        
//        Util.arrayCopy(lastCheckInDate, (short) 0, buffer, offset, (short) 8);
//        offset += 8;
//        Util.arrayCopy(lastCheckInTime, (short) 0, buffer, offset, (short) 6);
//        offset += 6;
//        Util.arrayCopy(lastCheckOutTime, (short) 0, buffer, offset, (short) 6);
//        offset += 6;
//        
//        apdu.setOutgoingAndSend((short) 0, offset);
//    }
//    
//    // ==================== PAYMENT METHODS ====================
//    
//    /**
//     * Get balance
//     */
//    private void getBalance(APDU apdu) {
//        if (!authenticated) {
//            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
//        }
//        
//        byte[] buffer = apdu.getBuffer();
//        buffer[0] = (byte) (balance >> 8);
//        buffer[1] = (byte) (balance & 0xFF);
//        apdu.setOutgoingAndSend((short) 0, (short) 2);
//    }
//    
//    /**
//     * Add balance
//     * Format: [amount(2)]
//     */
//    private void addBalance(APDU apdu) {
//        if (!authenticated) {
//            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
//        }
//        
//        byte[] buffer = apdu.getBuffer();
//        short bytesRead = apdu.setIncomingAndReceive();
//        
//        short amount = Util.makeShort(buffer[ISO7816.OFFSET_CDATA], 
//                                       buffer[(short) (ISO7816.OFFSET_CDATA + 1)]);
//        
//        balance += amount;
//        
//        // Record transaction (type 1 = add)
//        recordTransaction((byte) 1, amount);
//        
//        ISOException.throwIt(ISO7816.SW_NO_ERROR);
//    }
//    
//    /**
//     * Deduct balance
//     * Format: [amount(2)]
//     */
//    private void deductBalance(APDU apdu) {
//        if (!authenticated) {
//            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
//        }
//        
//        byte[] buffer = apdu.getBuffer();
//        short bytesRead = apdu.setIncomingAndReceive();
//        
//        short amount = Util.makeShort(buffer[ISO7816.OFFSET_CDATA], 
//                                       buffer[(short) (ISO7816.OFFSET_CDATA + 1)]);
//        
//        if (balance < amount) {
//            ISOException.throwIt(SW_INSUFFICIENT_BALANCE);
//        }
//        
//        balance -= amount;
//        
//        // Record transaction (type 2 = deduct)
//        recordTransaction((byte) 2, amount);
//        
//        ISOException.throwIt(ISO7816.SW_NO_ERROR);
//    }
//    
//    /**
//     * Get transaction
//     * P1 = transaction index (0-9)
//     */
//    private void getTransaction(APDU apdu) {
//        if (!authenticated) {
//            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
//        }
//        
//        byte[] buffer = apdu.getBuffer();
//        byte index = buffer[ISO7816.OFFSET_P1];
//        
//        if (index >= transactionCount) {
//            ISOException.throwIt(ISO7816.SW_RECORD_NOT_FOUND);
//        }
//        
//        // Return transaction: [date(8)][time(6)][amount(2)][type(1)]
//        Util.arrayCopy(transactions[index], (short) 0, buffer, (short) 0, (short) 16);
//        apdu.setOutgoingAndSend((short) 0, (short) 16);
//    }
//    
//    /**
//     * Record a transaction
//     */
//    private void recordTransaction(byte type, short amount) {
//        byte index = transactionCount;
//        if (transactionCount >= MAX_TRANSACTIONS) {
//            // Shift array left (remove oldest)
//            for (byte i = 1; i < MAX_TRANSACTIONS; i++) {
//                Util.arrayCopy(transactions[i], (short) 0, transactions[(byte) (i - 1)], (short) 0, (short) 16);
//            }
//            index = (byte) (MAX_TRANSACTIONS - 1);
//        } else {
//            transactionCount++;
//        }
//        
//        // Store: [date(8)][time(6)][amount(2)][type(1)][spare(5)]
//        short offset = 0;
//        Util.arrayCopy(lastCheckInDate, (short) 0, transactions[index], offset, (short) 8);
//        offset += 8;
//        Util.arrayCopy(lastCheckInTime, (short) 0, transactions[index], offset, (short) 6);
//        offset += 6;
//        transactions[index][offset++] = (byte) (amount >> 8);
//        transactions[index][offset++] = (byte) (amount & 0xFF);
//        transactions[index][offset] = type;
//    }
//}
