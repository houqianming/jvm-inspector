package cn.hqm.jvm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.PrintWriter;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;


/**
 * 
 * @author linxuan
 *
 */
public class RequestProcessor {
    public static String process(File outputDir, String line, BufferedWriter bw, Instrumentation instrumentation) {
        if (InspectAgent.inspector.getPkgTree() == null) {
            return "Not initialized, wait a moment...s";
        }
        String[] tokens = line.trim().split("\\s");
        try {
            if ("get".equalsIgnoreCase(tokens[0])) {
                if (tokens.length < 2) {
                    return "Usage:get com.x.Foo.bar";
                }
                return eval(tokens[1]);
            }
            else if ("set".equalsIgnoreCase(tokens[0])) {
                if (tokens.length < 3) {
                    return "Usage:set com.x.Foo.bar 3\n      set com.x.Foo.bar \"abc\"";
                }
                Object obj = execute(tokens[1], tokens[2]);
                return "old value:" + (obj == null ? "null" : obj.toString());
            }
            else if ("classesdump".equals(tokens[0])) {
                return InspectAgent.inspector.dump(outputDir, instrumentation, tokens);
            }
            else if ("dumpthreads".equals(tokens[0])) {
                File file = ThreadSpy.dumpall(outputDir);
                return Common.DONE + " thread infos dumped to " + file.getAbsolutePath();
            }
            else if ("trace".equals(tokens[0])) {
                return MethodSpy.trace(outputDir, instrumentation, tokens);
            }
            else if ("help".equals(tokens[0])) {
                if (tokens.length > 1) {
                    if ("trace".equals(tokens[1])) {
                        return MethodSpy.usage();
                    }
                    if ("classesdump".equals(tokens[1])) {
                        return ClassLoadingSpy.usage();
                    }
                }
                return usage();
            }
            else if (tokens[0].startsWith("#")) {
                //#号当做注释；用于文件输入的场景
                return "";
            }
            else if (tokens[0].length() > 0) {
                //return usage();
                //和所有命令不匹配， 当做是直接执行表达式
                return eval(tokens[0]);
            }
            return ""; //空换行不做任何事情
        }
        catch (Exception e) {
            Log.warn("", e);
            e.printStackTrace(new PrintWriter(bw));
            return "";
        }
    }


    private static String usage() {
        return "usage:"// 
                + "\n  quit        terminate the process." //
                + "\n  help        display this infomation." //
                + "\n  classesdump dump all class informations to pid.classloaders.n.html"//
                + "\n  dumpthreads dump all thread stacks to pid.threads.n.txt"//
                + "\n  trace       display or output infomation of method invocaton." //
                + "\n     trace org.apache.catalina.servlets.DefaultServlet.doGet()"//
                + "\n  get         execute a java-style expression and display the result." //
                + "\n     get org.apache.catalina.servlets.DefaultServlet.factory.isNamespaceAware()" //
                + "\n  set         set a value to a reference specified by the java-style expression"//
                + "\n     set org.apache.catalina.servlets.DefaultServlet.factory.namespaceAware true"//
        ;
    }


    private static String eval(String expr) throws SecurityException, NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException {
        Object obj = execute(expr, null);
        if (obj == null) {
            return "null";
        }
        else if (obj.getClass().isArray()) {
            return Arrays.toString((Object[]) obj);
        }
        return obj.toString();

    }


    private static Object execute(String expr, String valueExpr) throws SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException,
            ClassNotFoundException {
        Class<?> clazz = InspectAgent.inspector.getClazzByExepression(expr);
        if (clazz == null) {
            throw new IllegalArgumentException("Class not found. expr:" + expr);
        }
        String[] options = Common.splitSkipPairs(expr.substring(clazz.getName().length()), '.', '(', ')');
        Object currentObject = null;
        Class<?> currentClass = clazz;
        Field lastField = null;
        Object lastFieldObject = null;
        for (String option : options) {
            option = option.trim();
            if (option.length() == 0) {
                continue;
            }
            int index = option.indexOf("(");
            if (index == -1) {
                if (option.equals("length") && currentClass.isArray()) {
                    //get com.xx.PhpEngineInitializer._quercus._classDefMap.length
                    return Array.getLength(currentObject);
                }
                if (option.equals("class")) {
                    //cn.hqm.jvm.AgentArgs.class
                    currentObject = currentClass;
                    currentClass = java.lang.Class.class;
                    continue;
                }
                Integer arrayIndex = null;
                if (option.endsWith("]")) {
                    //get com.xx.PhpEngineInitializer._quercus._classDefMap[0] 
                    int indexa = option.indexOf("[");
                    if (indexa == -1) {
                        return "invalid expression near '" + option + "'";
                    }
                    arrayIndex = Integer.valueOf(option.substring(indexa + 1, option.length() - 1).trim());
                    option = option.substring(0, indexa).trim();
                }

                //字段取值
                Field field = getField(currentClass, option, true);
                if (field == null) {
                    return "Field " + option + " not found in " + currentClass;
                }
                lastField = field;
                lastFieldObject = currentObject;
                field.setAccessible(true);
                currentObject = field.get(currentObject);

                if (arrayIndex != null) {
                    currentObject = Array.get(currentObject, arrayIndex);
                }
                //lastAoRef[0] = field;
            }
            else {
                //方法调用
                String methodName = option.substring(0, index).trim();
                //int index2 = option.lastIndexOf(")", index + 1);  //这个竟然返回-1
                int index2 = option.lastIndexOf(")");
                String argsExpr = index2 > 0 ? option.substring(index + 1, index2) : "";
                String[] argStrings = argsExpr.trim().length() == 0 ? new String[0] : argsExpr.split(",");
                Object[] methodAndArgValues = getMethodAndArgValues(currentClass, methodName, argStrings);
                if (methodAndArgValues != null) {
                    Method method = (Method) methodAndArgValues[0];
                    Object[] argValues = (Object[]) methodAndArgValues[1];
                    method.setAccessible(true);
                    currentObject = method.invoke(currentObject, argValues);
                }
                else {
                    return "Method " + option + " not found in " + currentClass;
                }
            }
            if (currentObject != null) {
                currentClass = currentObject.getClass();
            }
        }

        if (valueExpr != null) {
            //赋值
            //Object value = parseValue(lastField, valueExpr);
            setValue(lastFieldObject, lastField, valueExpr.trim());
        }

        return currentObject;
    }


