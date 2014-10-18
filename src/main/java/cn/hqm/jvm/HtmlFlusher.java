package cn.hqm.jvm;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * Flush jvm class loading info to a html file.
 * 
 * -DHtmlFlusher.enableHyperlink=true to enable html hyperlink.
 * 
 * @author Houqianming
 *
 */
public class HtmlFlusher implements Flusher {
    private static final String SEP = ", ";
    private static final String bracket1 = "[";
    private static final String bracket2 = "]";
    private static final String nullClassloaderName = "BootStrapLoader";
    private static final String nullUrlName = "BootStrapLoaderLocation";
    private static final int MAX_ITEM_COUNT_NOT_HIERARCHICAL = 6;
    private static final MessageFormat packageTemplate = new MessageFormat("{0}" + SEP + "{1}" + SEP + "{2}" + SEP
            + "{3}");
    private static final MessageFormat locationTemplate = new MessageFormat("{0}" + SEP + "{2}" + SEP + "{3}");
    private static final MessageFormat loaderTemplate = new MessageFormat("{0}" + SEP + "{1}" + SEP + "{3}");

    /**
     * -DStringAlias0=looooooongString=shortString
     * Map<LongString,ShortString>
     */
    private Map<String, String> stringAlias = new TreeMap<String, String>();
    private Map<ClassLoader, String> loaderAlias = new HashMap<ClassLoader, String>();
    private Map<URL, String> urlAlias = new HashMap<URL, String>();
    private int libNumber = 0;
    private int loaderNumber = 0;
    private int pkgNumber = 0;
    private Map<URL, List<ClassLoadingInfo>> sortedLocations = new TreeMap<URL, List<ClassLoadingInfo>>(
        new Comparator<URL>() {
            public int compare(URL o1, URL o2) {
                //return o1.getPath().compareTo(o2.getPath());
                return urlAlias.get(o1).compareTo(urlAlias.get(o2));
            }
        });
    private boolean enableHyperlink;


    public HtmlFlusher() {
        enableHyperlink = "true".equals(System.getProperty("HtmlFlusher.enableHyperlink"));
    }


    public HtmlFlusher(boolean enableHyperlink) {
        this.enableHyperlink = enableHyperlink;
    }


    private void fillAlias(Map<ClassLoader, List<ClassLoadingInfo>> classLoaders,
            Map<URL, List<ClassLoadingInfo>> locations) {
        this.libNumber = 0;
        this.loaderNumber = 0;
        this.pkgNumber = 0;
        for (Map.Entry<ClassLoader, List<ClassLoadingInfo>> entry : classLoaders.entrySet()) {
            fillAlias(entry.getKey());
        }
        for (Map.Entry<URL, List<ClassLoadingInfo>> entry : locations.entrySet()) {
            fillAlias(entry.getKey());
            sortedLocations.put(entry.getKey(), entry.getValue());
        }
    }


    /**
     * Reserved please
     */
    @SuppressWarnings("unused")
    private void fillAlias(String propPreFix) {
        for (int i = 0; true; i++) {
            String alias = System.getProperty(propPreFix + i);
            if (alias == null)
                break;
            int index = alias.indexOf("=");
            if (index == -1)
                break;
            stringAlias.put(alias.substring(0, index), alias.substring(index + 1));
        }
    }


    private void fillAlias(ClassLoader loader) {
        String shortStr = loader == null ? nullClassloaderName : loaderNumber + "-" + loader.getClass().getSimpleName();
        this.loaderAlias.put(loader, bracket1 + shortStr + bracket2);
        loaderNumber++;
    }


