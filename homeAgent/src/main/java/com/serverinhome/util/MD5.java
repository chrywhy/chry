package com.serverinhome.util;

import java.security.MessageDigest;


public class MD5 {
    static private char[] _CHARS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f'
    };

    static public String digest(String content) {
        try {
            MessageDigest encoder = MessageDigest.getInstance("MD5");
            byte[] result = encoder.digest(content.getBytes());

            StringBuffer buf = new StringBuffer();
            for (byte b : result) {
                int a = (b >> 4) & 0x0f;
                buf.append(_CHARS[a]);
                a = b & 0x0f;
                buf.append(_CHARS[a]);
            }

            return buf.toString();
        }
        catch (Exception e) {
            // not a big deal
        }

        return null;
    }

    // 
    public static void main(String[] args) {
        if(args.length == 0) {
            System.out.println("No arguments given.");
            System.exit(-1);
        }

        for(String arg : args) {
            System.out.println(String.format("MD5 of %s => %s", arg, digest(arg)));
        }
    }
}
