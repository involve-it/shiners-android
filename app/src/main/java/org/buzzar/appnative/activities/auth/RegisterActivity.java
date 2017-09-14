package org.buzzar.appnative.activities.auth;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.buzzar.appnative.R;
import org.buzzar.appnative.logic.AccountHandler;
import org.buzzar.appnative.logic.SettingsHandler;
import org.buzzar.appnative.logic.analytics.AnalyticsProvider;
import org.buzzar.appnative.logic.analytics.TrackingKeys;
import org.buzzar.appnative.logic.ui.MeteorActivityBase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;

public class RegisterActivity extends MeteorActivityBase {
    @BindView(R.id.activity_new_post_txt_title) EditText editText1;
    @BindView(R.id.activity_new_post_txt_description) EditText editText2;
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
    protected void onResume() {
        super.onResume();

        AnalyticsProvider.LogScreen(this, TrackingKeys.Screens.REGISTER);
    }

    @OnClick(R.id.button4)
    public void onClick() {
        if (editText3.getText().toString().equals(editText4.getText().toString()) && !"".equals(editText3.getText().toString())){
            if (!MeteorSingleton.getInstance().isConnected()) {
                MeteorSingleton.getInstance().reconnect();
                new AlertDialog.Builder(this).setMessage(R.string.msg_not_connected).setTitle(R.string.title_oops).setPositiveButton(R.string.txt_ok, null).show();
                return;
            }

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
                                AnalyticsProvider.LogRegister(RegisterActivity.this);
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

    @Override
    protected void meteorConnected() {
        super.meteorConnected();
        btnRegister.setEnabled(true);
    }

    @Override
    protected void meteorDisconnected() {
        super.meteorDisconnected();
        btnRegister.setEnabled(false);
    }
}
