package com.involveit.shiners;

import android.app.Application;
import android.content.Intent;

import com.involveit.shiners.Logic.MeteorCallbackHandler;
import com.involveit.shiners.Services.LocationService;

import java.util.HashMap;
import java.util.Map;

import im.delight.android.ddp.Meteor;
import im.delight.android.ddp.MeteorSingleton;

/**
 * Created by Xaker on 04.01.2017.
 */

public class App extends Application {
    private static final String METEOR_URL = "ws://shiners.mobi/websocket";

    public static String homePositionFragment="homePositionFragment";
    public static double locationLat=55.75222,locationLng=37.61556;

    public static Map<String,Object> keyMap=new HashMap<>();
    public static Map<String,Object> keyDetails=new HashMap<>();

    private MeteorCallbackHandler meteorCallbackHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        this.meteorCallbackHandler = new MeteorCallbackHandler(this);

        MeteorSingleton.createInstance(this, METEOR_URL);
        MeteorSingleton.getInstance().addCallback(this.meteorCallbackHandler);

        MeteorSingleton.getInstance().connect();

        Intent intent = new Intent(this, LocationService.class);
        startService(intent);
    }
}
