package cn.hqm.jvm;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.hqm.jvm.Common.FileName;
import cn.hqm.jvm.transformer.ClassDumpTransformer;


/**
 * 
 * @author linxuan
 *
 */
public class ClassLoadingSpy {
    private static Logger logger;

    private Class<?>[] allLoadedClasses;
    private Set<ClassLoader> allClassLoaders;
    private Tree<ClassLoadingInfo, String> pkgTree;
    private boolean enableHyperlink;
    private final File outputDir;


    //private boolean 

    public ClassLoadingSpy(File outputDir, Logger alogger, Class<?>[] loadedClasses) {
        this.outputDir = outputDir;
        logger = alogger;
        this.allLoadedClasses = loadedClasses;
        //flushClasses(loadedClasses);
        this.allClassLoaders = new HashSet<ClassLoader>();
        List<ClassLoadingInfo> allClasses = new ArrayList<ClassLoadingInfo>(10240);
        for (Class<?> clazz : loadedClasses) {
            allClassLoaders.add(clazz.getClassLoader());
            allClasses.add(new ClassLoadingInfo(outputDir, clazz, -1));
        }
        this.pkgTree = HtmlFlusher.getPackageTree(allClasses);
    }


    private void addClass(ClassLoadingInfo info, Map<ClassLoader, List<ClassLoadingInfo>> classLoaders,
            Map<URL, List<ClassLoadingInfo>> locations, List<ClassLoadingInfo> allClasses) {
        allClasses.add(info);

        List<ClassLoadingInfo> loaderView = classLoaders.get(info.getLoader());
        if (loaderView == null) {
            loaderView = new ArrayList<ClassLoadingInfo>();
            classLoaders.put(info.getLoader(), loaderView);
        }
        loaderView.add(info);

        URL dir = getRootOnDirLocation(info);
        URL key = dir == null ? info.getLocation() : dir;
        List<ClassLoadingInfo> locationView = locations.get(key);
        if (locationView == null) {
            locationView = new ArrayList<ClassLoadingInfo>();
            locations.put(key, locationView);
        }
        locationView.add(info);
    }


    public String showClass(String className) {
        String[] pathes = className.split("\\.");
        Tree<ClassLoadingInfo, String> classTree = pkgTree.locate(pathes, true);
        if (classTree == null) {
            return className + " not found";
        }
        ClassLoadingInfo cli = classTree.getEntity();
        //一个类被多个loader加载的情况，要全部展示出来
        StringBuilder sb = new StringBuilder();
        for (ClassLoadingInfo info : classTree.getParent().getLeaves()) {
            if (info.getName().equals(cli.getName())) {
                sb.append("\nloader  :").append(info.getLoader()).append("\nlocation:").append(info.getLocation())
                    .append("\n");
            }
        }
        return sb.toString();
    }


    public File flushClasses() throws IOException {
        return flushClasses(this.allLoadedClasses);
    }


    public File flushClasses(Class<?>[] loadedClasses) throws IOException {
        Map<ClassLoader, List<ClassLoadingInfo>> classLoaders = new HashMap<ClassLoader, List<ClassLoadingInfo>>();
        Map<URL, List<ClassLoadingInfo>> locations = new HashMap<URL, List<ClassLoadingInfo>>();
        List<ClassLoadingInfo> allClasses = new ArrayList<ClassLoadingInfo>(10240);

        for (Class<?> clazz : loadedClasses) {
            addClass(new ClassLoadingInfo(outputDir, clazz, -1), classLoaders, locations, allClasses);
        }
        this.allClassLoaders = classLoaders.keySet();
        System.out.println("[ClassLoadingSpy.flushClasses] called. total classloaders:" + classLoaders.size()
                + ", total locations:" + locations.size() + ", total class:" + loadedClasses.length);
        Flusher flusher = new HtmlFlusher(enableHyperlink);
        File file = Common.getFile(outputDir, filename);
        this.pkgTree = flusher.flush(file, classLoaders, locations, allClasses, null);
        return file;
    }

    private static Method m_findLoadedClass;
    static {
        try {
            m_findLoadedClass = ClassLoader.class.getDeclaredMethod("findLoadedClass", new Class<?>[] { String.class });
            m_findLoadedClass.setAccessible(true);
        }
        catch (Exception e) {
            Log.warn("", e);
        }
    }


