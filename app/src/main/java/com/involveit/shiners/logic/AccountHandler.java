package com.involveit.shiners.logic;

import android.util.Log;

import com.involveit.shiners.logic.objects.User;
import com.involveit.shiners.logic.objects.response.GetUserResponse;

import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;
import im.delight.android.ddp.SubscribeListener;

/**
 * Created by yury on 2/3/17.
 */

public class AccountHandler {
    private static final String TAG = "AccountHandler";
    private static boolean accountLoaded = false;
    private static User currentUser;
    private static String messagesSubscriptionId, commentsSubscriptionId;

    public static boolean isAccountLoaded(){
        return accountLoaded;
    }

    public static User getCurrentUser(){
        return currentUser;
    }

    public static void loadAccount(){
        if (MeteorSingleton.getInstance().isLoggedIn()){
            MeteorSingleton.getInstance().call(Constants.MethodNames.GET_USER, new Object[]{MeteorSingleton.getInstance().getUserId()}, new ResultListener() {
                @Override
                public void onSuccess(String result) {
                    GetUserResponse response = JsonProvider.defaultGson.fromJson(result, GetUserResponse.class);
                    if (response.success){
                        Log.d(TAG, "User loaded.");
                        currentUser = response.result;
                    } else {
                        Log.d(TAG, "User load failed");
                    }
                    accountLoaded = true;
                }

                @Override
                public void onError(String error, String reason, String details) {
                    accountLoaded = true;
                    Log.d(TAG, "User load failed. Error: " + error);
                }
            });

            subscribeToCollections();
        }
    }

    private static void subscribeToCollections(){
        if (messagesSubscriptionId != null){
            MeteorSingleton.getInstance().unsubscribe(messagesSubscriptionId);
        }
        if (commentsSubscriptionId != null){
            MeteorSingleton.getInstance().unsubscribe(commentsSubscriptionId);
        }

        messagesSubscriptionId = MeteorSingleton.getInstance().subscribe("messages-new", null, new SubscribeListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "messages-new subscribed");
            }

            @Override
            public void onError(String error, String reason, String details) {
                Log.d(TAG, "messages-new subscription failed");
            }
        });
        commentsSubscriptionId = MeteorSingleton.getInstance().subscribe("comments-my", null, new SubscribeListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "comments-my subscribed");
            }

            @Override
            public void onError(String error, String reason, String details) {
                Log.d(TAG, "comments-my subscription failed");
            }
        });
    }
}
