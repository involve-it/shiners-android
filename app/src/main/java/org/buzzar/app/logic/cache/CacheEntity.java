package org.buzzar.app.logic.cache;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yury on 2/2/17.
 */

public class CacheEntity<T> implements Serializable {
    private static final long serialVersionUID = 7529685098267757690L;
    private T mObject;
    private Date mTimestamp;
    private boolean mStale;

    public void setObject(T object){
        mObject = object;
        mTimestamp = new Date();
        mStale = false;
    }

    public T getObject(){
        return mObject;
    }

    public Date getTimestamp(){
        return mTimestamp;
    }

    public void setStale(){
        mStale = true;
    }

    public boolean isStale(){
        return mStale;
    }

    public CacheEntity(T object){
        setObject(object);
    }
}
