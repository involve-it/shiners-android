package com.involveit.shiners.logic;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.involveit.shiners.R;

import im.delight.android.ddp.MeteorCallback;

/**
 * Created by yury on 1/30/17.
 */

public class MeteorCallbackHandler implements MeteorCallback {
    static final String CONNECTED = "shiners:MeteorCallbackHandler.CONNECTED";
    static final String DISCONNECTED = "shiners:MeteorCallbackHandler.DISCONNECTED";
    public Context context;

    public MeteorCallbackHandler(Context context){
        this.context = context;
    }

    @Override
    public void onConnect(boolean signedInAutomatically) {
        Intent intent = new Intent(CONNECTED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        Toast.makeText(context, R.string.message_connected, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisconnect() {
        Intent intent = new Intent(DISCONNECTED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        Toast.makeText(context, R.string.message_disconnected, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onException(Exception e) {

    }

    @Override
    public void onDataAdded(String collectionName, String documentID, String newValuesJson) {

    }

    @Override
    public void onDataChanged(String collectionName, String documentID, String updatedValuesJson, String removedValuesJson) {

    }

    @Override
    public void onDataRemoved(String collectionName, String documentID) {

    }
}
