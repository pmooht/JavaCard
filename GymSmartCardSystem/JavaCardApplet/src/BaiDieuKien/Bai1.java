package BaiDieuKien;

import javacard.framework.*;

public class Bai1 extends Applet
{
	final static byte CLA = (byte) 0xA0;
	
	final static byte INS_SET_ALL = (byte)0x01;
	final static byte INS_GET_FIELD = (byte)0x11;
	final static byte INS_GET_ALL = (byte)0x12;

	byte[] masv = new byte[20];
	short len_masv = 0;
	
	byte[] hoten = new byte[40];
	short len_hoten = 0;
	
	byte[] ngaysinh = new byte[15];
	short len_ngaysinh = 0;
	
	byte[] quequan = new byte[40];
	short len_quequan = 0;

	public static void install(byte[] bArray, short bOffset, byte bLength) 
	{
		new Bai1().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
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
		case INS_SET_ALL:
			parseData(buf, ISO7816.OFFSET_CDATA, len);
			break;
			
		case INS_GET_FIELD:
			byte field = buf[ISO7816.OFFSET_P1];
			short outLen = 0;
			
			switch(field){
			case 1:
				Util.arrayCopy(masv, (short)0, buf, (short)0, len_masv);
				outLen = len_masv;
				break;
			
			case 2:
				Util.arrayCopy(hoten, (short)0, buf, (short)0, len_hoten);
				outLen = len_hoten;
				break;
				
			case 3:
				Util.arrayCopy(ngaysinh, (short)0, buf, (short)0, len_ngaysinh);
				outLen = len_ngaysinh;
				break;
				
			case 4:
				Util.arrayCopy(quequan, (short)0, buf, (short)0, len_quequan);
				outLen = len_quequan;
				break;
			
			default:
                ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
			}
			
			apdu.setOutgoing();
			apdu.setOutgoingLength(outLen);
			apdu.sendBytes((short)0, outLen);
			break;
			
		case INS_GET_ALL:
			short pos = 0;
			
			Util.arrayCopy(masv, (short)0, buf, pos, len_masv);
			pos += len_masv;
			buf[pos++] = '|';
			
			Util.arrayCopy(hoten, (short)0, buf, pos, len_hoten);
			pos += len_hoten;
			buf[pos++] = '|';
			
			Util.arrayCopy(ngaysinh, (short)0, buf, pos, len_ngaysinh);
			pos += len_ngaysinh;
			buf[pos++] = '|';
			
			Util.arrayCopy(quequan, (short)0, buf, pos, len_quequan);
			pos += len_quequan;
			
			
			apdu.setOutgoing();
			apdu.setOutgoingLength(pos);
			apdu.sendBytes((short)0, pos);
			break;
			
		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}
	
	
	private void parseData(byte[] buf, short offset, short len){
		short i;
		short start = offset;
		byte part = 1;
		
		for(i = offset; i < offset + len; i++){
			if(buf[i] == '|'){
				copyPart(part, buf, start, (short)(i - start));
				part++;
				start = (short)(i+1);
			}
		}
		
		copyPart(part, buf, start, (short)(offset + len - start));
	}
	
	private void copyPart(byte part, byte[] buf, short start, short length){
		switch(part){
		case 1:
			Util.arrayCopy(buf, start, masv, (short)0, length);
			len_masv = length;
			break;
			
		case 2:
			Util.arrayCopy(buf, start, hoten, (short)0, length);
			len_hoten = length;
			break;
			
		case 3: 
			Util.arrayCopy(buf, start, ngaysinh,(short)0, length);
			len_ngaysinh = length;
			break;
			
		case 4: 
			Util.arrayCopy(buf, start, quequan, (short)0, length);
			len_quequan = length;
			break;
		}
	}

}
