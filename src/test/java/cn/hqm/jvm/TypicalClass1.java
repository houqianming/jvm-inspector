package cn.hqm.jvm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


public class TypicalClass1 {
    private static final int type = 1;
    public static String KN = "keyname1";

    private byte byte0;
    private short short0;
    private char char0;
    private int int0;
    private static long long0;
    private long id;

    private static class MyException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
    public static @interface Fortest {
        boolean required() default true;
    }


    public TypicalClass1(long id) {
        this.id = id;
    }


    private long getId() {
        return this.id;
    }


    @Fortest(required = false)
    public String getName() {
        return KN;
    }


    public static int foo(@Fortest(required = false) int i) throws MyException {
        if (i > 100) {
            throw new MyException();
        }
        return i++;
    }


    public synchronized long increId() {
        return id++;
    }


    public Object useswitch(short i) {
        try {
            switch (i) {
            case 1:
                System.out.println("case 1");
                break;
            case 2:
                return id + 1;
            case 3:
                return long0 / i;
            default:
                return "default";
            }
            return null;
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }


    public Object uselookupwitch(byte i) {
        switch (i) {
        case 1:
            long a = 1L + 5;
            System.out.println(a);
            //return 1 + 5;
            //return 5;
            break;
        case 3:
            return id + 1;
        case 10:
            return long0 / i;
        default:
            return "default";
        }
        return null;
    }


    public static void main(String[] args) {
        System.out.println(new TypicalClass1(0).getName());
    }
}

class NotAMember{
    
}
