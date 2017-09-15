package org.buzzar.appnative.logic;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import org.buzzar.appnative.R;
import org.buzzar.appnative.logic.cache.CacheEntity;
import org.buzzar.appnative.logic.cache.CachingHandler;
import org.buzzar.appnative.logic.objects.User;
import org.buzzar.appnative.logic.objects.response.GetUserResponse;
import org.buzzar.appnative.logic.objects.response.ResponseBase;
import org.buzzar.appnative.services.BackgroundLocationService;

import java.io.IOException;
import java.util.HashMap;

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

    public static void initFromCache(Context context){
        CacheEntity<User> cache =  CachingHandler.getCacheObject(context, CachingHandler.KEY_CURRENT_USER);
        if (cache != null){
            currentUser = cache.getObject();
        }
    }

    public static void logoff(Context context){
        currentUser = null;
        if (messagesSubscriptionId != null){
            MeteorSingleton.getInstance().unsubscribe(messagesSubscriptionId);
        }
        if (commentsSubscriptionId != null){
            MeteorSingleton.getInstance().unsubscribe(commentsSubscriptionId);
        }
        accountLoaded = false;

        CachingHandler.removeObject(context, CachingHandler.KEY_CURRENT_USER);
        CachingHandler.removeObject(context, CachingHandler.KEY_MY_POSTS);
        CachingHandler.removeObject(context, CachingHandler.KEY_DIALOGS);
    }

    public static boolean isLoggedIn(){
        return currentUser != null;
    }

    private static void gcmRegistrationFailed(){
        Log.d(TAG, "GCM Registration failed.");
    }
    
    public static synchronized void registerGcm(final Context context){
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int result = googleApiAvailability.isGooglePlayServicesAvailable(context);
        if (result != ConnectionResult.SUCCESS){
            Log.d(TAG, "GCM registration failed - Google Play is not available");
            return;
        }

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                InstanceID instanceID = InstanceID.getInstance(context);
                try {
                    final String userId = MeteorSingleton.getInstance().getUserId();
                    final String token = instanceID.getToken(context.getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                    Log.d(TAG, "Registered GCM token: " + token);
                    HashMap<String, Object> raixRequest = new HashMap<>();
                    HashMap<String, Object> tokenMap = new HashMap<>();
                    tokenMap.put("gcm", token);
                    raixRequest.put("token", tokenMap);
                    raixRequest.put("appName", "com.involveit.shiners");
                    raixRequest.put("userId", userId);
                    MeteorSingleton.getInstance().call(Constants.MethodNames.REGISTER_PUSH_TOKEN_RAIX, new Object[]{raixRequest}, new ResultListener() {
                        @Override
                        public void onSuccess(String result) {
                            Log.d(TAG, "Raix token registration successful");
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("token", token);
                            map.put("deviceId", SettingsHandler.getDeviceId(context));
                            map.put("platform", "gcm");
                            map.put("userId", userId);

                            MeteorSingleton.getInstance().call(Constants.MethodNames.REGISTER_PUSH_TOKEN, new Object[]{map}, new ResultListener() {
                                @Override
                                public void onSuccess(String result) {
                                    ResponseBase response = JsonProvider.defaultGson.fromJson(result, ResponseBase.class);
                                    if (response.success){
                                        Log.d(TAG, "GCM token registration completed");
                                    } else {
                                        gcmRegistrationFailed();
                                    }
                                }

                                @Override
                                public void onError(String error, String reason, String details) {
                                    gcmRegistrationFailed();
                                }
                            });
                        }

                        @Override
                        public void onError(String error, String reason, String details) {
                            gcmRegistrationFailed();
                        }
                    });
                } catch (IOException e) {
                    gcmRegistrationFailed();
                    e.printStackTrace();
                }
            }
        });
    }

    public static void loadAccount(final Context context, final AccountHandlerDelegate delegate){
        if (MeteorSingleton.getInstance().isLoggedIn()){
            registerGcm(context);
            MeteorSingleton.getInstance().call(Constants.MethodNames.GET_USER, new Object[]{MeteorSingleton.getInstance().getUserId()}, new ResultListener() {
                @Override
                public void onSuccess(String result) {
                    GetUserResponse response = JsonProvider.defaultGson.fromJson(result, GetUserResponse.class);
                    accountLoaded = true;
                    if (response.success){
                        Log.d(TAG, "User loaded.");
                        currentUser = response.result;
                        if (!currentUser.isInvisible){
                            context.startService(new Intent(context, BackgroundLocationService.class));
                        }

                        CachingHandler.setObject(context, CachingHandler.KEY_CURRENT_USER, currentUser);
                        if (delegate != null){
                            delegate.accountLoaded();
                        }
                    } else {
                        Log.d(TAG, "User load failed");
                        if (delegate != null){
                            delegate.accountLoadFailed();
                        }
                    }
                }

                @Override
                public void onError(String error, String reason, String details) {
                    accountLoaded = true;
                    Log.d(TAG, "User load failed. Error: " + error);
                    if (delegate != null){
                        delegate.accountLoadFailed();
                    }
                }
            });

            subscribeToCollections();
        } else {
            logoff(context);
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

    public interface AccountHandlerDelegate{
        void accountLoaded();
        void accountLoadFailed();
    }
}
