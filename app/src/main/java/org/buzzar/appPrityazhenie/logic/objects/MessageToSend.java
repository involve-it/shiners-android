package org.buzzar.appPrityazhenie.logic.objects;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by yury on 6/8/17.
 */

public class MessageToSend implements Serializable {
    @SerializedName("message")
    public String message;
    @SerializedName("type")
    public String type;
    @SerializedName("destinationUserId")
    public String destinationUserId;
    @SerializedName("associatedPostId")
    public String associatedPostId;
}
