package org.buzzar.appnative.activities.settings;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.buzzar.appnative.R;
import org.buzzar.appnative.logic.AccountHandler;
import org.buzzar.appnative.logic.Constants;
import org.buzzar.appnative.logic.JsonProvider;
import org.buzzar.appnative.logic.objects.User;
import org.buzzar.appnative.logic.objects.response.ResponseBase;

import butterknife.BindView;
import butterknife.ButterKnife;
import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;


public class MyProfileActivity extends AppCompatActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my_profile);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        ButterKnife.bind(this);

        userEmail.setInputType(InputType.TYPE_NULL);
        userEmail.setEnabled(false);

        this.user = AccountHandler.getCurrentUser();
        if (this.user == null){
            onBackPressed();
        }

        fillProfile();
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

        MeteorSingleton.getInstance().call(Constants.MethodNames.EDIT_USER, new Object[]{user}, new ResultListener() {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.m_activity_my_profile, menu);
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
