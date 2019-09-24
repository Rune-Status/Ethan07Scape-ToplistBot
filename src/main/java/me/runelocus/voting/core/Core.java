package me.runelocus.voting.core;

import me.runelocus.voting.api.ProxyHandler;
import me.runelocus.voting.data.Constants;
import me.runelocus.voting.threading.VotingThread;

public class Core {
    private static ProxyHandler proxyHandler = new ProxyHandler(System.getProperty("user.home") + "\\Desktop\\proxies.txt");

    public static void main(String[] args) {
        System.out.println("Attempted to start " + Constants.threadCount + " voting threads.");
        for (int i = 0; i < Constants.threadCount; i++) {
            final VotingThread thread = new VotingThread("Thread-" + i);
            //ui.getThreadList().add(thread);
            thread.start();
            thread.setRunning(true);
        }
        System.out.println("Voting threads has been started.");
    }

    public static ProxyHandler getProxyHandler() {
        return proxyHandler;
    }
}
