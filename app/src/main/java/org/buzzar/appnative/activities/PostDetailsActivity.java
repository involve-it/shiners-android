package org.buzzar.appnative.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.buzzar.appnative.R;
import org.buzzar.appnative.logic.AccountHandler;
import org.buzzar.appnative.logic.Constants;
import org.buzzar.appnative.logic.Helper;
import org.buzzar.appnative.logic.JsonProvider;
import org.buzzar.appnative.logic.LocationHandler;
import org.buzzar.appnative.logic.objects.MessageToSend;
import org.buzzar.appnative.logic.objects.Post;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.buzzar.appnative.logic.objects.User;
import org.buzzar.appnative.logic.objects.response.GetPostResponse;
import org.buzzar.appnative.logic.objects.response.SendMessageResponse;
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
    @BindView(R.id.txtUserFullName)
    TextView txtUserFullName;
    @BindView(R.id.imgUserPhoto)
    ImageView imgUserPhoto;
    @BindView(R.id.btnCall)
    Button btnCall;
    @BindView(R.id.btnMessage)
    Button btnMessage;
    @BindView(R.id.cardUser)
    CardView cardUser;
    @BindView(R.id.toolBar)
    Toolbar toolBar;

    Post post;

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
        setContentView(R.layout.activity_post_details);
        ButterKnife.bind(this);
        setSupportActionBar(toolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        post = getIntent().getParcelableExtra(EXTRA_POST);

        if (post != null) {
            populatePost();
        } else {
            String postId = getIntent().getStringExtra(Constants.Gcm.EXTRA_ID);
            if (postId != null) {
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage(getResources().getText(R.string.message_loading_posts));
                progressDialog.show();
                progressDialog.setCancelable(false);

                MeteorSingleton.getInstance().call(Constants.MethodNames.GET_POST, new Object[]{postId}, new ResultListener() {
                    @Override
                    public void onSuccess(String result) {
                        progressDialog.dismiss();
                        GetPostResponse response = JsonProvider.defaultGson.fromJson(result, GetPostResponse.class);
                        if (response.success) {
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

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callPhone();
            }
        });
        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User currentUser = AccountHandler.getCurrentUser();
                if (currentUser == null){
                    new AlertDialog.Builder(PostDetailsActivity.this).setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(R.string.msg_not_logged_in)
                            .setMessage(R.string.msg_please_log_in_to_send_message)
                            .setPositiveButton(R.string.msg_log_in, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent();
                                    intent.putExtra(HomeActivity.EXTRA_TAB, HomeActivity.TAB_SETTINGS);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            })
                            .setNegativeButton(R.string.msg_cancel, null)
                            .show();
                } else {
                    showSendMessageDialog();
                }
            }
        });

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMaps);
        supportMapFragment.getMapAsync(PostDetailsActivity.this);
    }

    private void showSendMessageDialog(){
        final EditText txtMessage = new EditText(this);
        txtMessage.setHint(R.string.hint_message);
        new AlertDialog.Builder(this)
                 .setView(txtMessage)
                 .setMessage(getString(R.string.msg_send_message_to) + " " + post.user.getFullName())
                 .setPositiveButton(R.string.btn_label_send, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(final DialogInterface dialog, int which) {
                         final ProgressDialog progressDialog = new ProgressDialog(PostDetailsActivity.this);
                         progressDialog.setMessage(getString(R.string.msg_sending_message));
                         progressDialog.setCancelable(false);
                         progressDialog.show();

                         MessageToSend message = new MessageToSend();
                         message.destinationUserId = post.user.id;
                         message.message = txtMessage.getText().toString();
                         message.associatedPostId = post.id;

                         MeteorSingleton.getInstance().call(Constants.MethodNames.ADD_MESSAGE, new Object[]{message}, new ResultListener() {
                             @Override
                             public void onSuccess(String result) {
                                 final SendMessageResponse response = JsonProvider.defaultGson.fromJson(result, SendMessageResponse.class);
                                 runOnUiThread(new Runnable() {
                                     @Override
                                     public void run() {
                                         progressDialog.dismiss();
                                         if (response.success){
                                             Toast.makeText(PostDetailsActivity.this, R.string.msg_message_sent, Toast.LENGTH_SHORT).show();
                                         } else {
                                             Toast.makeText(PostDetailsActivity.this, R.string.msg_error_while_sending_message, Toast.LENGTH_SHORT).show();
                                         }
                                     }
                                 });
                             }

                             @Override
                             public void onError(String error, String reason, String details) {
                                 Toast.makeText(PostDetailsActivity.this, R.string.msg_error_while_sending_message, Toast.LENGTH_SHORT).show();
                                 progressDialog.dismiss();
                             }
                         });
                     }
                 })
                 .setNegativeButton(R.string.msg_cancel, null)
                 .show();
    }

    private void callPhone() {
        String phoneNumber = post.user.getProfileDetail(User.ProfileDetail.PHONE);
        if (phoneNumber != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 1);
                } else {
                    Toast.makeText(PostDetailsActivity.this, R.string.toast_unable_to_make_call, Toast.LENGTH_SHORT).show();
                }
            } else {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(intent);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED)) {
            callPhone();
        } else {
            Toast.makeText(PostDetailsActivity.this, R.string.toast_unable_to_make_call, Toast.LENGTH_SHORT).show();
        }
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
                .load(post.details.photos.get(0).data)
                .fit()
                .centerCrop()
                .into(imageView2);

        //Date
        textDesc.setText(Helper.formatDate(this, post.timestamp));

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

        if (post.user != null) {
            User currentUser = AccountHandler.getCurrentUser();
            if (post.user.id != null && currentUser != null && post.user.id.equals(currentUser.id)){
                cardUser.setVisibility(View.GONE);
            } else {
                txtUserFullName.setText(post.user.getFullName());
                if (post.user.image != null && post.user.image.getImageUrl() != null) {
                    Picasso.with(this).load(post.user.image.getImageUrl()).fit().centerCrop().into(imgUserPhoto);
                }
                String phoneNumber = post.user.getProfileDetail(User.ProfileDetail.PHONE);
                if (phoneNumber == null){
                    btnCall.setVisibility(View.INVISIBLE);
                }
            }
        } else {
            cardUser.setVisibility(View.GONE);
        }
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
