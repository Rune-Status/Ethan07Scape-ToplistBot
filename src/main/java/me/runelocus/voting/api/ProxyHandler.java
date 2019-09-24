package me.runelocus.voting.api;


import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ProxyHandler {
    private BlockingQueue<String> proxyQueue;
    private String path;

    public ProxyHandler(String path) {
        this.path = path;
        loadProxies(path);
    }

    private void loadProxies(String path) {
        try {
            final List<String> tempProxies = new ArrayList<>();
            FileInputStream inputStream = null;
            Scanner sc = null;
            try {
                inputStream = new FileInputStream(path);
                sc = new Scanner(inputStream, String.valueOf(StandardCharsets.UTF_8));
                while (sc.hasNextLine()) {
                    final String line = sc.nextLine();
                    tempProxies.add(line);
                }
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (sc != null) {
                    sc.close();
                }
            }
            proxyQueue = new ArrayBlockingQueue<>(tempProxies.size());
            for (String p : tempProxies) {
                proxyQueue.put(p);
                System.out.println("Added proxy: " + p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean checkProxy(String host, int port) {
        try {
            final CloseableHttpClient httpclient = HttpClients.createDefault();
            try {
                final HttpHost target = new HttpHost("runelocus.com", 443, "https");
                final HttpHost proxy = new HttpHost(host, port, "http");

                final RequestConfig config = RequestConfig.custom()
                        .setProxy(proxy)
                        .setConnectTimeout(10000)
                        .build();
                final HttpGet request = new HttpGet("/");
                request.setConfig(config);

                System.out.println("Sending request " + request.getRequestLine() + " to " + target + " with " + proxy);

                final CloseableHttpResponse response = httpclient.execute(target, request);
                try {
                    System.out.println("----------------------------------------");
                    System.out.println(response.getStatusLine());
                    if (response.getStatusLine().toString().contains("OK")) {
                        return true;
                    }
                    EntityUtils.consume(response.getEntity());
                } finally {
                    response.close();
                }
            } finally {
                httpclient.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String grabProxy() {
        try {
            return proxyQueue.take();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "NULL";
    }

    public void resetProxy(String oldProxy, String oldPort) {
        final String proxy = oldProxy + ":" + oldPort;
        writeOldProxy(oldProxy, oldPort);
        removeLineFromFile(path, proxy);
    }

    private void writeOldProxy(String oldProxy, String oldPort) {
        try {
            final String proxy = System.lineSeparator() + oldProxy + ":" + oldPort;
            final File proxies = new File(System.getProperty("user.home") + "\\Desktop\\Used Proxies.txt");
            Files.write(Paths.get(proxies.getAbsolutePath()), proxy.getBytes(), StandardOpenOption.APPEND);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeLineFromFile(String file, String lineToRemove) {

        try {

            final File inFile = new File(file);

            if (!inFile.isFile()) {
                System.out.println("Parameter is not an existing file");
                return;
            }

            final File tempFile = new File(inFile.getAbsolutePath() + ".tmp");

            final BufferedReader br = new BufferedReader(new FileReader(file));
            final PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

            String line = null;

            while ((line = br.readLine()) != null) {

                if (!line.trim().equals(lineToRemove)) {

                    pw.println(line);
                    pw.flush();
                }
            }
            pw.close();
            br.close();


            if (!inFile.delete()) {
                System.out.println("Could not delete file");
                return;
            }

            if (!tempFile.renameTo(inFile))
                System.out.println("Could not rename file");

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
