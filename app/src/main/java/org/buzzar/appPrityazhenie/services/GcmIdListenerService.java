package org.buzzar.appPrityazhenie.services;

import com.google.android.gms.iid.InstanceIDListenerService;
import org.buzzar.appPrityazhenie.logic.AccountHandler;

public class GcmIdListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        AccountHandler.registerGcm(this);
    }
}
