package org.buzzar.appPrityazhenie.activities.newpost;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import org.buzzar.appPrityazhenie.R;
import org.buzzar.appPrityazhenie.logic.Helper;
import org.buzzar.appPrityazhenie.logic.analytics.AnalyticsProvider;
import org.buzzar.appPrityazhenie.logic.analytics.TrackingKeys;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;

public class WhenActivity extends NewPostBaseActivity implements View.OnClickListener {
    @BindView(R.id.activity_new_post_when_btn_custom_date)
    Button mBtnCustomDate;
    @BindView(R.id.activity_new_post_when_txt_date)
    TextView mTxtDate;

    Calendar mSelectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post_when);
        setActivityDefaults(true);

        setSelectedDate();

        populateUi();

        mBtnCustomDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment fragment = new DatePickerFragment();
                fragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        for(int id : new int[] {
                R.id.activity_new_post_when_btn_1d,
                R.id.activity_new_post_when_btn_2d,
                R.id.activity_new_post_when_btn_1w,
                R.id.activity_new_post_when_btn_2w,
                R.id.activity_new_post_when_btn_1m,
                R.id.activity_new_post_when_btn_1y}){
            findViewById(id).setOnClickListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        AnalyticsProvider.LogScreen(this, TrackingKeys.Screens.NEW_POST_WHEN);
    }

    private void setSelectedDate(){
        if (mPost != null && mPost.endDatePost != null){
            mSelectedDate = Calendar.getInstance();
            mSelectedDate.setTime(mPost.endDatePost);
        }
        if (mSelectedDate == null){
            mSelectedDate = Calendar.getInstance();
            mSelectedDate.add(Calendar.DAY_OF_MONTH, 7);
        }
    }

    @Override
    protected void populateUi() {
        if (mSelectedDate != null){
            Date endDate = new Date(mSelectedDate.getTimeInMillis());
            mTxtDate.setText(Helper.formatDate(this, endDate));
        }
    }

    @Override
    protected void populatePost() {
        mPost.endDatePost = new Date(mSelectedDate.getTimeInMillis());
    }

    @Override
    protected Intent getNextStepIntent() {
        return new Intent(this, PhotoActivity.class);
    }

    @Override
    protected boolean isValid() {
        boolean valid = true;
        Calendar today = Calendar.getInstance();
        if (today.getTimeInMillis() >= mSelectedDate.getTimeInMillis()){
            valid = false;
            Toast.makeText(this, "Дата окончания действия поста должна быть в будущем", Toast.LENGTH_SHORT).show();
        }
        return valid;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.activity_new_post_when_btn_1d:
                mSelectedDate = Calendar.getInstance();
                mSelectedDate.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case R.id.activity_new_post_when_btn_2d:
                mSelectedDate = Calendar.getInstance();
                mSelectedDate.add(Calendar.DAY_OF_MONTH, 2);
                break;
            case R.id.activity_new_post_when_btn_1w:
                mSelectedDate = Calendar.getInstance();
                mSelectedDate.add(Calendar.DAY_OF_MONTH, 7);
                break;
            case R.id.activity_new_post_when_btn_2w:
                mSelectedDate = Calendar.getInstance();
                mSelectedDate.add(Calendar.DAY_OF_MONTH, 14);
                break;
            case R.id.activity_new_post_when_btn_1m:
                mSelectedDate = Calendar.getInstance();
                mSelectedDate.add(Calendar.MONTH, 1);
                break;
            case R.id.activity_new_post_when_btn_1y:
                mSelectedDate = Calendar.getInstance();
                mSelectedDate.add(Calendar.YEAR, 1);
                break;
        }
        // mark somehow selected icon:
        findViewById(R.id.activity_new_post_when_btn_1d).setAlpha(1f);
        findViewById(R.id.activity_new_post_when_btn_2d).setAlpha(1f);
        findViewById(R.id.activity_new_post_when_btn_1w).setAlpha(1f);
        findViewById(R.id.activity_new_post_when_btn_2w).setAlpha(1f);
        findViewById(R.id.activity_new_post_when_btn_1m).setAlpha(1f);
        findViewById(R.id.activity_new_post_when_btn_1y).setAlpha(1f);
        v.setAlpha(0.3f);
        populateUi();
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener{
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //return super.onCreateDialog(savedInstanceState);
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            WhenActivity activity = ((WhenActivity)getActivity());
            activity.mSelectedDate = calendar;
            activity.populateUi();
        }
    }
}
