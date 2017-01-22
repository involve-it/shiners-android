package com.eranewgames.shiners.NewPosts;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.eranewgames.shiners.App;
import com.eranewgames.shiners.R;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.eranewgames.shiners.App.keyMap;

public class NewPostsText extends AppCompatActivity {

    @BindView(R.id.editText1) EditText editText1;
    @BindView(R.id.editText2) EditText editText2;
    @BindView(R.id.spinner) AppCompatSpinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_posts_text);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        String test[]={"categ1","categ2","categ1","categ2"};
        spinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,test));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            onBackPressed();
        }

        if (item.getItemId()==R.id.next){
            keyMap.clear();
            keyMap.put("userId",App.meteor.getUserId());
            keyMap.put("type","all");
            App.keyDetails.put("title",editText1.getText().toString());
            App.keyDetails.put("description",editText2.getText().toString());

            startActivity(new Intent(NewPostsText.this, NewPostsLocation.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.m_new_posts_text,menu);
        return super.onCreateOptionsMenu(menu);
    }

}
