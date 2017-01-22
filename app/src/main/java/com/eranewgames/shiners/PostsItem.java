package com.eranewgames.shiners;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.delight.android.ddp.ResultListener;

public class PostsItem extends AppCompatActivity implements OnMapReadyCallback {

    @BindView(R.id.imageView2) ImageView imageView2;
    @BindView(R.id.textTitle) TextView textTitle;
    @BindView(R.id.textDesc) TextView textDesc;
    @BindView(R.id.imageFav) ImageView imageFav;
    @BindView(R.id.imageVisible) ImageView imageVisible;
    @BindView(R.id.textVisible) TextView textVisible;
    @BindView(R.id.textNear) TextView textNear;
    @BindView(R.id.textLocation) TextView textLocation;
    @BindView(R.id.locationDesc) TextView locationDesc;
    @BindView(R.id.btnLike) LinearLayout btnLike;
    @BindView(R.id.btnComment) LinearLayout btnComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_item);
        ButterKnife.bind(this);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMaps);
        supportMapFragment.getMapAsync(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        App.meteor.call("getPost", new Object[]{1}, new ResultListener() {
            @Override
            public void onSuccess(String result) {
                Log.e("PostsItem=onSuccess", result);
            }

            @Override
            public void onError(String error, String reason, String details) {
                Log.e("PostsItem=onError", error + "  " + reason + "  " + details);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng sydney = new LatLng(-33.867, 151.206);

        googleMap.setMyLocationEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13));

        googleMap.addMarker(new MarkerOptions()
                .title("Sydney")
                .snippet("The most populous city in Australia.")
                .position(sydney));
    }

    @OnClick({R.id.btnLike, R.id.btnComment})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnLike:
                break;
            case R.id.btnComment:
                break;
        }
    }
}
