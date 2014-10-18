package cn.hqm.jvm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import jline.ArgumentCompletor;
import jline.Completor;
import jline.ConsoleReader;
import jline.NullCompletor;
import jline.SimpleCompletor;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;


/**
 * 
 * @author linxuan
 *
 */
public class InspectAttacher {
    private static final String prompt = "inspect>";
    private static VirtualMachine vm;
    private static final String[] commands = { "get", "set", "quit", "classesdump", "dumpthreads", "trace" };


    //private static Thread listenConsole;

    public static void main(String[] args) throws AttachNotSupportedException, IOException, AgentLoadException,
            AgentInitializationException {
        if (args.length < 1) {
            System.out
                .println("Usage: java -Xbootclasspath/a:$JAVA_HOME/lib/tools.jar -jar tae-jvm-inspect.jar 15809 [agentArgs]");
            return;
        }

        String agentArgs = args.length > 1 ? args[1] : null;
        attach(args[0], agentArgs);
        AgentArgs aa = new AgentArgs(agentArgs);
        Socket socket = null;
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            socket = new Socket();
            socket.setReuseAddress(true);
            socket.connect(new InetSocketAddress(aa.port));
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            listenConsole(bw);
            String distline;
            while ((distline = br.readLine()) != null) {
                //Log.warn("distline:" + distline);
                if ("quit".equalsIgnoreCase(distline) || "exit".equalsIgnoreCase(distline)
                        || "bye".equalsIgnoreCase(distline)) {
                    return;
                }
                if (Protocol.OutputEndFlag.equals(distline)) {
                    System.out.print(prompt);
                }
                else {
                    System.out.println(distline);
                }
            }
        }
        catch (Exception e) {
            Log.warn("", e);
            return;
        }
        finally {
            if (bw != null) {
                try {
                    bw.close();
                }
                catch (IOException e) {
                }
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
        }
    }


    private static void listenConsole(final BufferedWriter distWriter) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.print(prompt);
                try {
                    //BufferedReader sysReader = new BufferedReader(new InputStreamReader(System.in));
                    ConsoleReader consoleReader = new ConsoleReader();
                    List<Completor> completors = new ArrayList<Completor>();
                    completors.add(new SimpleCompletor(commands));
                    //completors.add(new FileNameCompletor());
                    //completors.add(new ClassNameCompletor());
                    completors.add(new NullCompletor());
                    consoleReader.addCompletor(new ArgumentCompletor(completors));
                    String sysline; //不使用ConsoleReader设置prompt，否者返回信息前也有prompt了
                    while ((sysline = consoleReader.readLine()) != null) {
                        //System.out.println("inspect>:" + sysline);
                        distWriter.write(sysline);
                        distWriter.newLine();
                        distWriter.flush();
                        if (Protocol.isQuit(sysline)) {
                            return; //退出监听console输入
                        }
                    }
                }
                catch (IOException e) {
                    Log.warn("", e);
                }
            }
        }, "listenConsole").start();
    }


    public static void attach(String pid, String agentArgs) throws AttachNotSupportedException, IOException,
            AgentLoadException, AgentInitializationException {
        Log.warn("attach to pid:" + pid);
        vm = VirtualMachine.attach(pid);
        Log.warn("successed attach to pid:" + pid + ",vm:" + vm);
        File path = new File(InspectAttacher.class.getProtectionDomain().getCodeSource().getLocation().getFile());
        Log.warn("loadAgent from:" + path);
        vm.loadAgent(path.getAbsolutePath(), agentArgs);
        Log.warn("successed loadAgent from:" + path);
        vm.detach();
    }


    public static void attachself() throws AttachNotSupportedException, IOException, AgentLoadException,
            AgentInitializationException {
        String pid = getPid();
        attach(pid, null);
    }


    private static String getPid() {
        //获取进程的PID
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        String name = runtime.getName(); // format: "pid@hostname" 
        Log.warn(name);
        try {
            return name.substring(0, name.indexOf('@'));
        }
        catch (Exception e) {
            Log.warn("", e);
            return "-1";
        }
    }

}
