package Bai3;

import javacard.framework.*;

public class Bai3 extends Applet {

    // ===== CONSTANT =====
    private static final byte CLA = (byte) 0xA0;

    private static final byte INS_INPUT   = (byte) 0x01;
    private static final byte INS_GET_MSV = (byte) 0x02;
    private static final byte INS_GET_NAME= (byte) 0x03;
    private static final byte INS_GET_DOB = (byte) 0x04;
    private static final byte INS_GET_ALL = (byte) 0x05;

    // ===== DATA =====
    private byte[] masv = new byte[20];
    private short lenMasv;

    private byte[] hoten = new byte[40];
    private short lenHoten;

    private byte[] ngaysinh = new byte[15];
    private short lenNgaySinh;

    // ===== INSTALL =====
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new Bai3().register();
    }

    // ===== CONSTRUCTOR =====
    protected Bai3() {
        // Mã SV mc nh: CT060339
        byte[] defaultMSV = {
            'C','T','0','6','0','3','3','9'
        };
        Util.arrayCopy(defaultMSV, (short)0, masv, (short)0, (short)defaultMSV.length);
        lenMasv = (short) defaultMSV.length;
    }

    // ===== PROCESS =====
    public void process(APDU apdu) {
        if (selectingApplet()) return;

        byte[] buf = apdu.getBuffer();
        short len = apdu.setIncomingAndReceive();

        if (buf[ISO7816.OFFSET_CLA] != CLA)
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);

        switch (buf[ISO7816.OFFSET_INS]) {

        case INS_INPUT:
            parseInput(buf, ISO7816.OFFSET_CDATA, len);
            break;

        case INS_GET_MSV:
            sendData(apdu, masv, lenMasv);
            break;

        case INS_GET_NAME:
            sendData(apdu, hoten, lenHoten);
            break;

        case INS_GET_DOB:
            sendData(apdu, ngaysinh, lenNgaySinh);
            break;

        case INS_GET_ALL:
            short pos = 0;
            Util.arrayCopy(masv, (short)0, buf, pos, lenMasv);
            pos += lenMasv;
            buf[pos++] = '|';
            Util.arrayCopy(hoten, (short)0, buf, pos, lenHoten);
            pos += lenHoten;

            apdu.setOutgoing();
            apdu.setOutgoingLength(pos);
            apdu.sendBytes((short)0, pos);
            break;

        default:
            ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    // ===== PARSE INPUT =====
    // Format: masv|hoten|ngaysinh
    private void parseInput(byte[] buf, short offset, short len) {
        short start = offset;
        byte part = 1;

        for (short i = offset; i < offset + len; i++) {
            if (buf[i] == '|') {
                savePart(part, buf, start, (short)(i - start));
                part++;
                start = (short)(i + 1);
            }
        }
        savePart(part, buf, start, (short)(offset + len - start));
    }

    private void savePart(byte part, byte[] buf, short start, short length) {
        switch (part) {
        case 1:
        	// masv (có th ghi è)
        	if(length > 0){
				Util.arrayCopy(buf, start, masv, (short)0, length);
				lenMasv = length;
        	}
            break;

        case 2: // hoten
            Util.arrayCopy(buf, start, hoten, (short)0, length);
            lenHoten = length;
            break;

        case 3: // ngaysinh
            Util.arrayCopy(buf, start, ngaysinh, (short)0, length);
            lenNgaySinh = length;
            break;
        }
    }

    // ===== SEND DATA =====
    private void sendData(APDU apdu, byte[] data, short length) {
        apdu.setOutgoing();
        apdu.setOutgoingLength(length);
        apdu.sendBytesLong(data, (short)0, length);
    }
}
