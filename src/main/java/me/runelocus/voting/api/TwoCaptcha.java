package me.runelocus.voting.api;

import me.runelocus.voting.utils.Condition;

public class TwoCaptcha {
    private String siteKey;
    private String googleKey;
    private String action;
    private String url;
    private int timeCount;
    private String response;

    public TwoCaptcha(String siteKey, String googleKey, String action, String url) {
        this.siteKey = siteKey;
        this.googleKey = googleKey;
        this.action = action;
        this.url = url;
    }

    public String getCaptchaID() {
        try {
            final String parameters = "key=" + siteKey
                    + "&method=userrecaptcha"
                    + "&version=v3"
                    + "&action=" + action
                    + "&min_score=0.3"
                    + "&googlekey=" + googleKey
                    + "&pageurl=" + url;
            final HttpWrapper httpWrapper = new HttpWrapper("https://2captcha.com/in.php?" + parameters, null, 0);
            return httpWrapper.get().replaceAll("OK\\|", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "ERROR";
    }

    public String solve() {
        try {
            final String captchaId = getCaptchaID();
            System.out.println("Captcha ID: " + captchaId);
            final String parameters = "key=" + siteKey
                    + "&action=get"
                    + "&id=" + captchaId;
            final HttpWrapper httpWrapper = new HttpWrapper("http://2captcha.com/res.php?" + parameters, null, 0);
            String response;
            do {
                response = httpWrapper.get();
                setResponse(response);
                Condition.sleep(1000);
                timeCount++;
                System.out.println("Waiting on captcha to be solved.");
            } while (getResponse().contains("NOT_READY"));
            System.out.println("Captcha took " + timeCount + " seconds to solve.");
            return response.replaceAll("OK\\|", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "ERROR";
    }

    public String getSiteKey() {
        return siteKey;
    }

    public String getGoogleKey() {
        return googleKey;
    }

    public String getAction() {
        return action;
    }

    public String getUrl() {
        return url;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
