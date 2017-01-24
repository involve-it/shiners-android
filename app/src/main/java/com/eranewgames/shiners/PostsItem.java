package com.eranewgames.shiners;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eranewgames.shiners.GSON.GsonPostsItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

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
    @BindView(R.id.locationDesc) TextView locationDesc;
    @BindView(R.id.btnLike) LinearLayout btnLike;
    @BindView(R.id.btnComment) LinearLayout btnComment;
    @BindView(R.id.textView12) TextView textViewType;
    @BindView(R.id.imageNear) ImageView imageNear;
    @BindView(R.id.textNear) TextView textNear;
    @BindView(R.id.imageLocation) ImageView imageLocation;
    @BindView(R.id.textLocation) TextView textLocation;
    GsonPostsItem gsonPostsItem;
    @BindView(R.id.toolBar) Toolbar toolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_item);
        ButterKnife.bind(this);
        setSupportActionBar(toolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        App.meteor.call("getPost", new Object[]{getIntent().getStringExtra("position")}, new ResultListener() {
            @Override
            public void onSuccess(String result) {
                Log.e("PostsItem=onSuccess", result);
                gsonPostsItem = new Gson().fromJson(result, GsonPostsItem.class);

                //Title
                textTitle.setText(gsonPostsItem.result.details.title);
                //Image
                Picasso.with(PostsItem.this)
                        .load(gsonPostsItem.result.details.photos.get(0).data)
                        .fit()
                        .centerCrop()
                        .into(imageView2);

                //Date
                double dateJson = Double.parseDouble(gsonPostsItem.result.timestamp.$date);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis((long) dateJson);
                textDesc.setText(DateFormat.format("MMM d, yyyy hh:mm aaa", calendar));

                //Text Image Locations
                textVisible.setText(String.valueOf(gsonPostsItem.result.stats.seenTotal));
                textViewType.setText(String.valueOf(gsonPostsItem.result.type));
                textLocation.setText(gsonPostsItem.result.details.locations.get(0).name);
                locationDesc.setText(Html.fromHtml(gsonPostsItem.result.details.description));

                //Расчет дистанции для Locations
                Location locationA = new Location("A");
                Location locationB = new Location("B");
                locationA.setLatitude(gsonPostsItem.result.details.locations.get(0).coords.lat);
                locationA.setLongitude(gsonPostsItem.result.details.locations.get(0).coords.lng);
                locationB.setLatitude(App.locationLat);
                locationB.setLongitude(App.locationLng);
                long distance = (int) locationA.distanceTo(locationB);
                if (distance < 5280) {
                    textNear.setText(distance + " ft");
                } else {
                    textNear.setText(distance / 5280 + " mi");
                }

                ((TextView)toolBar.getChildAt(0)).setText(gsonPostsItem.result.details.title);

                SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMaps);
                supportMapFragment.getMapAsync(PostsItem.this);
            }

            @Override
            public void onError(String error, String reason, String details) {
                Log.e("PostsItem=onError", error + "  " + reason + "  " + details);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng sydney = new LatLng(gsonPostsItem.result.details.locations.get(0).coords.lat, gsonPostsItem.result.details.locations.get(0).coords.lng);

        googleMap.setMyLocationEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 12));

        googleMap.addMarker(new MarkerOptions()
                .title(gsonPostsItem.result.details.locations.get(0).name)
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
