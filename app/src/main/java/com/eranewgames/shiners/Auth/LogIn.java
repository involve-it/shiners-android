package com.eranewgames.shiners.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.eranewgames.shiners.App;
import com.eranewgames.shiners.Home;
import com.eranewgames.shiners.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.delight.android.ddp.ResultListener;

public class LogIn extends AppCompatActivity {

    @BindView(R.id.editText) EditText editTextLogin;
    @BindView(R.id.editText2) EditText editTextPass;
    @BindView(R.id.button2) Button buttonLogIn;
    @BindView(R.id.button3) Button buttonRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.button2, R.id.button3})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button2:
                App.meteor.loginWithUsername(editTextLogin.getText().toString(), editTextPass.getText().toString(), new ResultListener() {
                    @Override
                    public void onSuccess(String result) {
                        System.out.println("Successfully logged in: " + result);
                        Toast.makeText(LogIn.this, "Авторизация прошла успешно!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LogIn.this, Home.class)
                            .putExtra(App.homePositionFragment,1));
                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                        finish();
                    }

                    @Override
                    public void onError(String error, String reason, String details) {
                        Toast.makeText(LogIn.this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case R.id.button3:
                startActivity(new Intent(LogIn.this, Register.class));
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                break;
        }
    }
}
