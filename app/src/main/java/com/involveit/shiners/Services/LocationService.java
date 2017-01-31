package com.involveit.shiners.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.involveit.shiners.App;
import com.involveit.shiners.logic.LocationHandler;

public class LocationService extends Service{
    public static final String BROADCAST_LOCATION_REPORTED = "com.involveit.shiners.LocationService.BROADCAST_LOCATION_REPORTED";
    public static final String EXTRA_LOCATION = "com.involveit.shiners.LocationService.extra.EXTRA_LOCATION";

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        locationPosition();
    }

    public void locationPosition(){
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                LocationHandler.setLatestReportedLocation(LocationService.this, location);
                Intent intent = new Intent(BROADCAST_LOCATION_REPORTED);
                intent.putExtra(EXTRA_LOCATION, location);
                LocalBroadcastManager.getInstance(LocationService.this).sendBroadcast(intent);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, getMainLooper());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
