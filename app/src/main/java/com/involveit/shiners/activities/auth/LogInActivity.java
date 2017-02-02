package com.involveit.shiners.activities.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.involveit.shiners.App;
import com.involveit.shiners.activities.HomeActivity;
import com.involveit.shiners.R;
import com.involveit.shiners.logic.MeteorBroadcastReceiver;
import com.involveit.shiners.logic.SettingsHandler;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;

public class LogInActivity extends AppCompatActivity {

    @BindView(R.id.txtUsername) EditText editTextLogin;
    @BindView(R.id.txtPassword) EditText editTextPass;
    @BindView(R.id.button2) Button buttonLogIn;
    @BindView(R.id.button3) Button buttonRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        ButterKnife.bind(this);

        if (MeteorSingleton.getInstance().isConnected()){
            buttonLogIn.setEnabled(true);
        }

        editTextLogin.requestFocus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        meteorBroadcastReceiver.unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        meteorBroadcastReceiver.register(this);
    }

    @OnClick({R.id.button2, R.id.button3})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button2:
                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setMessage(getResources().getText(R.string.message_logging_in));
                progressDialog.show();
                progressDialog.setCancelable(false);
                MeteorSingleton.getInstance().loginWithUsername(editTextLogin.getText().toString(), editTextPass.getText().toString(), new ResultListener() {
                    @Override
                    public void onSuccess(String result) {
                        Toast.makeText(LogInActivity.this, R.string.message_authentication_successful, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LogInActivity.this, HomeActivity.class)
                            .putExtra(App.homePositionFragment,1));
                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                        SettingsHandler.setStringSetting(LogInActivity.this, SettingsHandler.USERNAME, MeteorSingleton.getInstance().getUserId());
                        progressDialog.dismiss();
                        finish();
                    }

                    @Override
                    public void onError(String error, String reason, String details) {
                        progressDialog.dismiss();
                        Toast.makeText(LogInActivity.this, R.string.message_authentication_error, Toast.LENGTH_SHORT).show();
                        SettingsHandler.removeSetting(LogInActivity.this, SettingsHandler.USERNAME);
                    }
                });
                break;
            case R.id.button3:
                startActivity(new Intent(this, RegisterActivity.class));
                //overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                break;
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

    private MeteorBroadcastReceiver meteorBroadcastReceiver = new MeteorBroadcastReceiver() {
        @Override
        public void connected() {
            buttonLogIn.setEnabled(true);
        }

        @Override
        public void disconnected() {
            buttonLogIn.setEnabled(false);
        }
    };
}
