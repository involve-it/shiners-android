package com.involveit.shiners.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.involveit.shiners.R;

public class GcmNotificationService extends GcmListenerService {
    private final static String TAG = "GcmNotificationService";
    @Override
    public void onMessageReceived(String s, Bundle bundle) {
        NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.shiners_add3x)
                .setContentTitle("Message from: " + bundle.getString("title"))
                .setContentText(bundle.getString("message"))
                .setSound(uri)
                ;
        Log.d(TAG, bundle.toString());
        notificationManager.notify(1, mBuilder.build());
        //super.onMessageReceived(s, bundle);
    }
}
