package com.involveit.shiners;

import android.app.Application;

import com.involveit.shiners.logic.LocationHandler;
import com.involveit.shiners.logic.MeteorCallbackHandler;

import java.util.HashMap;
import java.util.Map;

import im.delight.android.ddp.MeteorSingleton;

/**
 * Created by Xaker on 04.01.2017.
 */

public class App extends Application {
    private static final String METEOR_URL = "ws://shiners.mobi/websocket";

    public static String homePositionFragment="homePositionFragment";

    //TODO: remove
    public static Map<String,Object> keyMap=new HashMap<>();
    public static Map<String,Object> keyDetails=new HashMap<>();

    private MeteorCallbackHandler meteorCallbackHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        LocationHandler.init(this);

        this.meteorCallbackHandler = new MeteorCallbackHandler(this);

        MeteorSingleton.createInstance(this, METEOR_URL);
        MeteorSingleton.getInstance().addCallback(this.meteorCallbackHandler);

        MeteorSingleton.getInstance().connect();
    }
}
