package com.involveit.shiners.logic;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by yury on 1/31/17.
 */

public final class SettingsHandler {
    private static final String SETTINGS_FILENAME = "com.involveit.shiners.SettingsHandler.SETTINGS";

    public static final String USERNAME = "com.involveit.shiners.SettingsHandler.setting.USERNAME";
    public static final String HOME_PAGE_INDEX = "com.involveit.shiners.SettingsHandler.setting.HOME_PAGE_INDEX";

    public static void setStringSetting(Context context, String key, String value){
        SharedPreferences prefs = context.getSharedPreferences(SETTINGS_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getStringSetting(Context context, String key){
        SharedPreferences prefs = context.getSharedPreferences(SETTINGS_FILENAME, Context.MODE_PRIVATE);
        return prefs.getString(key, null);
    }

    public static void setIntSetting(Context context, String key, int value){
        SharedPreferences prefs = context.getSharedPreferences(SETTINGS_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static int getIntSetting(Context context, String key){
        SharedPreferences prefs = context.getSharedPreferences(SETTINGS_FILENAME, Context.MODE_PRIVATE);
        return prefs.getInt(key, 0);
    }

    public static void removeSetting(Context context, String key){
        SharedPreferences prefs = context.getSharedPreferences(SETTINGS_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(key);
        editor.apply();
    }
}
