package com.serverinhome.util;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class DES {
    private static final String KEY = "wj1@L0gc8jyFr";

    private static SecretKey getKey(String strKey) {
        SecretKey deskey = null;
        try {
            DESKeySpec desKeySpec = new DESKeySpec(strKey.getBytes());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            deskey = keyFactory.generateSecret(desKeySpec);
        }
        catch (Exception e) {

        }
        return deskey;
    }

    public static String encrypt(String content) {
        byte[] byteContent;
        String result = "";
        BASE64Encoder base64en = new BASE64Encoder();
        try {
            byteContent = content.getBytes();
            Cipher cipher;
            byte[] byteFina;
            cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, getKey(KEY));
            byteFina = cipher.doFinal(byteContent);
            result = base64en.encode(byteFina);
        }
        catch (Exception e) {
        }
        return result;
    }

    public static String decrypt(String content) {
        BASE64Decoder base64De = new BASE64Decoder();
        byte[] byteContent;
        String result = "";
        try {
            byteContent = base64De.decodeBuffer(content);
            byte[] byteFina;
            Cipher cipher;
            cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, getKey(KEY));
            byteFina = cipher.doFinal(byteContent);
            result = new String(byteFina);
        }
        catch (Exception e) {
        }
        return result;
    }

/*
    static public void main(String[] args) throws Exception {
        System.out.println(encrypt(""));
        System.out.println(decrypt("/0yFKiaQYfI="));
    }
*/

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: DES <enc|dec> <string>");
            System.exit(-1);
        }

        if (args[0].equalsIgnoreCase("enc")) {
            System.out.println(String.format(
                    "Encoding %s: %s", args[1], encrypt(args[1])
            ));
        }
        else if (args[0].equalsIgnoreCase("dec")) {
            System.out.println(String.format(
                    "Decoding %s: %s", args[1], decrypt(args[1])
            ));
        }
        else {
            System.out.println("Unknown command - " + args[0]);
        }
    }
}
