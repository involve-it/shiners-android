package org.buzzar.appPrityazhenie.activities.newpost;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import org.buzzar.appPrityazhenie.R;
import org.buzzar.appPrityazhenie.logic.AccountHandler;
import org.buzzar.appPrityazhenie.logic.analytics.AnalyticsProvider;
import org.buzzar.appPrityazhenie.logic.analytics.TrackingKeys;
import org.buzzar.appPrityazhenie.logic.objects.Post;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;

public class NewPostActivity extends NewPostBaseActivity {
    @BindView(R.id.activity_new_post_txt_title) EditText txtTitle;
    @BindView(R.id.activity_new_post_txt_description) EditText txtDescription;
    @BindView(R.id.activity_new_post_spinner_category) AppCompatSpinner spCategory;

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mPost == null){
            mPost = new Post();
        }

        setContentView(R.layout.activity_new_post_title);
        setActivityDefaults(true);
        String test[]=getResources().getStringArray(R.array.post_categories);
        spCategory.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,test));
        String role = AccountHandler.getRole();
        if (role.equals("user")) {
            new AlertDialog.Builder(this).setMessage(R.string.message_temporarily_block).setTitle(R.string.title_temporarily_block)
                    .setPositiveButton(R.string.txt_ok, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    } ).show();
        }
        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPost.type = getResources().getStringArray(R.array.post_categories_short)[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mPost.type = null;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        AnalyticsProvider.LogScreen(this, TrackingKeys.Screens.NEW_POST_TITLE);
    }

    protected void populateUi(){
        txtTitle.setText(mPost.details.title);
        txtDescription.setText(mPost.details.description);
        if (mPost.type != null) {
            List<String> categories = Arrays.asList(getResources().getStringArray(R.array.post_categories_short));
            int catIndex = categories.indexOf(mPost.type);
            if (catIndex != -1){
                spCategory.setSelection(catIndex);
            }
        }
    }

    @Override
    protected void populatePost() {
        mPost.details.title = txtTitle.getText().toString();
        mPost.details.description = txtDescription.getText().toString();
    }

    @Override
    protected Intent getNextStepIntent() {
        if (mPost.type.compareTo("events") == 0) {
            return new Intent(NewPostActivity.this, WhereEventActivity.class);
        } else if(mPost.type.compareTo("dating") == 0){
            return new Intent(NewPostActivity.this, WhereActivity.class);

        } else if(mPost.type.compareTo("sales") == 0){
            return new Intent(NewPostActivity.this, WhereActivity.class);

        } else {
            return new Intent(NewPostActivity.this, WhereActivity.class);
        }
    }

    @Override
    protected boolean isValid() {
        boolean valid = true;
        if ("".equals(txtTitle.getText().toString())){
            valid = false;
            Toast.makeText(this, R.string.validation_post_title_empty, Toast.LENGTH_SHORT).show();
            txtTitle.requestFocus();
        }
        return valid;
    }
}
