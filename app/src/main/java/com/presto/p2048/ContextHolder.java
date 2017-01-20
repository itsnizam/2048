package com.presto.p2048;

import android.app.Activity;
import android.content.Context;

import com.google.firebase.analytics.FirebaseAnalytics;

public class ContextHolder {

    static Context mApplicationContext;
    static Activity mMainActivity;
    static FirebaseAnalytics mFirebaseAnalytics;

    public static void setApplicationContext(Context applicationContext) {
        mApplicationContext = applicationContext;
    }

    public static Context getApplicationContext() {
        return mApplicationContext;
    }


    public static void setMainActivity(Activity activity) {
        mMainActivity = activity;
    }

    public static void setFirebaseAnalytics(FirebaseAnalytics firebaseAnalytics) {
        mFirebaseAnalytics = firebaseAnalytics;
    }

    public static FirebaseAnalytics getFirebaseAnalytics() {
        return mFirebaseAnalytics;
    }

    public static Context getMainActivity() {
        return mMainActivity;
    }
}
