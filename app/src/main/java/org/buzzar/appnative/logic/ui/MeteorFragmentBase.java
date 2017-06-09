package org.buzzar.appnative.logic.ui;

import android.support.v4.app.Fragment;

import im.delight.android.ddp.ResultListener;

/**
 * Created by yury on 6/9/17.
 */

public abstract class MeteorFragmentBase extends Fragment {
    protected MeteorActivityBase getMeteorActivity(){
        return (MeteorActivityBase)getActivity();
    }

    protected boolean callMeteorMethod(String methodName, Object[] parameters, ResultListener resultListener){
        return getMeteorActivity().callMeteorMethod(methodName, parameters, resultListener);
    }
}
