package cn.hqm.jvm.transformer;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import cn.hqm.jvm.Log;
import cn.hqm.jvm.asm.MethodInterceptor;


/**
 * 
 * @author linxuan
 *
 */
public class TraceTransformer implements ClassFileTransformer {
    private Set<String> methods;
    private Set<Class<?>> classes;
    private MethodInterceptor methodInterceptor;


    public TraceTransformer(Set<String> methods, Set<Class<?>> classes) {
        this.methods = methods;
        this.classes = classes;
    }


    @Override
    public byte[] transform(final ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        Log.warn("transform " + className);
        if (!classes.contains(classBeingRedefined)) {
            return null;
        }
        ClassWriter cw = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS) {
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                Class<?> c, d;
                try {
                    if (loader != null) {
                        c = loader.loadClass(type1.replace('/', '.'));
                        d = loader.loadClass(type2.replace('/', '.'));
                    }
                    else {
                        c = Class.forName(type1.replace('/', '.'));
                        d = Class.forName(type2.replace('/', '.'));
                    }
                }
                catch (Exception e) {
                    throw new RuntimeException(e.toString());
                }
                if (c.isAssignableFrom(d)) {
                    return type1;
                }
                if (d.isAssignableFrom(c)) {
                    return type2;
                }
                if (c.isInterface() || d.isInterface()) {
                    return "java/lang/Object";
                }
                else {
                    do {
                        c = c.getSuperclass();
                    } while (!c.isAssignableFrom(d));
                    return c.getName().replace('.', '/');
                }
            }
        };
        ClassReader cr = new ClassReader(classfileBuffer);
        methodInterceptor = new MethodInterceptor(cw, className, methods);
        try {
            cr.accept(methodInterceptor, ClassReader.EXPAND_FRAMES);
            //cr.accept(cw, ClassReader.EXPAND_FRAMES);
        }
        catch (Exception e) {
            Log.warn("accept failed. className:" + className, e);
            return null;
        }
        byte[] bytes = cw.toByteArray();
        //dump2file(bytes, "TraceTransformer.x.class");
        /*
        try {
            Class<?> c = loader.loadClass("cn.hqm.jvm.asm.MethodAdvice");
            Log.warn("MethodAdvice loaded by the ClassLoader of " + className);
        }
        catch (ClassNotFoundException e) {
            Log.warn("MethodAdvice can't load by the ClassLoader of " + className, e);
        }
        */
        return bytes;
    }


    public static void dump2file(byte[] bytes, String fileName) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(new File(fileName));
            out.write(bytes);
            out.flush();
            out.close();
        }
        catch (Exception e) {
            Log.warn("", e);
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException e) {
                }
            }
        }
    }
}
