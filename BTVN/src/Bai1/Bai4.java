package Bai1;

import javacard.framework.*;

public class Bai4 extends Applet {

    private static final byte CLA_APP = (byte) 0xA0;

    private static final byte INS_SET_ALL   = (byte) 0x10; // Ghi du lieu MSSV|HOTEN|DOB|QUE
    private static final byte INS_GET_MSSV  = (byte) 0x20;
    private static final byte INS_GET_HOTEN = (byte) 0x21;
    private static final byte INS_GET_DOB   = (byte) 0x22;
    private static final byte INS_GET_QUE   = (byte) 0x23;
    private static final byte INS_GET_ALL   = (byte) 0x24;

    private static final byte INS_GET_LEN   = (byte) 0x25;  // Tr 4 byte: len(MSSV,HOTEN,DOB,QUE)
    private static final byte INS_GET_CHUNK = (byte) 0x26;  // P1=field, P2=offset

    private static final byte FIELD_ALL   = (byte) 0x00;
    private static final byte FIELD_MSSV  = (byte) 0x01;
    private static final byte FIELD_HOTEN = (byte) 0x02;
    private static final byte FIELD_DOB   = (byte) 0x03;
    private static final byte FIELD_QUE   = (byte) 0x04;

    private static final short MAX_MSSV = 16;
    private static final short MAX_NAME = 40;
    private static final short MAX_DOB  = 10;  // "dd/mm/yyyy"
    private static final short MAX_QUE  = 40;
    private static final short MAX_ALL  = 120; // MSSV|HOTEN|DOB|QUE

    private final byte[] mssv = new byte[MAX_MSSV];
    private short mssvLen = 0;

    private final byte[] hoten = new byte[MAX_NAME];
    private short hotenLen = 0;

    private final byte[] dob = new byte[MAX_DOB];
    private short dobLen = 0;

    private final byte[] que = new byte[MAX_QUE];
    private short queLen = 0;

    private final byte[] allBuf = new byte[MAX_ALL];
    private short allLen = 0;

