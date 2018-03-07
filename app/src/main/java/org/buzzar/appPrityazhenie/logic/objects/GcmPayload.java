package org.buzzar.appPrityazhenie.logic.objects;

import com.google.gson.annotations.SerializedName;

/**
 * Created by yury on 2/8/17.
 */

public class GcmPayload {
    @SerializedName("type")
    public String type;
    @SerializedName("_id")
    public String _id;
}
