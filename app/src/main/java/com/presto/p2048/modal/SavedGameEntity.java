package com.presto.p2048.modal;

/**
 * Created by haniza on 11/20/2016.
 */

public class SavedGameEntity {
    public String key;
    public String name;
    public String prefName;

    private static final int MAX_NAME_LENGTH = 15;

    public SavedGameEntity(String key, String name, String prefName) {
        this.key = key;
        this.name = name;
        this.prefName = prefName;
    }

    public String toString() {
        return this.name;
    }


    public static boolean isValidName(String name) {
        if (name.length() == 0)
            return false;
        if (name.length() > MAX_NAME_LENGTH)
            return false;
        return true;
    }
}