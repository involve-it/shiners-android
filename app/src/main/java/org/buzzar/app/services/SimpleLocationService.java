package org.buzzar.app.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import org.buzzar.app.logic.LocationHandler;

public class SimpleLocationService extends Service {
    public static final String BROADCAST_LOCATION_REPORTED = "com.involveit.shiners.SimpleLocationService.BROADCAST_LOCATION_REPORTED";
    public static final String EXTRA_LOCATION = "com.involveit.shiners.SimpleLocationService.extra.EXTRA_LOCATION";

    public SimpleLocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        requestLocationOnce();
    }

    public void requestLocationOnce() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                LocationHandler.setLatestReportedLocation(SimpleLocationService.this, location);
                Intent intent = new Intent(BROADCAST_LOCATION_REPORTED);
                intent.putExtra(EXTRA_LOCATION, location);
                LocalBroadcastManager.getInstance(SimpleLocationService.this).sendBroadcast(intent);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Normal use of this application requires geolocation. Please allow this in settings.", Toast.LENGTH_LONG).show();
            return;
        }
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, getMainLooper());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