    private final byte[] tmp = JCSystem.makeTransientByteArray(MAX_ALL, JCSystem.CLEAR_ON_DESELECT); // bien tpm la buffer tam thoi, dung de xu ly du lieu tam thoi

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new Bai4().register(bArray, (short)(bOffset + 1), bArray[bOffset]);
    }

    public void process(APDU apdu) {
        if (selectingApplet()) return;

        byte[] buf = apdu.getBuffer();

        if (buf[ISO7816.OFFSET_CLA] != CLA_APP) {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        switch (buf[ISO7816.OFFSET_INS]) {
            case INS_SET_ALL:
                receiveAndSetAll(apdu, buf);
                return;
            case INS_GET_MSSV:
                sendField(apdu, mssv, mssvLen);
                return;
            case INS_GET_HOTEN:
                sendField(apdu, hoten, hotenLen);
                return;
            case INS_GET_DOB:
                sendField(apdu, dob, dobLen);
                return;
            case INS_GET_QUE:
                sendField(apdu, que, queLen);
                return;
            case INS_GET_ALL:
                sendField(apdu, allBuf, allLen);
                return;
            case INS_GET_LEN:
                sendLengths(apdu);
                return;
            case INS_GET_CHUNK: {
                byte which = buf[ISO7816.OFFSET_P1];
                short off  = (short) (buf[ISO7816.OFFSET_P2] & 0xFF);
                sendChunk(apdu, which, off);
                return;
            }
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    private void receiveAndSetAll(APDU apdu, byte[] buf) {
        short bytesRead;
        short dataLenTotal = 0;

        bytesRead = apdu.setIncomingAndReceive();
        while (bytesRead > 0) {
            if ((short)(dataLenTotal + bytesRead) > MAX_ALL) {
                ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            }
            Util.arrayCopyNonAtomic(buf, ISO7816.OFFSET_CDATA, tmp, dataLenTotal, bytesRead);
            dataLenTotal += bytesRead;
            bytesRead = apdu.receiveBytes(ISO7816.OFFSET_CDATA);
        }

        if (dataLenTotal <= 0) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        allLen = dataLenTotal;
        Util.arrayCopy(tmp, (short)0, allBuf, (short)0, allLen);

        short sep1 = findByte(tmp, (short)0, dataLenTotal, (byte) '|');
        short sep2 = findByte(tmp, (short)(sep1+1), dataLenTotal, (byte) '|');
        short sep3 = findByte(tmp, (short)(sep2+1), dataLenTotal, (byte) '|');

        if (sep1 < 0 || sep2 < 0 || sep3 < 0) {
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        }

        copyField(tmp, (short)0, (short)(sep1 - 0), mssv, MAX_MSSV);
        copyField(tmp, (short)(sep1+1), (short)(sep2 - (sep1+1)), hoten, MAX_NAME);
        copyField(tmp, (short)(sep2+1), (short)(sep3 - (sep2+1)), dob, MAX_DOB);
        copyField(tmp, (short)(sep3+1), (short)(dataLenTotal - (sep3+1)), que, MAX_QUE);
    }

    private void sendField(APDU apdu, byte[] field, short fieldLen) {
        byte[] buf = apdu.getBuffer();
        short le = apdu.setOutgoing();
        short outLen = fieldLen;

        if (le != 0 && le < outLen) outLen = le;

        apdu.setOutgoingLength(outLen);
        if (outLen > 0) Util.arrayCopyNonAtomic(field, (short)0, buf, (short)0, outLen);
        apdu.sendBytes((short)0, outLen);
    }

    private void sendLengths(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        short le = apdu.setOutgoing();
        short outLen = 4;
        if (le != 0 && le < outLen) outLen = le;

        apdu.setOutgoingLength(outLen);
        buf[0] = (byte) (mssvLen & 0xFF);
        buf[1] = (byte) (hotenLen & 0xFF);
        buf[2] = (byte) (dobLen & 0xFF);
        buf[3] = (byte) (queLen & 0xFF);
        apdu.sendBytes((short)0, outLen);
    }

    private void sendChunk(APDU apdu, byte which, short off) {
        byte[] buf = apdu.getBuffer();

        byte[] src;
        short srcLen;

        switch (which) {
            case FIELD_ALL:   src = allBuf; srcLen = allLen; break;
            case FIELD_MSSV:  src = mssv;   srcLen = mssvLen; break;
            case FIELD_HOTEN: src = hoten;  srcLen = hotenLen; break;
            case FIELD_DOB:   src = dob;    srcLen = dobLen; break;
            case FIELD_QUE:   src = que;    srcLen = queLen; break;
            default:
                ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2); return;
        }

        if (off > srcLen) {
            apdu.setOutgoing();
            apdu.setOutgoingLength((short)0);
            apdu.sendBytes((short)0,(short)0);
            return;
        }

        short le = apdu.setOutgoing();
        short remain = (short)(srcLen - off);
        short outLen = remain;
        if (le != 0 && le < outLen) outLen = le;

        apdu.setOutgoingLength(outLen);
        if (outLen > 0) Util.arrayCopyNonAtomic(src, off, buf, (short)0, outLen);
        apdu.sendBytes((short)0, outLen);
    }

    private short findByte(byte[] arr, short off, short len, byte b) {
        short end = (short)(off + len);
        for (short i = off; i < end; i++) {
            if (arr[i] == b) return i;
        }
        return -1;
    }

    private void copyField(byte[] src, short srcOff, short copyLen, byte[] dst, short dstMax) {
        if (copyLen < 0 || copyLen > dstMax) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        Util.arrayCopy(src, srcOff, dst, (short)0, copyLen);

        if (dst == mssv) mssvLen = copyLen;
        else if (dst == hoten) hotenLen = copyLen;
        else if (dst == dob) dobLen = copyLen;
        else if (dst == que) queLen = copyLen;
    }
}
