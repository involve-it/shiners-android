package org.buzzar.appPrityazhenie.logic.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by yury on 1/30/17.
 */

public class User implements Parcelable, Serializable {
    private  static final int LOCATION_REPORT_EXPIRATION = 20*60*1000;
    @SerializedName("_id")
    public String _id;
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

    @SerializedName("profile")
    public UserProfile profile;
    public Date lastMobileLocationReport;
    public boolean enableNearbyNotifications;
    public boolean isInvisible;

    @SerializedName("test")
    public String test;



    @SerializedName("emails")
    public List<ProfileEmail> emails;



    public User (){}

    protected User(Parcel in) {

        _id = in.readString();
        username = in.readString();
        online = in.readByte() != 0;
        profile = in.readParcelable(UserProfile.class.getClassLoader());
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

        emails = in.createTypedArrayList(ProfileEmail.CREATOR);


    }

    public void deleteProfileDetail(String key){
        ProfileDetail profileDetail = getProfileDetail(key);
        if (profileDetail != null){
            profileDetails.remove(profileDetail);
        }
    }

    public void setProfileDetail(String key, String value){
        if (this.profileDetails == null){
            this.profileDetails = new ArrayList<>();
        }
        ProfileDetail profileDetail = getProfileDetail(key);
        if (profileDetail == null){
            profileDetail = new ProfileDetail();
            this.profileDetails.add(profileDetail);
        }
        profileDetail.key = key;
        profileDetail.value = value;
    }

    public ProfileDetail getProfileDetail(String key){
        if (profileDetails != null && key != null){
            for (ProfileDetail profileDetail : profileDetails) {
                if (key.equals(profileDetail.key)){
                    return profileDetail;
                }
            }
        }

        return null;
    }

    public String getProfileDetailValue(String key){
        ProfileDetail profileDetail = this.getProfileDetail(key);
        if (profileDetail == null){
            return null;
        } else {
            return profileDetail.value;
        }
    }

    public String getFullName(){
        String firstName = getProfileDetailValue(ProfileDetail.FIRST_NAME);
        String lastName = getProfileDetailValue(ProfileDetail.LAST_NAME);
        String fullName = null;
        if (firstName != null){
            fullName = firstName;
        }
        if (lastName != null){
            if (fullName == null){
                fullName = lastName;
            } else {
                fullName += " " + lastName;
            }
        }

        if (fullName != null)
            return fullName;
        else
            return username;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {return new User(in);
        }

        @Override
        public User[] newArray(int size) {return new User[size];
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
        parcel.writeString(_id);
        parcel.writeString(username);
        parcel.writeByte((byte) (online ? 1 : 0));
        parcel.writeParcelable(profile, i);
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
        parcel.writeTypedList(emails);
    }

    public static class  UserProfile implements Parcelable, Serializable {
        @SerializedName("phone")
        public String phone;
        @SerializedName("inviteCode")
        public String inviteCode;
        @SerializedName("city")
        public String city;
        @SerializedName("role")
        public String role;

        UserProfile(){}

        protected UserProfile(Parcel in){
            phone = in.readString();
            inviteCode = in.readString();
            city = in.readString();
            role = in.readString();
        }

        public static final Creator<UserProfile> CREATOR = new Creator<UserProfile>() {
            @Override
            public UserProfile createFromParcel(Parcel in) {return new UserProfile(in);}

            @Override
            public UserProfile[] newArray(int size) {return new UserProfile[size];}
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(phone);
            parcel.writeString(inviteCode);
            parcel.writeString(city);
            parcel.writeString(role);
        }
    }

    public static class ProfileDetail implements Parcelable, Serializable{
        public static final String LAST_NAME = "lastName";
        public static final String FIRST_NAME = "firstName";
        public static final String CITY = "city";
        public static final String PHONE = "phone";
        public static final String SKYPE = "skype";
        public static final String VK = "vk";
        public static final String TELEGRAM = "telegram";
        public static final String TWITTER = "twitter";
        public static final String FACEBOOK ="facebook";

        @SerializedName("_id")
        public String _id;
        public String userId;
        public String key;
        public String value;
        public String policy;

        public ProfileDetail(){}

        protected ProfileDetail(Parcel in) {
            _id = in.readString();
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
            parcel.writeString(_id);
            parcel.writeString(userId);
            parcel.writeString(key);
            parcel.writeString(value);
            parcel.writeString(policy);
        }
    }

    public static class ProfileEmail implements Parcelable, Serializable {

        @SerializedName("address")
        public String address;

        @SerializedName("verified")
        public boolean verified;

        protected ProfileEmail(Parcel in) {
            address = in.readString();
            verified = in.readByte() != 0;
        }

        public static final Creator<ProfileEmail> CREATOR = new Creator<ProfileEmail>() {
            @Override
            public ProfileEmail createFromParcel(Parcel in) {
                return new ProfileEmail(in);
            }

            public ProfileEmail[] newArray(int size) {
                return new ProfileEmail[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(address);
            parcel.writeByte((byte) (verified ? 1 : 0));
        }
    }

}
