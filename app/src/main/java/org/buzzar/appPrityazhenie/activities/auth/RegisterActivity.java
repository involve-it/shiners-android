package org.buzzar.appPrityazhenie.activities.auth;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.view.View;

import org.buzzar.appPrityazhenie.R;
import org.buzzar.appPrityazhenie.logic.AccountHandler;
import org.buzzar.appPrityazhenie.logic.SettingsHandler;
import org.buzzar.appPrityazhenie.logic.analytics.AnalyticsProvider;
import org.buzzar.appPrityazhenie.logic.analytics.TrackingKeys;
import org.buzzar.appPrityazhenie.logic.ui.MeteorActivityBase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;

import java.util.HashMap;

public class RegisterActivity extends MeteorActivityBase {
    @BindView(R.id.activity_new_post_txt_title) EditText editText1;
    @BindView(R.id.activity_new_post_txt_description) EditText editText2;
    @BindView(R.id.editText3) EditText editText3;
    @BindView(R.id.editText4) EditText editText4;
    @BindView(R.id.editText5) EditText editText5;
    @BindView(R.id.editText6) EditText editText6;
    @BindView(R.id.button4) Button btnRegister;
    @BindView(R.id.spinner) Spinner spinnerCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        if (MeteorSingleton.getInstance().isConnected()){
            btnRegister.setEnabled(true);
        }

        //Если нужно программно заполнить spinner
//        String[] data = {"Липецк", "Елец", "Воронеж"};
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
//        spinner.setAdapter(adapter);
        // заголовок
        //spinner.setPrompt("@string/city_spinner");
        // выделяем элемент
        //spinner.setSelection(2);
        // устанавливаем обработчик нажатия
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                // показываем позиция нажатого элемента
                //Toast.makeText(getBaseContext(), "Position = " + position, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        AnalyticsProvider.LogScreen(this, TrackingKeys.Screens.REGISTER);
    }

    @OnClick(R.id.button4)
    public void onClick() {
        if (!editText5.getText().toString().equals("")) {
            if (!editText6.getText().toString().equals("")) {
                if (spinnerCity.getSelectedItemId()!=0) {
                    String selectedCity = spinnerCity.getSelectedItem().toString();
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("phone", editText5.getText().toString());
                    map.put("inviteCode", editText6.getText().toString());
                    //может быть 2 варианта(английское название и русское к примеру: Lipetsk - Липецк)
                    map.put("city", selectedCity);

                    if (editText3.getText().toString().equals(editText4.getText().toString()) && !"".equals(editText3.getText().toString())) {
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
                                editText2.getText().toString(), editText3.getText().toString(), map, new ResultListener() {
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
                                                Toast.makeText(RegisterActivity.this, R.string.message_registration_unsuccessful, Toast.LENGTH_LONG).show();
                                                SettingsHandler.removeSetting(RegisterActivity.this, SettingsHandler.USER_ID);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(String error, String reason, String details) {
                                        progressDialog.dismiss();
                                        String stdTxt = getString(R.string.message_registration_unsuccessful);
                                        Toast.makeText(RegisterActivity.this,  stdTxt + ", детали: " + reason, Toast.LENGTH_LONG).show();
                                        SettingsHandler.removeSetting(RegisterActivity.this, SettingsHandler.USER_ID);
                                    }
                                });
                    }
                }else{
                    //не выбран город
                    new AlertDialog.Builder(this).setMessage(R.string.message_city_not_selected).setTitle(R.string.title_empty_field).setPositiveButton(R.string.txt_ok, null).show();
                }
            } else {
                //не указан регистрационный код
                new AlertDialog.Builder(this).setMessage(R.string.message_empty_regCode).setTitle(R.string.title_empty_field).setPositiveButton(R.string.txt_ok, null).show();
            }
        }else{
            //не заполнен номер телефона
            new AlertDialog.Builder(this).setMessage(R.string.message_empty_phone).setTitle(R.string.title_empty_field).setPositiveButton(R.string.txt_ok, null).show();

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
