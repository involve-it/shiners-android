package org.buzzar.appPrityazhenie.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.buzzar.appPrityazhenie.R;
import org.buzzar.appPrityazhenie.activities.auth.LogInActivity;
import org.buzzar.appPrityazhenie.activities.auth.RegisterActivity;
import org.buzzar.appPrityazhenie.logic.analytics.AnalyticsProvider;
import org.buzzar.appPrityazhenie.logic.Constants;
import org.buzzar.appPrityazhenie.logic.analytics.TrackingKeys;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsNotLoggedInFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsNotLoggedInFragment extends Fragment {
    public SettingsNotLoggedInFragment() {
        // Required empty public constructor
    }

    public static SettingsNotLoggedInFragment newInstance() {
        SettingsNotLoggedInFragment fragment = new SettingsNotLoggedInFragment();
        /*Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);*/
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings_not_logged_in, container, false);
        ButterKnife.bind(this, view);

        AnalyticsProvider.LogScreen(getActivity(), TrackingKeys.Screens.SETTINGS_NOT_LOGGED_IN);

        return view;
    }

    @OnClick({R.id.btn_login, R.id.btnRegister})
    public void OnClick(View view){
        switch (view.getId()){
            case R.id.btn_login:
                getActivity().startActivityForResult(new Intent(getActivity(), LogInActivity.class), Constants.ActivityRequestCodes.LOGIN);
                AnalyticsProvider.LogButtonClick(getActivity(), TrackingKeys.Buttons.LOGIN);
                break;
            case R.id.btnRegister:
                startActivityForResult(new Intent(getActivity(), RegisterActivity.class), Constants.ActivityRequestCodes.REGISTER);
                AnalyticsProvider.LogButtonClick(getActivity(), TrackingKeys.Buttons.REGISTER);
                break;
        }
    }
}
