package Bai1;

import javacard.framework.*;

public class Bai2 extends Applet {

    final static byte INFO_CLA = (byte)0xA0;

    final static byte INS_HOTEN = (byte)0x00;
    final static byte INS_NGAYSINH = (byte)0x01;
    final static byte INS_HOTEN_NGAYSINH = (byte)0x02;

    private static final byte[] HOTEN = {
        (byte)'N', (byte)'g', (byte)'u', (byte)'y', (byte)'e', (byte)'n', (byte)' ', (byte)'T', (byte)'h', (byte)'o', (byte)'n', (byte)'g'
    };
    private static final byte[] NGAYSINH = {
        (byte)'1', (byte)'0', (byte)'/', (byte)'0', (byte)'4', (byte)'/', (byte)'2', (byte)'0', (byte)'0', (byte)'3'
    };

    protected Bai2() {
        register(); 
    }

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new Bai2();
    }

    public void process(APDU apdu) {
        if (selectingApplet()) return;

        byte[] buf = apdu.getBuffer();

        apdu.setIncomingAndReceive();

        if (buf[ISO7816.OFFSET_CLA] != INFO_CLA) {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        short le;
        switch (buf[ISO7816.OFFSET_INS]) {
            case INS_HOTEN:
                le = (short) HOTEN.length;
                apdu.setOutgoing();
                apdu.setOutgoingLength(le);
                Util.arrayCopyNonAtomic(HOTEN, (short)0, buf, (short)0, le);
                apdu.sendBytes((short)0, le);
                break;

            case INS_NGAYSINH:
                le = (short) NGAYSINH.length;
                apdu.setOutgoing();
                apdu.setOutgoingLength(le);
                Util.arrayCopyNonAtomic(NGAYSINH, (short)0, buf, (short)0, le);
                apdu.sendBytes((short)0, le);
                break;

            case INS_HOTEN_NGAYSINH:
                le = (short) (HOTEN.length + NGAYSINH.length + 1); 
                apdu.setOutgoing();
                apdu.setOutgoingLength(le);
            
                Util.arrayCopyNonAtomic(HOTEN, (short)0, buf, (short)0, (short)HOTEN.length);
              
                buf[HOTEN.length] = (byte)' ';
              
                Util.arrayCopyNonAtomic(NGAYSINH, (short)0, buf, (short)(HOTEN.length + 1), (short)NGAYSINH.length);
                apdu.sendBytes((short)0, le);
                break;

            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }
}
