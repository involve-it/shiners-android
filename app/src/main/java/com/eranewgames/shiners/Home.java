package com.eranewgames.shiners;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.eranewgames.shiners.Fragments.FragmentMe;
import com.eranewgames.shiners.Fragments.FragmentPosts;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Home extends AppCompatActivity {

    @BindView(R.id.tabLayout) TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        ButterKnife.bind(this);
        displayView(0);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case 0: displayView(0); break;
                    case 1: displayView(1); break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void displayView(int position) {
        String actionBarTitle[]={"Posts","Me","","Messages","Settings"};
        Fragment fragment = null;

        switch (position) {
            case 0: fragment = new FragmentPosts(); break;
            case 1: fragment=new FragmentMe(); break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.navigation_container, fragment);
            fragmentTransaction.commit();
        }

        getSupportActionBar().setTitle(actionBarTitle[position]);
    }
}
