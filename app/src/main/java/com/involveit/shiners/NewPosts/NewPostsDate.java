package com.involveit.shiners.NewPosts;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;

import com.involveit.shiners.App;
import com.involveit.shiners.R;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewPostsDate extends AppCompatActivity {

    @BindView(R.id.datePicker) DatePicker datePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_posts_date);
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
            Calendar calendar= Calendar.getInstance();
            calendar.set(datePicker.getYear(),datePicker.getMonth(),datePicker.getDayOfMonth());
            App.keyMap.put("endDatePost", calendar.getTimeInMillis()/1000 );

            startActivity(new Intent(NewPostsDate.this, NewPostsPhoto.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.m_new_posts_text, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
