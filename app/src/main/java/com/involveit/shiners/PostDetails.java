package com.involveit.shiners;

import android.icu.text.DateFormat;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.involveit.shiners.Logic.Objects.Post;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PostDetails extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_POST = "shiners:PostDetails.EXTRA_POST";

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
    Post post;
    @BindView(R.id.toolBar) Toolbar toolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_item);
        ButterKnife.bind(this);
        setSupportActionBar(toolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        Post post = getIntent().getParcelableExtra(EXTRA_POST);

        //Title
        textTitle.setText(post.details.title);
        //Photo
        Picasso.with(PostDetails.this)
                .load(post.details.photos.get(0).original)
                .fit()
                .centerCrop()
                .into(imageView2);

        //Date
        DateFormat dateFormat = DateFormat.getDateInstance();
        textDesc.setText(dateFormat.format(post.timestamp));

        //Text Photo Locations
        textVisible.setText(String.valueOf(post.stats.seenTotal));
        textViewType.setText(String.valueOf(post.type));
        textLocation.setText(post.details.locations.get(0).name);
        locationDesc.setText(Html.fromHtml(post.details.description));

        //Расчет дистанции для Locations
        Location locationA = new Location("A");
        Location locationB = new Location("B");
        locationA.setLatitude(post.details.locations.get(0).coords.lat);
        locationA.setLongitude(post.details.locations.get(0).coords.lng);
        locationB.setLatitude(App.locationLat);
        locationB.setLongitude(App.locationLng);
        long distance = (int) locationA.distanceTo(locationB);
        if (distance < 5280) {
            textNear.setText(distance + " ft");
        } else {
            textNear.setText(distance / 5280 + " mi");
        }

        ((TextView)toolBar.getChildAt(0)).setText(post.details.title);

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMaps);
        supportMapFragment.getMapAsync(PostDetails.this);

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
        LatLng sydney = new LatLng(post.details.locations.get(0).coords.lat, post.details.locations.get(0).coords.lng);

        googleMap.setMyLocationEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 12));

        googleMap.addMarker(new MarkerOptions()
                .title(post.details.locations.get(0).name)
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
