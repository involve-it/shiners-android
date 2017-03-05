package org.buzzar.appnative.logic.cache;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by yury on 2/2/17.
 */

public final class CachingHandler {
    private static final String TAG = "CachingHandler";
    private static Cache _cache = null;
    private static final String FILENAME = "com.involveit.shiners.CachingHandler.CACHE";
    private static final long FILE_UPDATE_DELAY = 1000;

    private static final Object saveLock = new Object();
    private static final Object setLock = new Object();
    private static final Timer timer = new Timer();
    private static CacheTimerTask timerTask = null;

    public static final String KEY_NEARBY_POSTS = "com.involveit.shiners.CachingHandler.key.NEARBY_POSTS";
    public static final String KEY_MY_POSTS = "com.involveit.shiners.CachingHandler.key.MY_POSTS";
    public static final String KEY_CURRENT_USER = "com.involveit.shiners.CachingHandler.key.CURRENT_USER";
    public static final String KEY_DIALOGS = "com.involveit.shiners.CachingHandler.key.DIALOGS";

    private static void updateFile(final Context context){
        if (_cache != null) {
            if (timerTask != null){
                Log.d(TAG, "Cancelling task with id: " + timerTask.mId);
                timerTask.cancel();
            }

            UUID taskId = UUID.randomUUID();
            Log.d(TAG, "Scheduling task with id: " + taskId);

            timerTask = new CacheTimerTask(taskId) {
                @Override
                public void run() {
                    try {
                        Log.d(TAG, "Running task with id: " + this.getId());
                        FileOutputStream file = new FileOutputStream(new File(context.getFilesDir(), FILENAME), false);
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(file);
                        objectOutputStream.writeObject(_cache);
                        objectOutputStream.close();
                        file.flush();
                        file.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            timer.schedule(timerTask, FILE_UPDATE_DELAY);
        }
    }

    /*private static String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }*/

    private static void initFromFile(Context context){
        try {
            File file = new File(context.getFilesDir(), FILENAME);
            if (file.exists()){
                FileInputStream fileStream = new FileInputStream(file);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileStream);
                _cache = (Cache) objectInputStream.readObject();
                for (Map.Entry<String, CacheEntity> entry: _cache.entrySet()) {
                    entry.getValue().setStale();
                }
                objectInputStream.close();
                fileStream.close();
            } else {
                Log.d(TAG, "Cache file does not exist");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (_cache == null){
            Log.d(TAG, "Cache is empty, creating new object");
            _cache = new Cache();
        }
    }

    public static void init(Context context){
        if (_cache == null){
            synchronized(saveLock){
                if (_cache == null){
                    initFromFile(context);
                }
            }
        }
    }

    public static<T> void setObject(Context context, String key, T object){
        Log.d(TAG, "Setting object for key: " + key);
        init(context);

        synchronized(setLock){
            _cache.put(key, new CacheEntity<>(object));
        }

        synchronized(saveLock) {
            updateFile(context);
        }
    }

    public static<T> CacheEntity<T> getCacheObject(Context context, String key){
        init(context);

        return _cache.get(key);
    }

    public static void removeObject(Context context, String key){
        Log.d(TAG, "Deleting object for key: " + key);
        init(context);

        synchronized (setLock){
            _cache.remove(key);
        }

        synchronized(saveLock) {
            updateFile(context);
        }
    }

    private static class Cache extends HashMap<String, CacheEntity> implements Serializable{
        private static final long serialVersionUID = 6529685098267757690L;
    }

    private static abstract class CacheTimerTask extends TimerTask{
        private UUID mId;

        public UUID getId(){
            return mId;
        }

        private CacheTimerTask(UUID taskId){
            mId = taskId;
        }
    }
}
