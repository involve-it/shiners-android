package org.buzzar.appnative.logic.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yury on 2/1/17.
 */

public class Message implements Parcelable, Serializable, UniqueIdContainer {
    @SerializedName("_id")
    public String _id;
    @SerializedName("userId")
    public String userId;
    @SerializedName("toUserId")
    public String toUserId;
    @SerializedName("chatId")
    public String chatId;
    @SerializedName("text")
    public String text;
    @SerializedName("timestamp")
    public Date timestamp;
    @SerializedName("keyMessage")
    public String keyMessage;
    @SerializedName("seen")
    public boolean seen;
    @SerializedName("associatedPostId")
    public String associatedPostId;

    public Message(){}

    protected Message(Parcel in) {
        _id = in.readString();
        userId = in.readString();
        toUserId = in.readString();
        chatId = in.readString();
        text = in.readString();
        keyMessage = in.readString();
        seen = in.readByte() != 0;
        associatedPostId = in.readString();
        long time = in.readLong();
        if (time != 0){
            timestamp = new Date(time);
        }
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(_id);
        parcel.writeString(userId);
        parcel.writeString(toUserId);
        parcel.writeString(chatId);
        parcel.writeString(text);
        parcel.writeString(keyMessage);
        parcel.writeByte((byte) (seen ? 1 : 0));
        parcel.writeString(associatedPostId);
        if (timestamp != null){
            parcel.writeLong(timestamp.getTime());
        } else {
            parcel.writeLong(0);
        }
    }

    @Override
    public String getId() {
        return _id;
    }
}
