package cn.hqm.jvm;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.hqm.jvm.Common.FileName;


/**
 * 
 * @author linxuan
 *
 */
public class ThreadSpy {
    public static File dumpall(File dir) throws IOException {
        File file = Common.getFile(dir, filename);
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(file));
            JVMUtils.jstack(out);
            return file;
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

    private static FileName filename = new FileName() {
        @Override
        public String getFileName(int n) {
            return JVMUtils.pid + ".threads." + n + ".txt";
        }
    };
}
