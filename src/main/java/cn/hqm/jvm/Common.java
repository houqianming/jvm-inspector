package cn.hqm.jvm;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * 公共的常量和规则
 * 
 * @author linxuan
 */
public class Common {
    public static final String DONE = "Done.";

    public static interface FileName {
        String getFileName(int n);
    }


    public static File getFile(File dir, FileName naming) {
        int i = 1;
        File file = new File(dir, naming.getFileName(i++));
        while (file.exists()) {
            file = new File(dir, naming.getFileName(i++));
        }
        return file;
    }


    /**
     * 跳过引号内容的split
     * @return
     */
    public static String[] splitSkipQuotes(String str, char sep, char quoteChar) {
        List<String> list = new ArrayList<String>();
        //int quote1 = str.indexOf("\"");
        char[] chars = str.toCharArray();
        int lastIndex = 0;
        boolean inQuotes = false;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == quoteChar) {
                inQuotes = !inQuotes;
                continue;
            }
            if (c == sep && !inQuotes) {
                list.add(new String(chars, lastIndex, i - lastIndex));
                lastIndex = i + 1;
                continue;
            }
        }
        if (lastIndex < chars.length) {
            list.add(new String(chars, lastIndex, chars.length - lastIndex));
        }
        return list.toArray(new String[list.size()]);
    }


    /**
     * 跳过类似括号之类的内容的split
     * @return
     */
    public static String[] splitSkipPairs(String str, char sep, char pairs1, char pairs2) {
        if (pairs1 == pairs2) {
            throw new IllegalArgumentException("pairs1 equals pairs2. use splitSkipQuotes instead");
        }
        List<String> list = new ArrayList<String>();
        //int quote1 = str.indexOf("\"");
        char[] chars = str.toCharArray();
        int lastIndex = 0;
        int pairdepth = 0;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == pairs1) {
                pairdepth++;
                continue;
            }
            if (c == pairs2) {
                pairdepth--;
                continue;
            }
            if (c == sep && pairdepth <= 0) {
                list.add(new String(chars, lastIndex, i - lastIndex));
                lastIndex = i + 1;
                continue;
            }
        }
        if (lastIndex < chars.length) {
            list.add(new String(chars, lastIndex, chars.length - lastIndex));
        }
        return list.toArray(new String[list.size()]);
    }


    public static boolean isPrimitiveExpression(String valueExpr) {
        if (valueExpr.startsWith("\"") && valueExpr.endsWith("\"") //
                || valueExpr.startsWith("'") && valueExpr.endsWith("'")) {
            return true;
        }
        char first = valueExpr.charAt(0);
        if (first >= '0' && first <= '9' || first == '-') {
            char last = valueExpr.charAt(valueExpr.length() - 1);
            if (last >= '0' && last <= '9' || last == 'L' || last == 'l' || last == 'f' || last == 'F') {
                for (int i = 1, n = valueExpr.length() - 1; i < n; i++) {
                    char m = valueExpr.charAt(i);
                    if ((m < '0' || m > '9') && m != '.') {
                        return false;
                    }
                }
                return true;
            }
        }
        if (valueExpr.equalsIgnoreCase("true") || valueExpr.equalsIgnoreCase("false")) {
            return true;
        }
        return false;
    }


    public static Object getPrimitiveValue(Class<?> type, String valueExpr) {
        if (type == String.class) {
            if (valueExpr.startsWith("\"") && valueExpr.endsWith("\"") //
                    || valueExpr.startsWith("'") && valueExpr.endsWith("'")) {
                return valueExpr.substring(1, valueExpr.length() - 1);
            }
        }
        else if (type == Long.class) {
            return Long.valueOf(valueExpr);
        }
        else if (type == Integer.class) {
            return Integer.valueOf(valueExpr);
        }
        else if (type == Short.class) {
            return Short.valueOf(valueExpr);
        }
        else if (type == Boolean.class) {
            return Boolean.valueOf(valueExpr);
        }
        else if (type == Character.class || type == char.class) {
            if (valueExpr.startsWith("'") && valueExpr.endsWith("'")) {
                return Character.valueOf(valueExpr.charAt(1));
            }
        }
        else if (type == Float.class) {
            return Float.valueOf(valueExpr);
        }
        else if (type == float.class) {
            return Float.parseFloat(valueExpr);
        }
        else if (type == int.class) {
            return Integer.parseInt(valueExpr);
        }
        else if (type == long.class) {
            return Long.parseLong(valueExpr);
        }
        else if (type == short.class) {
            return Short.parseShort(valueExpr);
        }
        else if (type == boolean.class) {
            return Boolean.parseBoolean(valueExpr);
        }
        else if (type == Object.class) {
            if (valueExpr.startsWith("\"") && valueExpr.endsWith("\"")) {
                return valueExpr.substring(1, valueExpr.length() - 1);
            }
            else if (valueExpr.startsWith("'") && valueExpr.endsWith("'")) {
                String value = valueExpr.substring(1, valueExpr.length() - 1);
                if (value.length() == 1) {
                    return Character.valueOf(value.charAt(0));
                }
                else {
                    return value;
                }
            }
            char c = valueExpr.charAt(0);
            if (c >= 0 && c <= 9 || c == '-') {
                char last = valueExpr.charAt(valueExpr.length() - 1);
                if (last == 'l' || last == 'L') {
                    return Long.valueOf(valueExpr.substring(0, valueExpr.length() - 1));
                }
                else if (last == 'f' || last == 'F') {
                    return Float.valueOf(valueExpr.substring(0, valueExpr.length() - 1));
                }
                else {
                    return Integer.valueOf(valueExpr);
                }
            }
            else {
                return Boolean.valueOf(valueExpr);
            }
        }
        throw new IllegalArgumentException(valueExpr + " not match the type:" + type);
    }


    public static void main(String[] args) {
        System.out.println(Arrays.toString("a.b.c".split("\\.")));
        System.out.println(Arrays.toString("a.get(\"cn.hqm\").name".split("\\.")));
        System.out.println(Arrays.toString(".a.get(\"cn.hqm\").name".split("\\.")));
        System.out.println(Arrays.toString("a.get(\"cn.hqm\").name.".split("\\.")));
        System.out.println(Arrays.toString(".a.get(\"cn.hqm\").name.".split("\\.")));
        System.out.println();
        System.out.println(Arrays.toString(splitSkipQuotes("a.b.c", '.', '"')));
        System.out.println(Arrays.toString(splitSkipQuotes("a.get(\"cn.hqm\").name", '.', '"')));
        System.out.println(Arrays.toString(splitSkipQuotes(".a.get(\"cn.hqm\").name", '.', '"')));
        System.out.println(Arrays.toString(splitSkipQuotes("a.get(\"cn.hqm\").name.", '.', '"')));
        System.out.println(Arrays.toString(splitSkipQuotes(".a.get(\"cn.hqm\").name.", '.', '"')));
        System.out.println();
        System.out
            .println(Arrays
                .toString(splitSkipPairs(
                    "org.apache.commons.logging.LogFactory.getLog(\"cn.hqm.jvm\").getLogger().setLevel(org.apache.log4j.Level.toLevel(\"INFO\"))",
                    '.', '(', ')')));
        System.out.println();
        System.out.println("getLog(cn.hqm.jvm)".lastIndexOf(")"));
        System.out.println("getLog(cn.hqm.jvm)".lastIndexOf(")", 0));
        System.out.println("getLog(cn.hqm.jvm)a".lastIndexOf(")", 0));

        System.out.println("ab(c(adf)d)ef".lastIndexOf(")"));
        System.out.println("ab(c(adf)d)ef".lastIndexOf(")", 0));
        System.out.println("ab(c(adf)d)ef".lastIndexOf(")", 0));
    }
}
