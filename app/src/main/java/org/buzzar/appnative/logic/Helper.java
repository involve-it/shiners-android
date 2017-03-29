package org.buzzar.appnative.logic;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.buzzar.appnative.logic.objects.UniqueIdContainer;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by yury on 2/1/17.
 */

public final class Helper {
    private static final String TAG = "Helper";
    public static String formatDate(Context context, Date date){
        if (date != null) {
            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
            return dateFormat.format(date);
        }

        return "";
    }

    public static<T extends UniqueIdContainer> void mergeDataToArrayAdapter(ArrayList<T> newObjects, ArrayAdapter<T> adapter, boolean replaceAll){
        int index = 0;
        for (T obj : newObjects) {
            if (adapter.getCount() > index){
                T currentObj = adapter.getItem(index);
                if (replaceAll || !currentObj.getId().equals(obj.getId())){
                    adapter.remove(currentObj);
                    adapter.insert(obj, index);
                }
            } else {
                adapter.add(obj);
            }
            index++;
        }
        if (adapter.getCount() > index){
            int total = adapter.getCount();
            for(int i = index; i < total; i++){
                adapter.remove(adapter.getItem(index));
            }
        }

        Log.d(TAG, "Merge complete. Adapter items count: " + adapter.getCount());
    }

    public static<T extends UniqueIdContainer> T find(ArrayList<T> list, String id){
        for (T item : list) {
            if (id.equals(item.getId())){
                return item;
            }
        }

        return null;
    }
}
