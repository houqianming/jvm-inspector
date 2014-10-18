/*
 * Copyright 2012 zhongl
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package cn.hqm.jvm.asm;

import java.io.Flushable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import cn.hqm.jvm.InspectAgent;


/**
 * See <a href="https://github.com/zhongl/HouseMD/issues/17">#17</a> for more information.
 *
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 * @author linxuan
 */
public class MethodAdvice {
    public static final Logger log = Logger.getLogger("MethodAdvice");
    public static final String CLASS = "class";
    public static final String METHOD = "method";
    public static final String VOID_RETURN = "voidReturn";
    public static final String THIS = "this";
    public static final String ARGUMENTS = "arguments";
    public static final String DESCRIPTOR = "descriptor";
    public static final String STACK = "stack";
    public static final String STARTED = "started";
    public static final String STOPPED = "stopped";
    public static final String TIMEUSE = "timeuse";
    public static final String RESULT = "result";
    public static final String THREAD = "thread";
    public static final String CLASS_LOADER = "classLoader";

    public static final Method ON_METHOD_BEGIN;
    public static final Method ON_METHOD_END;

    private static final ConcurrentHashMap<Thread, Stack<Map<String, Object>>> threadBoundContexts;

    public static Set<Class<?>> usedClasses = new HashSet<Class<?>>();
    static {
        try {
            ON_METHOD_BEGIN =
                    MethodAdvice.class.getMethod("onMethodBegin", String.class, String.class, String.class,
                        Object.class, Object[].class);
            ON_METHOD_END = MethodAdvice.class.getMethod("onMethodEnd", Object.class);
        }
        catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        threadBoundContexts = new ConcurrentHashMap<Thread, Stack<Map<String, Object>>>();
        reset();
        //onMethodBegin onMethodEnd两个函数用到的JDK类
        usedClasses.add(java.util.Map.class);
        usedClasses.add(java.util.HashMap.class);
        usedClasses.add(java.util.Set.class);
        usedClasses.add(java.util.HashSet.class);
        usedClasses.add(java.util.concurrent.atomic.AtomicInteger.class);
        usedClasses.add(java.util.concurrent.ConcurrentHashMap.class);
        usedClasses.add(java.util.Stack.class);
        usedClasses.add(java.lang.StringBuilder.class);
        usedClasses.add(java.io.BufferedWriter.class);
        usedClasses.add(java.io.InputStreamReader.class);
        usedClasses.add(java.io.OutputStreamWriter.class);
    }

    

    public static void onMethodBegin(String className, String methodName, String descriptor, Object thisObject,
            Object[] arguments) {
        Map<String, Object> context = new HashMap<String, Object>();
        if (displays.contains(CLASS)) {
            context.put(CLASS, className.replace('/', '.'));
        }
        context.put(METHOD, methodName);
        if (displays.contains(ARGUMENTS)) {
            context.put(ARGUMENTS, arguments);
        }
        if (displays.contains(TIMEUSE)) {
            context.put(STARTED, System.nanoTime());
        }
        if (displays.contains(THREAD)) {
            context.put(THREAD, Thread.currentThread());
        }
        if (displays.contains(DESCRIPTOR)) {
            context.put(DESCRIPTOR, descriptor);
        }
        if (displays.contains(THIS)) {
            context.put(THIS, thisObject);
        }
        if (displays.contains(CLASS_LOADER)) {
            context.put(CLASS_LOADER, loader);
        }
        if (displays.contains(VOID_RETURN)) {
            context.put(VOID_RETURN, isVoidReturn(descriptor));
        }
        if (displays.contains(STACK)) {
            context.put(STACK, currentStackTrace());
        }
        enterWith(context, stackPush(context));
    }


    public static void onMethodEnd(Object resultOrException) {
        Stack<Map<String, Object>> invokeStack = threadBoundContexts.get(Thread.currentThread());
        Map<String, Object> context = invokeStack.pop();
        if (displays.contains(TIMEUSE)) {
            context.put(STOPPED, System.nanoTime());
        }
        if (displays.contains(RESULT)) {
            context.put(RESULT, resultOrException);
        }
        exitWith(context, invokeStack.size());
    }


    /**
     * @return push之前栈的深度
     */
    private static int stackPush(Map<String, Object> context) {
        Thread t = Thread.currentThread();
        Stack<Map<String, Object>> s = threadBoundContexts.get(t);
        if (s == null) {
            s = new Stack<Map<String, Object>>();
            Stack<Map<String, Object>> old = threadBoundContexts.putIfAbsent(t, s);
            if (old != null) {
                s = old;
            }
        }
        s.push(context);
        return s.size() - 1;
    }


