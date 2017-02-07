package com.involveit.shiners.activities.auth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.involveit.shiners.R;
import com.involveit.shiners.logic.AccountHandler;
import com.involveit.shiners.logic.MeteorBroadcastReceiver;
import com.involveit.shiners.logic.SettingsHandler;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;

public class RegisterActivity extends AppCompatActivity {
    @BindView(R.id.editText1) EditText editText1;
    @BindView(R.id.editText2) EditText editText2;
    @BindView(R.id.editText3) EditText editText3;
    @BindView(R.id.editText4) EditText editText4;
    @BindView(R.id.button4) Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        if (MeteorSingleton.getInstance().isConnected()){
            btnRegister.setEnabled(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.meteorBroadcastReceiver.unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.meteorBroadcastReceiver.register(this);
    }

    @OnClick(R.id.button4)
    public void onClick() {
        if (editText3.getText().toString().equals(editText4.getText().toString())){
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getResources().getText(R.string.message_registering));
            progressDialog.show();
            progressDialog.setCancelable(false);

            MeteorSingleton.getInstance().registerAndLogin(editText1.getText().toString(),
                editText2.getText().toString(), editText3.getText().toString(), new ResultListener() {
                    @Override
                    public void onSuccess(String result) {
                        SettingsHandler.setStringSetting(RegisterActivity.this, SettingsHandler.USER_ID, MeteorSingleton.getInstance().getUserId());
                        AccountHandler.loadAccount(RegisterActivity.this, new AccountHandler.AccountHandlerDelegate() {
                            @Override
                            public void accountLoaded() {
                                Toast.makeText(RegisterActivity.this, R.string.message_registration_success, Toast.LENGTH_SHORT).show();
                                setResult(Activity.RESULT_OK);
                                progressDialog.dismiss();
                                finish();
                            }

                            @Override
                            public void accountLoadFailed() {
                                progressDialog.dismiss();
                                Toast.makeText(RegisterActivity.this, R.string.message_registration_unsuccessful, Toast.LENGTH_SHORT).show();
                                SettingsHandler.removeSetting(RegisterActivity.this, SettingsHandler.USER_ID);
                            }
                        });
                    }

                    @Override
                    public void onError(String error, String reason, String details) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, R.string.message_registration_unsuccessful, Toast.LENGTH_SHORT).show();
                        SettingsHandler.removeSetting(RegisterActivity.this, SettingsHandler.USER_ID);
                    }
                });
        }
    }

    private MeteorBroadcastReceiver meteorBroadcastReceiver = new MeteorBroadcastReceiver() {
        @Override
        public void connected() {
            btnRegister.setEnabled(true);
        }

        @Override
        public void disconnected() {
            btnRegister.setEnabled(false);
        }
    };
}
