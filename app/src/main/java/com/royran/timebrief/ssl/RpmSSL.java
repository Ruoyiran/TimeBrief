package com.royran.timebrief.ssl;

public class RpmSSL {
    public static native byte[] encryptBytes(byte[] data);

    public static native byte[] decryptBytes(byte[] data);
}
