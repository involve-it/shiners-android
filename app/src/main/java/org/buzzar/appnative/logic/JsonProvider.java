package org.buzzar.appnative.logic;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yury on 1/30/17.
 */

final public class JsonProvider {
    private static final String dateFormatString = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    private static final DateFormat dateFormat = new SimpleDateFormat(dateFormatString);
    public static final Gson defaultGson =
            new GsonBuilder()
                    .setDateFormat(dateFormatString)
                    .registerTypeAdapter(Date.class, new DateJsonDeserializer())
                    .create();

    private static class DateJsonDeserializer implements JsonDeserializer<Date>{
        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                JsonElement element = json.getAsJsonObject().get("$date");
                if (element == JsonNull.INSTANCE){
                    return null;
                } else {
                    return new Date(element.getAsLong());
                }
            }
            catch (Exception ignored){
                try {
                    Long dateLong = json.getAsLong();
                    if (dateLong > 0){
                        return new Date(dateLong);
                    } else {
                        return null;
                    }
                }
                catch (Exception ignored2){
                    String dateString = json.getAsJsonPrimitive().getAsString();
                    try {
                        return dateFormat.parse(dateString);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        }
    }
}
