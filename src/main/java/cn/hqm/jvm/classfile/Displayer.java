package cn.hqm.jvm.classfile;

import java.util.Arrays;


/**
 * Class文件结构显示
 * 
 * @author linxuan
 * @date 2014-3-21 上午8:00:38
 */
public class Displayer {
    private StringBuilder sb = new StringBuilder(
        "<style>.s1:hover{border-style:solid}.s2:hover{background-color:#11f0dd}"
                + ".s3:hover{background-color:aaaaaa}.s4:hover{background-color:C7EDCC}"
                + ".s5:hover{background-color:cc00cc}"
                + "#cp{position:fixed; top:10;left:400; width:100%; background:#11f0dd;}" // 
                + "</style><div>");
    private int count = 1;


    public void writeAndStepOffset(byte[] b, int[] offset, int len, String msg) {
        int index = offset[0];
        write(b, index, len, msg);
        offset[0] = index + len;
    }


    public void write(byte[] b, int offset, int len, String msg) {
        sb.append("<span class=s1 title='").append(msg).append("'>");
        for (int i = offset, n = offset + len; i < n; i++) {
            sb.append(upperBytesInHex[0xff & b[i]]);
            if (count++ % 16 == 0) {
                sb.append("<br>");
            }
            else {
                sb.append(" ");
            }
        }
        if (sb.charAt(sb.length() - 1) == ' ') {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("</span> ");
    }


    /**
     * @param style: s1 s2 ...
     */
    public void spanBegin(int level) {
        sb.append("<span class=s" + level + ">");
    }


    public void spanEnd() {
        if (sb.charAt(sb.length() - 1) == ' ') {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("</span> ");
    }


    public void divBegin() {
        sb.append("<div>");
    }


    public void divBegin(String id) {
        sb.append("<div id=" + id + ">");
    }


    public void divEnd() {
        sb.append("</div> ");
    }


    public void write(String content) {
        sb.append(content);
    }


    public String getHtml() {
        return sb.append("<div>").toString();
    }

    private static final char[] upperHex = // 
            new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    private static final String[] upperBytesInHex = new String[256];
    static {
        for (int i = 0; i < 256; i++) {
            int b1 = (i >> 4) & 0xf;
            int b2 = i & 0xf;
            upperBytesInHex[i] = "" + upperHex[b1] + upperHex[b2];
        }
    }


    public static void main(String[] args) {
        System.out.println(Arrays.toString(upperBytesInHex));
    }
}
