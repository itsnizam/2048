package com.presto.p2048;

import android.app.Activity;
import android.content.Context;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.presto.p2048.activities.MainActivity;

public class ContextHolder {

    static Context mApplicationContext;
    static MainActivity mMainActivity;
    static FirebaseAnalytics mFirebaseAnalytics;

    public static void setApplicationContext(Context applicationContext) {
        mApplicationContext = applicationContext;
    }

    public static Context getApplicationContext() {
        return mApplicationContext;
    }


    public static void setMainActivity(MainActivity activity) {
        mMainActivity = activity;
    }

    public static void setFirebaseAnalytics(FirebaseAnalytics firebaseAnalytics) {
        mFirebaseAnalytics = firebaseAnalytics;
    }

    public static FirebaseAnalytics getFirebaseAnalytics() {
        return mFirebaseAnalytics;
    }

    public static MainActivity getMainActivity() {
        return mMainActivity;
    }
}
