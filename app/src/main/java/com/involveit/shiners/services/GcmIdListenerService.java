package com.involveit.shiners.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.android.gms.iid.InstanceIDListenerService;
import com.involveit.shiners.logic.AccountHandler;

public class GcmIdListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        AccountHandler.registerGcm(this);
    }
}
