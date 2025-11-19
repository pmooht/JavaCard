package Bai1;

import javacard.framework.*;

public class Bai3 extends Applet {


    private static final byte AREA_CLA = (byte) 0xA0;

    private static final byte INS_SQUARE = (byte) 0x10;
    private static final byte INS_RECT   = (byte) 0x11; 

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new Bai3().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
    }

    public void process(APDU apdu) {

        if (selectingApplet()) return;

        byte[] buf = apdu.getBuffer();

    
        apdu.setIncomingAndReceive();


        if (buf[ISO7816.OFFSET_CLA] != AREA_CLA) {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        short res; 

        switch (buf[ISO7816.OFFSET_INS]) {

            case INS_SQUARE: {

                short side = (short) (buf[ISO7816.OFFSET_P1] & 0xFF);
               
                res = (short) (side * side);
                break;
            }

            case INS_RECT: {

                short a = (short) (buf[ISO7816.OFFSET_P1] & 0xFF);
                short b = (short) (buf[ISO7816.OFFSET_P2] & 0xFF);

                res = (short) (a * b); 
                break;
            }

            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
                return; 
        }

        Util.setShort(buf, (short) 0, res);
        
        short le = apdu.setOutgoing();
        if (le < (short) 2) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        apdu.setOutgoingLength((short) 2);
        apdu.sendBytes((short) 0, (short) 2);
    }
}
