package org.buzzar.appPrityazhenie.activities.auth;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.buzzar.appPrityazhenie.R;
import org.buzzar.appPrityazhenie.logic.AccountHandler;
import org.buzzar.appPrityazhenie.logic.Constants;
import org.buzzar.appPrityazhenie.logic.SettingsHandler;
import org.buzzar.appPrityazhenie.logic.analytics.AnalyticsProvider;
import org.buzzar.appPrityazhenie.logic.analytics.TrackingKeys;
import org.buzzar.appPrityazhenie.logic.ui.MeteorActivityBase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;

public class LogInActivity extends MeteorActivityBase {
    @BindView(R.id.txtUsername) EditText editTextLogin;
    @BindView(R.id.txtPassword) EditText editTextPass;
    @BindView(R.id.button2) Button buttonLogIn;
    @BindView(R.id.button3) Button buttonRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        ButterKnife.bind(this);

        editTextLogin.requestFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();

        AnalyticsProvider.LogScreen(this, TrackingKeys.Screens.LOG_IN);
    }

    @OnClick({R.id.button2, R.id.button3})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button2:
                if (!MeteorSingleton.getInstance().isConnected()) {
                    MeteorSingleton.getInstance().reconnect();
                    new AlertDialog.Builder(this).setMessage(R.string.msg_not_connected).setTitle(R.string.title_oops).setPositiveButton(R.string.txt_ok, null).show();
                    return;
                }

                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setMessage(getResources().getText(R.string.message_logging_in));
                progressDialog.show();
                progressDialog.setCancelable(false);
                MeteorSingleton.getInstance().loginWithUsername(editTextLogin.getText().toString(), editTextPass.getText().toString(), new ResultListener() {
                    @Override
                    public void onSuccess(String result) {
                        SettingsHandler.setStringSetting(LogInActivity.this, SettingsHandler.USER_ID, MeteorSingleton.getInstance().getUserId());
                        AccountHandler.loadAccount(LogInActivity.this, new AccountHandler.AccountHandlerDelegate() {
                            @Override
                            public void accountLoaded() {
                                Toast.makeText(LogInActivity.this, R.string.message_authentication_successful, Toast.LENGTH_SHORT).show();
                                setResult(Activity.RESULT_OK);
                                progressDialog.dismiss();
                                AnalyticsProvider.LogLogIn(LogInActivity.this);
                                finish();
                            }

                            @Override
                            public void accountLoadFailed() {
                                progressDialog.dismiss();
                                Toast.makeText(LogInActivity.this, R.string.message_authentication_error, Toast.LENGTH_SHORT).show();
                                SettingsHandler.removeSetting(LogInActivity.this, SettingsHandler.USER_ID);
                            }
                        });
                    }

                    @Override
                    public void onError(String error, String reason, String details) {
                        progressDialog.dismiss();
                        Toast.makeText(LogInActivity.this, R.string.message_authentication_error, Toast.LENGTH_SHORT).show();
                        SettingsHandler.removeSetting(LogInActivity.this, SettingsHandler.USER_ID);
                    }
                });
                break;
            case R.id.button3:
                startActivityForResult(new Intent(this, RegisterActivity.class), Constants.ActivityRequestCodes.REGISTER);
                AnalyticsProvider.LogButtonClick(this, TrackingKeys.Buttons.REGISTER);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.ActivityRequestCodes.REGISTER && resultCode == RESULT_OK){
            setResult(RESULT_OK);
            finish();
        }
    }

    @OnEditorAction({R.id.txtPassword, R.id.txtUsername})
    public boolean onEditorAction(TextView view, int actionId, KeyEvent event){
        boolean handled = false;
        switch (view.getId()){
            case R.id.txtPassword:
                editTextPass.clearFocus();
                onClick(buttonLogIn);
                handled = true;
                break;
            case R.id.txtUsername:
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    editTextPass.requestFocus();
                    handled = true;
                }
                break;
        }

        return handled;
    }

    @Override
    protected void meteorConnected() {
        super.meteorConnected();
    }

    @Override
    protected void meteorDisconnected() {
        super.meteorDisconnected();
    }
}
