package cn.hqm.jvm;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.sun.management.HotSpotDiagnosticMXBean;


/**
 * copy form cn.hqm.jvm.JVMUtils
 * 
 * JVM工具类，提供jstack jmap memory等信息的输出
 * 
 * @author xuanxiao
 * @author linxuan
 */
public class JVMUtils {

    private static final String HOTSPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";
    private static volatile HotSpotDiagnosticMXBean hotspotMBean;
    private static volatile MemoryMXBean memoryMBean;
    private static Object lock = new Object();
    public static final String pid = getPid();


    private static String getPid() {
        //获取进程的PID
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        String name = runtime.getName(); // format: "pid@hostname" 
        Log.warn(name);
        try {
            return name.substring(0, name.indexOf('@'));
        }
        catch (Exception e) {
            Log.warn("", e);
            return "pid";
        }
    }


    public static void jMap(String fileName, boolean live) throws Exception {
        try {
            initHotspotMBean();
            File f = new File(fileName);
            if (f.exists()) {
                f.delete();
            }
            hotspotMBean.dumpHeap(fileName, live);
        }
        catch (Exception e) {
            throw e;
        }
    }


    public static void jstack(OutputStream stream) throws IOException {
        Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
        Iterator<Map.Entry<Thread, StackTraceElement[]>> ite = map.entrySet().iterator();
        while (ite.hasNext()) {
            Map.Entry<Thread, StackTraceElement[]> entry = ite.next();
            StackTraceElement[] elements = entry.getValue();
            if (elements != null && elements.length > 0) {
                stream.write(buildThreadInfo(entry.getKey()).getBytes());
                for (StackTraceElement el : elements) {
                    String stack = "   " + el.toString() + "\n";
                    stream.write(stack.getBytes());
                }
                stream.write("\n".getBytes());
            }
        }
    }


    //"Druid-ConnectionPool-Create-2020353747" daemon prio=10 tid=0x00007fd804c77000 nid=0x1352b waiting on condition [0x0000000055388000]
    private static String buildThreadInfo(Thread thread) {
        StringBuilder sb = new StringBuilder();
        sb.append("\"").append(thread.getName()).append("\"");
        if (thread.isDaemon())
            sb.append(" daemon");
        sb.append(" prio=").append(thread.getPriority());
        sb.append(" tid=").append(thread.getId());
        sb.append("\n");
        return sb.toString();
    }


    public static void memoryUsed(OutputStream stream) throws Exception {
        try {
            initMemoryMBean();
            stream
                .write("**********************************Memory Used**********************************\n".getBytes());
            String heapMemoryUsed = memoryMBean.getHeapMemoryUsage().toString() + "\n";
            stream.write(("Heap Memory Used: " + heapMemoryUsed).getBytes());
            String nonHeapMemoryUsed = memoryMBean.getNonHeapMemoryUsage().toString() + "\n";
            stream.write(("NonHeap Memory Used: " + nonHeapMemoryUsed).getBytes());
        }
        catch (Exception e) {
            throw e;
        }
    }


    private static HotSpotDiagnosticMXBean getHotspotMBean() throws Exception {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<HotSpotDiagnosticMXBean>() {
                public HotSpotDiagnosticMXBean run() throws Exception {
                    MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                    Set<ObjectName> s = server.queryNames(new ObjectName(HOTSPOT_BEAN_NAME), null);
                    Iterator<ObjectName> itr = s.iterator();
                    if (itr.hasNext()) {
                        ObjectName name = itr.next();
                        HotSpotDiagnosticMXBean bean =
                                ManagementFactory.newPlatformMXBeanProxy(server, name.toString(),
                                    HotSpotDiagnosticMXBean.class);
                        return bean;
                    }
                    else {
                        return null;
                    }
                }
            });
        }
        catch (Exception exp) {
            throw exp;
        }
    }


    private static MemoryMXBean getMemoryMBean() throws Exception {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<MemoryMXBean>() {
                public MemoryMXBean run() throws Exception {
                    return ManagementFactory.getMemoryMXBean();
                }
            });
        }
        catch (Exception exp) {
            throw exp;
        }
    }


    private static void initHotspotMBean() throws Exception {
        if (hotspotMBean == null) {
            synchronized (lock) {
                if (hotspotMBean == null) {
                    hotspotMBean = getHotspotMBean();
                }
            }
        }
    }


    private static void initMemoryMBean() throws Exception {
        if (memoryMBean == null) {
            synchronized (lock) {
                if (memoryMBean == null) {
                    memoryMBean = getMemoryMBean();
                }
            }
        }
    }
}
