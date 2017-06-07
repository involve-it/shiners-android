package org.buzzar.appnative.logic.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by yury on 1/30/17.
 */

public class User implements Parcelable, Serializable {
    private  static final int LOCATION_REPORT_EXPIRATION = 20*60*1000;

    @SerializedName("_id")
    public String id;
    @SerializedName("createdAt")
    public Date createdAt;
    @SerializedName("username")
    public String username;
    @SerializedName("online")
    public boolean online;
    @SerializedName("lastLogin")
    public Date lastLogin;
    @SerializedName("image")
    public Photo image;
    @SerializedName("profileDetails")
    public List<ProfileDetail> profileDetails;
    public Date lastMobileLocationReport;
    public boolean enableNearbyNotifications;
    public boolean isInvisible;

    public User (){}

    protected User(Parcel in) {
        id = in.readString();
        username = in.readString();
        online = in.readByte() != 0;
        image = in.readParcelable(Photo.class.getClassLoader());
        profileDetails = in.createTypedArrayList(ProfileDetail.CREATOR);
        long temp = in.readLong();
        if (temp != 0){
            this.createdAt = new Date(temp);
        }
        temp = in.readLong();
        if (temp != 0){
            this.lastLogin = new Date(temp);
        }
        temp = in.readLong();
        if (temp != 0){
            this.lastMobileLocationReport = new Date(temp);
        }
        enableNearbyNotifications = in.readByte() != 0;
        isInvisible = in.readByte() != 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public Boolean isOnline(){
        Boolean isOnline = false;
        if (!this.isInvisible) {
            if (this.online || (this.lastMobileLocationReport != null && (System.currentTimeMillis() - this.lastMobileLocationReport.getTime()) <= LOCATION_REPORT_EXPIRATION)){
                isOnline = true;
            }
        }

        return isOnline;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(username);
        parcel.writeByte((byte) (online ? 1 : 0));
        parcel.writeParcelable(image, i);
        parcel.writeTypedList(profileDetails);
        if (this.createdAt != null)
            parcel.writeLong(this.createdAt.getTime());
        else
            parcel.writeLong(0);
        if (this.lastLogin != null){
            parcel.writeLong(this.lastLogin.getTime());
        } else {
            parcel.writeLong(0);
        }
        if (this.lastMobileLocationReport != null){
            parcel.writeLong(this.lastMobileLocationReport.getTime());
        } else {
            parcel.writeLong(0);
        }
        //parcel.writeByte((byte)((enableNearbyNotifications == null ? false : enableNearbyNotifications) ? 1 : 0));
        //parcel.writeByte((byte)((isInvisible == null ? false : isInvisible) ? 1 : 0));
        parcel.writeByte((byte) (enableNearbyNotifications ? 1 : 0));
        parcel.writeByte((byte) (isInvisible ? 1 : 0));
    }

    public static class ProfileDetail implements Parcelable, Serializable{
        @SerializedName("_id")
        public String id;
        public String userId;
        public String key;
        public String value;
        public String policy;

        protected ProfileDetail(Parcel in) {
            id = in.readString();
            userId = in.readString();
            key = in.readString();
            value = in.readString();
            policy = in.readString();
        }

        public static final Creator<ProfileDetail> CREATOR = new Creator<ProfileDetail>() {
            @Override
            public ProfileDetail createFromParcel(Parcel in) {
                return new ProfileDetail(in);
            }

            @Override
            public ProfileDetail[] newArray(int size) {
                return new ProfileDetail[size];
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
            parcel.writeString(key);
            parcel.writeString(value);
            parcel.writeString(policy);
        }
    }
}