    private void fillAlias(URL url) {
        if (url == null) {
            this.urlAlias.put(url, bracket1 + nullUrlName + bracket2);
            return;
        }
        String path = url.getPath();
        if (path == null) {
            this.urlAlias.put(url, url.toString());
            return;
        }

        int index = path.lastIndexOf('/');
        if (index == -1) {
            this.urlAlias.put(url, url.toString());
            return;
        }

        String libPath = path.substring(0, index);
        String libShort = this.stringAlias.get(libPath);
        if (libShort == null) {
            libShort = bracket1 + getLibShort(libPath) + bracket2;
            this.stringAlias.put(libPath, libShort);
        }

        String urlAliasValue =
                this.enableHyperlink ? "<a href=\"#a" + libShort + "\" title=\"" + url + "\">" + libShort + "</a>"
                        + path.substring(index) : libShort + path.substring(index);
        this.urlAlias.put(url, urlAliasValue);
    }


    private String getLibShort(String libPath) {
        String libShort = "lib";
        String[] libs = libPath.split("/");
        int len = libs.length;
        if (len >= 2)
            libShort = libs[len - 2] + "/" + libs[len - 1];
        else if (len == 1)
            libShort = libs[0];
        int number = libNumber++;
        return (number < 10 ? "0" : "") + number + "-" + libShort;//libPath.hashCode();
    }


    private void outputAliasTable(PrintWriter pw) {
        pw.println("<br><table border=1>");
        pw.println("<tr><td>Alias</td><td>Content</td></tr>");
        for (Map.Entry<String, String> entry : this.stringAlias.entrySet()) {
            pw.println(this.enableHyperlink ? "<tr><td><a name=\"a" + entry.getValue() + "\">" + entry.getValue()
                    + "</td><td>" + entry.getKey() + "</td></tr>" : "<tr><td>" + entry.getValue() + "</td><td>"
                    + entry.getKey() + "</td></tr>");
        }
        for (Map.Entry<ClassLoader, String> entry : this.loaderAlias.entrySet()) {
            pw.println(this.enableHyperlink ? "<tr><td><a name=\"a" + entry.getValue() + "\">" + entry.getValue()
                    + "</td><td>" + entry.getKey() + "</td></tr>" : "<tr><td>" + entry.getValue() + "</td><td>"
                    + entry.getKey() + "</td></tr>");

        }
        pw.println("</table>");
    }


    private String toShort(ClassLoader loader) {
        if (loader == null)
            return nullClassloaderName;
        String shortStr = this.loaderAlias.get(loader);
        if (shortStr == null)
            shortStr = loader.toString();
        return this.enableHyperlink ? "<a href=\"#a" + shortStr + "\" title=\"" + loader.getClass().getName() + "\">"
                + shortStr + "</a>" : shortStr;
    }


    private String toShort(URL url) {
        return this.urlAlias.get(url);
    }


    /**
     * Reserved please
     */
    @SuppressWarnings("unused")
    private String toShort(Object obj) {
        if (obj == null)
            return null;
        String str = obj.toString();
        for (Map.Entry<String, String> entry : this.stringAlias.entrySet()) {
            if (str.startsWith(entry.getKey()))
                return bracket1 + entry.getValue() + bracket2 + str.substring(entry.getKey().length());
            int index = str.indexOf(entry.getKey());
            if (index != -1)
                return str.substring(0, index) + bracket1 + entry.getValue() + bracket2
                        + str.substring(index + entry.getKey().length());
        }
        return str;
    }


    public Tree<ClassLoadingInfo, String> flush(File file, Map<ClassLoader, List<ClassLoadingInfo>> classLoaders,
            Map<URL, List<ClassLoadingInfo>> locations, List<ClassLoadingInfo> allClasses, Class<?>[] preLoadClasses)
            throws IOException {
        PrintWriter pw = new PrintWriter(file, "utf-8");
        return this.flush(pw, classLoaders, locations, allClasses, preLoadClasses);
    }


    public Tree<ClassLoadingInfo, String> flush(PrintWriter pw, Map<ClassLoader, List<ClassLoadingInfo>> classLoaders,
            Map<URL, List<ClassLoadingInfo>> locations, List<ClassLoadingInfo> allClasses, Class<?>[] preLoadClasses)
            throws IOException

