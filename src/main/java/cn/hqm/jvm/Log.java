package cn.hqm.jvm;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * 
 * @author linxuan
 *
 */
public class Log {
    private static final Logger log = Logger.getLogger("InspectAgent");


    public static void warn(String msg) {
        //System.out.println(msg);
        log.warning(msg);
    }


    public static void warn(String msg, Throwable t) {
        //t.printStackTrace();
        log.log(Level.WARNING, msg, t);
    }

}