    public Class<?> getClazzByExepression(String expr) throws ClassNotFoundException {
        String[] pathes = expr.split("\\.");
        Tree<ClassLoadingInfo, String> classTree = pkgTree.locate(pathes, false);
        if (classTree == null) {
            return null;
        }
        String clazz = classTree.getId();
        Tree<ClassLoadingInfo, String> parent = classTree;
        while ((parent = parent.getParent()) != null) {
            if (parent != pkgTree) {
                clazz = parent.getId() + "." + clazz;
            }
        }
        //Log.warn("getClazz returns:" + clazz);
        Class<?> res = getLoadedClass(clazz);
        if (res == null) {
            throw new ClassNotFoundException(clazz);
        }
        return res;
    }


    /**
     * 返回第一个加载了这个类的loader所定义的类； 
     * 若有其他loader加载了同一个类，只返回第一个遍历到的loader所加载的Class对象
     */
    public Class<?> getLoadedClass(String clazz) {
        for (ClassLoader loader : allClassLoaders) {
            if (loader == null) {
                continue;
            }
            String loaderName = loader.getClass().getName();
            if ("sun.reflect.DelegatingClassLoader".equals(loaderName)
                    || "groovy.lang.GroovyClassLoader$InnerLoader".equals(loaderName)) {
                continue;
            }
            try {
                //return loader.loadClass(clazz);
                //注意直接反射调用绕过了ClassLoader的内部类名锁：parallelLockMap
                Class<?> res = (Class<?>) m_findLoadedClass.invoke(loader, new Object[] { clazz });
                if (res != null) {
                    return res;
                }
            }
            catch (Exception e) {
                Log.warn("", e);
            }
        }
        return null;
    }


    //WEB-INF/classes/com.x.A.class return WEB-INF/classes
    private static URL getRootOnDirLocation(ClassLoadingInfo info) {
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

    private static final FileName filename = new FileName() {
        @Override
        public String getFileName(int n) {
            return JVMUtils.pid + ".classloaders." + n + ".html";
        }
    };


    public void setEnableHyperlink(boolean enableHyperlink) {
        this.enableHyperlink = enableHyperlink;
    }


    public Tree<ClassLoadingInfo, String> getPkgTree() {
        return pkgTree;
    }


    public String dump(File outputDir, Instrumentation instrumentation, String[] tokens) throws IOException,
            ClassNotFoundException {
        if (tokens.length < 2) {
            File file = flushClasses();
            return Common.DONE + " class & loader infos dumped to " + file.getAbsolutePath();
        }
        else if (tokens.length > 2 && tokens[1].equals("-b")) {
            Class<?> clazz = getClazzByExepression(tokens[2]);
            if (clazz == null) {
                return "Class not found. expr:" + tokens[2];
            }
            return dumpByteCode(outputDir, instrumentation, clazz);
        }
        else {
            return showClass(tokens[1]);
        }
    }


    private String dumpByteCode(File outputDir, Instrumentation instrumentation, Class<?> clazz) {
        ClassDumpTransformer ctf = new ClassDumpTransformer(outputDir);
        try {
            try {
                instrumentation.addTransformer(ctf, true);
                instrumentation.retransformClasses(new Class[] { clazz });
            }
            finally {
                instrumentation.removeTransformer(ctf);
                instrumentation.retransformClasses(new Class[] { clazz });
            }
            return Common.DONE + " bytecode dumped to " + ctf.getDumpFile(clazz).getAbsolutePath();
        }
        catch (UnmodifiableClassException e) {
            throw new RuntimeException("trace failed", e);
        }
        catch (Exception e) {
            throw new RuntimeException("trace failed", e);
        }
    }


    public static String usage() {
        return "classesdump [bytecode] [qualified-class-name]"// 
                + "\n  classesdump  dump all class informations to pid.classloaders.n.html" //
                + "\n  classesdump <<qualified-class-name>>  dump the class informations to console" //
                + "\n  classesdump -b <<qualified-class-name>> dump the class's bytecode to a file." //
        ;
    }
}
