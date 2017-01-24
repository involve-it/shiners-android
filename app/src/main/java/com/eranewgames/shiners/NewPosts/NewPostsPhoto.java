package com.eranewgames.shiners.NewPosts;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.eranewgames.shiners.App;
import com.eranewgames.shiners.Home;
import com.eranewgames.shiners.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.delight.android.ddp.ResultListener;

import static com.eranewgames.shiners.App.keyDetails;
import static com.eranewgames.shiners.App.keyMap;

public class NewPostsPhoto extends AppCompatActivity {

    @BindView(R.id.button5) Button addPhoto;
    @BindView(R.id.imageView) ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_posts_photo);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        if (item.getItemId() == R.id.next) {
            keyMap.put("details", keyDetails);

            App.meteor.call("addPost", new Object[]{keyMap}, new ResultListener() {
                @Override
                public void onSuccess(String result) {
                    Log.e("NewPostsPhoto=onSuccess", result);
                }

                @Override
                public void onError(String error, String reason, String details) {

                }
            });

            startActivity(new Intent(NewPostsPhoto.this, Home.class)
                    .putExtra(App.homePositionFragment,1)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.m_new_posts_text, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.next);
        menuItem.setTitle("Готово");
        return super.onPrepareOptionsMenu(menu);
    }

    @OnClick(R.id.button5)
    public void onClick() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1){
            if (resultCode == RESULT_OK) {
                File file=new File(data.getData().getPath());
                new Async(file).execute();
                imageView.setImageBitmap(BitmapFactory.decodeFile(file.getPath()));
            }
        }
    }

    class Async extends AsyncTask {
        File file;
        ProgressDialog progressDialog;

        Async(File file){
            this.file=file;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(NewPostsPhoto.this);
            progressDialog.setMessage("Загрузка данных");
            progressDialog.show();
            progressDialog.setCancelable(false);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            AmazonS3Client s3Client = new AmazonS3Client(new BasicAWSCredentials("AKIAJUHRBKTJ4FBPKQ6Q","m1c/Q80xbc+urqhZk6AeBymsK6rGF2TX6V0KVPfa"));
            s3Client.putObject(new PutObjectRequest("shiners/v1.0/public/images", "AKIAJUHRBKTJ4FBPKQ6Q", new File(file.getPath())).withCannedAcl(CannedAccessControlList.PublicRead));
            String resp=s3Client.getResourceUrl("shiners/v1.0/public/images", "AKIAJUHRBKTJ4FBPKQ6Q");
            Log.e("NewPostsPhoto=onActivityResult", resp);
            ArrayList arrayListPhotos=new ArrayList();
            Map<String,Object> keyPhotos=new HashMap<String, Object>();
            keyPhotos.put("data",resp);
            arrayListPhotos.add(keyPhotos);
            keyDetails.put("photos",arrayListPhotos);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            progressDialog.dismiss();
        }
    }
}
