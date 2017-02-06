package com.involveit.shiners.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.annotations.SerializedName;
import com.involveit.shiners.R;
import com.involveit.shiners.activities.newpost.NewPostActivity;
import com.involveit.shiners.fragments.MeFragment;
import com.involveit.shiners.fragments.MessagesFragment;
import com.involveit.shiners.fragments.NearbyPostsFragment;
import com.involveit.shiners.fragments.SettingsFragment;
import com.involveit.shiners.fragments.SettingsNotLoggedInFragment;
import com.involveit.shiners.logic.AccountHandler;
import com.involveit.shiners.logic.MeteorBroadcastReceiver;
import com.involveit.shiners.logic.SettingsHandler;
import com.involveit.shiners.services.LocationService;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import im.delight.android.ddp.MeteorSingleton;

public class HomeActivity extends AppCompatActivity implements SettingsFragment.SettingsDelegate {
    private static final int TAB_NEARBY_POSTS = 0;
    private static final int TAB_ME = 1;
    private static final int TAB_MESSAGES = 3;
    private static final int TAB_SETTINGS = 4;
    private static final int TAB_SETTINGS_NOT_LOGGED_IN = 5;

    @BindView(R.id.tabLayout) TabLayout tabLayout;

    private int mLastTabSelected = -1;
    private ArrayMap<Integer, Fragment> mFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startService(new Intent(this, LocationService.class));

        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        displayView(SettingsHandler.getIntSetting(this, SettingsHandler.HOME_PAGE_INDEX));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 2){
                    tabLayout.getTabAt(mLastTabSelected).select();
                    startActivity(new Intent(HomeActivity.this, NewPostActivity.class));
                } else {
                    SettingsHandler.setIntSetting(HomeActivity.this, SettingsHandler.HOME_PAGE_INDEX, position);
                    displayView(position);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        refreshLoggedInStatus();
    }

    private void refreshLoggedInStatus(){
        Boolean isLoggedIn;
        if (MeteorSingleton.getInstance().isConnected()){
            isLoggedIn = MeteorSingleton.getInstance().isLoggedIn();
        } else {
            isLoggedIn = SettingsHandler.getStringSetting(this, SettingsHandler.USERNAME) != null;
        }

        if (isLoggedIn){
            ((ViewGroup)tabLayout.getChildAt(0)).getChildAt(1).setVisibility(View.VISIBLE);
            ((ViewGroup)tabLayout.getChildAt(0)).getChildAt(3).setVisibility(View.VISIBLE);
        } else {
            ((ViewGroup)tabLayout.getChildAt(0)).getChildAt(1).setVisibility(View.GONE);
            ((ViewGroup)tabLayout.getChildAt(0)).getChildAt(3).setVisibility(View.GONE);
        }
    }

    private Fragment getExistingFragment(int position){
        if (mFragments == null){
            mFragments = new ArrayMap<>();
        }

        return mFragments.get(position);
    }

    private void displayView(int position) {
        if (mLastTabSelected != position) {
            String actionBarTitle[] = getResources().getStringArray(R.array.tab_titles);

            tabLayout.getTabAt(position).select();

            if (position == TAB_SETTINGS && !AccountHandler.isLoggedIn()) {
                position = TAB_SETTINGS_NOT_LOGGED_IN;
            }

            Fragment fragment = getExistingFragment(position);
            if (fragment == null) {
                switch (position) {
                    case TAB_NEARBY_POSTS:
                        fragment = new NearbyPostsFragment();
                        break;
                    case TAB_ME:
                        fragment = new MeFragment();
                        break;
                    case TAB_MESSAGES:
                        fragment = MessagesFragment.newInstance();
                        break;
                    case TAB_SETTINGS:
                        fragment = SettingsFragment.newInstance();
                        break;
                    case TAB_SETTINGS_NOT_LOGGED_IN:
                        fragment = SettingsNotLoggedInFragment.newInstance();
                        break;
                }
                mFragments.put(position, fragment);
            }

            if (position == TAB_SETTINGS_NOT_LOGGED_IN) {
                getSupportActionBar().hide();
            } else {
                getSupportActionBar().show();
            }

            if (fragment != null) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.navigation_container, fragment);
                fragmentTransaction.commit();
            }

            getSupportActionBar().setTitle(actionBarTitle[position]);
            mLastTabSelected = position;
        }
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
        if (mLastTabSelected == TAB_SETTINGS_NOT_LOGGED_IN){
            displayView(TAB_SETTINGS);
        }
    }

    @Override
    public void onLogout() {
        SettingsHandler.removeSetting(this, SettingsHandler.USERNAME);
        refreshLoggedInStatus();
        displayView(0);
    }

    private MeteorBroadcastReceiver meteorBroadcastReceiver = new MeteorBroadcastReceiver() {
        @Override
        public void connected() {
            refreshLoggedInStatus();
            if (MeteorSingleton.getInstance().isLoggedIn()){
                SettingsHandler.setStringSetting(HomeActivity.this, SettingsHandler.USERNAME, MeteorSingleton.getInstance().getUserId());
            } else {
                SettingsHandler.removeSetting(HomeActivity.this, SettingsHandler.USERNAME);
            }
        }

        @Override
        public void disconnected() {

        }
    };
}
