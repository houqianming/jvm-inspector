package cn.hqm.jvm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.instrument.Instrumentation;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.jar.JarFile;

import cn.hqm.jvm.Common.FileName;


/**
 * 
 * @author linxuan
 *
 */
public class InspectAgent {
    public static ClassLoadingSpy inspector;
    private static Instrumentation instrumentation;
    private static final Object listenReady = new Object();
    private static volatile BufferedWriter agentWriter = null;
    private static volatile BufferedWriter fileWriter = null;
    private static Thread listenThread = null;
    private static File outputDir;


    public static void premain(String agentArgs, Instrumentation inst) {
        Log.warn("[InspectAgent.premain] begin. agentArgs:" + agentArgs);
        main(agentArgs, inst);
    }


    public static void agentmain(String agentArgs, Instrumentation inst) {
        Log.warn("[InspectAgent.agentmain] begin. agentArgs:" + agentArgs);
        main(agentArgs, inst);
    }


    public static void main(String agentArgs, Instrumentation inst) {
        Log.warn("[InspectAgent] Instrumentation:" + inst);
        Log.warn("[InspectAgent] isRedefineClassesSupported:" + inst.isRedefineClassesSupported());
        final AgentArgs aa = new AgentArgs(agentArgs);

        outputDir = aa.outputDir;
        Logger logger = Logger.getLogger(aa.logFile);
        Class<?>[] allLoadedClasses = inst.getAllLoadedClasses();
        if (inspector == null) {
            inspector = new ClassLoadingSpy(aa.outputDir, logger, allLoadedClasses);
        }
        inspector.setEnableHyperlink(aa.enableHyperlink);

        Class<?>[] bootstrapClasses = inst.getInitiatedClasses(null);
        logger.info("[InspectAgent] total loaded classes: " + allLoadedClasses.length);
        logger.info("[InspectAgent] total bootstrap loaded class: " + bootstrapClasses.length);
        for (Class<?> c : allLoadedClasses) {
            if (c == null) {
                logger.info("c == null");
                continue;
            }
            logger.info("[InspectAgent.main] " + c + " loaded by [" + c.getClassLoader() + "] from:" + getLocation(c));
        }
        instrumentation = inst;
        try {
            String thisjar = InspectAgent.class.getProtectionDomain().getCodeSource().getLocation().getFile();
            instrumentation.appendToSystemClassLoaderSearch(new JarFile(thisjar));
            Log.warn("appendToSystemClassLoaderSearch:" + thisjar);
        }
        catch (IOException e1) {
            Log.warn("appendToSystemClassLoaderSearch failed", e1);
        }

        if (listenThread != null) {
            //上一次attach遗留下来的监听线程
            listenThread.interrupt();
            try {
                listenThread.join();
            }
            catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            listenThread = null;
        }
        listenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                listen(aa.port);
            }
        }, "InspectAgentServer");
        listenThread.setDaemon(true);
        synchronized (listenReady) {
            listenThread.start();
            try {
                listenReady.wait();
            }
            catch (InterruptedException e) {
            }
        }
    }


    private static String getLocation(Class<?> c) {
        if (c.getProtectionDomain() == null) {
            return "ProtectionDomain is Null";
        }
        if (c.getProtectionDomain().getCodeSource() == null) {
            return "CodeSource is Null";
        }
        if (c.getProtectionDomain().getCodeSource().getLocation() == null) {
            return "CodeSource's locations is Null";
        }
        return c.getProtectionDomain().getCodeSource().getLocation().toString();
    }


    private static void listen(int port) {
        ServerSocket serverSocket = null;
        Socket socket = null;
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(port));
            synchronized (listenReady) {
                listenReady.notify();
            }
            socket = serverSocket.accept();
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            agentWriter = bw;
            String line;
            while ((line = br.readLine()) != null) {
                if (Protocol.isQuit(line)) {
                    deattach();
                    return;
                }
                String res = RequestProcessor.process(outputDir, line, bw, instrumentation);
                echo(res, bw);
            }
        }
        catch (IOException e) {
            Log.warn("", e);
        }
        finally {
            if (bw != null) {
                try {
                    bw.close();
                }
                catch (IOException e) {
                }
                agentWriter = null;
            }
            if (br != null) {
                try {
                    br.close();
                }
                catch (IOException e) {
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                }
                catch (IOException e) {
                }
            }
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                }
                catch (IOException e) {
                }
            }
            //保证bind端口失败时，agentmain仍然能返回
            synchronized (listenReady) {
                listenReady.notify();
            }
        }
    }


    public static void deattach() throws IOException {
        if (inspector != null) {
            Log.warn("[deattach] stopFlushThread");
            if (instrumentation != null) {
                //instrumentation.removeTransformer(inspector);
            }
            inspector = null;
        }
        else {
            Log.warn("[deattach] inspector is null");
        }
    }


    private static void echo(String line, BufferedWriter bw) throws IOException {
        bw.write(line);
        bw.newLine();
        bw.write(Protocol.OutputEndFlag);
        bw.newLine();
        bw.flush();
    }


    public static void echo(String msg) {
        BufferedWriter target = fileWriter != null ? fileWriter : agentWriter;
        if (target != null) {
            try {
                target.write(msg);
                target.newLine();
                target.flush();
            }
            catch (IOException e) {
                Log.warn("echo failed. msg:" + msg, e);
            }
        }
    }


    public static File openEchoToFile(File dir) {
        if (fileWriter == null) {
            File file = Common.getFile(dir, filename);
            try {
                fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            }
            catch (FileNotFoundException e) {
                e.printStackTrace(new PrintWriter(agentWriter));
            }
            return file;
        }
        else {
            throw new IllegalStateException("fileWriter not null");
        }
    }


    public static void closeEchoToFile() {
        if (fileWriter != null) {
            try {
                fileWriter.close();
            }
            catch (IOException e) {
                if (agentWriter != null) {
                    e.printStackTrace(new PrintWriter(agentWriter));
                }
            }
            fileWriter = null;
        }
        else {
            throw new IllegalStateException("fileWriter is null");
        }
    }

    private static final FileName filename = new FileName() {
        @Override
        public String getFileName(int n) {
            return JVMUtils.pid + ".details." + n + ".txt";
        }
    };


    public static void main(String[] args) {
        System.out.println("=== main ===");
    }
}
