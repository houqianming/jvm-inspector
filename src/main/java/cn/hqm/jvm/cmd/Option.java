package cn.hqm.jvm.cmd;

/**
 * 
 * @author linxuan
 *
 */
public class Option {
    public final Character key;
    public final String msg;
    public final boolean hasvalue;
    public final String name;


    public Option(Character key, boolean hasvalue, String msg, String name) {
        this.key = key;
        this.hasvalue = hasvalue;
        this.msg = msg;
        this.name = name;
    }
}
