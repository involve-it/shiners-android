package org.buzzar.appnative.activities.newpost;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import org.buzzar.appnative.R;
import org.buzzar.appnative.logic.objects.Post;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        String test[]=getResources().getStringArray(R.array.post_categories);
        spCategory.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,test));
    }

    protected void populateUi(){
        txtTitle.setText(mPost.details.title);
        txtDescription.setText(mPost.details.description);
        if (mPost.type != null) {
            List<String> categories = Arrays.asList(getResources().getStringArray(R.array.post_categories));
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
        mPost.type = spCategory.getSelectedItem().toString();
    }

    @Override
    protected Intent getNextStepIntent() {
        return new Intent(NewPostActivity.this, WhereActivity.class);
    }
}
