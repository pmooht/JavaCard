package BaiDieuKien;

import javacard.framework.*;

public class Bai2 extends Applet
{
	final static byte CLA = (byte) 0xA0;
	final static byte INS_INPUT = (byte) 0x01;
	final static byte INS_PRINT = (byte) 0x02;
	final static short MAX_BUFFER_SIZE = (short)128;
	
	private byte[] tempBuffer;
	private short tempLen = 0;

	public static void install(byte[] bArray, short bOffset, byte bLength) 
	{
		new Bai2().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
	}
	
	public Bai2(){
		tempBuffer = JCSystem.makeTransientByteArray(
			MAX_BUFFER_SIZE,
			JCSystem.CLEAR_ON_DESELECT
		);
		tempLen = 0;
	}

	public void process(APDU apdu)
	{
		if (selectingApplet())
		{
			return;
		}

		byte[] buf = apdu.getBuffer();
		
		short len = apdu.setIncomingAndReceive();
		
		switch (buf[ISO7816.OFFSET_INS])
		{
		case INS_INPUT:
			if(len == 0 || len > MAX_BUFFER_SIZE){
				ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
			}
			Util.arrayCopyNonAtomic(buf, ISO7816.OFFSET_CDATA, tempBuffer, (short)0, len);
			tempLen = len;
			break;
			
		case INS_PRINT:
			apdu.setOutgoing();
			apdu.setOutgoingLength(tempLen);
			apdu.sendBytesLong(tempBuffer, (short)0, tempLen);
			
			// ===== GP BO DEM =====
            Util.arrayFillNonAtomic(tempBuffer, (short) 0, (short) tempLen, (byte) 0);
            tempLen = 0;
            
            break;
			
		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}

}