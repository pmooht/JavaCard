package BaiDieuKien;

import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.*;

public class Bai5 extends Applet {

    /* ================== CONSTANT ================== */

    private static final byte CLA_BAI5 = (byte) 0xA0;
    private static final byte INS_SIGN   = (byte) 0x10;
    private static final byte INS_VERIFY = (byte) 0x20;

    // RSA key length
    private static final short RSA_KEY_BITS = KeyBuilder.LENGTH_RSA_1024;

    // RSA 1024  ch ký dài 128 bytes
    private static final short SIG_LENGTH = (short) 128;

    /* ================== GLOBAL OBJECT ================== */

    private RSAPrivateKey privateKey;   // khóa bí mt dùng  ký
    private RSAPublicKey  publicKey;    // khóa công khai dùng  verify
    private Signature     rsaSignature; // i tng ký RSA

    private byte[] sigBuffer;            // buffer lu ch ký

    /* ================== CONSTRUCTOR ================== */

    private Bai5() {

        // Buffer cha ch ký
        sigBuffer = new byte[SIG_LENGTH];

        try {
            // To cp khóa RSA khi cài applet (fix key)
            KeyPair keyPair = new KeyPair(KeyPair.ALG_RSA, RSA_KEY_BITS);
            keyPair.genKeyPair();

            privateKey = (RSAPrivateKey) keyPair.getPrivate();
            publicKey  = (RSAPublicKey)  keyPair.getPublic();

            // Thut toán: RSA + SHA + PKCS1
            rsaSignature = Signature.getInstance(
                    Signature.ALG_RSA_SHA_PKCS1, false);

        } catch (Exception e) {
            rsaSignature = null;
        }
    }

    /* ================== INSTALL ================== */

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new Bai5().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
    }

    /* ================== PROCESS ================== */

    public void process(APDU apdu) {

        // Khi SELECT applet
        if (selectingApplet()) {
            return;
        }

        byte[] buffer = apdu.getBuffer();

        // Kim tra CLA
        if (buffer[ISO7816.OFFSET_CLA] != CLA_BAI5) {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        // Kim tra trng thái i tng crypto
        if (rsaSignature == null || privateKey == null) {
            ISOException.throwIt((short) 0x6F01);
        }

        switch (buffer[ISO7816.OFFSET_INS]) {

            case INS_SIGN:
                signData(apdu);
                break;

            case INS_VERIFY:
                verifySignature(apdu);
                break;

            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    /* ================== SIGN FUNCTION ================== */

    /**
     * INS = 0x10
     * Input : DATA
     * Output: RSA Signature (128 bytes)
     */
    private void signData(APDU apdu) {

        byte[] buffer = apdu.getBuffer();

        // Nhn toàn b d liu t APDU (chun JavaCard)
        short bytesRead = apdu.setIncomingAndReceive();
        short offset = ISO7816.OFFSET_CDATA;

        while (bytesRead > 0) {
            offset += bytesRead;
            bytesRead = apdu.receiveBytes(offset);
        }

        short dataLen = (short) (offset - ISO7816.OFFSET_CDATA);

        if (dataLen <= 0) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        // Khi to ch  ký
        rsaSignature.init(privateKey, Signature.MODE_SIGN);

        // Thc hin ký
        short sigLen = rsaSignature.sign(
                buffer,
                ISO7816.OFFSET_CDATA,
                dataLen,
                sigBuffer,
                (short) 0
        );

        // Tr ch ký ra APDU
        Util.arrayCopy(sigBuffer, (short) 0, buffer, (short) 0, sigLen);
        apdu.setOutgoingAndSend((short) 0, sigLen);
    }

    /* ================== VERIFY FUNCTION ================== */

    /**
     * INS = 0x20
     * Input : DATA || SIGNATURE
     * Output: 01 (valid) | 00 (invalid)
     */
    private void verifySignature(APDU apdu) {

        byte[] buffer = apdu.getBuffer();

        // Nhn toàn b d liu
        short bytesRead = apdu.setIncomingAndReceive();
        short offset = ISO7816.OFFSET_CDATA;

        while (bytesRead > 0) {
            offset += bytesRead;
            bytesRead = apdu.receiveBytes(offset);
        }

        short totalLen = (short) (offset - ISO7816.OFFSET_CDATA);

        // Tng  dài phi > ch ký
        if (totalLen <= SIG_LENGTH) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        // DATA length = total - signature
        short dataLen = (short) (totalLen - SIG_LENGTH);

        // Khi to verify
        rsaSignature.init(publicKey, Signature.MODE_VERIFY);

        boolean isValid = rsaSignature.verify(
                buffer,
                ISO7816.OFFSET_CDATA,
                dataLen,
                buffer,
                (short) (ISO7816.OFFSET_CDATA + dataLen),
                SIG_LENGTH
        );

        // Tr kt qu
        buffer[0] = isValid ? (byte) 0x01 : (byte) 0x00;
        apdu.setOutgoingAndSend((short) 0, (short) 1);
    }
}