    {
        pw.println("<head><meta http-equiv=\"content-type\" content=\"text/html\"; charset=\"UTF-8\"></head>");
        pw.println("<script>");
        pw.println("function onItemClick(idSuffix)");
        pw.println("{");
        pw.println("   subs = window.eval('ul_'+idSuffix)");
        pw.println("   if(subs.style.display=='none')");
        pw.println("      subs.style.display='block';");
        pw.println("   else");
        pw.println("      subs.style.display='none';");
        pw.println("   event.cancelBubble=true;");
        pw.println("}");
        pw.println("function expendAll(flag)");
        pw.println("{");
        pw.println("   var uls = document.getElementsByTagName('UL')");
        pw.println("   var display = flag==0? 'none' :'block';");
        pw.println("   for(var i=0; i<uls.length; i++)");
        pw.println("      uls[i].style.display = display;");
        pw.println("}");
        pw.println("</script>");
        pw.println("<style type=\"text/css\">");
        pw.println("   li{list-style-type:none; margin-top:0px; margin-bottom:0px; margin-left:5}");
        pw.println("</style>");
        pw.println("<input type=button value=\"全部展开\" onclick=\"expendAll(1)\" />");
        pw.println("<input type=button value=\"全部折叠\" onclick=\"expendAll(0)\" />");

        fillAlias(classLoaders, locations);

        //flushLoaderView(pw, classLoaders, preLoadClasses);
        flushLoaderViewHierarchical(pw, classLoaders, preLoadClasses);
        //flushJarView(pw, this.sortedLocations);
        flushJarViewHierarchical(pw, this.sortedLocations);
        Tree<ClassLoadingInfo, String> pkgTree = flushPackageView(pw, allClasses);

        outputAliasTable(pw);

        pw.flush();
        pw.close();
        return pkgTree;
    }


