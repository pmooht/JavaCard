package Bai4;

import javacard.framework.*;

public class Server extends Applet implements HpInterface
{
	private static final byte[] STUDENT_IDS = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05};
	private static final byte[] HOC_PHI = new byte[]{0x12, 0x19, 0x0F, 0x10, 0x14};
	
	private static final byte TOTAL_STUDENTS = (byte)0x05;

	public static void install(byte[] bArray, short bOffset, byte bLength) {
        new Server();
    }
    
    public Server(){
	    register();
    }

	public void process(APDU apdu)
	{
		if (selectingApplet())
		{
			return;
		}
	}
	
	public Shareable getShareableInterfaceObject(AID clientAID, byte parameter) {
        if (parameter != (byte)0x00) {
            return null;
        }
        return this;
    }
    
    public byte xemHP(byte svID) {
        for (byte i = 0; i < TOTAL_STUDENTS; i++) {
            if (STUDENT_IDS[i] == svID) {
                return HOC_PHI[i];
            }
        }
        return (byte)0x00;
    }
}