package cn.hqm.jvm;

import java.io.File;


/**
 * 
 * @author linxuan
 *
 */
public class AgentArgs {
    public final File outputDir;
    public final File logFile;
    public final long flushIntervalSeconds;
    public final boolean enableHyperlink;
    public final int port;


    public AgentArgs(String agentArgs) {
        String logFile = "jvm.inspect";
        long flushIntervalSeconds = 3 * 60;
        boolean enableHyperlink = false;
        int port = 54321;
        String outputdir = System.getProperty("user.dir");
        outputdir = new File(outputdir).canWrite() ? outputdir : System.getProperty("java.io.tmpdir");
        if (agentArgs != null && agentArgs.length() != 0) {
            String[] options = agentArgs.split(",");
            for (String option : options) {
                option = option.trim();
                if (option.startsWith("outputfile=")) {
                    logFile = option.substring("outputfile=".length());
                }
                else if (option.startsWith("flushIntervalSecond=")) {
                    flushIntervalSeconds = Long.parseLong(option.substring("flushIntervalSecond=".length()));
                }
                else if (option.startsWith("enableHyperlink=")) {
                    enableHyperlink = "true".equals(option.substring("enableHyperlink=".length()));
                }
                else if (option.startsWith("listenPort=")) {
                    port = Integer.parseInt(option.substring("listenPort=".length()));
                }
                else if (option.startsWith("outputdir=")) {
                    outputdir = option.substring("outputdir=".length());
                }
                //System.out.println("[SimpleAgent.premain] agent-option:"+option);
            }
        }
        this.outputDir = new File(outputdir);
        this.logFile = new File(outputDir, logFile);
        this.flushIntervalSeconds = flushIntervalSeconds;
        this.enableHyperlink = enableHyperlink;
        this.port = port;
    }


    public static void main(String[] args) {
        System.out.println(new File(System.getProperty("user.dir")).canWrite());
        System.out.println(new File("x").getAbsoluteFile());
    }
}
