package com.example.test.utils;


import com.example.test.utils.aes.AES128Util;
import com.example.test.utils.jt808.JT808Util;

public class CodecUtil {

    /*
     * 数据加密
     * */
    public static byte[] encrypt(byte[] src) throws Exception{

        return encrypt(src, AES128Util.defaultKey);
    }

    /*
     * 数据解密
     * */
    public static byte[] decrypt(byte[] src)throws Exception{

        return decrypt(src, AES128Util.defaultKey);
    }

    /*
    * 数据加密
    * */
    public static byte[] encrypt(byte[] src, byte[] key) throws Exception{

        //NOTE  AES加密
        byte[] aesData = AES128Util.encrypt(src, key);

        //NOTE  JT808转义
        byte[] buff = JT808Util.encrypt(aesData, src.length);

        return buff;
    }

    /*
    * 数据解密
    * */
    public static byte[] decrypt(byte[] src, byte[] key)throws Exception{
        //NOTE  JT808转义
        byte[] buff =JT808Util.decrypt(src);
//        System.out.println("JT808解密后: " +RadixUtil.bytesToHex(buff));

        Integer len =buff[0] +buff[1];
        byte crc =buff[buff.length -1];

        byte[] aesData =new byte[buff.length -3];
        System.arraycopy(buff, 2, aesData, 0, buff.length-3);
//        System.out.println("AES解密前: " +RadixUtil.bytesToHex(buff));

        //NOTE  AES加密
        aesData =AES128Util.decrypt(aesData, key);
//        System.out.println("AES解密后: " +RadixUtil.bytesToHex(aesData));

        byte[] ret =new byte[len];
        System.arraycopy(aesData, 0, ret, 0, len);
//        System.out.println("AES解密后: " +RadixUtil.bytesToHex(ret));

        return ret;
    }
}
