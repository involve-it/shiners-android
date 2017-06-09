package org.buzzar.appnative.services;

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
import org.buzzar.appnative.R;
import org.buzzar.appnative.activities.DialogActivity;
import org.buzzar.appnative.activities.PostDetailsActivity;
import org.buzzar.appnative.logic.Constants;
import org.buzzar.appnative.logic.JsonProvider;
import org.buzzar.appnative.logic.objects.GcmPayload;

public class GcmNotificationService extends GcmListenerService {
    private final static String TAG = "GcmNotificationService";
    @Override
    public void onMessageReceived(String s, Bundle bundle) {
        PendingIntent pendingIntent = getPendingIntent(bundle);

        if (pendingIntent != null) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.shiners_add3x)
                    .setContentTitle(bundle.getString("title"))
                    .setContentText(bundle.getString("message"))
                    .setSound(uri)
                    .setContentIntent(getPendingIntent(bundle));
            notificationManager.notify(1, mBuilder.build());
        }
        
        Log.d(TAG, bundle.toString());

        //super.onMessageReceived(s, bundle);
    }

    private PendingIntent getPendingIntent(Bundle bundle){
        GcmPayload payload = JsonProvider.defaultGson.fromJson(bundle.getString("ejson"), GcmPayload.class);

        Intent intent = null;
        switch (payload.type){
            case Constants.Gcm.PayloadType.CHAT:
                intent = new Intent(this, DialogActivity.class);
                break;
            case Constants.Gcm.PayloadType.POST:
            //case Constants.Gcm.PayloadType.COMMENT:
                intent = new Intent(this, PostDetailsActivity.class);
                break;

        }
        if (intent != null) {
            intent.putExtra(Constants.Gcm.EXTRA_ID, payload._id);
            return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        return null;
    }
}
