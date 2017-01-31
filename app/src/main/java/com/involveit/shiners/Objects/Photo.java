package com.involveit.shiners.Objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by yury on 1/30/17.
 */

public class Photo implements Parcelable {
    @SerializedName("_id")
    public String _id;
    @SerializedName("userId")
    public String userId;
    @SerializedName("name")
    public String name;
    @SerializedName("data")
    public String original;
    @SerializedName("thumbnail")
    public String thumbnail;

    protected Photo(Parcel in) {
        _id = in.readString();
        userId = in.readString();
        name = in.readString();
        original = in.readString();
        thumbnail = in.readString();
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
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
        parcel.writeString(name);
        parcel.writeString(original);
        parcel.writeString(thumbnail);
    }
}
