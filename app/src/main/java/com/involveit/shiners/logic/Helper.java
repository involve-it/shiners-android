package com.involveit.shiners.logic;

import android.icu.text.DateFormat;
import android.widget.ArrayAdapter;

import com.involveit.shiners.logic.objects.UniqueIdContainer;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by yury on 2/1/17.
 */

public final class Helper {
    public static String formatDate(Date date){
        if (date != null) {
            DateFormat dateFormat = DateFormat.getDateInstance();
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
