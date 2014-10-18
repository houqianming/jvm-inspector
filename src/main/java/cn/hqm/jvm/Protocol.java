package cn.hqm.jvm;

/**
 * 
 * @author linxuan
 *
 */
public class Protocol {
    public static final String OutputEndFlag = "end";


    public static boolean isQuit(String line) {
        return "quit".equalsIgnoreCase(line) || "exit".equalsIgnoreCase(line) || "bye".equalsIgnoreCase(line);
    }
}
