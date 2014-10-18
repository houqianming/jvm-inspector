package cn.hqm.jvm.transformer;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.hqm.jvm.ClassLoadingInfo;
import cn.hqm.jvm.Flusher;
import cn.hqm.jvm.HtmlFlusher;
import cn.hqm.jvm.Log;
import cn.hqm.jvm.Logger;
import cn.hqm.jvm.Tree;


/**
 * 
 * @author linxuan
 *
 */
public class ClassLoadingInspector implements ClassFileTransformer {
    private final Map<ClassLoader, List<ClassLoadingInfo>> classLoaders =
            new ConcurrentHashMap<ClassLoader, List<ClassLoadingInfo>>();
    private final Map<URL, List<ClassLoadingInfo>> locations = new ConcurrentHashMap<URL, List<ClassLoadingInfo>>();
    private final List<ClassLoadingInfo> allClasses = new ArrayList<ClassLoadingInfo>(10240);
    private final Class<?>[] preLoadClasses;
    private final Logger logger;

    private long flushInterval = 3 * 60 * 1000L;
    private boolean enableHyperlink;
    private int classCount = 0;
    private volatile Tree<ClassLoadingInfo, String> pkgTree;
    //private boolean 

    private final Object lock = new Object();
    private final File outputDir;


    public ClassLoadingInspector(File outputDir, Logger logger, Class<?>[] loadedClasses) {
        this.logger = logger;
        this.preLoadClasses = loadedClasses;
        this.outputDir = outputDir;

        for (Class<?> clazz : loadedClasses) {
            addClass(new ClassLoadingInfo(outputDir, clazz.getName(), clazz.getClassLoader(),
                clazz.getProtectionDomain(), -1));
        }
    }


    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        String classNameDot = className.replaceAll("/", ".");
        ClassLoadingInfo info = new ClassLoadingInfo(outputDir, classNameDot, loader, protectionDomain, classCount++);

        addClass(info);

        logger.info(new StringBuilder(loader.getClass().getName()).append("@")
            .append(Integer.toHexString(loader.hashCode())).append(" loaded class: ").append(classNameDot)
            .append(" from: ").append(info.getLocation()).toString());

        return null;
    }


    private ClassLoadingInfo addClass(ClassLoadingInfo info) {
        //ClassLoadingInfo info = new ClassLoadingInfo(classNameDot, loader, protectionDomain, classCount);

        synchronized (lock) {
            allClasses.add(info);
        }

        List<ClassLoadingInfo> loaderView = classLoaders.get(info.getLoader());
        if (loaderView == null) {
            loaderView = new ArrayList<ClassLoadingInfo>();
            classLoaders.put(info.getLoader(), loaderView);
        }
        synchronized (lock) {
            loaderView.add(info);
        }

        URL dir = getRootOnDirLocation(info);
        URL key = dir == null ? info.getLocation() : dir;
        List<ClassLoadingInfo> locationView = locations.get(key);
        if (locationView == null) {
            locationView = new ArrayList<ClassLoadingInfo>();
            locations.put(key, locationView);
        }
        synchronized (lock) {
            locationView.add(info);
        }
        return info;
    }


    //WEB-INF/classes/com.x.A.class return WEB-INF/classes
    private URL getRootOnDirLocation(ClassLoadingInfo info) {
        if (info.isNullLocation()) {
            return null;
        }
        //URL location = info.getLocation();
        String location = info.getLocation().getFile();
        if (location.endsWith(".class")) {
            //String classpath = info.getName().replaceAll("\\.", "/") + ".class";
            //if (location.endsWith(classpath)) {
            try {
                //return new URL(location.substring(0, location.length() - info.getName().length() - 6));
                return new File(location.substring(0, location.length() - info.getName().length() - 6)).toURI().toURL();
            }
            catch (MalformedURLException e) {
                logger.info(e.getMessage());
            }
            //}
        }
        return null;
    }

    private Thread flushThread;
    private volatile boolean goon = true;


    public void startFlushThread() {
        if (flushThread == null) {
            initFlushThread();
            flushThread.start();
        }
        else {
            logger.info("flushThread is not null. state:" + flushThread.getState());
        }
    }


    public void stopFlushThread() {
        logger.info("stopFlushThread");
        Log.warn("stopFlushThread");
        goon = false;
        if (flushThread != null) {
            flushThread.interrupt();
            flushThread = null;
        }
    }


    public void initFlushThread() {
        final Flusher flusher = new HtmlFlusher(enableHyperlink);
        flushThread = new Thread(new Runnable() {
            public void run() {
                while (goon) {
                    try {
                        synchronized (lock) {
                            //flushInfo();
                            System.out.println("[ClassLoadingInspector.flushInfo] called. total classloaders:"
                                    + classLoaders.size() + ", total locations:" + locations.size() + ", total class:"
                                    + classCount);

                            pkgTree =
                                    flusher.flush(new File("classloaders.html"), classLoaders, locations, allClasses,
                                        preLoadClasses);
                        }
                    }
                    catch (Exception e) {
                        //e.printStackTrace();
                        Log.warn("", e);
                    }

                    try {
                        Thread.sleep(flushInterval);
                    }
                    catch (InterruptedException e1) {
                    }
                }
            }
        }, "ClassLoadingInspector-FlushThread");
    }


    public void setFlushInterval(long flushInterval) {
        if (flushInterval > 0) {
            this.flushInterval = flushInterval;
        }
    }


    public void setEnableHyperlink(boolean enableHyperlink) {
        this.enableHyperlink = enableHyperlink;
    }


    public Tree<ClassLoadingInfo, String> getPkgTree() {
        return pkgTree;
    }


    public Map<ClassLoader, List<ClassLoadingInfo>> getClassLoaders() {
        return classLoaders;
    }

}
