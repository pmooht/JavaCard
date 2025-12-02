/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gymcard.Crypto;

import java.security.*;
import java.nio.file.*;
import java.util.Base64;

public class AppKeyStore {

    private static final String KEY_FILE = "app_rsa.key";

    public static KeyPair loadOrCreateAppKeyPair() throws Exception {
        if (Files.exists(Paths.get(KEY_FILE))) {
            String content = new String(Files.readAllBytes(Paths.get(KEY_FILE)), "UTF-8");
            String[] parts = content.split("\\|");
            byte[] pubBytes = Base64.getDecoder().decode(parts[0]);
            byte[] privBytes = Base64.getDecoder().decode(parts[1]);

            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey pub = kf.generatePublic(new java.security.spec.X509EncodedKeySpec(pubBytes));
            PrivateKey priv = kf.generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(privBytes));
            return new KeyPair(pub, priv);
        } else {
            // Tạo mới
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();

            String pubB64 = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());
            String privB64 = Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded());
            String content = pubB64 + "|" + privB64;
            Files.write(Paths.get(KEY_FILE), content.getBytes("UTF-8"));

            return kp;
        }
    }
}