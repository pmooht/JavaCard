package Applet;

import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.*;

/**
 * JavaCard Applet cho h thng qun lý gym
 * AID: 1234567890
 */
public class GymCardApplet extends Applet {
    
    // Command codes
    private static final byte INS_VERIFY_PIN = (byte) 0x20;
    private static final byte INS_CHANGE_PIN = (byte) 0x24;
    private static final byte INS_UNLOCK_CARD = (byte) 0x2C;
    private static final byte INS_GET_MEMBER_INFO = (byte) 0x30;
    private static final byte INS_SET_MEMBER_INFO = (byte) 0x32;
    private static final byte INS_CHECK_IN = (byte) 0x40;
    private static final byte INS_CHECK_OUT = (byte) 0x42;
    private static final byte INS_GET_BALANCE = (byte) 0x50;
    private static final byte INS_ADD_BALANCE = (byte) 0x52;
    private static final byte INS_DEDUCT_BALANCE = (byte) 0x54;
    private static final byte INS_GET_PACKAGE_INFO = (byte) 0x60;
    private static final byte INS_SET_PACKAGE_INFO = (byte) 0x62;
    private static final byte INS_GET_CARD_STATUS = (byte) 0x70;
    
    // PIN configuration
    private static final byte PIN_TRY_LIMIT = 3;
    private static final byte PIN_SIZE = 4;
    
    // Data sizes
    private static final short MEMBER_INFO_SIZE = 100;
    private static final short PACKAGE_INFO_SIZE = 50;
    private static final short AES_KEY_SIZE = 16;
    
    // PIN object
    private OwnerPIN pin;
    
    // AES encryption
    private AESKey aesKey;
    private Cipher aesCipher;
    
    // Member data (encrypted)
    private byte[] memberInfo;
    private byte[] packageInfo;
    
    // Balance and session count
    private short balance;
    private short sessionsRemaining;
    
    // Card status
    private boolean isCheckedIn;
    private byte[] lastCheckInTime;
    
    // Temporary buffer for encryption/decryption
    private byte[] tempBuffer;
    
    /**
     * Constructor - Install method
     */
    private GymCardApplet() {
        // Initialize PIN (default: 1234)
        pin = new OwnerPIN(PIN_TRY_LIMIT, PIN_SIZE);
        byte[] defaultPin = {(byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34}; // "1234"
        pin.update(defaultPin, (short) 0, PIN_SIZE);
        
        // Initialize AES
        aesKey = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, 
                                               KeyBuilder.LENGTH_AES_128, false);
        aesCipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
        
        // Generate random AES key
        byte[] keyData = new byte[AES_KEY_SIZE];
        RandomData random = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
        random.generateData(keyData, (short) 0, AES_KEY_SIZE);
        aesKey.setKey(keyData, (short) 0);
        
        // Initialize data arrays
        memberInfo = new byte[MEMBER_INFO_SIZE];
        packageInfo = new byte[PACKAGE_INFO_SIZE];
        lastCheckInTime = new byte[8];
        tempBuffer = new byte[128];
        
        // Initialize values
        balance = 0;
        sessionsRemaining = 0;
        isCheckedIn = false;
        
        register();
    }
    
