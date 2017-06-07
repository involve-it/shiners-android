package org.buzzar.appnative.activities.newpost;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.squareup.picasso.Picasso;

import org.buzzar.appnative.R;
import org.buzzar.appnative.logic.objects.Photo;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PhotoActivity extends NewPostBaseActivity {
    private static final String TAG = "PhotoActivity";
    private static final int PHOTO_REQUEST_ID = 2;
    @BindView(R.id.activity_new_post_photo_btn_add_photo) Button mBtnAddPhoto;
    @BindView(R.id.activity_new_post_photo_lst_images)

    ListView mLstImages;
    PhotosArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post_photo);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        populateUi();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.m_new_posts_done, menu);
        return true;
    }


    @OnClick(R.id.activity_new_post_photo_btn_add_photo)
    public void onClick() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PHOTO_REQUEST_ID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_REQUEST_ID && resultCode == RESULT_OK){
            try {
                InputStream inputStream = this.getContentResolver().openInputStream(data.getData());
                new AmazonUploadAsyncTask(this.getContentResolver().getType(data.getData())).execute(inputStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void populateUi() {
        if (adapter == null) {
            adapter = new PhotosArrayAdapter(this, R.layout.row_new_post_photo, mPost.details.photos);
            mLstImages.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void populatePost() {

    }

    @Override
    protected Intent getNextStepIntent() {
        return null;
    }

    private class AmazonUploadAsyncTask extends AsyncTask<InputStream, Integer, String> {
        ProgressDialog mProgressDialog;
        String mContentType;

        AmazonUploadAsyncTask(String contentType){
            
        }

        @Override
        protected String doInBackground(InputStream... params) {
            InputStream inputStream = params[0];
            AmazonS3Client s3Client = new AmazonS3Client(new BasicAWSCredentials("AKIAJUHRBKTJ4FBPKQ6Q","m1c/Q80xbc+urqhZk6AeBymsK6rGF2TX6V0KVPfa"));
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(mContentType);
            String fileName = UUID.randomUUID().toString();
            s3Client.putObject(new PutObjectRequest("shiners/v1.0/public/images", fileName, inputStream, metadata).withCannedAcl(CannedAccessControlList.PublicRead));
            return s3Client.getResourceUrl("shiners/v1.0/public/images", fileName);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(PhotoActivity.this);
            mProgressDialog.setMessage(PhotoActivity.this.getResources().getString(R.string.message_uploading_image));
            mProgressDialog.show();
            mProgressDialog.setCancelable(false);
        }

        @Override
        protected void onPostExecute(String url) {
            super.onPostExecute(url);

            Photo photo = new Photo();
            photo.original = url;
            photo.imageUrl = url;
            mPost.details.photos.add(photo);

            populateUi();

            mProgressDialog.dismiss();
        }
    }

    private class PhotosArrayAdapter extends ArrayAdapter<Photo>{
        public PhotosArrayAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Photo> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null){
                LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.row_new_post_photo, parent, false);
            }
            ImageView imageView = (ImageView)convertView;
            Photo photo = getItem(position);
            Picasso.with(getContext()).load(photo.original).into(imageView);

            return convertView;
        }
    }
}
