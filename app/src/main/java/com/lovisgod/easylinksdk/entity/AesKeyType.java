package com.lovisgod.easylinksdk.entity;

public enum AesKeyType {
    AES_128((byte)0x02),
    AES_192((byte) 0x03),
    AES_256((byte) 0x04);

    private byte pedKeyType;

    private AesKeyType(byte pedKeyType) {
        this.pedKeyType = pedKeyType;
    }

    public byte getPedkeyType() {
        return this.pedKeyType;
    }
}
