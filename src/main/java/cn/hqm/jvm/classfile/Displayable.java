package cn.hqm.jvm.classfile;

public interface Displayable {
    void display(Displayer displayer, byte[] b, int[] offset, ConstantInfo[] pool);
}
