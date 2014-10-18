package cn.hqm.jvm;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;


/**
 * 
 * @author linxuan
 *
 */
public class ClassLoadingInfo {
    private final URL NULL_URL;
    private final ClassLoader BootStrapLoader;


    private URL initNullURL(File outputDir) {
        try {
            return new File(outputDir, "null").toURI().toURL();
        }
        catch (MalformedURLException e) {
            return null;
        }
    }

    private String name; //full class name 
    private ClassLoader loader;
    private URL location;
    private int order;


    public ClassLoadingInfo(File outputDir) {
        this.NULL_URL = initNullURL(outputDir);
        BootStrapLoader = new URLClassLoader(new URL[] { NULL_URL });
    }


    public ClassLoadingInfo(File outputDir, String name, ClassLoader loader, URL classLocation, int order) {
        this(outputDir);
        this.name = name;
        this.loader = loader == null ? BootStrapLoader : loader;
        this.location = classLocation == null ? NULL_URL : classLocation;
        this.order = order;
    }


    public ClassLoadingInfo(File outputDir, String name, ClassLoader loader, ProtectionDomain protectionDomain,
            int order) {
        this(outputDir, name, loader, protectionDomain.getCodeSource() == null ? null : protectionDomain
            .getCodeSource().getLocation(), order);
    }


    public ClassLoadingInfo(File outputDir, Class<?> clazz, int order) {
        this(outputDir, clazz.getName(), clazz.getClassLoader(), clazz.getProtectionDomain(), order);
    }


    public boolean isNullLocation() {
        return this.location == null || this.location == NULL_URL;
    }


    public ClassLoader getLoader() {
        return loader;
    }


    public void setLoader(ClassLoader loader) {
        this.loader = loader;
    }


    public URL getLocation() {
        return location;
    }


    public void setLocation(URL location) {
        this.location = location;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public int getOrder() {
        return order;
    }


    public void setOrder(int order) {
        this.order = order;
    }
}
