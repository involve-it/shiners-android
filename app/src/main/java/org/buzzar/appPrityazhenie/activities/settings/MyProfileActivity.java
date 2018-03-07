package org.buzzar.appPrityazhenie.activities.settings;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.buzzar.appPrityazhenie.R;
import org.buzzar.appPrityazhenie.logic.AccountHandler;
import org.buzzar.appPrityazhenie.logic.Constants;
import org.buzzar.appPrityazhenie.logic.JsonProvider;
import org.buzzar.appPrityazhenie.logic.analytics.AnalyticsProvider;
import org.buzzar.appPrityazhenie.logic.analytics.TrackingKeys;
import org.buzzar.appPrityazhenie.logic.objects.User;
import org.buzzar.appPrityazhenie.logic.objects.response.ResponseBase;
import org.buzzar.appPrityazhenie.logic.ui.MeteorActivityBase;

import butterknife.BindView;
import im.delight.android.ddp.ResultListener;


public class MyProfileActivity extends MeteorActivityBase {
    public static final String EXTRA_USER = "org.buzzar.app.MyProfileActivity.EXTRA_USER";
    User user;

    @BindView(R.id.userImageView)
    ImageView userImageView;

    @BindView(R.id.userFullName)
    TextView lblUsername;

    @BindView(R.id.userFirstName)
    EditText userFirstName;

    @BindView(R.id.userLastName)
    EditText userLastName;

    @BindView(R.id.userEmail)
    EditText userEmail;

    @BindView(R.id.userPhone)
    EditText userPhone;

    @BindView(R.id.userSkype)
    EditText userSkype;

    private boolean isCurrentUser(){
        return this.user != null && AccountHandler.getCurrentUser() != null && AccountHandler.getCurrentUser()._id.equals(this.user._id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my_profile);

        setActivityDefaults(true);

        userEmail.setInputType(InputType.TYPE_NULL);
        userEmail.setEnabled(false);

        this.user = getIntent().getParcelableExtra(EXTRA_USER);
        if (this.user == null) {
            this.user = AccountHandler.getCurrentUser();
        }
        if (this.user == null){
            onBackPressed();
        }

        setEnabled(isCurrentUser());

        fillProfile();
    }

    private void setEnabled(boolean enabled){
        userFirstName.setEnabled(enabled);
        userLastName.setEnabled(enabled);
        userPhone.setEnabled(enabled);
        userSkype.setEnabled(enabled);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_my_profile_save:
                saveUser();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveUser(){
        View view = getCurrentFocus();
        if (view != null){
            InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving profile...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String value = userFirstName.getText().toString().trim();
        if (value.isEmpty()){
            user.deleteProfileDetail(User.ProfileDetail.FIRST_NAME);
        } else {
            user.setProfileDetail(User.ProfileDetail.FIRST_NAME, value);
        }

        value = userLastName.getText().toString().trim();
        if (value.isEmpty()){
            user.deleteProfileDetail(User.ProfileDetail.LAST_NAME);
        } else {
            user.setProfileDetail(User.ProfileDetail.LAST_NAME, value);
        }

        value = userPhone.getText().toString().trim();
        if (value.isEmpty()){
            user.deleteProfileDetail(User.ProfileDetail.PHONE);
        } else {
            user.setProfileDetail(User.ProfileDetail.PHONE, value);
        }

        value = userSkype.getText().toString().trim();
        if (value.isEmpty()){
            user.deleteProfileDetail(User.ProfileDetail.SKYPE);
        } else {
            user.setProfileDetail(User.ProfileDetail.SKYPE, value);
        }

        callMeteorMethod(Constants.MethodNames.EDIT_USER, new Object[]{user}, new ResultListener() {
            @Override
            public void onSuccess(String result) {
                final ResponseBase response = JsonProvider.defaultGson.fromJson(result, ResponseBase.class);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.success) {
                            Toast.makeText(MyProfileActivity.this, R.string.profile_saved, Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        } else {
                            Toast.makeText(MyProfileActivity.this, R.string.message_internal_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onError(String error, String reason, String details) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MyProfileActivity.this, R.string.message_internal_error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        AnalyticsProvider.LogScreen(this, TrackingKeys.Screens.MY_PROFILE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isCurrentUser()) {
            getMenuInflater().inflate(R.menu.m_activity_my_profile, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    protected void fillProfile() {
        String imageUrl = user.image.getImageUrl();
        if (imageUrl != null) {
            Picasso.with(MyProfileActivity.this)
                    .load(user.image.imageUrl)
                    .fit()
                    .centerCrop()
                    .into(userImageView);
        }

        for(User.ProfileDetail detail : user.profileDetails) {
            switch (detail.key) {
                case User.ProfileDetail.FIRST_NAME: {
                    userFirstName.setText(detail.value);
                    break;
                }
                case User.ProfileDetail.LAST_NAME: {
                    userLastName.setText(detail.value);
                    break;
                }
                case User.ProfileDetail.SKYPE: {
                    userSkype.setText(detail.value);
                    break;
                }
                case User.ProfileDetail.PHONE: {
                    userPhone.setText(detail.value);
                    break;
                }
            }
        }

        if (user.emails != null && user.emails.size() > 0){
            userEmail.setText(user.emails.get(0).address);
        }

        lblUsername.setText(user.username);
    }
}
