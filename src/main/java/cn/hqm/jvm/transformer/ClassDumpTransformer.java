package cn.hqm.jvm.transformer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import cn.hqm.jvm.Log;


/**
 * 
 * @author linxuan
 *
 */
public class ClassDumpTransformer implements ClassFileTransformer {
    private final File outputDir;


    public ClassDumpTransformer(File outputDir) {
        this.outputDir = outputDir;
    }


    public File getDumpFile(Class<?> classBeingRedefined) {
        return new File(outputDir, classBeingRedefined.getName() + ".class");
    }


    @Override
    public byte[] transform(final ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        dump2file(classfileBuffer, getDumpFile(classBeingRedefined));
        return null;
    }


    public static void dump2file(byte[] bytes, File file) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
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
