/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gymcard.Crypto;

import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;
import java.util.Base64;

public class RSAKeyUtils {

    // encode RSAPublicKey -> modulus bytes (đơn giản để đưa lên thẻ)
    public static byte[] exportModulus(RSAPublicKey pubKey) {
        return pubKey.getModulus().toByteArray();
    }

    // decode modulus -> RSAPublicKey (exponent = 65537 cố định)
    public static RSAPublicKey importFromModulus(byte[] modulusBytes) throws Exception {
        java.math.BigInteger mod = new java.math.BigInteger(1, modulusBytes);
        java.math.BigInteger exp = java.math.BigInteger.valueOf(65537);
        RSAPublicKeySpec spec = new RSAPublicKeySpec(mod, exp);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) kf.generatePublic(spec);
    }

    // dùng khi truyền full encoded key (X.509)
    public static byte[] encodeRSAPublicKey(RSAPublicKey pubKey) {
        return pubKey.getEncoded();
    }

    public static RSAPublicKey decodeRSAPublicKey(byte[] x509Bytes) throws Exception {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(x509Bytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) kf.generatePublic(spec);
    }
}