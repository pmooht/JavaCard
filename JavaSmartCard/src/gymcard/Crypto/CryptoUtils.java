/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gymcard.Crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.GeneralSecurityException;
import java.util.Base64;

public class CryptoUtils {

    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int AES_KEY_BITS = 128;
    private static final int IV_LEN = 16;

    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Sinh AES key từ PIN + salt bằng PBKDF2.
     * (Dùng khi bạn muốn key = chức năng của PIN, phía app)
     */
    public static SecretKey deriveKeyFromPin(char[] pin, byte[] salt) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(pin, salt, 65536, AES_KEY_BITS);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Sinh IV ngẫu nhiên 16 bytes.
     */
    public static byte[] generateIv() {
        byte[] iv = new byte[IV_LEN];
        secureRandom.nextBytes(iv);
        return iv;
    }

    public static byte[] aesEncrypt(byte[] keyBytes, byte[] ivBytes, byte[] plaintext)
            throws GeneralSecurityException {

        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        return cipher.doFinal(plaintext);
    }

    public static byte[] aesDecrypt(byte[] keyBytes, byte[] ivBytes, byte[] ciphertext)
            throws GeneralSecurityException {

        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        return cipher.doFinal(ciphertext);
    }

    // Helper encode/decode Base64 cho việc lưu trữ / debug
    public static String toBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public static byte[] fromBase64(String s) {
        return Base64.getDecoder().decode(s);
    }
}