package com.involveit.shiners.logic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import static com.involveit.shiners.logic.MeteorCallbackHandler.CONNECTED;
import static com.involveit.shiners.logic.MeteorCallbackHandler.DISCONNECTED;

/**
 * Created by yury on 1/30/17.
 */

public abstract class MeteorBroadcastReceiver extends BroadcastReceiver {
        @Override
        public final void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (CONNECTED.equals(action)){
                connected();
            } else if (DISCONNECTED.equals(action)){
                disconnected();
            }
        }

        public final void register(Context context){
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(CONNECTED);
            intentFilter.addAction(DISCONNECTED);
            LocalBroadcastManager.getInstance(context).registerReceiver(this, intentFilter);
        }

        public final void unregister(Context context){
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
        }

        abstract public void connected();
        abstract public void disconnected();
}
