package org.buzzar.appPrityazhenie.activities.newpost;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.buzzar.appPrityazhenie.R;
import org.buzzar.appPrityazhenie.logic.LocationHandler;
import org.buzzar.appPrityazhenie.logic.analytics.AnalyticsProvider;
import org.buzzar.appPrityazhenie.logic.analytics.TrackingKeys;

import android.widget.AutoCompleteTextView;

public class WhereEventActivity extends NewPostBaseActivity implements OnMapReadyCallback, CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "WhereActivity";

//    @BindView(R.id.activity_where_loc_dynamic)
//    Switch switchDynamic;
//    @BindView(R.id.activity_where_loc_static)
//    Switch switchStatic;

    SupportPlaceAutocompleteFragment autocompleteFragment;

    GoogleMap mGoogleMap;
    Marker mStaticLocationMarker, mDynamicLocationMarker;
    LatLng staticLatLng;
    AutoCompleteTextView addressLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_post_event_where);

        setActivityDefaults(true);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        supportMapFragment.getMapAsync(this);

        //switchDynamic.setOnCheckedChangeListener(this);
        //switchStatic.setOnCheckedChangeListener(this);
        addressLabel = (AutoCompleteTextView) findViewById(R.id.activity_new_posts_event_location_label);

        autocompleteFragment = (SupportPlaceAutocompleteFragment) getSupportFragmentManager().findFragmentById(R.id.activity_where_place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
//                switchStatic.setChecked(true);
                if (mStaticLocationMarker != null){
                    mStaticLocationMarker.remove();
                    mStaticLocationMarker = null;
                }

                if (mGoogleMap != null) {
                    mStaticLocationMarker = mGoogleMap.addMarker(new MarkerOptions()
                            .title(place.getName().toString())
                            .position(place.getLatLng()));
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 12));
                    addressLabel.setText(place.getName());
                }
                staticLatLng = place.getLatLng();
            }

            @Override
            public void onError(Status status) {
                Log.w(TAG, "Error: " + status.getStatusMessage());
            }
        });

        populateUi();
    }

    @Override
    protected void onResume() {
        super.onResume();

        AnalyticsProvider.LogScreen(this, TrackingKeys.Screens.NEW_POST_WHERE);
    }

    protected void populateUi(){
        if (mGoogleMap != null) {
            boolean haveLocation = false;
            if (mPost != null) {
                org.buzzar.appPrityazhenie.logic.objects.Location location = mPost.details.getLocation(org.buzzar.appPrityazhenie.logic.objects.Location.LOCATION_TYPE_DYNAMIC);
                if (location != null) {
                    haveLocation = true;
//                    switchDynamic.setChecked(true);
                }

                location = mPost.details.getLocation(org.buzzar.appPrityazhenie.logic.objects.Location.LOCATION_TYPE_STATIC);
                if (location != null) {
                    haveLocation = true;
                    staticLatLng = new LatLng(location.coords.lat, location.coords.lng);
//                    switchStatic.setChecked(true);
                }
            }

            if (!haveLocation) {
//                switchDynamic.setChecked(true);
            }
        }
    }

    protected void populatePost(){
        mPost.details.locations.clear();
//        if (switchDynamic.isChecked()){
//            org.buzzar.appnative.logic.objects.Location location = mPost.details.getLocation(org.buzzar.appnative.logic.objects.Location.LOCATION_TYPE_DYNAMIC);
//            if (location == null){
//                location = new org.buzzar.appnative.logic.objects.Location(org.buzzar.appnative.logic.objects.Location.LOCATION_TYPE_DYNAMIC);
//            }
//            Location currentLocation = LocationHandler.getLatestReportedLocation();
//            location.coords.lat = currentLocation.getLatitude();
//            location.coords.lng = currentLocation.getLongitude();
//            mPost.details.locations.add(location);
//        }


        org.buzzar.appPrityazhenie.logic.objects.Location location = mPost.details.getLocation(org.buzzar.appPrityazhenie.logic.objects.Location.LOCATION_TYPE_STATIC);
        if (location == null){
            location = new org.buzzar.appPrityazhenie.logic.objects.Location(org.buzzar.appPrityazhenie.logic.objects.Location.LOCATION_TYPE_STATIC);
        }

        if (mStaticLocationMarker != null) {
            location.name = mStaticLocationMarker.getTitle();
        }
        if (staticLatLng != null) {
            location.coords.lat = staticLatLng.latitude;
            location.coords.lng = staticLatLng.longitude;
            mPost.details.locations.add(location);
        }
    }

    @Override
    protected Intent getNextStepIntent() {
        return new Intent(this, WhenEventActivity.class);
    }

    @Override
    protected boolean isValid() {
        boolean valid = true;
//        if (!switchDynamic.isChecked() && !switchStatic.isChecked()){
//            valid = false;
//            Toast.makeText(this, "Please select location", Toast.LENGTH_SHORT).show();
//        }
        return valid;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Location currentLocation = LocationHandler.getLatestReportedLocation();
        mGoogleMap = googleMap;
        if (currentLocation != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                } else {
                    // for users simply use cities location!
                    Toast.makeText(this, "Unable to determine location", Toast.LENGTH_SHORT).show();
                }
            } else {
                mGoogleMap.setMyLocationEnabled(true);
                displayCurrentLocation();
                //switchDynamic.setChecked(true);
                populateUi();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            //displayCurrentLocation();
            //mGoogleMap.setMyLocationEnabled(true);
            populateUi();
        }
    }

    private void displayCurrentLocation(){
        Location currentLocation = LocationHandler.getLatestReportedLocation();
        if (currentLocation != null && mGoogleMap != null) {
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));

            mDynamicLocationMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .title(getResources().getString(R.string.label_current_location))
                    .position(latLng));
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        Log.d(TAG, "checked");
//        if (buttonView.equals(this.switchDynamic)){
//            if (isChecked){
//                this.switchStatic.setChecked(false);
//
//                displayCurrentLocation();
//            } else {
//                if (mGoogleMap != null && mDynamicLocationMarker != null){
//                    //mGoogleMap.setMyLocationEnabled(false);
//                    mDynamicLocationMarker.remove();
//                    mDynamicLocationMarker = null;
//                }
//            }
//        }
//
//        if (buttonView.equals(this.switchStatic)){
//            if (isChecked){
//                this.switchDynamic.setChecked(false);
//
//                Location currentLocation = LocationHandler.getLatestReportedLocation();
//                if (currentLocation != null && mGoogleMap != null) {
//                    if (staticLatLng == null) {
//                        staticLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//                    }
//
//                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(staticLatLng, 12));
//
//                    mStaticLocationMarker = mGoogleMap.addMarker(new MarkerOptions()
//                            .title(getResources().getString(R.string.label_current_location))
//                            .position(staticLatLng));
//                }
//            } else {
//                staticLatLng = null;
//                autocompleteFragment.setText("");
//                if (mStaticLocationMarker != null){
//                    mStaticLocationMarker.remove();
//                    mStaticLocationMarker = null;
//                }
//            }
//        }
    }
}
