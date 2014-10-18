package cn.hqm.jvm;

public class MethodCallTest {
    public Object call1() {
        System.getProperty("user.dir");
        //方法有返回值,但是没有赋值给其他变量,字节码会有个POP指令从oprand stack上丢弃返回值
        return "/";
    }


    public Object call2() {
        String dir = System.getProperty("user.dir");
        return dir;
    }


    public Object call3() {
        return System.getProperty("user.dir");
    }


    public Object call4() {
        System.out.println(); //方法没有返回值
        return "/";
    }


    public static void main(String[] args) {
        System.out.println(Integer.MAX_VALUE);
        System.out.println(0x7fffffff);
        System.out.println(0xffffffff);
        System.out.println(0xfffffffe);
        System.out.println(1 << 31);
        System.out.println(Integer.MIN_VALUE);
        System.out.println((1 << 31) | 1);
        System.out.println((1 << 31) + 1);
        System.out.println((1 << 31) - 1);
        System.out.println((Integer.MIN_VALUE - 1) == Integer.MAX_VALUE);
        System.out.println((Integer.MAX_VALUE + 1) == Integer.MIN_VALUE);

    }
}
