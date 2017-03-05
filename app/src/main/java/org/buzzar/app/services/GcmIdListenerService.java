package org.buzzar.app.services;

import com.google.android.gms.iid.InstanceIDListenerService;
import org.buzzar.app.logic.AccountHandler;

public class GcmIdListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        AccountHandler.registerGcm(this);
    }
}