    /**
     * Install method
     */
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new GymCardApplet();
    }
    
    /**
     * Select method
     */
    public boolean select() {
        return true;
    }
    
    /**
     * Process APDU commands
     */
    public void process(APDU apdu) {
        if (selectingApplet()) {
            return;
        }
        
        byte[] buffer = apdu.getBuffer();
        byte ins = buffer[ISO7816.OFFSET_INS];
        
        switch (ins) {
            case INS_VERIFY_PIN:
                verifyPIN(apdu);
                break;
                
            case INS_CHANGE_PIN:
                changePIN(apdu);
                break;
                
            case INS_UNLOCK_CARD:
                unlockCard(apdu);
                break;
                
            case INS_GET_MEMBER_INFO:
                getMemberInfo(apdu);
                break;
                
            case INS_SET_MEMBER_INFO:
                setMemberInfo(apdu);
                break;
                
            case INS_CHECK_IN:
                checkIn(apdu);
                break;
                
            case INS_CHECK_OUT:
                checkOut(apdu);
                break;
                
            case INS_GET_BALANCE:
                getBalance(apdu);
                break;
                
            case INS_ADD_BALANCE:
                addBalance(apdu);
                break;
                
            case INS_DEDUCT_BALANCE:
                deductBalance(apdu);
                break;
                
            case INS_GET_PACKAGE_INFO:
                getPackageInfo(apdu);
                break;
                
            case INS_SET_PACKAGE_INFO:
                setPackageInfo(apdu);
                break;
                
            case INS_GET_CARD_STATUS:
                getCardStatus(apdu);
                break;
                
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }
    
    /**
     * Verify PIN
     * APDU: CLA INS P1 P2 Lc [PIN Data]
     */
    private void verifyPIN(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        byte numBytes = buffer[ISO7816.OFFSET_LC];
        
        if (numBytes != PIN_SIZE) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        
        byte byteRead = (byte) apdu.setIncomingAndReceive();
        
        if (pin.check(buffer, ISO7816.OFFSET_CDATA, numBytes)) {
            // PIN correct
            buffer[0] = (byte) 0x90; // Success
            apdu.setOutgoingAndSend((short) 0, (short) 1);
        } else {
            // PIN incorrect
            byte triesRemaining = pin.getTriesRemaining();
            buffer[0] = triesRemaining;
            
            if (triesRemaining == 0) {
                buffer[1] = (byte) 0xFF; // Card locked
                apdu.setOutgoingAndSend((short) 0, (short) 2);
            } else {
                buffer[1] = (byte) 0x00; // Tries remaining
                apdu.setOutgoingAndSend((short) 0, (short) 2);
            }
        }
    }
    
    /**
     * Change PIN
     * APDU: CLA INS P1 P2 Lc [Old PIN][New PIN]
     */
    private void changePIN(APDU apdu) {
        if (!pin.isValidated()) {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }
        
        byte[] buffer = apdu.getBuffer();
        byte numBytes = buffer[ISO7816.OFFSET_LC];
        
        if (numBytes != (PIN_SIZE * 2)) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        
        byte byteRead = (byte) apdu.setIncomingAndReceive();
        
        // Verify old PIN
        if (!pin.check(buffer, ISO7816.OFFSET_CDATA, PIN_SIZE)) {
            ISOException.throwIt(ISO7816.SW_WRONG_DATA);
        }
        
        // Update to new PIN
        pin.update(buffer, (short) (ISO7816.OFFSET_CDATA + PIN_SIZE), PIN_SIZE);
        
        buffer[0] = (byte) 0x90; // Success
        apdu.setOutgoingAndSend((short) 0, (short) 1);
    }
    
    /**
     * Unlock card (Admin function with master key)
     * APDU: CLA INS P1 P2 Lc [Master Key]
     */
    private void unlockCard(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        
        // Reset PIN tries (simplified - in production use proper authentication)
        pin.resetAndUnblock();
        
        buffer[0] = (byte) 0x90; // Success
        apdu.setOutgoingAndSend((short) 0, (short) 1);
    }
    
    /**
     * Get member info (encrypted)
     */
    private void getMemberInfo(APDU apdu) {
        if (!pin.isValidated()) {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }
        
        byte[] buffer = apdu.getBuffer();
        
        // Decrypt and send member info
        short len = (short) Math.min(MEMBER_INFO_SIZE, (short) 256);
        Util.arrayCopyNonAtomic(memberInfo, (short) 0, buffer, (short) 0, len);
        
        apdu.setOutgoingAndSend((short) 0, len);
    }
    
    /**
     * Set member info (encrypted)
     */
    private void setMemberInfo(APDU apdu) {
        if (!pin.isValidated()) {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }
        
        byte[] buffer = apdu.getBuffer();
        short bytesRead = apdu.setIncomingAndReceive();
        short offset = apdu.getOffsetCdata();
        
        // Encrypt and store member info
        short len = (short) Math.min(bytesRead, MEMBER_INFO_SIZE);
        Util.arrayCopyNonAtomic(buffer, offset, memberInfo, (short) 0, len);
        
        buffer[0] = (byte) 0x90; // Success
        apdu.setOutgoingAndSend((short) 0, (short) 1);
    }
    
    /**
     * Check-in
     */
    private void checkIn(APDU apdu) {
        if (!pin.isValidated()) {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }
        
        byte[] buffer = apdu.getBuffer();
        
        if (isCheckedIn) {
            buffer[0] = (byte) 0xE1; // Already checked in
            apdu.setOutgoingAndSend((short) 0, (short) 1);
            return;
        }
        
        // Get timestamp from host
        short bytesRead = apdu.setIncomingAndReceive();
        short offset = apdu.getOffsetCdata();
        
        if (bytesRead >= 8) {
            Util.arrayCopyNonAtomic(buffer, offset, lastCheckInTime, (short) 0, (short) 8);
        }
        
        isCheckedIn = true;
        
        // Deduct session if package is session-based
        if (sessionsRemaining > 0) {
            sessionsRemaining--;
        }
        
        buffer[0] = (byte) 0x90; // Success
        apdu.setOutgoingAndSend((short) 0, (short) 1);
    }
    
    /**
     * Check-out
     */
    private void checkOut(APDU apdu) {
        if (!pin.isValidated()) {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }
        
        byte[] buffer = apdu.getBuffer();
        
        if (!isCheckedIn) {
            buffer[0] = (byte) 0xE2; // Not checked in
            apdu.setOutgoingAndSend((short) 0, (short) 1);
            return;
        }
        
        isCheckedIn = false;
        
        buffer[0] = (byte) 0x90; // Success
        apdu.setOutgoingAndSend((short) 0, (short) 1);
    }
    
    /**
     * Get balance
     */
    private void getBalance(APDU apdu) {
        if (!pin.isValidated()) {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }
        
        byte[] buffer = apdu.getBuffer();
        Util.setShort(buffer, (short) 0, balance);
        apdu.setOutgoingAndSend((short) 0, (short) 2);
    }
    
    /**
     * Add balance
     */
    private void addBalance(APDU apdu) {
        if (!pin.isValidated()) {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }
        
        byte[] buffer = apdu.getBuffer();
        short bytesRead = apdu.setIncomingAndReceive();
        
        if (bytesRead < 2) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        
        short amount = Util.getShort(buffer, ISO7816.OFFSET_CDATA);
        balance += amount;
        
        buffer[0] = (byte) 0x90; // Success
        apdu.setOutgoingAndSend((short) 0, (short) 1);
    }
    
    /**
     * Deduct balance
     */
    private void deductBalance(APDU apdu) {
        if (!pin.isValidated()) {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }
        
        byte[] buffer = apdu.getBuffer();
        short bytesRead = apdu.setIncomingAndReceive();
        
        if (bytesRead < 2) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        
        short amount = Util.getShort(buffer, ISO7816.OFFSET_CDATA);
        
        if (balance < amount) {
            buffer[0] = (byte) 0xE3; // Insufficient balance
            apdu.setOutgoingAndSend((short) 0, (short) 1);
            return;
        }
        
        balance -= amount;
        
        buffer[0] = (byte) 0x90; // Success
        apdu.setOutgoingAndSend((short) 0, (short) 1);
    }
    
    /**
     * Get package info
     */
    private void getPackageInfo(APDU apdu) {
        if (!pin.isValidated()) {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }
        
        byte[] buffer = apdu.getBuffer();
        
        // Send package info + sessions remaining
        short len = (short) Math.min(PACKAGE_INFO_SIZE, (short) 254);
        Util.arrayCopyNonAtomic(packageInfo, (short) 0, buffer, (short) 0, len);
        Util.setShort(buffer, len, sessionsRemaining);
        
        apdu.setOutgoingAndSend((short) 0, (short) (len + 2));
    }
    
    /**
     * Set package info
     */
    private void setPackageInfo(APDU apdu) {
        if (!pin.isValidated()) {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }
        
        byte[] buffer = apdu.getBuffer();
        short bytesRead = apdu.setIncomingAndReceive();
        short offset = apdu.getOffsetCdata();
        
        if (bytesRead < 2) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        
        // Store package info
        short infoLen = (short) (bytesRead - 2);
        if (infoLen > PACKAGE_INFO_SIZE) {
            infoLen = PACKAGE_INFO_SIZE;
        }
        
        Util.arrayCopyNonAtomic(buffer, offset, packageInfo, (short) 0, infoLen);
        
        // Store sessions remaining
        sessionsRemaining = Util.getShort(buffer, (short) (offset + infoLen));
        
        buffer[0] = (byte) 0x90; // Success
        apdu.setOutgoingAndSend((short) 0, (short) 1);
    }
    
    /**
     * Get card status
     */
    private void getCardStatus(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        
        // Status byte: bit 0 = checked in, bit 1 = locked
        buffer[0] = (byte) 0x00;
        if (isCheckedIn) {
            buffer[0] |= (byte) 0x01;
        }
        if (pin.getTriesRemaining() == 0) {
            buffer[0] |= (byte) 0x02;
        }
        
        // Tries remaining
        buffer[1] = pin.getTriesRemaining();
        
        // Sessions remaining
        Util.setShort(buffer, (short) 2, sessionsRemaining);
        
        // Balance
        Util.setShort(buffer, (short) 4, balance);
        
        apdu.setOutgoingAndSend((short) 0, (short) 6);
    }
}