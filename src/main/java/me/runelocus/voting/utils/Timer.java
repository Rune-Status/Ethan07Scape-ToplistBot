package me.runelocus.voting.utils;

public class Timer {

    private long start;
    private long end;
    private long period;

    public Timer(long period) {
        this.start = System.currentTimeMillis();
        this.period = period;
        this.end = start + period;
    }

    public boolean isRunning() {
        return System.currentTimeMillis() < end;
    }

    public long getElapsed() {
        return System.currentTimeMillis() - start;
    }

    public long getRemaining() {
        return isRunning() ? end - System.currentTimeMillis() : 0;
    }

    public void reset() {
        this.end = System.currentTimeMillis() + period;
    }

    public boolean finished() {
        return end <= System.currentTimeMillis();
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public String toElapsedString() {
        return parse(getElapsed());
    }

    public String toRemainingString() {
        return parse(getRemaining());
    }

    public String parse(long millis) {
        long time = millis / 1000;
        String seconds = Integer.toString((int) (time % 60));
        String minutes = Integer.toString((int) ((time % 3600) / 60));
        String hours = Integer.toString((int) (time / 3600));
        String days = Integer.toString((int) (time / (3600 * 24)));

        for (int i = 0; i < 5; i++) {
            if (Integer.parseInt(hours) >= 24) {
                hours = Integer.parseInt(hours) - 24 + "";
            }
        }

        for (int i = 0; i < 2; i++) {
            if (seconds.length() < 2)
                seconds = "0" + seconds;
            if (minutes.length() < 2)
                minutes = "0" + minutes;
            if (hours.length() < 2)
                hours = "0" + hours;
        }

        if (Integer.parseInt(days) > 0)
            return days + ":" + hours + ":" + minutes + ":" + seconds;

        return hours + ":" + minutes + ":" + seconds;
    }
}