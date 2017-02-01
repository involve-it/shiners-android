package com.involveit.shiners.logic;

import android.icu.text.DateFormat;

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
}
