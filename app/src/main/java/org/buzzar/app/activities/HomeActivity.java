package org.buzzar.app.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import org.buzzar.app.R;
import org.buzzar.app.activities.newpost.NewPostActivity;
import org.buzzar.app.fragments.MeFragment;
import org.buzzar.app.fragments.MessagesFragment;
import org.buzzar.app.fragments.NearbyPostsFragment;
import org.buzzar.app.fragments.SettingsFragment;
import org.buzzar.app.fragments.SettingsNotLoggedInFragment;
import org.buzzar.app.logic.AccountHandler;
import org.buzzar.app.logic.MeteorBroadcastReceiver;
import org.buzzar.app.logic.SettingsHandler;
import org.buzzar.app.services.SimpleLocationService;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import im.delight.android.ddp.MeteorSingleton;

public class HomeActivity extends AppCompatActivity implements SettingsFragment.SettingsDelegate {
    private static final int TAB_NEARBY_POSTS = 0;
    private static final int TAB_ME = 1;
    private static final int TAB_MESSAGES = 3;
    private static final int TAB_SETTINGS = 4;
    private static final int TAB_SETTINGS_NOT_LOGGED_IN = 5;

    public static final int REQUEST_LOGIN = 1;

    @BindView(R.id.tabLayout) TabLayout tabLayout;

    private int mLastTabSelected = -1;
    private ArrayMap<Integer, Fragment> mFragments;

    private boolean displayNearbyPosts = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayList<String> permissions = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            permissions.addAll(Arrays.asList(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WAKE_LOCK, Manifest.permission.INTERNET));
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.addAll(Arrays.asList(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION));
        } else {
            startService(new Intent(this, SimpleLocationService.class));
        }

        if (permissions.size() > 0){
            requestPermissions(permissions.toArray(new String[permissions.size()]), 1);
        }

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

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int result = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS){
            if (googleApiAvailability.isUserResolvableError(result)){
                googleApiAvailability.getErrorDialog(this, result, 9000).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)){
            startService(new Intent(this, SimpleLocationService.class));
        }
    }

    private void refreshLoggedInStatus(){
        Boolean isLoggedIn;
        if (MeteorSingleton.getInstance().isConnected()){
            isLoggedIn = MeteorSingleton.getInstance().isLoggedIn();
        } else {
            isLoggedIn = SettingsHandler.getStringSetting(this, SettingsHandler.USER_ID) != null;
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

        if (displayNearbyPosts){
            displayView(TAB_NEARBY_POSTS);
            refreshLoggedInStatus();
            displayNearbyPosts = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOGIN && resultCode == Activity.RESULT_OK){
            displayNearbyPosts = true;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onLogout() {
        SettingsHandler.removeSetting(this, SettingsHandler.USER_ID);
        refreshLoggedInStatus();
        displayView(0);
    }

    private MeteorBroadcastReceiver meteorBroadcastReceiver = new MeteorBroadcastReceiver() {
        @Override
        public void connected() {
            refreshLoggedInStatus();
            if (MeteorSingleton.getInstance().isLoggedIn()){
                SettingsHandler.setStringSetting(HomeActivity.this, SettingsHandler.USER_ID, MeteorSingleton.getInstance().getUserId());
            } else {
                SettingsHandler.removeSetting(HomeActivity.this, SettingsHandler.USER_ID);
            }
        }

        @Override
        public void disconnected() {

        }
    };
}
