package Bai4;

import javacard.framework.*;

import javacard.framework.Shareable;

public interface HpInterface extends Shareable {
    public byte xemHP(byte svID);
}