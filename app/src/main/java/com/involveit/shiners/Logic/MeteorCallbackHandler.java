package com.involveit.shiners.logic;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.involveit.shiners.R;
import com.involveit.shiners.logic.objects.Message;

import im.delight.android.ddp.MeteorCallback;

/**
 * Created by yury on 1/30/17.
 */

public class MeteorCallbackHandler implements MeteorCallback {
    private static final String TAG = "MeteorCallbackHandler";

    static final String BROADCAST_CONNECTED = "com.involveit.shiners.MeteorCallbackHandler.broadcast.BROADCAST_CONNECTED";
    static final String BROADCAST_DISCONNECTED = "com.involveit.shiners.MeteorCallbackHandler.broadcast.BROADCAST_DISCONNECTED";

    public static final String BROADCAST_MESSAGE_ADDED = "com.involveit.shiners.MeteorCallbackHandler.broadcast.MESSAGE_ADDED";
    public static final String BROADCAST_COMMENT_ADDED = "com.involveit.shiners.MeteorCallbackHandler.broadcast.COMMENT_ADDED";
    public static final String EXTRA_COLLECTION_OBJECT = "com.involveit.shiners.MeteorCallbackHandler.extra.OBJECT";

    public Context context;

    public MeteorCallbackHandler(Context context){
        this.context = context;
    }

    @Override
    public void onConnect(boolean signedInAutomatically) {
        Intent intent = new Intent(BROADCAST_CONNECTED);

        AccountHandler.loadAccount();

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        Toast.makeText(context, R.string.message_connected, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Meteor connected");
    }

    @Override
    public void onDisconnect() {
        Intent intent = new Intent(BROADCAST_DISCONNECTED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        Toast.makeText(context, R.string.message_disconnected, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Meteor disconnected");
    }

    @Override
    public void onException(Exception e) {
        Log.d(TAG, "Meteor exception");
        e.printStackTrace();
    }

    @Override
    public void onDataAdded(String collectionName, String documentID, String newValuesJson) {
        Log.d(TAG, "Data added. Collection name: " + collectionName + ", document ID: " + documentID + ". Data:");
        Log.d(TAG, newValuesJson);

        Parcelable object = null;
        String broadcast = null;
        if (Constants.CollectionNames.MESSAGES.equals(collectionName)){
            object = JsonProvider.defaultGson.fromJson(newValuesJson, Message.class);
            ((Message)object).id = documentID;
            broadcast = BROADCAST_MESSAGE_ADDED;
        } /*else if (Constants.CollectionNames.COMMENTS.equals(collectionName)){
            object = JsonProvider.defaultGson.fromJson(newValuesJson, Comment.class);
        }*/

        if (object != null && broadcast != null) {
            Intent intent = new Intent(broadcast);
            intent.putExtra(EXTRA_COLLECTION_OBJECT, object);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    @Override
    public void onDataChanged(String collectionName, String documentID, String updatedValuesJson, String removedValuesJson) {
        Log.d(TAG, "Data changed. Collection name: " + collectionName + ", document ID: " + documentID + ". Updated values: ");
        if (updatedValuesJson != null) {
            Log.d(TAG, updatedValuesJson);
        }
        Log.d(TAG, "Removed values: ");
        if (removedValuesJson != null) {
            Log.d(TAG, removedValuesJson);
        }
    }

    @Override
    public void onDataRemoved(String collectionName, String documentID) {
        Log.d(TAG, "Data removed. Collection name: " + collectionName + ", document ID: " + documentID);
    }
}