    private static Field getField(Class<?> currentClass, String fieldName, boolean istop) throws NoSuchFieldException {
        //NoSuchFieldException e = null;
        try {
            return currentClass.getDeclaredField(fieldName); //本类定义的字段
        }
        catch (NoSuchFieldException e0) {
            //e = e0;
            if (istop) {
                try {
                    return currentClass.getField(fieldName); //本类可访问的public字段
                }
                catch (NoSuchFieldException e1) {
                    //e = e1;
                }
            }
            Class<?> superClass = currentClass.getSuperclass();
            if (superClass == null) {
                //throw e;
                return null;
            }
            else {
                return getField(superClass, fieldName, false); //父类定义的字段
            }
        }
    }


    private static Object[] getMethodAndArgValues(Class<?> currentClass, String methodName, String[] argsInString)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException, ClassNotFoundException {
        Method[] methods = currentClass.getMethods();
        IllegalArgumentException exception = null;
        Outer: for (Method m : methods) {
            if (!m.getName().equals(methodName)) {
                continue;
            }
            Class<?>[] argTypes = m.getParameterTypes();
            if (argTypes.length != argsInString.length) {
                continue;
            }
            Object[] args = new Object[argTypes.length];
            for (int i = 0; i < argTypes.length; i++) {
                try {
                    args[i] = getValue(argTypes[i], argsInString[i]);
                }
                catch (IllegalArgumentException e) {
                    //throwOnTypeUnmatch
                    exception = e;
                    continue Outer; //换下一个方法
                }
            }
            return new Object[] { m, args };
        }
        if (exception != null) {
            throw exception;
        }
        return null;
    }


    private static void setValue(Object currentObject, Field field, String valueExpr) throws IllegalArgumentException,
            IllegalAccessException, SecurityException, NoSuchFieldException, NoSuchMethodException,
            InvocationTargetException, ClassNotFoundException {
        if (valueExpr.equals("null")) {
            field.set(currentObject, null);
        }
        field.set(currentObject, getValue(field.getType(), valueExpr));
    }


    private static Object getValue(Class<?> type, String valueExpr) throws SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException,
            ClassNotFoundException {
        valueExpr = valueExpr.trim();
        if (Common.isPrimitiveExpression(valueExpr)) {
            return Common.getPrimitiveValue(type, valueExpr);
        }
        else {
            Object value = execute(valueExpr, null);
            if (value == null || type.isAssignableFrom(value.getClass())) {
                return value;
            }
            else {
                throw new IllegalArgumentException(valueExpr + " not match the type:" + type);
            }
        }

    }


    public static void main(String[] args) throws SecurityException, IllegalArgumentException, NoSuchFieldException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException {
        Class<?>[] classes = new Class<?>[] { MethodSpy.class, String.class };
        InspectAgent.inspector = new ClassLoadingSpy(new File(""), Logger.getLogger(new File("main.log")), classes);
        System.out.println(int.class.isAssignableFrom(Integer.class)); //false
//        
//        System.out.println(execute("cn.hqm.jvm.MethodSpy.options.entrySet().iterator().next().getValue().msg",
//            null));
//        System.out.println(execute("cn.hqm.jvm.MethodSpy.options.get('t').msg", null));
//        System.out.println(execute("cn.hqm.jvm.MethodSpy.options.get('l').msg", null));
//
//        String[] ss = new String[] { "-123", "-5L", "789l", "-L", "5.12", "10f" };
//        for (String s : ss) {
//            System.out.println("isPrimitiveExpression(" + s + "):" + Common.isPrimitiveExpression(s));
//        }

        System.out.println(execute("cn.hqm.jvm.MethodSpy.usage", "java.lang.String.valueOf(123)"));
        System.out.println(execute("cn.hqm.jvm.MethodSpy.usage", null));
        System.out.println(MethodSpy.class.getProtectionDomain().getCodeSource().getLocation());
    }
}
