package me.runelocus.voting.api;

import me.runelocus.voting.data.Constants;
import me.runelocus.voting.utils.Random;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HttpWrapper {
    private final String USER_AGENT = "Mozilla/5.0";
    private final CookieStore cookieStore = new BasicCookieStore();
    private final HttpClient httpClient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
    private String url;
    private String host;
    private int port;

    public HttpWrapper(String url, String host, int port) {
        this.url = url;
        this.host = host;
        this.port = port;
    }

    public String get() throws Exception {
        final HttpGet request = new HttpGet(url);
        if (host != null && host.length() > 0) {
            final HttpHost proxy = new HttpHost(host, port, "http");

            final RequestConfig config = RequestConfig.custom()
                    .setProxy(proxy)
                    .setConnectTimeout(10000)
                    .build();
            request.setConfig(config);
        }
        request.setHeader("User-Agent", USER_AGENT);
        request.setHeader("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        request.setHeader("Accept-Language", "en-US,en;q=0.5");

        final HttpResponse response = httpClient.execute(request);
        int responseCode = response.getStatusLine().getStatusCode();

        final BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        final StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        return result.toString();
    }

    public boolean collectCaptcha() throws Exception {
        final HttpGet request = new HttpGet(url);
        if (host != null && host.length() > 0) {
            final HttpHost proxy = new HttpHost(host, port, "http");

            final RequestConfig config = RequestConfig.custom()
                    .setProxy(proxy)
                    .setConnectTimeout(10000)
                    .build();
            request.setConfig(config);
        }
        request.setHeader("User-Agent", USER_AGENT);
        request.setHeader("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        request.setHeader("Accept-Language", "en-US,en;q=0.5");
        request.setHeader("Accept-Encoding", "gzip, deflate, br");
        request.setHeader("Connection", "keep-alive");
        request.setHeader("Host", "www.runelocus.com");
        request.setHeader("TE", "Trailers");
        request.setHeader("Upgrade-Insecure-Requests", "1");

        /*We're just collecting the cookies now, saving the image is optional.*/
        final HttpResponse response = httpClient.execute(request);
        final int responseCode = response.getStatusLine().getStatusCode();
        final HttpEntity entity = response.getEntity();

        if (Constants.saveCaptchas) {
            final BufferedInputStream bis = new BufferedInputStream(entity.getContent());
            final String filePath = "C:\\Users\\Ethan\\Desktop\\Drivers\\sample.png";
            final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
            int inByte;
            while ((inByte = bis.read()) != -1) bos.write(inByte);
            bis.close();
            bos.close();
        }
        return getCookie("PHPSESSID").length() > 0 || !getCookie("PHPSESSID").isEmpty();
    }

    public String postCaptcha(String captchaResponse, List<NameValuePair> nameValuePairs)
            throws Exception {

        final HttpPost post = new HttpPost(url);
        if (host != null && host.length() > 0) {
            final HttpHost proxy = new HttpHost(host, port, "http");

            final RequestConfig config = RequestConfig.custom()
                    .setProxy(proxy)
                    .setConnectTimeout(10000)
                    .build();
            post.setConfig(config);
        }
        post.setHeader("Host", "www.runelocus.com");
        post.setHeader("User-Agent", USER_AGENT);
        post.setHeader("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        post.setHeader("Accept-Language", "en-US,en;q=0.5");
        post.setHeader("Accept-Encoding", "gzip, deflate, br");
        post.setHeader("Cookie", "mja=MQ; mje=MQ; mq=Mg; mg=Mg; " + getCookie("__cfduid") + " _ga=GA1.2.1213725184.1569270569; _gid=GA1.2.536524139.1569270569; _fbp=fb.1.1569270570003.1373345096; " + getCookie("PHPSESSID") + " _gat=1");
        post.setHeader("Connection", "keep-alive");
        post.setHeader("Referer", Constants.voteLink);
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        post.setHeader("TE", "Trailers");
        post.setHeader("Upgrade-Insecure-Requests", "1");
        final String serverId = Random.substringBetween(Constants.voteLink, "vote-", "/");

        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        final HttpResponse response = httpClient.execute(post);

        final int responseCode = response.getStatusLine().getStatusCode();

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        return result.toString();

    }

    public List<NameValuePair> getFormParams(String html, String response, String serverID) throws UnsupportedEncodingException {

        System.out.println("Extracting form's data...");

        Document doc = Jsoup.parse(html);

        Element loginForm = doc.getElementsByAttributeValue("action", "/top-rsps-list/vote-" + serverID + "/").get(0);
        Elements inputElements = loginForm.getElementsByTag("input");

        List<NameValuePair> paramList = new ArrayList<NameValuePair>();

        for (Element inputElement : inputElements) {
            String key = inputElement.attr("name");
            String value = inputElement.attr("value");
            if (key.equals("recaptcha_response")) {
                value = response;
            } else if (key.equals("sw")) {
                value = "sw";
            } else if (key.equals("jad")) {
                value = String.valueOf(Random.nextInt(20, 200));
            } else if (key.equals("brl")) {
                value = "455";
            } else if (key.equals("hloud")) {
                value = serverID;
            } else if (key.equals("wbdrvr")) {
                value = "2";
            }
            paramList.add(new BasicNameValuePair(key, value));
        }
        paramList.add(new BasicNameValuePair("count", "0"));
        paramList.add(new BasicNameValuePair("countanswer", "1"));

        return paramList;
    }

    public void printCookies() {
        final List<Cookie> cookies = cookieStore.getCookies();
        for (Cookie c : cookies) {
            System.out.println(c.toString());
        }
    }

    public String getCookie(String cookie) {
        final List<Cookie> cookies = cookieStore.getCookies();
        for (Cookie c : cookies) {
            if (c.getName().equals(cookie)) {
                return c.getName() + "=" + c.getValue() + ";";
            }
        }
        System.out.println("COOKIE NO FOUND!");
        return "ERROR";
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
