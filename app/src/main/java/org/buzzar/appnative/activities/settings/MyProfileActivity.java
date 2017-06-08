package org.buzzar.appnative.activities.settings;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.buzzar.appnative.R;
import org.buzzar.appnative.logic.AccountHandler;
import org.buzzar.appnative.logic.objects.User;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MyProfileActivity extends AppCompatActivity {

    User user;

    @BindView(R.id.userImageView)
    ImageView userImageView;

    @BindView(R.id.userFullName)
    TextView userFullName;

    @BindView(R.id.userEmail)
    TextView userEmail;

    @BindView(R.id.userPhone)
    TextView userPhone;

    @BindView(R.id.userSkype)
    TextView userSkype;


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
    }

}
