package Bai4;

import javacard.framework.*;
import Bai4.HpInterface;

public class Client extends Applet {
    
    private static final byte[] SERVER_AID = {
        (byte)0x11, (byte)0x22, (byte)0x33, (byte)0x44, (byte)0x55, (byte)0x66, (byte)0x77, (byte)0x02
    };
    
    private static final byte INS_XEM_HOCPHI = (byte)0x20;
    
    private HpInterface serverInterface;
    
   
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new Client();
    }
    
    protected Client() {
        register();
    }
    
    private void connectToServer() {
        if (serverInterface == null) {
            AID serverAID = JCSystem.lookupAID(SERVER_AID, (short)0, (byte)SERVER_AID.length);
            
            if (serverAID == null) {
                ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
            }
            Shareable sio = JCSystem.getAppletShareableInterfaceObject(serverAID, (byte)0);
            
            if (sio == null) {
                ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
            }
            
            if (sio instanceof HpInterface) {
                serverInterface = (HpInterface) sio;
            } else {
                ISOException.throwIt(ISO7816.SW_DATA_INVALID);
            }
        }
    }
    
  
    public void process(APDU apdu) {
        if (selectingApplet()) {
            return;
        }
        
        byte[] buffer = apdu.getBuffer();
        byte ins = buffer[ISO7816.OFFSET_INS];
        
        switch (ins) {
            case INS_XEM_HOCPHI:
                xemHocPhi(apdu);
                break;
                
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }
    
  
    private void xemHocPhi(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        byte svID = buffer[ISO7816.OFFSET_P1];
        
        connectToServer();
        
        byte hocPhi = serverInterface.xemHP(svID);
        
        buffer[0] = hocPhi;
        apdu.setOutgoingAndSend((short)0, (short)1);
    }
}