    private void flushLoaderViewHierarchical(PrintWriter pw, Map<ClassLoader, List<ClassLoadingInfo>> classLoaders,
            Class<?>[] preLoadClasses) {
        pw.println("<p><li id=li_loaderview onclick=\"onItemClick('loaderview')\" style=\"{list-style-type:circle;}\">ClassLoader View</li>");
        pw.println("<ul id=ul_loaderview>");

        if (preLoadClasses != null) {
            pw.println("<li id=li_preload onclick=\"onItemClick('preload')\">PreLoadedClasses" + SEP
                    + preLoadClasses.length + " classes</li>");
        }
        /*
        pw.println("<ul id=ul_preload style=\"{display:none}\">");
        List<ClassLoadingInfo> preLoadInfos = new ArrayList<ClassLoadingInfo>();
        for (Class<?> c : preLoadClasses) {
        	preLoadInfos.add(new ClassLoadingInfo(c.getName(), c.getClassLoader(), c.getProtectionDomain(), -1));
        }
        flushPackageView0(pw, preLoadInfos, locationTemplate);
        pw.println("</ul>");
        */

        this.loaderNumber = 0;
        for (Map.Entry<ClassLoader, List<ClassLoadingInfo>> entry : classLoaders.entrySet()) {
            ClassLoader loader = entry.getKey();
            //int hash = loader==null? 0 :loader.hashCode();
            //String idSuffix = "loader_" + (hash < 0 ? "_" + Math.abs(hash) : hash);
            String idSuffix = "loader" + this.loaderNumber++;
            pw.println("<li id=li_" + idSuffix + " onclick=\"onItemClick('" + idSuffix + "')\">" + toShort(loader)
                    + SEP + (entry.getValue() == null ? 0 : entry.getValue().size()) + " classes</li>");
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                pw.println("<ul id=ul_" + idSuffix + " style=\"{display:none}\">");
                if (entry.getValue().size() <= MAX_ITEM_COUNT_NOT_HIERARCHICAL) {
                    for (ClassLoadingInfo info : entry.getValue()) {
                        pw.println("<li>" + info.getName() + SEP + toShort(info.getLocation()) + SEP + info.getOrder()
                                + "</li>");
                    }
                }
                else {
                    flushPackageView0(pw, entry.getValue(), loaderTemplate);
                }
                pw.println("</ul>");
            }
        }
        pw.println("</ul>");
    }


    private void flushJarViewHierarchical(PrintWriter pw, Map<URL, List<ClassLoadingInfo>> locations) {
        pw.println("<p><li id=li_jarview onclick=\"onItemClick('jarview')\" style=\"{list-style-type:circle;}\">Physical Location View</li>");
        pw.println("<ul id=ul_jarview>");

        this.libNumber = 0; //reuse for jar count, not for lib count
        for (Map.Entry<URL, List<ClassLoadingInfo>> entry : locations.entrySet()) {
            URL url = entry.getKey();
            //int hash = url==null? 0 :url.hashCode();
            //String idSuffix = "url_" + (hash < 0 ? "_" + Math.abs(hash) : hash);
            String idSuffix = "url" + this.libNumber++;
            pw.println("<li id=li_" + idSuffix + " onclick=\"onItemClick('" + idSuffix + "')\" >" + toShort(url) + SEP
                    + (entry.getValue() == null ? 0 : entry.getValue().size()) + " classes</li>");
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                pw.println("<ul id=ul_" + idSuffix + " style=\"{display:none}\">");
                if (entry.getValue().size() <= MAX_ITEM_COUNT_NOT_HIERARCHICAL) {
                    for (ClassLoadingInfo info : entry.getValue()) {
                        pw.println("<li>" + info.getName() + SEP + toShort(info.getLoader()) + SEP + info.getOrder()
                                + "</li>");//
                    }
                }
                else {
                    flushPackageView0(pw, entry.getValue(), locationTemplate);
                }
                pw.println("</ul>");
            }
        }
        pw.println("</ul>");
    }


    private Tree<ClassLoadingInfo, String> flushPackageView(PrintWriter pw, Collection<ClassLoadingInfo> allClasses) {
        pw.println("<p><li id=li_packageview onclick=\"onItemClick('packageview')\" style=\"{list-style-type:circle;}\">Package View</li>");
        pw.println("<ul id=ul_packageview>");
        Tree<ClassLoadingInfo, String> pkgTree = flushPackageView0(pw, allClasses, packageTemplate);
        pw.println("</ul>");
        return pkgTree;
    }


    private Tree<ClassLoadingInfo, String> flushPackageView0(PrintWriter pw, Collection<ClassLoadingInfo> allClasses,
            final MessageFormat msgTemplate) {
        /*
        Tree<ClassLoadingInfo, String> pkgTree = new TreeImpl<ClassLoadingInfo, String>(null, "");
        for (ClassLoadingInfo info : allClasses) {
            String name = info.getName();
            String[] dirs = name.split("\\.");
            Tree<ClassLoadingInfo, String> classleaf = pkgTree.mkdirs(dirs);
            if (classleaf.getEntity() == null) {
                classleaf.setEntity(info);
            }
            else {
                //说明同样的class已经有别的classloader装载过了
                classleaf.getParent().addChild(new TreeImpl<ClassLoadingInfo, String>(info, dirs[dirs.length - 1]));
            }
        }
        */
        Tree<ClassLoadingInfo, String> pkgTree = getPackageTree(allClasses);
        passHierarchy(pw, pkgTree, msgTemplate);
        return pkgTree;
    }


    public static Tree<ClassLoadingInfo, String> getPackageTree(Collection<ClassLoadingInfo> allClasses) {
        Tree<ClassLoadingInfo, String> pkgTree = new TreeImpl<ClassLoadingInfo, String>(null, "");
        for (ClassLoadingInfo info : allClasses) {
            String name = info.getName();
            String[] dirs = name.split("\\.");
            Tree<ClassLoadingInfo, String> classleaf = pkgTree.mkdirs(dirs);
            if (classleaf.getEntity() == null) {
                classleaf.setEntity(info);
            }
            else {
                //说明同样的class已经有别的classloader装载过了
                classleaf.getParent().addChild(new TreeImpl<ClassLoadingInfo, String>(info, dirs[dirs.length - 1]));
            }
        }
        return pkgTree;
    }

    private static final Comparator<Tree<ClassLoadingInfo, String>> comparator =
            new Comparator<Tree<ClassLoadingInfo, String>>() {
                public int compare(Tree<ClassLoadingInfo, String> o1, Tree<ClassLoadingInfo, String> o2) {
                    if (o1 == null || o2 == null) {
                        return 0;
                    }
                    return o1.getId().compareTo(o2.getId());
                }
            };


    /**
     * @param msgTemplate MessageFormat, 例如"{0},{2},{3}"
     * 将依序号填入[ClassShorName, Location, ClassLoader, LoadOrder]
     */
    private void passHierarchy(PrintWriter pw, Tree<ClassLoadingInfo, String> pkgTree, final MessageFormat msgTemplate) {
        Collections.sort(pkgTree.getChildren(), comparator);

        for (Tree<ClassLoadingInfo, String> tree : pkgTree.getChildren()) {
            String item = tree.getId();
            if (tree.isLeaf()) {
                if (tree.getEntity() != null) {
                    //item.append(SEP).append(toShort(tree.getEntity().getLocation())).append(SEP).append(
                    //    toShort(tree.getEntity().getLoader())).append(SEP).append(tree.getEntity().getOrder());
                    item =
                            msgTemplate.format(new Object[] { tree.getId(), toShort(tree.getEntity().getLocation()),
                                                             toShort(tree.getEntity().getLoader()),
                                                             tree.getEntity().getOrder() });
                }
                pw.println("<li>" + item + "</li>");
            }
            else {
                String idSuffix = "pkg" + this.pkgNumber++; // getPath(tree);
                pw.println("<li id=li_" + idSuffix + " onclick=\"onItemClick('" + idSuffix
                        + "')\" style=\"{list-style-type:circle;}\">" + item + "</li>");
                pw.println("<ul id=ul_" + idSuffix + " style=\"{display:none}\">");
                passHierarchy(pw, tree, msgTemplate);
                pw.println("</ul>");
            }
        }
    }


    /**
     * Reserved please
     */
    //@SuppressWarnings("unchecked")
    @SuppressWarnings("unused")
    private static String getPath(Tree<ClassLoadingInfo, String> tree) {
        StringBuilder path = new StringBuilder(tree.getId());
        while ((tree = tree.getParent()) != null)
            path.insert(0, "_").insert(0, tree.getId());
        return path.toString();
    }


    public static void main(String[] args) throws MalformedURLException {
        List<ClassLoadingInfo> infos = new ArrayList<ClassLoadingInfo>();
        File outputDir = new File("");
        infos.add(new ClassLoadingInfo(outputDir, "org.hqm.common.MD5", ClassLoader.getSystemClassLoader(), new URL(
            "file:///c:/object.jar"), 0));
        infos.add(new ClassLoadingInfo(outputDir, "org.hqm.common.util.StringUtil", ClassLoader.getSystemClassLoader(), new URL(
            "file:///c:/object.jar"), 1));
        infos.add(new ClassLoadingInfo(outputDir, "org.hqm.tool.JvmInspector", ClassLoader.getSystemClassLoader(), new URL(
            "file:///c:/object.jar"), 2));
        infos.add(new ClassLoadingInfo(outputDir, "org.hqm.tool.uml.UmlTool", ClassLoader.getSystemClassLoader(), new URL(
            "file:///c:/object.jar"), 3));

        Map<ClassLoader, List<ClassLoadingInfo>> map1 = new HashMap<ClassLoader, List<ClassLoadingInfo>>();
        Map<URL, List<ClassLoadingInfo>> map2 = new HashMap<URL, List<ClassLoadingInfo>>();
        try {
            new HtmlFlusher().flush(new PrintWriter(System.out), map1, map2, infos, new Class[] {});
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    public boolean isEnableHyperlink() {
        return enableHyperlink;
    }


    public void setEnableHyperlink(boolean enableHyperlink) {
        this.enableHyperlink = enableHyperlink;
    }
}
