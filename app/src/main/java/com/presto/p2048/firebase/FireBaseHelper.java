package com.presto.p2048.firebase;

import android.os.Bundle;

import com.presto.p2048.ContextHolder;

import java.util.Date;

/**
 * Created by haniza on 11/14/2016.
 */

public class FireBaseHelper {

    public static void logEvent(String key, String... values) {
        Bundle params = new Bundle();
        params.putString("time", "" + new Date().getTime());
        for (int i = 0; i < values.length; i++)
            params.putString("" + i, values[i]);
        ContextHolder.getFirebaseAnalytics().logEvent(key, params);

    }
}
