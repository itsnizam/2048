package com.presto.p2048.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.presto.p2048.MainGame;
import com.presto.p2048.MainView;
import com.presto.p2048.Tile;
import com.presto.p2048.modal.SavedGameEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SavedGameHelper {
    public static final String DEFAULT_SHARED_PREF_NAME = "current_game_shared_pref";
    public static final String PREF_TOTAL_SAVED_COUNT = "total_saved_count_pref";
    public static final String PREF_NEXT_VALUE = "last_value_pref";

    //saved_game_x is key where x is value
    //prefName,handle name is value. if it starts with # then its delted
    public static final String PREF_SAVED_GAME_PREFIX = "saved_game";

    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    public static final String SCORE = "score";
    public static final String HIGH_SCORE = "high score temp";
    public static final String UNDO_SCORE = "undo score";
    public static final String CAN_UNDO = "can undo";
    public static final String UNDO_GRID = "undo";
    public static final String GAME_STATE = "game state";
    public static final String UNDO_GAME_STATE = "undo game state";

    private Activity hostActivity;

    public SavedGameHelper(Activity hostActivity) {
        this.hostActivity = hostActivity;
    }

    public void saveGame(MainGame game, String handleName, String prefName) {
        save(game, prefName, true);
        makeEntry(handleName, prefName);
    }

    public List<SavedGameEntity> getSavedGames() {
        List<SavedGameEntity> savedGameList = new ArrayList<>();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(hostActivity);
        long totalSavedCount = settings.getLong(PREF_TOTAL_SAVED_COUNT, 0);
        long nextValue = settings.getLong(PREF_NEXT_VALUE, 0);
        for (int i = 0; i < nextValue; i++) {
            String key = PREF_SAVED_GAME_PREFIX + i;
            String entry = settings.getString(key, "");

            String[] splits = entry.split(",");

            if (splits == null || splits.length < 2 || splits[1].length() == 0)
                continue;

            savedGameList.add(new SavedGameEntity(entry, splits[1], splits[0]));
        }
        return savedGameList;
    }

    public void deleteSavedGame(MainGame game, String entryKey) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(hostActivity);
        String value = settings.getString(entryKey, " ");
        String[] splits = value.split(",");
        if (splits == null || splits.length == 0 || splits[0].length() == 0)
            return;

        deleteSharedPref(splits[0]);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(entryKey);
        long totalSavedCount = settings.getLong(PREF_TOTAL_SAVED_COUNT, 0);
        if (totalSavedCount > 0)
            editor.putLong(PREF_TOTAL_SAVED_COUNT, totalSavedCount - 1);
        editor.commit();
    }

    public long getTotalSavedGameCount() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(hostActivity);
        return settings.getLong(PREF_TOTAL_SAVED_COUNT, 0);
    }

    private void deleteSharedPref(String gameSharePrefName) {
        try {
            SharedPreferences settings = hostActivity.getSharedPreferences(gameSharePrefName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.clear();
            editor.commit();

            File file = new File("/data/data/com.presto.p2048/shared_prefs/" + gameSharePrefName + ".xml");
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void makeEntry(String handleName, String prefName) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(hostActivity);
        long totalSavedCount = settings.getLong(PREF_TOTAL_SAVED_COUNT, 0);
        long nextValue = settings.getLong(PREF_NEXT_VALUE, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_SAVED_GAME_PREFIX + nextValue, prefName + "," + handleName);
        nextValue = nextValue + 1;
        totalSavedCount = totalSavedCount + 1;
        editor.putLong(PREF_NEXT_VALUE, nextValue);
        editor.putLong(PREF_TOTAL_SAVED_COUNT, totalSavedCount);
        editor.commit();
    }

    public void save(MainView view) {
        save(view.game, DEFAULT_SHARED_PREF_NAME, false);
    }

    private void save(MainGame game, String prefName, boolean disbaleUndo) {
        //updateLeaderBoard();
        SharedPreferences settings = hostActivity.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        doSave(game, settings, disbaleUndo);
    }

    private void doSave(MainGame game, SharedPreferences settings, boolean disbaleUndo) {

        SharedPreferences.Editor editor = settings.edit();
        Tile[][] field = game.grid.field;
        Tile[][] undoField = game.grid.undoField;
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
        editor.putLong(SCORE, game.score);
        editor.putLong(HIGH_SCORE, game.highScore);
        editor.putLong(UNDO_SCORE, game.lastScore);
        if (disbaleUndo)
            editor.putBoolean(CAN_UNDO, false);
        else
            editor.putBoolean(CAN_UNDO, game.canUndo);
        editor.putInt(GAME_STATE, game.gameState);
        editor.putInt(UNDO_GAME_STATE, game.lastGameState);
        editor.commit();
    }

    public void load(MainView view) {
        load(view, DEFAULT_SHARED_PREF_NAME);
    }

    public void load(MainView view, String prefName) {
        // Stopping all animations
        view.game.aGrid.cancelAnimations();

        //SharedPreferences settings = PreferenceManager
        // .getDefaultSharedPreferences(hostActivity);
        SharedPreferences settings = hostActivity.getSharedPreferences(prefName, Context.MODE_PRIVATE);

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
        long savedHighScore = settings.getLong(HIGH_SCORE, view.game.highScore);
        long defaultHighScore = getHighScore(hostActivity);
        view.game.highScore = (savedHighScore > defaultHighScore ? savedHighScore : defaultHighScore);
        view.game.lastScore = settings.getLong(UNDO_SCORE, view.game.lastScore);
        view.game.canUndo = settings.getBoolean(CAN_UNDO, view.game.canUndo);
        view.game.gameState = settings.getInt(GAME_STATE, view.game.gameState);
        view.game.lastGameState = settings.getInt(UNDO_GAME_STATE,
                view.game.lastGameState);
    }

    public static long getHighScore(Context context) {
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(context);
        return settings.getLong(HIGH_SCORE, -1);
    }

    public static void recordHighScore(Context context, long highScore) {
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(HIGH_SCORE, highScore);
        editor.commit();
    }
}
