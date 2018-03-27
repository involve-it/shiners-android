package org.buzzar.appPrityazhenie.logic;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.squareup.picasso.Picasso;

import org.buzzar.appPrityazhenie.R;
import org.buzzar.appPrityazhenie.logic.Constants;
import org.buzzar.appPrityazhenie.logic.analytics.AnalyticsProvider;
import org.buzzar.appPrityazhenie.logic.analytics.TrackingKeys;
import org.buzzar.appPrityazhenie.logic.objects.Photo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * Created by arutune on 3/27/18.
 */
public class AmazonUploadAsyncTask extends AsyncTask<InputStream, Integer, String> {
    ProgressDialog mProgressDialog;
    String mContentType;
    Context PhotoActivity;
    Photo photoObject;
    Callback callback;
    public AmazonUploadAsyncTask(String contentType, Context photoActivity, Callback callback){
        this.PhotoActivity = photoActivity;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(InputStream... params) {
        InputStream inputStream = params[0];
        AmazonS3Client s3Client = new AmazonS3Client(new BasicAWSCredentials("AKIAJUHRBKTJ4FBPKQ6Q","m1c/Q80xbc+urqhZk6AeBymsK6rGF2TX6V0KVPfa"));
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(mContentType);
        try {
            metadata.setContentLength(inputStream.available());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        String fileName = UUID.randomUUID().toString();
        s3Client.putObject(new PutObjectRequest("shiners/v1.0/public/images", fileName, inputStream, metadata).withCannedAcl(CannedAccessControlList.PublicRead));
        return s3Client.getResourceUrl("shiners/v1.0/public/images", fileName);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = new ProgressDialog(PhotoActivity);
        mProgressDialog.setMessage(PhotoActivity.getResources().getString(R.string.message_uploading_image));
        mProgressDialog.show();
        mProgressDialog.setCancelable(false);
    }

    @Override
    protected void onPostExecute(String url) {
        super.onPostExecute(url);

        if (url == null){
            Toast.makeText(PhotoActivity, R.string.new_post_photo_error_upload, Toast.LENGTH_SHORT).show();
        } else {
            callback.callingBack(url);
        }

        mProgressDialog.dismiss();
    }
    public interface Callback {
        void callingBack(String url);
    }
}