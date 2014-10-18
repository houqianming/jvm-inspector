package cn.hqm.jvm;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;
import java.util.Map;


/**
 * 
 * @author Houqianming
 *
 */
public interface Flusher {
    Tree<ClassLoadingInfo, String> flush(File file, Map<ClassLoader, List<ClassLoadingInfo>> classLoaders,
            Map<URL, List<ClassLoadingInfo>> locations, List<ClassLoadingInfo> allClasses, Class<?>[] preLoadClasses)
            throws IOException;


    Tree<ClassLoadingInfo, String> flush(PrintWriter pw, Map<ClassLoader, List<ClassLoadingInfo>> classLoaders,
            Map<URL, List<ClassLoadingInfo>> locations, List<ClassLoadingInfo> allClasses, Class<?>[] preLoadClasses)
            throws IOException;
}
