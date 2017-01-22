package com.eranewgames.shiners;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import im.delight.android.ddp.MeteorCallback;

public class ServiceMeteor extends Service implements MeteorCallback {
    public ServiceMeteor() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        App.meteor.addCallback(this);
        App.meteor.connect();
    }

    @Override
    public void onConnect(boolean signedInAutomatically) {
        Toast.makeText(this, "Connect", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisconnect() {
        Toast.makeText(this, "Disconect", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onDestroy() {
        App.meteor.disconnect();
        App.meteor.removeCallback(this);
        super.onDestroy();
    }
}
