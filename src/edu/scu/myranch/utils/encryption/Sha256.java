package edu.scu.myranch.utils.encryption;


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/*
    I try to use C++ Code for SHA256 so that I don't need to rewrite it in java.
    However, the JNA framework doesn't work, somehow, this is really strange...
    Seems that there is something wrong with the translation between C++ std::string and Java String.
    I decide to rewrite the SHA256 algorithm in Java as I don't want to debug the JNA again.
*/

public class Sha256 {
    public static String getSHA256(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest messageDigest;
        String encodestr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes("UTF-8"));
            encodestr = byte2Hex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodestr;
    }

    private static String byte2Hex(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;
        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length() == 1) {
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }
}
