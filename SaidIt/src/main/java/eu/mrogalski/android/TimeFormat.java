package eu.mrogalski.android;

import android.content.res.Resources;

import eu.mrogalski.saidit.R;

public class TimeFormat {
    public static void naturalLanguage(Resources resources, float secondsF, Result outResult) {
        int seconds = (int) Math.floor(secondsF);
        int minutes = seconds / 60;
        seconds %= 60;

        String out = "";

        if(minutes != 0) {
            outResult.count = minutes;
            out += resources.getQuantityString(R.plurals.minute, minutes, minutes);

            if(seconds != 0) {
                out += resources.getString(R.string.minute_second_join);
                out += resources.getQuantityString(R.plurals.second, seconds, seconds);
            }
        } else {
            outResult.count = seconds;
            out += resources.getQuantityString(R.plurals.second, seconds, seconds);
        }

        outResult.text = out + ".";
    }

    public static String shortTimer(float seconds) {
        return String.format("%d:%02d", (int) Math.floor(seconds / 60), (int) Math.floor(seconds % 60));
    }

    public static class Result {
        public String text;
        public int count;
    }
}
