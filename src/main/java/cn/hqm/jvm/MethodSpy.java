package cn.hqm.jvm;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.hqm.jvm.asm.MethodAdvice;
import cn.hqm.jvm.cmd.Option;
import cn.hqm.jvm.transformer.TraceTransformer;


/**
 * 
 * @author linxuan
 *
 */
public class MethodSpy {
    private static final char k_t = 't';
    private static final char k_l = 'l';
    private static final char k_s = 's';
    private static final char k_d = 'd';
    private static Map<Character, Option> options = new LinkedHashMap<Character, Option>();
    private static String usage;
    static {
        put0(new Option(k_t, true, "time out in milliseconds. defaut 15000", null));
        put0(new Option(k_l, true, "max invoke count. defaut 2", null));
        put0(new Option('c', false, "show class name. default on", MethodAdvice.CLASS));
        put0(new Option('a', false, "show argument vlaues. default on.", MethodAdvice.ARGUMENTS));
        put0(new Option('r', false, "show return value. default on", MethodAdvice.RESULT));
        put0(new Option('e', false, "show time elapsed in nano. default on", MethodAdvice.TIMEUSE));
        put0(new Option('m', false, "show method description. default off", MethodAdvice.DESCRIPTOR));
        put0(new Option('h', false, "show thread name. default off", MethodAdvice.THREAD));
        put0(new Option(k_s, true, "show stack trace. default skip depth 0", MethodAdvice.STACK));
        put0(new Option('T', false, "show this Object. default off", MethodAdvice.THIS));
        put0(new Option('L', false, "show the ClassLoader of the inspect class. default off", MethodAdvice.CLASS_LOADER));
        put0(new Option(k_d, false, "dump details to file. default off", null));

        StringBuilder sb = new StringBuilder("trace qualified-mehtod-name [-t timout] [-l count] [-caremhsTLd]\n");
        for (Map.Entry<Character, Option> entry : options.entrySet()) {
            Option o = entry.getValue();
            sb.append("  -").append(o.key).append(" ").append(o.msg).append("\n");
        }
        usage = sb.toString();
    }


    private static void put0(Option o) {
        options.put(o.key, o);
    }


    public static String usage() {
        return usage;
    }


    //private Map<>
    private static String dealParam(String[] tokens, List<String> expressions, Map<Option, String> params) {
        Option lastOption = null;
        for (int i = 1; i < tokens.length; i++) { //跳过第一个
            String token = tokens[i];
            if (token.charAt(0) == '-') {
                int n = token.length();
                if (n < 2) {
                    return usage;
                }

                //负数的处理
                char c2 = token.charAt(1);
                if ('0' <= c2 && c2 <= '9') {
                    if (lastOption != null) {
                        params.put(lastOption, token);
                        lastOption = null;
                    }
                    continue;
                }

                lastOption = null;
                for (int j = 1; j < n; j++) {
                    char c = token.charAt(j);
                    Option o = options.get(c);
                    if (o != null) {
                        params.put(o, null);
                        if (o.hasvalue) {
                            lastOption = o;
                        }
                    }
                }
            }
            else if (lastOption != null) {
                params.put(lastOption, token);
                lastOption = null;
            }
            else {
                expressions.add(token);
            }
        }
        return null;
    }


    public static String trace(File outputDir, Instrumentation instrumentation, String[] tokens)
            throws ClassNotFoundException {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        Set<String> methods = new HashSet<String>();

        Map<Option, String> params = new HashMap<Option, String>();
        List<String> expressions = new ArrayList<String>();
        String errormsg = dealParam(tokens, expressions, params);
        if (errormsg != null) {
            return errormsg;
        }
        ClassLoader loader = null;
        for (String expr : expressions) {
            Class<?> clazz = InspectAgent.inspector.getClazzByExepression(expr);
            if (clazz == null) {
                return "Class not found. expr:" + expr;
            }
            if (!expr.startsWith(clazz.getName())) {
                throw new IllegalStateException(expr + ",but class:" + clazz);
            }
            if (cn.hqm.jvm.asm.MethodAdvice.usedClasses.contains(clazz)) {
                return "Class used by tracing self could not be traced: " + clazz;
            }
            loader = clazz.getClassLoader(); //TODO 用另一种方式
            String method = expr.substring(clazz.getName().length());
            int index1 = method.indexOf(".");
            int index2 = method.indexOf("(");
            if (index2 != -1) {
                method = method.substring(index1 + 1, index2).trim();
            }
            else {
                method = method.substring(index1 + 1).trim();
            }
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getName().equals(method)) {
                    methods.add(m.getName());
                    classes.add(m.getDeclaringClass());
                }
            }
            if (method.equals(clazz.getSimpleName())) {
                //构造函数
                methods.add("<init>");
                classes.add(clazz);
            }
        }
        if (classes.isEmpty() || methods.isEmpty()) {
            return "No methods found.";
        }

        //设置MethodAdvice参数及timeout
        long timeoutMS = 15000L;
        try {
            MethodAdvice.loader = loader;
            Set<String> displays = new HashSet<String>();
            for (Map.Entry<Option, String> entry : params.entrySet()) {
                Option o = entry.getKey();
                if (o.name != null) {
                    displays.add(o.name);
                }
            }
            if (!displays.isEmpty()) {
                MethodAdvice.displays = displays;
            }
            String stackDepth = params.get(options.get(k_s));
            if (stackDepth != null) {
                MethodAdvice.stackDepth = Integer.valueOf(stackDepth);
            }
            String timeout = params.get(options.get(k_t));
            if (timeout != null) {
                timeoutMS = Long.valueOf(timeout);
            }
            String targetInvokeCount = params.get(options.get(k_l));
            if (targetInvokeCount != null) {
                MethodAdvice.targetCount = Integer.valueOf(targetInvokeCount);
            }
        }
        catch (Exception e) {
            MethodAdvice.reset();
            return e.getMessage();
        }

        File detailFile = null;
        if (params.containsKey(options.get(k_d))) {
            detailFile = InspectAgent.openEchoToFile(outputDir);
        }

        //boolean isInterrupted = false;
        TraceTransformer ttf = new TraceTransformer(methods, classes);
        try {
            try {
                MethodAdvice.count.set(0);
                instrumentation.addTransformer(ttf, true);
                instrumentation.retransformClasses(classes.toArray(new Class[classes.size()]));
                synchronized (MethodAdvice.counterLock) {
                    try {
                        MethodAdvice.counterLock.wait(timeoutMS);
                    }
                    catch (InterruptedException e) {
                        //isInterrupted = true;
                        Thread.currentThread().interrupt();
                    }
                }
            }
            finally {
                instrumentation.removeTransformer(ttf);
                instrumentation.retransformClasses(classes.toArray(new Class[classes.size()]));
            }
        }
        catch (UnmodifiableClassException e) {
            throw new RuntimeException("trace failed", e);
        }
        catch (Exception e) {
            throw new RuntimeException("trace failed", e);
        }
        finally {
            MethodAdvice.reset();
            if (params.containsKey(options.get(k_d))) {
                InspectAgent.closeEchoToFile();
                return Common.DONE + " detail info dumped to " + detailFile.getAbsolutePath();
            }
        }
        return Common.DONE;
    }
}
