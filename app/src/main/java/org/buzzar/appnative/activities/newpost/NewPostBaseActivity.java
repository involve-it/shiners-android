package org.buzzar.appnative.activities.newpost;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.buzzar.appnative.R;
import org.buzzar.appnative.activities.HomeActivity;
import org.buzzar.appnative.logic.LocationHandler;
import org.buzzar.appnative.logic.objects.Location;
import org.buzzar.appnative.logic.objects.Post;

import java.util.HashMap;
import java.util.Map;

import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;

public abstract class NewPostBaseActivity extends AppCompatActivity {
    private static final String TAG = "NewPostBaseActivity";
    public static final String EXTRA_POST = "org.buzzar.appnative.NewPostActivity.EXTRA_POST";
    public static final int REQUEST_BACK = 1;
    protected Post mPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mPost == null){
            mPost = getIntent().getParcelableExtra(EXTRA_POST);

            if (mPost == null){
                mPost = new Post();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.m_new_posts_text, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        populatePost();
        Intent intent = new Intent();
        intent.putExtra(EXTRA_POST, (Parcelable) mPost);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.next:
                if (isValid()) {
                    populatePost();

                    Intent intent = getNextStepIntent();
                    intent.putExtra(EXTRA_POST, (Parcelable) mPost);
                    startActivityForResult(intent, REQUEST_BACK);
                }

                break;
            case R.id.done:
                if (isValid()) {
                    createPost();
                    startActivity(new Intent(this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void createPost(){
        Location.Coords coords = null;
        if (LocationHandler.getLatestReportedLocation() != null){
            coords = new Location.Coords();
            coords.lat = LocationHandler.getLatestReportedLocation().getLatitude();
            coords.lng = LocationHandler.getLatestReportedLocation().getLongitude();
        }

        MeteorSingleton.getInstance().call("addPost", new Object[]{mPost, coords}, new ResultListener() {
            @Override
            public void onSuccess(String result) {
                startActivity(new Intent(NewPostBaseActivity.this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                Log.d(TAG, result);
            }

            @Override
            public void onError(String error, String reason, String details) {
                Log.w(TAG, error);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_BACK && resultCode == RESULT_OK){
            mPost = data.getParcelableExtra(EXTRA_POST);
            populateUi();
        }
    }

    protected abstract void populateUi();
    protected abstract void populatePost();
    protected abstract Intent getNextStepIntent();
    protected abstract boolean isValid();
}
