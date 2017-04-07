package com.cpacm.musicbtn;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;

/**
 * @author cpacm
 * @date 2017/4/1
 * @desciption helper
 */

public class MusicUtils {


    /**
     * get time from int type to string type
     *
     * @param time
     * @return
     */
    public static StringBuilder getTime(int time) {
        int cache = time / 1000;
        int second = cache % 60;
        cache = cache / 60;
        int minute = cache % 60;
        int hour = cache / 60;
        StringBuilder timeStamp = new StringBuilder();
        if (hour > 0) {
            timeStamp.append(hour);
            timeStamp.append(":");
        }
        if (minute < 10) {
            timeStamp.append("0");
        }
        timeStamp.append(minute);
        timeStamp.append(":");

        if (second < 10) {
            timeStamp.append("0");
        }
        timeStamp.append(second);
        return timeStamp;
    }
}
