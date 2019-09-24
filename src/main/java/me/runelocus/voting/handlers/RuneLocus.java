package me.runelocus.voting.handlers;

import me.runelocus.voting.api.HttpWrapper;
import me.runelocus.voting.api.TwoCaptcha;
import me.runelocus.voting.data.Constants;
import me.runelocus.voting.utils.Random;


public class RuneLocus {
    private String host;
    private int port;

    public RuneLocus(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private String grabTwoCaptchaResponse() {
        final TwoCaptcha captcha = new TwoCaptcha(Constants.captchaKey, Constants.googleKey, "vote", Constants.voteLink);
        return captcha.solve();
    }

    public String handleCaptchaSolving() {
        try {
            final HttpWrapper httpWrapper = new HttpWrapper("https://www.runelocus.com/vote-captcha.php?key=BrexdSrDFDIesl9", host, port);
            final HttpWrapper getParams = new HttpWrapper(Constants.voteLink, host, port);
            if (httpWrapper == null)
                return "ERROR";
            if (httpWrapper.collectCaptcha()) {
                System.out.println("We have the cookies required to continue.");
                httpWrapper.setUrl(Constants.voteLink);
                final String response = grabTwoCaptchaResponse();
                if (response.equals("failed response(yet to receive)"))
                    return "ERROR/CAPTCHA";
                final String serverId = Random.substringBetween(Constants.voteLink, "vote-", "/");
                final String paramsPage = getParams.get();
                final String pageSource = httpWrapper.postCaptcha(response, getParams.getFormParams(paramsPage, response, serverId));
                if (hasFailedCaptcha(pageSource)) {
                    return "FAILED";
                } else if (hasAlreadyVoted(pageSource)) {
                    return "BADIP";
                } else if (hasVotedSuccessfully(pageSource)) {
                    return "GOOD";
                } else {
                    System.err.println("unknown response");
                    System.out.println(pageSource);
                    return "UNKNOWN";
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "ERROR";
    }

    public boolean hasAlreadyVoted(String pageSource) {
        return pageSource.contains("already voted for this server in the past");
    }

    public boolean hasFailedCaptcha(String pageSource) {
        return pageSource.contains("did not pass the anti-robot");
    }

    public boolean hasVotedSuccessfully(String pageSource) {
        return pageSource.contains("have recorded your");
    }

}
