package utils;

import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Tiện ích bảo mật
 */
public class SecurityUtils {
    
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String ENCRYPTION_ALGORITHM = "AES";
    
    /**
     * Hash PIN bằng SHA-256
     */
    public static String hashPIN(String pin) {
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hash = md.digest(pin.getBytes("UTF-8"));
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Verify PIN
     */
    public static boolean verifyPIN(String inputPIN, String storedHash) {
        String inputHash = hashPIN(inputPIN);
        return inputHash != null && inputHash.equals(storedHash);
    }
    
    /**
     * Generate random AES key
     */
    public static String generateAESKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM);
            keyGen.init(128, new SecureRandom());
            SecretKey secretKey = keyGen.generateKey();
            
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Mã hóa dữ liệu bằng AES
     */
    public static String encrypt(String data, String keyString) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(keyString);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ENCRYPTION_ALGORITHM);
            
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            
            byte[] encrypted = cipher.doFinal(data.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encrypted);
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Giải mã dữ liệu AES
     */
    public static String decrypt(String encryptedData, String keyString) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(keyString);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ENCRYPTION_ALGORITHM);
            
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            
            byte[] encrypted = Base64.getDecoder().decode(encryptedData);
            byte[] decrypted = cipher.doFinal(encrypted);
            
            return new String(decrypted, "UTF-8");
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Generate random card ID
     */
    public static String generateCardId() {
        SecureRandom random = new SecureRandom();
        long cardNumber = 1000000000L + (long)(random.nextDouble() * 9000000000L);
        return String.valueOf(cardNumber);
    }
    
    /**
     * Validate PIN format
     */
    public static boolean isValidPIN(String pin) {
        if (pin == null || pin.length() != 4) {
            return false;
        }
        
        for (char c : pin.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Validate phone number
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return true; // Optional field
        }
        
        String cleaned = phone.replaceAll("[\\s\\-\\(\\)]", "");
        return cleaned.matches("^0\\d{9}$");
    }
}