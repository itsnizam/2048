package com.presto.p2048.util;

import android.content.Intent;

import com.presto.p2048.ContextHolder;
import com.presto.p2048.R;
import com.presto.p2048.firebase.TelemetryHelper;

/**
 * Created by haniza on 11/14/2016.
 */

public class Util {
    static public void shareScore(long score, long highScore) {
        TelemetryHelper.logEvent("GameOverDialog", "shareApp");
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        String shareBody = String.format(ContextHolder.getApplicationContext().getString(R.string.shared_body_text), "" + score);
        shareBody += " https://play.google.com/store/apps/details?id=com.presto.p2048";
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "2048 offline");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        ContextHolder.getApplicationContext().startActivity(Intent.createChooser(sharingIntent, "Share via 2048 offline"));
    }
}
