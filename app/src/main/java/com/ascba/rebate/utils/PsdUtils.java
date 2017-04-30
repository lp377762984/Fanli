package com.ascba.rebate.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by 李鹏 on 2017/04/28 0028.
 */

public class PsdUtils {

    /**
     * 设置密码加密
     *
     * @param psd
     * @return
     */
    public static String encryptPsd(String psd) {
        String salt = "qlqw46c229d744bc3a013332aff722d32c23";
        return encryptMD5(psd, salt);
    }

    /**
     * 1.生成第一次md5加密：明文密码+qlqw46c229d744bc3a013332aff722d32c23
     * <p>
     * 2.md5值乱序：在第一次生成的md5值进行均等分成两个串进行颠倒，生成一个新的串
     * <p>
     * 3.二次md5加密：乱序后的串再次进行md5加密
     *
     * @param data——明文
     * @param salt——盐
     */
    private static String encryptMD5(String data, String salt) {
        //1、第一次加密
        String firstEncry = getMD5Str(data+salt);

        //2、乱序
        String arg0 = firstEncry.substring(0, firstEncry.length() / 2);
        String arg1 = firstEncry.substring(firstEncry.length() / 2, firstEncry.length());
        firstEncry = arg1 + arg0;

        //3、二次md5加密
        firstEncry = getMD5Str(firstEncry);

        return firstEncry.toLowerCase();
    }

    /**
     * 支付密码验证
     *
     * @param psd
     * @return
     */
    public static String getPayPsd(String psd) {
        String encryptPsd = encryptPsd(psd);
        long time = System.currentTimeMillis();
        time = time / 60000;
        encryptPsd = EncryptUtils.encryptMD5ToString(encryptPsd + time);
        return encryptPsd.toLowerCase();
    }

    public static String getMD5Str(String str) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException caught!");
            System.exit(-1);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] byteArray = messageDigest.digest();
        StringBuffer md5StrBuff = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            else
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
        }
        return md5StrBuff.toString();
    }
}