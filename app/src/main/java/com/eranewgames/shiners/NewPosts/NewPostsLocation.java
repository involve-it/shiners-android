package com.eranewgames.shiners.NewPosts;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.TextView;

import com.eranewgames.shiners.App;
import com.eranewgames.shiners.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewPostsLocation extends AppCompatActivity implements OnMapReadyCallback{

    @BindView(R.id.textView9) TextView textView9;
    @BindView(R.id.switchDynamic) Switch switchDynamic;
    @BindView(R.id.switchStatic) Switch switchStatic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_posts_location);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        SupportMapFragment supportMapFragment= (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        supportMapFragment.getMapAsync(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        if (item.getItemId() == R.id.next) {

//            switchDynamic.isChecked();
//            switchStatic.isChecked();
//
//            Map<String,Object> keyPresences=new HashMap<>()
//            App.keyMap.put()

            Map<String,Object> keyLocations=new HashMap<>();
            keyLocations.put("lat",0);
            keyLocations.put("lng",0);
            App.keyDetails.put("locations",keyLocations);
            startActivity(new Intent(NewPostsLocation.this, NewPostsDate.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.m_new_posts_text, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng sydney = new LatLng(App.locationLat,App.locationLng);

        googleMap.setMyLocationEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 12));

        googleMap.addMarker(new MarkerOptions()
                .title("Мое местоположение")
                .position(sydney));
    }
}
