package com.presto.p2048.activities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.GameHelper;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.presto.p2048.ContextHolder;
import com.presto.p2048.MainView;
import com.presto.p2048.R;
import com.presto.p2048.firebase.TelemetryHelper;
import com.presto.p2048.task.FeedbackSenderTask;
import com.presto.p2048.util.SavedGameHelper;

public class MainActivity extends FragmentActivity implements GameHelper.GameHelperListener {
    public static final String SHARED_PREF_ID = "shared_pref_id";
    private static final String PREF_SAVE_STATE = "save_state";

    protected int mRequestedClients = GameHelper.CLIENT_GAMES;
    protected boolean mDebugLog = true;
    private boolean connectedToGoogleService = false;
    private boolean pendingShowLeaderboard = false;
    private GameHelper mHelper;
    private SavedGameHelper mSavedgameHelper;
    private MainView mView;
    private String currentSharedPrefName;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "************************ oncreate started");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        ContextHolder.setApplicationContext(getApplicationContext());
        ContextHolder.setMainActivity(this);
        ContextHolder.setFirebaseAnalytics(FirebaseAnalytics.getInstance(this));
        TelemetryHelper.logEvent("app_open", "started");
        new FeedbackSenderTask().execute();

        setContentView(R.layout.activity_main);
        LinearLayout mainlayout = (LinearLayout) findViewById(R.id.gameContainer);

        String passedSharedPrefName = getIntent().getStringExtra(SHARED_PREF_ID);
        loadGame(savedInstanceState, passedSharedPrefName);
        mainlayout.addView(mView);

        initAdView();
        TelemetryHelper.logEvent("app_open", "completed");
        Log.d("MainActivity", "************************ oncreate Ended");
    }

    private void loadGame(Bundle savedInstanceState, String passedSharedPrefName) {
        currentSharedPrefName = passedSharedPrefName == null ? SavedGameHelper.DEFAULT_SHARED_PREF_NAME : passedSharedPrefName;
        mView = new MainView(getBaseContext(), this);
        mSavedgameHelper = new SavedGameHelper(this);

        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(this);
        mView.hasSaveState = settings.getBoolean(PREF_SAVE_STATE, false);

        if (passedSharedPrefName != null) {
            mSavedgameHelper.load(mView, currentSharedPrefName);
            mSavedgameHelper.save(mView);
        } else if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean("hasState")) {
                mSavedgameHelper.load(mView, currentSharedPrefName);
            }
        }
    }

    private void initAdView() {
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);
        adView.bringToFront();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            // Do nothing
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            mView.game.move(2);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            mView.game.move(0);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            mView.game.move(3);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            mView.game.move(1);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("hasState", true);
        mSavedgameHelper.save(mView);
    }

    protected void onPause() {
        super.onPause();
        mSavedgameHelper.save(mView);
    }


    protected void onResume() {
        super.onResume();
        mSavedgameHelper.load(mView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mHelper != null)
            mHelper.onStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mHelper != null)
            mHelper.onStop();
    }


    public void showLeaderBoard() {
        if (mHelper == null) {
            getGameHelper();
            mHelper.setup(this);
        }

        if (!connectedToGoogleService) {
            pendingShowLeaderboard = true;
            beginUserInitiatedSignIn();
            return;
        }

        pendingShowLeaderboard = false;
        Games.Leaderboards.submitScore(getApiClient(),
                getString(R.string.number_guesses_leaderboard),
                mView.game.score);

        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(
                getApiClient(), getString(R.string.number_guesses_leaderboard)),
                2);

    }

    @Override
    protected void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
        mHelper.onActivityResult(request, response, data);
    }

    public GameHelper getGameHelper() {
        if (mHelper == null) {
            mHelper = new GameHelper(this, mRequestedClients);
            mHelper.enableDebugLog(mDebugLog);
        }
        return mHelper;
    }

    protected void beginUserInitiatedSignIn() {
        mHelper.beginUserInitiatedSignIn();
    }

    protected GoogleApiClient getApiClient() {
        return mHelper.getApiClient();
    }

    public void updateLeaderBoard() {
        //beginUserInitiatedSignIn();
        if (!connectedToGoogleService)
            return;

        Games.Leaderboards.submitScore(getApiClient(),
                getString(R.string.number_guesses_leaderboard),
                mView.game.score);

    }

    @Override
    public void onSignInFailed() {
        // TODO Auto-generated method stub
        connectedToGoogleService = false;

    }

    @Override
    public void onSignInSucceeded() {
        connectedToGoogleService = true;
        if (pendingShowLeaderboard)
            showLeaderBoard();

    }

    public void shareApp() {
        TelemetryHelper.logEvent("GameOverDialog", "shareApp");
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        String shareBody = getApplicationContext().getString(R.string.shared_app_text);
        shareBody += " ";
        shareBody += getApplicationContext().getString(R.string.shared_link);

        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "2048 offline");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share via 2048 offline"));
    }
}
