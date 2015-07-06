package com.funcity.taxi;

/**
 * Created by anonymous on 15-5-16.
 * For invoke the kuaidi TaxiCore library
 */
public class JNILib {
    static{
        System.loadLibrary("TaxiCore");

    }
    public JNILib() {
        super();
    }

    public static native String getSign(String arg1);
}
