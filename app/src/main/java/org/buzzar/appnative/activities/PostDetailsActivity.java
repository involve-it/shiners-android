package org.buzzar.appnative.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.icu.text.DateFormat;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.buzzar.appnative.R;
import org.buzzar.appnative.logic.Constants;
import org.buzzar.appnative.logic.JsonProvider;
import org.buzzar.appnative.logic.LocationHandler;
import org.buzzar.appnative.logic.objects.Post;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import org.buzzar.appnative.logic.objects.response.GetPostResponse;
import org.buzzar.appnative.services.SimpleLocationService;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;

public class PostDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_POST = "shiners:PostDetailsActivity.EXTRA_POST";

    @BindView(R.id.imageView2)
    ImageView imageView2;
    @BindView(R.id.textTitle)
    TextView textTitle;
    @BindView(R.id.textDesc)
    TextView textDesc;
    @BindView(R.id.imageFav)
    ImageView imageFav;
    @BindView(R.id.imageVisible)
    ImageView imageVisible;
    @BindView(R.id.textVisible)
    TextView textVisible;
    @BindView(R.id.locationDesc)
    TextView locationDesc;
    @BindView(R.id.btnLike)
    LinearLayout btnLike;
    @BindView(R.id.btnComment)
    LinearLayout btnComment;
    @BindView(R.id.textView12)
    TextView textViewType;
    @BindView(R.id.imageNear)
    ImageView imageNear;
    @BindView(R.id.textNear)
    TextView textNear;
    @BindView(R.id.imageLocation)
    ImageView imageLocation;
    @BindView(R.id.textLocation)
    TextView textLocation;
    Post post;
    @BindView(R.id.toolBar)
    Toolbar toolBar;
    private ProgressDialog progressDialog;
    private GoogleMap googleMap;

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.locationBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(this.locationBroadcastReceiver, new IntentFilter(SimpleLocationService.BROADCAST_LOCATION_REPORTED));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_item);
        ButterKnife.bind(this);
        setSupportActionBar(toolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        post = getIntent().getParcelableExtra(EXTRA_POST);

        if (post != null) {
            populatePost();
        } else {
            String postId = getIntent().getStringExtra(Constants.Gcm.EXTRA_ID);
            if (postId != null){
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage(getResources().getText(R.string.message_loading_posts));
                progressDialog.show();
                progressDialog.setCancelable(false);

                MeteorSingleton.getInstance().call(Constants.MethodNames.GET_POST, new Object[]{postId}, new ResultListener() {
                    @Override
                    public void onSuccess(String result) {
                        progressDialog.dismiss();
                        GetPostResponse response = JsonProvider.defaultGson.fromJson(result, GetPostResponse.class);
                        if (response.success){
                            post = response.result;
                            populatePost();
                            updateMap();
                        } else {
                            navigateUp();
                        }
                    }

                    @Override
                    public void onError(String error, String reason, String details) {
                        navigateUp();
                    }
                });
            } else {
                navigateUp();
            }
        }

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMaps);
        supportMapFragment.getMapAsync(PostDetailsActivity.this);
    }

    private void navigateUp(){
        if (progressDialog != null){
            progressDialog.dismiss();
            progressDialog = null;
        }
        Toast.makeText(this, "An error occurred while loading your conversation", Toast.LENGTH_SHORT).show();
        NavUtils.navigateUpFromSameTask(this);
    }

    private void populatePost(){
        //Title
        textTitle.setText(post.details.title);
        //Photo
        Picasso.with(PostDetailsActivity.this)
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
        if (post.details.description != null) {
            locationDesc.setText(Html.fromHtml(post.details.description));
        }

        textNear.setText(R.string.message_na);

        recalculateDistances();

        ((TextView) toolBar.getChildAt(0)).setText(post.details.title);
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
        this.googleMap = googleMap;
        updateMap();
    }

    private  void updateMap(){
        if (post != null && googleMap != null) {
            LatLng latLng = new LatLng(post.details.locations.get(0).coords.lat, post.details.locations.get(0).coords.lng);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
            googleMap.addMarker(new MarkerOptions()
                    .title(post.details.locations.get(0).name)
                    .position(latLng));
        }
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

    private void recalculateDistances(){
        Location currentLocation = LocationHandler.getLatestReportedLocation();
        if (currentLocation != null && post != null) {
            org.buzzar.appnative.logic.objects.Location location = post.getLocation();
            if (location != null) {
                float distance = location.distanceFrom(currentLocation.getLatitude(), currentLocation.getLongitude());

                textNear.setText(LocationHandler.distanceFormatted(this, distance));
            }
        }
    }

    private BroadcastReceiver locationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (SimpleLocationService.BROADCAST_LOCATION_REPORTED.equals(action)){
                recalculateDistances();
            }
        }
    };
}
