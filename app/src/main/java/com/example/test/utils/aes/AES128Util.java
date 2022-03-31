package com.example.test.utils.aes;


import com.example.test.utils.RadixUtil;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AES128Util {

    public static byte[] defaultKey = RadixUtil.hexToByteArray("20572F52364B3F473050415811632D2B");

    public static byte[] encrypt(byte[] src){
        return encrypt(src, defaultKey);
    }

    public static byte[] encrypt(byte[] src, byte[] key){
        try {
            //NOTE  NoPadding
            byte[] cBuff =new byte[16 *(src.length/16 +(src.length%16==0? 0: 1))];
            System.arraycopy(src, 0, cBuff, 0, src.length);
//            System.out.println("AES加密前: " +RadixUtil.bytesToHex(cBuff));

            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] tar = cipher.doFinal(cBuff);
//            System.out.println("AES加密后: " +RadixUtil.bytesToHex(tar));

            return tar;

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] decrypt(byte[] src){
        return decrypt(src, defaultKey);
    }

    public static byte[] decrypt(byte[] src, byte[] key){
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");

            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(src);
//            System.out.println("AES解密后: " +RadixUtil.bytesToHex(encrypted));
            return encrypted;

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static byte[] genKey() {
        try {
            KeyGenerator kGenerator = KeyGenerator.getInstance("AES");
            kGenerator.init(128);
            //要生成多少位，只需要修改这里即可128, 192或256
            SecretKey sKey = kGenerator.generateKey();
            byte[] key = sKey.getEncoded();
            System.out.println(RadixUtil.bytesToHex(key));

            return key;
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.out.println("没有此算法。");
        }

        return null;
    }
}
