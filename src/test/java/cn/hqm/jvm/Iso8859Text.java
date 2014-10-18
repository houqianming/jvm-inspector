package cn.hqm.jvm;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;


public class Iso8859Text {
    public static void main(String[] args) throws UnsupportedEncodingException {
        byte[] b = new byte[256];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) i;
        }

        System.out.println(Arrays.toString(b));
        char[] c = new char[256];
        for (int i = 0; i < c.length; i++) {
            c[i] = (char) (b[i] & 0xff);
        }
        String char256 = new String(c);
        System.out.println(char256);
        System.out.println(Arrays.toString(char256.getBytes("iso-8859-1")));
        System.out.println(Arrays.toString(char256.getBytes("ascii")));
        byte[] ascii = char256.getBytes("ascii");
        System.out.println(new String(ascii));
    }
}
