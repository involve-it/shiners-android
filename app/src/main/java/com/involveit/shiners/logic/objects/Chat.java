package com.involveit.shiners.logic.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by yury on 2/1/17.
 */

public class Chat implements Parcelable, Serializable {
    @SerializedName("_id")
    public String id;
    @SerializedName("userId")
    public String userId;
    @SerializedName("users")
    public ArrayList<String> otherUserIds;
    @SerializedName("created")
    public Date created;
    @SerializedName("lastMessageTs")
    public Date lastMessageTimestamp;
    @SerializedName("activated")
    public boolean activated;
    @SerializedName("lastMessage")
    public Message lastMessage;
    @SerializedName("otherParty")
    public ArrayList<User> otherParties;
    @SerializedName("seen")
    public boolean seen;
    @SerializedName("toUserId")
    public String toUserId;

    public ArrayList<Message> messages;

    public User getOtherParty(){
        return otherParties.get(0);
    }

    protected Chat(Parcel in) {
        id = in.readString();
        userId = in.readString();
        otherUserIds = in.createStringArrayList();
        activated = in.readByte() != 0;
        lastMessage = in.readParcelable(Message.class.getClassLoader());
        otherParties = in.createTypedArrayList(User.CREATOR);
        seen = in.readByte() != 0;
        toUserId = in.readString();
        messages = in.createTypedArrayList(Message.CREATOR);
        long temp = in.readLong();
        if (temp != 0){
            created = new Date(temp);
        }
        temp = in.readLong();
        if (temp!=0){
            lastMessageTimestamp = new Date(temp);
        }
    }

    public static final Creator<Chat> CREATOR = new Creator<Chat>() {
        @Override
        public Chat createFromParcel(Parcel in) {
            return new Chat(in);
        }

        @Override
        public Chat[] newArray(int size) {
            return new Chat[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(userId);
        parcel.writeStringList(otherUserIds);
        parcel.writeByte((byte) (activated ? 1 : 0));
        parcel.writeParcelable(lastMessage, i);
        parcel.writeTypedList(otherParties);
        parcel.writeByte((byte) (seen ? 1 : 0));
        parcel.writeString(toUserId);
        parcel.writeTypedList(messages);
        if (created == null){
            parcel.writeLong(0);
        } else {
            parcel.writeLong(created.getTime());
        }
        if (lastMessageTimestamp == null){
            parcel.writeLong(0);
        } else {
            parcel.writeLong(lastMessageTimestamp.getTime());
        }
    }
}
