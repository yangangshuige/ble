package com.example.test.utils.jt808;



public class JT808Util {

    public static byte[] decrypt(byte[] buff) throws Exception{

        byte[] msg = new byte[1024];

        if(0x7E !=buff[0] || 0x7E !=buff[buff.length-1]){
            throw new Exception("JT808 parse err: head or tail is not 0x7E");
        }

        int j=0;
        for(int i=1; i<buff.length-1; i++, j++){
            if(j>=1024-1){
                throw new Exception("JT808 parse err: msg too long");
            }

            if(0x7E ==buff[i]){
                throw new Exception("JT808 parse err: msg inner has 0x7E");
            }else if(0x7D ==buff[i]){
                if(0x01 ==buff[i+1]) {
                    msg[j] = 0x7D;
                }else if(0x02 ==buff[i+1]) {
                    msg[j] = 0x7E;
                }else{
                    throw new Exception("JT808 parse err: msg inner has 0x7D " +buff[i+1]);
                }
                i++;
            }else{
                msg[j] =buff[i];
            }
        }

        byte[] ret = new byte[j];
        System.arraycopy(msg, 0, ret, 0, ret.length);
        return ret;
    }

    /*
    * len: 消息体buff加密前的长度
    * */
    public static byte[] encrypt(byte[] buff, Integer len)  throws Exception{
        byte[] msg = new byte[1024];

        byte[] tBuff =new byte[buff.length +2];
        //NOTE  1 添加消息体长度 2-byte
        tBuff[0] = (byte) (len/256);
        tBuff[1] =(byte) (len%256);
        System.arraycopy(buff, 0, tBuff, 2, buff.length);

        //NOTE  2. 添加 Xor校验码 1-byte
        byte[] cBuff =new byte[buff.length +3];
        System.arraycopy(tBuff, 0, cBuff, 0, tBuff.length);
        cBuff[cBuff.length-1] =crc(tBuff);

//        System.out.println("JT808转义前: " +RadixUtil.bytesToHex(cBuff));

        msg[0] =0x7E;
        int j =1;
        for(int i=0; i<cBuff.length; i++, j++){
            if(j>=1024-2){
                throw new Exception("JT808 encrypt err: msg too long");
            }
            if(0x7E ==cBuff[i]){
                msg[j] =0x7D;
                msg[j+1] =0x02;
                j++;
            }else if(0x7D ==cBuff[i]){
                msg[j] =0x7D;
                msg[j+1] =0x01;
                j++;
            }else{
                msg[j] =cBuff[i];
            }
        }

        msg[j] =0x7E;
        byte[] ret = new byte[j+1];
        System.arraycopy(msg, 0, ret, 0, ret.length);

//        System.out.println("JT808转义后: " +RadixUtil.bytesToHex(ret));

        return ret;
    }

    public static byte crc(byte[] buff){
        byte crc =buff[0];
        for(int i=1; i<buff.length; i++){
            crc ^=buff[i];
        }
        return crc;
    }
}
