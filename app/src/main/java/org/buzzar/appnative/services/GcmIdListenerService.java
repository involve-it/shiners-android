package org.buzzar.appnative.services;

import com.google.android.gms.iid.InstanceIDListenerService;
import org.buzzar.appnative.logic.AccountHandler;

public class GcmIdListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        AccountHandler.registerGcm(this);
    }
}
