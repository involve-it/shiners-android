package org.buzzar.appnative.logic.analytics;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by yury on 9/14/17.
 */

public class AnalyticsProvider {
    private static  final String SCREEN_ID = "SCREEN";
    private static  final String BUTTON = "BUTTON";

    public static void LogScreen(Context context, String name){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, SCREEN_ID);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);

        FirebaseAnalytics.getInstance(context).logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
    }

    public static void LogButtonClick(Context context, String name)
    {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, BUTTON);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        FirebaseAnalytics.getInstance(context).logEvent(TrackingKeys.Events.BUTTON_CLICK, bundle);
    }

    public static void LogLogIn(Context context){
        FirebaseAnalytics.getInstance(context).logEvent(FirebaseAnalytics.Event.LOGIN, null);
    }

    public static void LogRegister(Context context){
        FirebaseAnalytics.getInstance(context).logEvent(FirebaseAnalytics.Event.SIGN_UP, null);
    }

    public static void LogNewPostCreated(Context context){
        FirebaseAnalytics.getInstance(context).logEvent(TrackingKeys.Events.NEW_POST_CREATED, null);
    }

    public static void LogEvent(Context context, String event){
        FirebaseAnalytics.getInstance(context).logEvent(event, null);
    }
}
