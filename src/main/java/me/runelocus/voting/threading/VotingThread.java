package me.runelocus.voting.threading;

import me.runelocus.voting.core.Core;
import me.runelocus.voting.data.Constants;
import me.runelocus.voting.handlers.RuneLocus;
import me.runelocus.voting.utils.Condition;

public class VotingThread extends Thread {
    private volatile boolean running = false;
    private String threadName;
    private String proxyIP;
    private String proxyPort;
    private RuneLocus runeLocus;
    private int failed;
    private int voted;

    public VotingThread(String threadName) {
        this.threadName = threadName;
        if (Constants.useProxies) {
            grabValidProxy();
            this.runeLocus = new RuneLocus(proxyIP, Integer.parseInt(proxyPort));
        } else {
            this.runeLocus = new RuneLocus(null, 0);
        }
    }

    public synchronized void run() {
        try {
            while (isRunning()) {
                final String callback = runeLocus.handleCaptchaSolving();
                if (callback.equals("GOOD")) {
                    System.err.println("We have successfully voted for the server on: " + getThreadName());
                    voted++;
                    if (Constants.useProxies) {
                        grabValidProxy();
                    }
                    Condition.sleep(2500);
                } else if (callback.equals("BADIP")) {
                    System.err.println("We have already voted on this IP.");
                    failed++;
                    if (Constants.useProxies) {
                        grabValidProxy();
                    }
                    Condition.sleep(2500);
                } else if (callback.equals("FAILED")) {
                    System.err.println("We have failed to solve the captcha");
                    failed++;
                    Condition.sleep(500);
                } else if (callback.equals("ERROR")) {
                    System.err.println("There was a error in the handler!");
                    failed++;
                    Condition.sleep(2500);
                } else {
                    System.err.println("Unknown: " + callback);
                    failed++;
                    Condition.sleep(2500);
                }
                Condition.sleep(250);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void grabValidProxy() {
        final String proxy = Core.getProxyHandler().grabProxy();
        proxyIP = proxy.split(":")[0];
        proxyPort = proxy.split(":")[1];
        System.out.println("Set proxyIP: " + proxyIP);
        System.out.println("Set proxyPort: " + proxyPort);
        if (!Core.getProxyHandler().checkProxy(proxyIP, Integer.parseInt(proxyPort))) {
            Core.getProxyHandler().resetProxy(proxyIP, proxyPort);
            System.out.println("Proxy invalid, let's try another");
            grabValidProxy();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public String getThreadName() {
        return threadName;
    }
}
