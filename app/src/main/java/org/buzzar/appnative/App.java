package org.buzzar.appnative;

import android.app.Application;
import android.os.AsyncTask;

import org.buzzar.appnative.logic.AccountHandler;
import org.buzzar.appnative.logic.Constants;
import org.buzzar.appnative.logic.cache.CachingHandler;
import org.buzzar.appnative.logic.LocationHandler;
import org.buzzar.appnative.logic.MeteorCallbackHandler;

import java.util.HashMap;
import java.util.Map;

import im.delight.android.ddp.MeteorSingleton;

/**
 * Created by Xaker on 04.01.2017.
 */

public class App extends Application {
    public static String homePositionFragment="homePositionFragment";

    //TODO: remove
    //public static Map<String,Object> keyMap=new HashMap<>();
    //public static Map<String,Object> keyDetails=new HashMap<>();

    private MeteorCallbackHandler meteorCallbackHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                CachingHandler.init(App.this);
            }
        });

        LocationHandler.init(this);
        AccountHandler.initFromCache(this);

        this.meteorCallbackHandler = new MeteorCallbackHandler(this);

        MeteorSingleton.createInstance(this, Constants.Urls.METEOR_URL);
        MeteorSingleton.getInstance().addCallback(this.meteorCallbackHandler);

        MeteorSingleton.getInstance().connect();


    }
}
