package org.buzzar.appnative.logic.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by yury on 1/30/17.
 */

public class Post implements Parcelable, Serializable, UniqueIdContainer {
    @SerializedName("_id")
    public String id;
    @SerializedName("type")
    public String type;
    @SerializedName("tags")
    public ArrayList<String> tags;
    @SerializedName("details")
    public PostDetails details;
    @SerializedName("presences")
    public Presences presences;
    @SerializedName("status")
    public Status status;
    @SerializedName("timestamp")
    public Date timestamp;
    @SerializedName("timePause")
    public Date timePause;
    @SerializedName("endDatePost")
    public Date endDatePost;
    @SerializedName("stats")
    public Stats stats;
    @SerializedName("lastEditedTs")
    public Date lastEditedTs;
    @SerializedName("likes")
    public int likes;
    @SerializedName("liked")
    public boolean liked;
    @SerializedName("user")
    public User user;

    public Post(){
        details = new PostDetails();
        presences = new Presences();
        status = new Status();
        user = new User();
        tags = new ArrayList<>();
        stats = new Stats();
    }

    protected Post(Parcel in) {
        id = in.readString();
        type = in.readString();
        tags = new ArrayList<>();
        in.readStringList(tags);
        details = in.readParcelable(PostDetails.class.getClassLoader());
        likes = in.readInt();
        liked = in.readByte() != 0;
        user = in.readParcelable(User.class.getClassLoader());
        this.presences = new Presences();
        this.presences.dynamic = in.readString();
        this.presences.mstatic = in.readString();
        this.status = new Status();
        this.status.visible = in.readString();
        long temp = in.readLong();
        if (temp != 0){
            this.timestamp = new Date(temp);
        }
        temp = in.readLong();
        if (temp != 0){
            this.timePause = new Date(temp);
        }
        temp = in.readLong();
        if (temp != 0){
            this.endDatePost = new Date(temp);
        }
        this.stats = new Stats();
        this.stats.seenAll = in.readInt();
        this.stats.seenToday = in.readInt();
        this.stats.seenTotal = in.readInt();
        temp = in.readLong();
        if (temp != 0){
            this.lastEditedTs = new Date(temp);
        }
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(type);
        if (tags != null){
            parcel.writeStringList(tags);
        } else {
            parcel.writeStringList(new ArrayList<String>());
        }
        parcel.writeParcelable(details, i);
        parcel.writeInt(likes);
        parcel.writeByte((byte) (liked ? 1 : 0));
        parcel.writeParcelable(user, i);

        parcel.writeString(this.presences.dynamic);
        parcel.writeString(this.presences.mstatic);
        parcel.writeString(this.status.visible);
        if (this.timestamp != null){
            parcel.writeLong(this.timestamp.getTime());
        } else {
            parcel.writeLong(0);
        }
        if (this.timePause != null){
            parcel.writeLong(this.timePause.getTime());
        } else {
            parcel.writeLong(0);
        }
        if (this.endDatePost != null){
            parcel.writeLong(this.endDatePost.getTime());
        } else {
            parcel.writeLong(0);
        }
        parcel.writeInt(this.stats.seenAll);
        parcel.writeInt(this.stats.seenToday);
        parcel.writeInt(this.stats.seenTotal);
        if (this.lastEditedTs != null){
            parcel.writeLong(this.lastEditedTs.getTime());
        } else {
            parcel.writeLong(0);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    public static class PostDetails implements Parcelable, Serializable {
        @SerializedName("url")
        public String url;
        @SerializedName("title")
        public String title;
        @SerializedName("description")
        public String description;
        @SerializedName("price")
        public String price;
        @SerializedName("locations")
        public List<Location> locations;
        @SerializedName("photos")
        public List<Photo> photos;
        @SerializedName("photosUrls")
        public List<String> photosUrls;

        public PostDetails(){
            locations = new ArrayList<>();
            photos = new ArrayList<>();
            photosUrls = new ArrayList<>();
        }

        protected PostDetails(Parcel in) {
            url = in.readString();
            title = in.readString();
            description = in.readString();
            price = in.readString();
            locations = in.createTypedArrayList(Location.CREATOR);
            photos = in.createTypedArrayList(Photo.CREATOR);
            photosUrls = in.createStringArrayList();
        }

        public static final Creator<PostDetails> CREATOR = new Creator<PostDetails>() {
            @Override
            public PostDetails createFromParcel(Parcel in) {
                return new PostDetails(in);
            }

            @Override
            public PostDetails[] newArray(int size) {
                return new PostDetails[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(url);
            parcel.writeString(title);
            parcel.writeString(description);
            parcel.writeString(price);
            parcel.writeTypedList(locations);
            parcel.writeTypedList(photos);
            parcel.writeStringList(photosUrls);
        }

        public void removeLocation(String placeType){
            Location location = getLocation(placeType);
            if (location != null){
                locations.remove(location);
            }
        }

        public Location getLocation(String placeType){
            Location location = null;
            if (locations != null){
                for(Location loc: locations){
                    if (Objects.equals(loc.placeType, placeType)){
                        location = loc;
                        break;
                    }
                }
            }

            return location;
        }
    }

    public static class Stats implements Serializable {
        @SerializedName("seenTotal")
        public int seenTotal;
        @SerializedName("seenToday")
        public int seenToday;
        @SerializedName("seenAll")
        public int seenAll;
    }

    public static class Presences implements Serializable {
        @SerializedName("static")
        public String mstatic;
        @SerializedName("dynamic")
        public String dynamic;
    }

    public static class Status implements Serializable {
        @SerializedName("visible")
        public String visible;
    }

    public Location getLocation(){
        Location loc = null;
        if (this.details != null && this.details.locations != null && this.details.locations.size() > 0) {
            for (Location location : this.details.locations){
                loc = location;
                if (Location.LOCATION_TYPE_DYNAMIC.equals(location.placeType)){
                    break;
                }
            }
        }

        return loc;
    }

    private static final String PRESENCE_CLOSE = "close";

    public Boolean isLive(){
        return this.presences != null && (PRESENCE_CLOSE.equals(this.presences.dynamic) || PRESENCE_CLOSE.equals(this.presences.mstatic)) && this.user.isOnline();
    }

    public String getPostType(){
        Location loc = getLocation();
        if (loc != null){
            return loc.placeType;
        }

        return null;
    }

    public Photo getMainPhoto(){
        if (this.details != null && this.details.photos != null && this.details.photos.size() > 0){
            return this.details.photos.get(0);
        }

        return null;
    }
}
