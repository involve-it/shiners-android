package org.buzzar.appnative.logic;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

/**
 * Created by yury on 1/31/17.
 */

public final class SettingsHandler {
    private static final String SETTINGS_FILENAME = "com.involveit.shiners.SettingsHandler.SETTINGS";

    public static final String USER_ID = "com.involveit.shiners.SettingsHandler.setting.USER_ID";
    public static final String HOME_PAGE_INDEX = "com.involveit.shiners.SettingsHandler.setting.HOME_PAGE_INDEX";

    private static final String DEVICE_ID = "com.involveit.shiners.SettingsHandler.setting.DEVICE_ID";

    public synchronized static String getDeviceId(Context context){
        SharedPreferences prefs = context.getSharedPreferences(SETTINGS_FILENAME, Context.MODE_PRIVATE);
        String deviceId = prefs.getString(DEVICE_ID, null);
        if (deviceId == null){
            deviceId = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(DEVICE_ID, deviceId);
            editor.apply();
        }

        return deviceId;
    }

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
