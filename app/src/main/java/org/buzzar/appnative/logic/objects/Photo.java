package org.buzzar.appnative.logic.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by yury on 1/30/17.
 */

public class Photo implements Parcelable, Serializable {
    @SerializedName("_id")
    public String _id;
    @SerializedName("userId")
    public String userId;
    @SerializedName("name")
    public String name;
    @SerializedName("data")
    public String data;
    @SerializedName("thumbnail")
    public String thumbnail;
    @SerializedName("imageUrl")
    public String imageUrl;

    public String getImageUrl(){
        if (data == null){
            return imageUrl;
        } else {
            return data;
        }
    }

    public Photo(){}

    protected Photo(Parcel in) {
        _id = in.readString();
        userId = in.readString();
        name = in.readString();
        data = in.readString();
        thumbnail = in.readString();
        imageUrl = in.readString();
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
        parcel.writeString(data);
        parcel.writeString(thumbnail);
        parcel.writeString(imageUrl);
    }
}
