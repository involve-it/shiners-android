package org.buzzar.app.logic.objects;

import com.google.gson.annotations.SerializedName;

/**
 * Created by yury on 2/8/17.
 */

public class GcmPayload {
    @SerializedName("type")
    public String type;
    @SerializedName("id")
    public String id;
}
