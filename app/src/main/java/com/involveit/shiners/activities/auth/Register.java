package com.involveit.shiners.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.involveit.shiners.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;

public class Register extends AppCompatActivity {
    @BindView(R.id.editText1) EditText editText1;
    @BindView(R.id.editText2) EditText editText2;
    @BindView(R.id.editText3) EditText editText3;
    @BindView(R.id.editText4) EditText editText4;
    @BindView(R.id.button4) Button button4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.button4)
    public void onClick() {
        if (editText3.getText().toString().equals(editText4.getText().toString())){
            MeteorSingleton.getInstance().registerAndLogin(editText1.getText().toString(),
                editText2.getText().toString(), editText3.getText().toString(), new ResultListener() {

                    @Override
                    public void onSuccess(String result) {
                        Toast.makeText(Register.this, R.string.message_registration_success, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Register.this, LogIn.class));
                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                        finish();
                    }

                    @Override
                    public void onError(String error, String reason, String details) {
                        Toast.makeText(Register.this, R.string.message_registration_unsuccessful, Toast.LENGTH_SHORT).show();
                    }

                });
        }
    }
}
