package org.buzzar.app.logic.objects.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by yury on 1/30/17.
 */

public class ResponseBase<T> {
    @SerializedName("success")
    public Boolean success;
    @SerializedName("result")
    public T result;
    @SerializedName("error")
    public ResponseError error;
}
