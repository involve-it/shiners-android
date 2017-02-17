package com.involveit.shiners.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.involveit.shiners.R;
import com.involveit.shiners.logic.AccountHandler;
import com.involveit.shiners.logic.MeteorBroadcastReceiver;
import com.involveit.shiners.logic.cache.CachingHandler;
import com.involveit.shiners.logic.objects.User;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;

public class SettingsFragment extends Fragment {
    @BindView(R.id.sw_notify_nearby)
    Switch swNotifyNearby;

    @BindView(R.id.sw_invisible_mode)
    Switch swInvisibleMode;

    ProgressDialog progressDialog;

    private static final String TAG = "SettingsFragment";
    public SettingsFragment() {
        // Required empty public constructor
    }

    private SettingsDelegate mDelegate;

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        /*Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);*/
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }*/
    }

    @Override
    public void onPause() {
        super.onPause();
        meteorBroadcastReceiver.unregister(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        meteorBroadcastReceiver.register(getActivity());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);
        User currentUser = AccountHandler.getCurrentUser();
        if (currentUser != null) {
            updateControls(currentUser);
        } else {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getResources().getText(R.string.message_loading_account));
            progressDialog.show();
            progressDialog.setCancelable(false);
        }
        return view;
    }

    private void updateControls(final User currentUser){
        swNotifyNearby.post(new Runnable() {
            @Override
            public void run() {
                swNotifyNearby.setChecked(currentUser.enableNearbyNotifications);
                swInvisibleMode.setChecked(currentUser.isInvisible);
            }
        });
    }

    @OnClick({R.id.btn_logout})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_logout:
                new AlertDialog.Builder(getActivity()).setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.title_logout)
                        .setMessage(R.string.message_logout_confirmation)
                        .setPositiveButton(R.string.btn_label_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                logOut();
                            }
                        })
                        .setNegativeButton(R.string.btl_label_no, null)
                        .show();
                break;
        }
    }

    private void logOut(){
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getResources().getText(R.string.message_logging_out));
        progressDialog.show();
        progressDialog.setCancelable(false);

        MeteorSingleton.getInstance().logout(new ResultListener() {
            @Override
            public void onSuccess(String result) {
                AccountHandler.logoff(getActivity());
                progressDialog.dismiss();
                mDelegate.onLogout();
            }

            @Override
            public void onError(String error, String reason, String details) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), R.string.message_internal_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SettingsDelegate) {
            mDelegate = (SettingsDelegate) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SettingsDelegate");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDelegate = null;
    }

    public interface SettingsDelegate{
        void onLogout();
    }

    private MeteorBroadcastReceiver meteorBroadcastReceiver = new MeteorBroadcastReceiver() {
        @Override
        public void connected() {
            updateControls(AccountHandler.getCurrentUser());
        }

        @Override
        public void disconnected() {

        }
    };
}
