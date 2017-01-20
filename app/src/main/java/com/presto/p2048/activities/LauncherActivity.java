package com.presto.p2048.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import com.presto.p2048.R;
import com.presto.p2048.firebase.TelemetryHelper;

import java.math.BigInteger;

public class LauncherActivity extends FragmentActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        TelemetryHelper.logFirstTimeLaunch();
        setupViews();
    }

    private void setupViews() {
        addButtonListener(R.id.newButt);
        addButtonListener(R.id.savedButton);
        addButtonListener(R.id.howToButton);
    }

    private void addButtonListener(int buttId) {
        ((Button) findViewById(buttId)).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.newButt: {
                startActivity(MainActivity.class);
                break;
            }
            case R.id.savedButton: {
                startActivity(SavedGamesActivity.class);
                break;
            }
            case R.id.howToButton: {
                startActivity(HowToActivity.class);
                break;
            }
        }
    }

    private void startActivity(Class activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
    }

    public void onBackPressed() {

    }
}

