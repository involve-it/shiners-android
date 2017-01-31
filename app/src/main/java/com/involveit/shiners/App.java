package com.involveit.shiners;

import android.app.Application;

import java.util.HashMap;
import java.util.Map;

import im.delight.android.ddp.Meteor;

/**
 * Created by Xaker on 04.01.2017.
 */

public class App extends Application {
    public static Meteor meteor;
    public static String homePositionFragment="homePositionFragment";
    public static double locationLat=55.75222,locationLng=37.61556;

    public static Map<String,Object> keyMap=new HashMap<>();
    public static Map<String,Object> keyDetails=new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        meteor = new Meteor(this, "ws://shiners.mobi/websocket");
    }
}
