package com.involveit.shiners.logic.objects;

import com.google.gson.annotations.SerializedName;

/**
 * Created by yury on 2/7/17.
 */

public class LocationReport {
    @SerializedName("lat")
    public double lat;
    @SerializedName("lng")
    public double lng;
    @SerializedName("userId")
    public String userId;
    @SerializedName("deviceId")
    public String deviceId;
    @SerializedName("notify")
    public boolean notify;
}
