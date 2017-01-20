package com.presto.p2048;


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
import com.presto.p2048.firebase.FireBaseHelper;
import com.presto.p2048.task.FeedbackSenderTask;

public class MainActivity extends FragmentActivity implements GameHelper.GameHelperListener {
    MainView view;
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    public static final String SCORE = "score";
    public static final String HIGH_SCORE = "high score temp";
    public static final String UNDO_SCORE = "undo score";
    public static final String CAN_UNDO = "can undo";
    public static final String UNDO_GRID = "undo";
    public static final String GAME_STATE = "game state";
    public static final String UNDO_GAME_STATE = "undo game state";

    private boolean connectedToGoogleService = false;
    private boolean pendingShowLeaderboard = false;
    private GameHelper mHelper;
    protected int mRequestedClients = GameHelper.CLIENT_GAMES;
    protected boolean mDebugLog = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "************************ oncreate started");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        ContextHolder.setApplicationContext(getApplicationContext());
        ContextHolder.setMainActivity(this);
        ContextHolder.setFirebaseAnalytics(FirebaseAnalytics.getInstance(this));
        FireBaseHelper.logEvent("app_open", "started");
        new FeedbackSenderTask().execute();
        view = new MainView(getBaseContext(), this);

        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(this);
        view.hasSaveState = settings.getBoolean("save_state", false);

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean("hasState")) {
                load();
            }
        }

        setContentView(R.layout.activity_main);
        LinearLayout mainlayout = (LinearLayout) findViewById(R.id.gameContainer);
        mainlayout.addView(view);

        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);
        adView.bringToFront();
        FireBaseHelper.logEvent("app_open", "completed");
        Log.d("MainActivity", "************************ oncreate Ended");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            // Do nothing
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            view.game.move(2);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            view.game.move(0);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            view.game.move(3);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            view.game.move(1);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("hasState", true);
        save();
    }

    protected void onPause() {
        super.onPause();
        save();
    }

    private void save() {
        //updateLeaderBoard();
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        Tile[][] field = view.game.grid.field;
        Tile[][] undoField = view.game.grid.undoField;
        editor.putInt(WIDTH, field.length);
        editor.putInt(HEIGHT, field.length);
        for (int xx = 0; xx < field.length; xx++) {
            for (int yy = 0; yy < field[0].length; yy++) {
                if (field[xx][yy] != null) {
                    editor.putInt(xx + " " + yy, field[xx][yy].getValue());
                } else {
                    editor.putInt(xx + " " + yy, 0);
                }

                if (undoField[xx][yy] != null) {
                    editor.putInt(UNDO_GRID + xx + " " + yy,
                            undoField[xx][yy].getValue());
                } else {
                    editor.putInt(UNDO_GRID + xx + " " + yy, 0);
                }
            }
        }
        editor.putLong(SCORE, view.game.score);
        editor.putLong(HIGH_SCORE, view.game.highScore);
        editor.putLong(UNDO_SCORE, view.game.lastScore);
        editor.putBoolean(CAN_UNDO, view.game.canUndo);
        editor.putInt(GAME_STATE, view.game.gameState);
        editor.putInt(UNDO_GAME_STATE, view.game.lastGameState);
        editor.commit();
    }

    protected void onResume() {
        super.onResume();
        load();
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

    private void load() {
        // Stopping all animations
        view.game.aGrid.cancelAnimations();

        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(this);
        for (int xx = 0; xx < view.game.grid.field.length; xx++) {
            for (int yy = 0; yy < view.game.grid.field[0].length; yy++) {
                int value = settings.getInt(xx + " " + yy, -1);
                if (value > 0) {
                    view.game.grid.field[xx][yy] = new Tile(xx, yy, value);
                } else if (value == 0) {
                    view.game.grid.field[xx][yy] = null;
                }

                int undoValue = settings.getInt(UNDO_GRID + xx + " " + yy, -1);
                if (undoValue > 0) {
                    view.game.grid.undoField[xx][yy] = new Tile(xx, yy,
                            undoValue);
                } else if (value == 0) {
                    view.game.grid.undoField[xx][yy] = null;
                }
            }
        }

        view.game.score = settings.getLong(SCORE, view.game.score);
        view.game.highScore = settings.getLong(HIGH_SCORE, view.game.highScore);
        view.game.lastScore = settings.getLong(UNDO_SCORE, view.game.lastScore);
        view.game.canUndo = settings.getBoolean(CAN_UNDO, view.game.canUndo);
        view.game.gameState = settings.getInt(GAME_STATE, view.game.gameState);
        view.game.lastGameState = settings.getInt(UNDO_GAME_STATE,
                view.game.lastGameState);
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
                view.game.score);

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
                view.game.score);

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

    public void shareScore() {
        FireBaseHelper.logEvent("GameOverDialog", "shareScore");
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        String shareBody = String.format(getApplicationContext().getString(R.string.shared_body_text), "" + view.game.score);
        shareBody += " https://play.google.com/store/apps/details?id=com.presto.p2048";
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "2048 offline");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share via 2048 offline"));
    }
}
