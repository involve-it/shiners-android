package org.buzzar.appnative.logic.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by yury on 1/30/17.
 */

public class Location implements Parcelable, Serializable {
    public static final String LOCATION_TYPE_STATIC = "static";
    public static final String LOCATION_TYPE_DYNAMIC = "dynamic";

    @SerializedName("_id")
    public String id;
    @SerializedName("coords")
    public Coords coords;
    @SerializedName("name")
    public String name;
    @SerializedName("placeType")
    public String placeType;

    protected Location(Parcel in) {
        id = in.readString();
        name = in.readString();
        placeType = in.readString();
        this.coords = new Coords();
        this.coords.lat = in.readDouble();
        this.coords.lng = in.readDouble();
    }

    public static final Creator<Location> CREATOR = new Creator<Location>() {
        @Override
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(placeType);
        parcel.writeDouble(this.coords.lat);
        parcel.writeDouble(this.coords.lng);
    }

    public static class Coords implements Serializable {
        @SerializedName("lat")
        public double lat;
        @SerializedName("lng")
        public double lng;
    }

    public android.location.Location getAndroidLocation(){
        android.location.Location location = new android.location.Location("");
        location.setLatitude(this.coords.lat);
        location.setLongitude(this.coords.lng);
        return location;
    }

    public float distanceFrom(Double lat, Double lng){
        android.location.Location location = new android.location.Location("");
        location.setLatitude(lat);
        location.setLongitude(lng);

        return location.distanceTo(this.getAndroidLocation());
    }
}
