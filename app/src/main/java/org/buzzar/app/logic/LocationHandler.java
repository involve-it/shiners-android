package org.buzzar.app.logic;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import org.buzzar.app.R;

import java.util.Locale;

/**
 * Created by yury on 1/30/17.
 */

public class LocationHandler {
    public static final String UNITS_METRIC = "shiners:LocationHandler.UNITS_METRIC";
    public static final String UNITS_IMPERIAL = "shiners:LocationHandler.UNITS_IMPERIAL";

    private static final String LOCATION_SETTINGS_FILENAME = "com.involveit.shiners.LocationHandler.LOCATION_SETTINGS";
    private static final String SETTINGS_LAST_LAT = "com.involveit.shiners.LocationHandler.setting.SETTINGS_LAST_LAT";
    private static final String SETTINGS_LAST_LNG = "com.involveit.shiners.LocationHandler.setting.SETTINGS_LAST_LNG";

    private static final float FEET_TO_METERS_COEFFICIENT = 0.3048f;

    private static Location latestReportedLocation;

    public static void init(Context context){
        if (latestReportedLocation == null) {
            SharedPreferences prefs = context.getSharedPreferences(LOCATION_SETTINGS_FILENAME, Context.MODE_PRIVATE);
            if (prefs.contains(SETTINGS_LAST_LAT) && prefs.contains(SETTINGS_LAST_LNG)) {
                latestReportedLocation = new Location("");
                latestReportedLocation.setLatitude((double) prefs.getFloat(SETTINGS_LAST_LAT, 0));
                latestReportedLocation.setLongitude((double) prefs.getFloat(SETTINGS_LAST_LNG, 0));
            }
        }
    }

    public static void setLatestReportedLocation(Context context, Location location){
        latestReportedLocation = location;

        SharedPreferences prefs = context.getSharedPreferences(LOCATION_SETTINGS_FILENAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(SETTINGS_LAST_LAT, (float)location.getLatitude());
        editor.putFloat(SETTINGS_LAST_LNG, (float)location.getLongitude());
        editor.apply();
    }

    public static Location getLatestReportedLocation(){
        return latestReportedLocation;
    }

    public static String distanceFormatted(Context context, float distance){
        String suffix;
        if (UNITS_METRIC.equals(getDefaultMeasuringUnits())){
            distance = distance * FEET_TO_METERS_COEFFICIENT;

            if (distance < 1000){
                suffix = context.getResources().getString(R.string.units_m);
            } else {
                suffix = context.getResources().getString(R.string.units_km);
                distance = distance / 1000;
            }
        } else {
            if (distance < 5280){
                suffix = context.getResources().getString(R.string.units_ft);
            } else {
                distance = distance / 5280;
                suffix = context.getResources().getString(R.string.units_mi);
            }
        }
        return String.format(Locale.getDefault(), "%1$.2f %2$s", distance, suffix);
    }

    public static String getDefaultMeasuringUnits(){
        String countryCode = Locale.getDefault().getCountry();
        switch (countryCode){
            case "US":
            case "LR":
            case "MM":
                return UNITS_IMPERIAL;
            default:
                return UNITS_METRIC;
        }
    }
}