    //只是为了尽量减少copyOfRange的使用 // trim useless stack trace elements.
    private static StackTraceElement[] currentStackTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackDepth == 0) {
            return stackTrace;
        }
        int depth1 = 0;
        int depth2 = stackTrace.length;
        if (stackDepth < 0) {
            depth1 = stackTrace.length + stackDepth - stackDepthSkip;
            if (depth1 < 0) {
                depth1 = 0;
            }
            depth2 = stackTrace.length;
        }
        else {
            depth1 = 0;
            depth2 = stackDepth + stackDepthSkip;
            if (depth2 > stackTrace.length) {
                depth2 = stackTrace.length;
            }
        }

        if (depth1 == 0 && depth2 == stackTrace.length) {
            return stackTrace;
        }
        return Arrays.copyOfRange(stackTrace, depth1, depth2);
    }


    private static Boolean isVoidReturn(String descriptor) {
        return descriptor.charAt(descriptor.indexOf(')') + 1) == 'V';
    }

    public static final AtomicInteger count = new AtomicInteger(0);
    public static final Object counterLock = new Object();
    public static volatile int targetCount;
    public static volatile int stackDepth;
    private static final int stackDepthSkip = 3; //跳过adviser的stack
    public static volatile Set<String> displays;
    public static volatile ClassLoader loader;
    private static final String[] indents = new String[] { "", " ", "  ", "   ", "    ", "     " };


    public static void reset() {
        targetCount = 2;
        stackDepth = 0;
        loader = null;
        displays = new HashSet<String>();
        displays.add(CLASS);
        displays.add(METHOD);
        displays.add(ARGUMENTS);
        displays.add(RESULT);
        displays.add(TIMEUSE);
    }


    private static String getIndent(int invokeStackDepth) {
        String indent = null;
        if (invokeStackDepth < indents.length) {
            indent = indents[invokeStackDepth];
        }
        else {
            indent = indents[indents.length - 1];
            while (indent.length() < invokeStackDepth) {
                indent = indent + " ";
            }
        }
        return indent;
    }


    public static void enterWith(Map<String, Object> context, int invokeStackDepth) {
        int curr = count.incrementAndGet();
        StringBuilder sb = new StringBuilder(getIndent(invokeStackDepth));
        sb.append("total invok ").append(curr).append(". ").append(context.get(METHOD));
        sb.append(displays.contains(DESCRIPTOR) ? context.get(DESCRIPTOR) : "()");
        //sb.append(" begin.");
        InspectAgent.echo(sb.toString());
    }


    public static void exitWith(Map<String, Object> context, int invokeStackDepth) {
        String indent = getIndent(invokeStackDepth);
        StringBuilder sb = new StringBuilder(indent);
        if (displays.contains(TIMEUSE)) {
            long nanotime = (Long) context.get(STOPPED) - (Long) context.get(STARTED);
            sb.append(nanotime).append("ns,");
        }
        if (displays.contains(CLASS)) {
            sb.append(context.get(CLASS)).append(".");
        }
        sb.append(context.get(METHOD)).append("(");
        if (displays.contains(ARGUMENTS)) {
            Object[] arguments = (Object[]) context.get(ARGUMENTS);
            for (Object arg : arguments) {
                if(arg == null){
                    sb.append("null,");
                }
                else if (arg instanceof Flushable) {
                    //避免ByteArrayOutputStream StringWriter等将页面输出内容直接dump出来
                    sb.append(arg.getClass().getSimpleName()).append("@")
                        .append(Integer.toHexString(System.identityHashCode(arg))).append(",");
                }
                else if (arg.getClass().isArray() && !(arg instanceof byte[])) {
                    sb.append(Arrays.toString((Object[]) arg)).append(",");
                }
                else {
                    sb.append(arg).append(",");
                }
            }
        }
        sb.append(")");
        if (displays.contains(RESULT)) {
            sb.append(context.get(RESULT));
        }
        if (displays.contains(THREAD)) {
            sb.append(",thread:").append(context.get(THREAD));
        }
        /*
        if (displays.contains(DESCRIPTOR)) {
            sb.append(",descriptor:").append(context.get(DESCRIPTOR));
        }
        */
        if (displays.contains(THIS)) {
            sb.append(",this:").append(context.get(THIS));
        }
        if (displays.contains(CLASS_LOADER)) {
            sb.append(",loader:").append(
                context.get(THIS) != null ? context.get(THIS).getClass().getClassLoader() : context.get(CLASS_LOADER));
        }
        if (displays.contains(VOID_RETURN)) {
            sb.append(",isVoidReturn:").append(context.get(VOID_RETURN));
        }
        if (displays.contains(STACK)) {
            sb.append(",stack:");
            StackTraceElement[] stackTraceElements = (StackTraceElement[]) context.get(STACK);
            //for (StackTraceElement stackTraceElement : stackTraceElements) {
            for (int i = stackDepthSkip; i < stackTraceElements.length; i++) {
                sb.append("\n ").append(indent).append(stackTraceElements[i]);
            }
        }
        InspectAgent.echo(sb.toString());
        log.fine("" + context);

        //通知前端不再等待
        if (invokeStackDepth == 0) {
            int curr = count.get();
            if (curr >= targetCount) {
                synchronized (counterLock) {
                    counterLock.notifyAll();
                }
            }
        }
    }
}
