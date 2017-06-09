package org.buzzar.appnative.activities.settings;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.buzzar.appnative.R;
import org.buzzar.appnative.logic.AccountHandler;
import org.buzzar.appnative.logic.objects.User;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MyProfileActivity extends AppCompatActivity implements View.OnClickListener {

    User user;

    @BindView(R.id.userImageView)
    ImageView userImageView;

    @BindView(R.id.userFullName)
    TextView userFullName;

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

        fillProfile();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_my_profile_save:
                Toast.makeText(this, R.string.profile_saved,
                        Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.m_activity_my_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    protected void fillProfile() {
        user = AccountHandler.getCurrentUser();
        Picasso.with(MyProfileActivity.this)
                .load(user.image.imageUrl)
                .fit()
                .centerCrop()
                .into(userImageView);



        String firstName = "";
        String lastName = "";
        for(User.ProfileDetail detail : user.profileDetails) {
            switch (detail.key) {
                case "firstName": {
                    firstName = detail.value;
                    break;
                }
                case "lastName": {
                    lastName = detail.value;
                    break;
                }
                case "skype": {
                    userSkype.setText(detail.value);
                    break;
                }
                case "phone": {
                    userPhone.setText(detail.value);
                    break;
                }
                default:
                    break;
            }
        }

        for(User.ProfileEmail email: user.emails) {
            userEmail.setText(email.address);
            break;
        }

        userFullName.setText(firstName + " " + lastName);
        userFirstName.setText(firstName);
        userLastName.setText(lastName);
    }

    @Override
    public void onClick(View v) {

    }
}
