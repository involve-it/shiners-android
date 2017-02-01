package com.involveit.shiners.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.involveit.shiners.App;
import com.involveit.shiners.R;
import com.involveit.shiners.activities.newpost.NewPostActivity;
import com.involveit.shiners.fragments.MeFragment;
import com.involveit.shiners.fragments.NearbyPostsFragment;
import com.involveit.shiners.fragments.SettingsFragment;
import com.involveit.shiners.fragments.SettingsNotLoggedInFragment;
import com.involveit.shiners.logic.Constants;
import com.involveit.shiners.logic.MeteorBroadcastReceiver;
import com.involveit.shiners.logic.SettingsHandler;

import butterknife.BindView;
import butterknife.ButterKnife;
import im.delight.android.ddp.MeteorSingleton;

public class HomeActivity extends AppCompatActivity implements SettingsFragment.SettingsDelegate {
    @BindView(R.id.tabLayout) TabLayout tabLayout;

    private int mLastTabSelected = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        displayView(0);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 2){
                    tabLayout.getTabAt(mLastTabSelected).select();
                    startActivity(new Intent(HomeActivity.this, NewPostActivity.class));
                } else {
                    displayView(tab.getPosition());
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
            isLoggedIn = SettingsHandler.getSetting(this, SettingsHandler.USERNAME) != null;
        }

        if (isLoggedIn){
            ((ViewGroup)tabLayout.getChildAt(0)).getChildAt(1).setVisibility(View.VISIBLE);
            ((ViewGroup)tabLayout.getChildAt(0)).getChildAt(3).setVisibility(View.VISIBLE);
        } else {
            ((ViewGroup)tabLayout.getChildAt(0)).getChildAt(1).setVisibility(View.GONE);
            ((ViewGroup)tabLayout.getChildAt(0)).getChildAt(3).setVisibility(View.GONE);
        }
    }

    private void displayView(int position) {
        if (mLastTabSelected != position) {
            String actionBarTitle[] = getResources().getStringArray(R.array.tab_titles);
            Fragment fragment = null;
            tabLayout.getTabAt(position).select();

            Boolean hideActionBar = false;
            switch (position) {
                case 0:
                    fragment = new NearbyPostsFragment();
                    break;
                case 1:
                    fragment = new MeFragment();
                    break;
                case 4:
                    if (MeteorSingleton.getInstance().isLoggedIn()) {
                        fragment = SettingsFragment.newInstance();
                    } else {
                        hideActionBar = true;
                        fragment = SettingsNotLoggedInFragment.newInstance();
                    }
                    break;
            }

            if (hideActionBar) {
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
                SettingsHandler.setSetting(HomeActivity.this, SettingsHandler.USERNAME, MeteorSingleton.getInstance().getUserId());
            } else {
                SettingsHandler.removeSetting(HomeActivity.this, SettingsHandler.USERNAME);
            }
        }

        @Override
        public void disconnected() {

        }
    };
}
