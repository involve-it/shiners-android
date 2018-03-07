package org.buzzar.appPrityazhenie.logic.ui;

import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import org.buzzar.appPrityazhenie.R;
import org.buzzar.appPrityazhenie.logic.MeteorBroadcastReceiver;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.ButterKnife;
import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;

/**
 * Created by yury on 6/9/17.
 */

public abstract class MeteorActivityBase extends AppCompatActivity {
    private static final int CONNECTION_WAIT_TIMEOUT_MILLISECONDS = 60 * 1000;
    private ArrayList<MeteorMethodInfo> mPendingMethodCalls = new ArrayList<>();
    private Handler mTimerHandler = new Handler();
    private Runnable mTimeoutHandler = new Runnable() {
        @Override
        public void run() {
            if (mPendingMethodCalls.size() > 0){
                for(MeteorMethodInfo methodInfo : mPendingMethodCalls){
                    if (methodInfo.resultListener != null){
                        methodInfo.resultListener.onError("Connection timeout", "Connection timeout", null);
                    }
                }
                mPendingMethodCalls.clear();
            }
        }
    };

    protected void setActivityDefaults(boolean displayBackButton){
        ButterKnife.bind(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (displayBackButton) {
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
            }
        }
    }

    /**
     * Calls meteor method. Returns true if connection is already established, or false if it's postponing method call until Meteor is connected.
     * @param methodName method name to call
     * @param parameters parameters to method
     * @param resultListener result listener
     * @return true if Meteor is connected
     */
    protected boolean callMeteorMethod(String methodName, Object[] parameters, ResultListener resultListener){
        if (MeteorSingleton.getInstance().isConnected()){
            MeteorSingleton.getInstance().call(methodName, parameters, resultListener);
            return true;
        } else {
            MeteorMethodInfo methodInfo = new MeteorMethodInfo(methodName, parameters, resultListener);
            mPendingMethodCalls.add(methodInfo);

            mTimerHandler.postDelayed(mTimeoutHandler, CONNECTION_WAIT_TIMEOUT_MILLISECONDS);
            return false;
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

    protected void meteorConnected(){}
    protected void meteorDisconnected(){}

    private MeteorBroadcastReceiver meteorBroadcastReceiver = new MeteorBroadcastReceiver() {
        @Override
        public void connected() {
            mTimerHandler.removeCallbacks(mTimeoutHandler);

            meteorConnected();

            if (mPendingMethodCalls.size() > 0){
                for(MeteorMethodInfo methodInfo : mPendingMethodCalls){
                    MeteorSingleton.getInstance().call(methodInfo.methodName, methodInfo.parameters, methodInfo.resultListener);
                }
                mPendingMethodCalls.clear();
            }
        }

        @Override
        public void disconnected() {
            meteorDisconnected();
        }
    };

    private static class MeteorMethodInfo{
        private Calendar timestamp;
        private String methodName;
        private Object[] parameters;
        private ResultListener resultListener;

        private MeteorMethodInfo(String methodName, Object[] parameters, ResultListener resultListener){
            this.methodName = methodName;
            this.parameters = parameters;
            this.resultListener = resultListener;
            this.timestamp = Calendar.getInstance();
        }
    }
}
