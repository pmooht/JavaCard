package Bai1;

import javacard.framework.*;

public class Bai1 extends Applet {
    private static final byte[] MSG = {
        (byte)'X', (byte)'i', (byte)'n', (byte)' ',
        (byte)'c', (byte)'h', (byte)'a', (byte)'o',
        (byte)',', (byte)' ', (byte)'K', (byte)'M', (byte)'A'
    };

    // Constructor
    protected Bai1() {
        register(); 
    }
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new Bai1();
    }

    public void process(APDU apdu) {
        if (selectingApplet()) return;

        byte[] buffer = apdu.getBuffer();

        apdu.setIncomingAndReceive();

        switch (buffer[ISO7816.OFFSET_INS]) {
            case (byte)0x00:
                short len = (short) MSG.length;

                apdu.setOutgoing();            
                apdu.setOutgoingLength(len);   
                Util.arrayCopyNonAtomic(MSG, (short)0, buffer, (short)0, len); 
                apdu.sendBytes((short)0, len); 
                break;

            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }
}
