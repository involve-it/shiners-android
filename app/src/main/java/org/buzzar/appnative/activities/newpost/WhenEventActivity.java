package org.buzzar.appnative.activities.newpost;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.TextView;
import android.widget.Toast;

import org.buzzar.appnative.R;
import org.buzzar.appnative.logic.Helper;
import org.buzzar.appnative.logic.analytics.AnalyticsProvider;
import org.buzzar.appnative.logic.analytics.TrackingKeys;

import java.util.Calendar;
import java.util.Date;
import android.text.format.Time;
import android.text.format.DateFormat;


import butterknife.BindView;

public class WhenEventActivity extends NewPostBaseActivity implements View.OnClickListener {
    @BindView(R.id.activity_new_post_event_when_btn_choose_date)
    Button mBtnChooseDate;
    @BindView(R.id.activity_new_post_event_when_btn_choose_time)
    Button mBtnChooseTime;
    @BindView(R.id.activity_new_post_when_txt_date)
    TextView mTxtDate;
    @BindView(R.id.activity_new_post_when_txt_time)
    TextView mTxtTime;

    Calendar mSelectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post_event_when);
        setActivityDefaults(true);

        setSelectedDate();

        populateUi();

        mBtnChooseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment fragment = new DatePickerFragment();
                fragment.show(getSupportFragmentManager(), "datePicker");
//                DatePickerFragment fragment = new DatePickerFragment();
//                fragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
        mBtnChooseTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment fragment = new TimePickerFragment();
                fragment.show(getSupportFragmentManager(), "timePicker");
            }
        });
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
            Date date = new Date(mSelectedDate.getTimeInMillis());
            //Time time = new Time(mSelectedDate.getTime());
            mTxtDate.setText(Helper.formatDate(this, date));
//            mTxtDate.setText(Helper.formatDate(this, date));
//            mTxtTime.setText(Helper.formatDate(this, time));
        }
    }

    @Override
    protected void populatePost() {

        //int t = mSelectedTime.getTimeInMillis();
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
            Toast.makeText(this, "Дата события должна быть в будущем", Toast.LENGTH_SHORT).show();
        }
        return valid;
    }

    @Override
    public void onClick(View v) {


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
            WhenEventActivity activity = ((WhenEventActivity)getActivity());
            activity.mSelectedDate = calendar;
            activity.populateUi();
        }
    }
    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener{
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //return super.onCreateDialog(savedInstanceState);
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR);
            int minute = c.get(Calendar.MINUTE);
//            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new TimePickerDialog(getActivity(),this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
//            return new TimePickerDialog(getActivity(), this, hour, minute);
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//            Calendar calendar = Calendar.getInstance();
//            calendar.set(hourOfDay, minute);
//            WhenEventActivity activity = ((WhenEventActivity)getActivity());
//            activity.mSelectedDate = calendar;
//            activity.populateUi();

            Calendar calendar = Calendar.getInstance();
            calendar.set(hourOfDay, minute);
            WhenEventActivity activity = ((WhenEventActivity)getActivity());
            activity.mSelectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
            activity.mSelectedDate.set(Calendar.MINUTE, minute);
//            activity.populateUi();

            TextView tv = (TextView) getActivity().findViewById(R.id.activity_new_post_when_txt_time);
            //Display the user changed time on TextView
            tv.setText(String.valueOf(hourOfDay) + " : " + String.valueOf(minute));
        }
    }
}
