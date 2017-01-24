package com.eranewgames.shiners.GSON;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Xaker on 22.01.2017.
 */

public class GsonPostsItem {

    @SerializedName("success")
    public boolean success;
    @SerializedName("result")
    public Result result;

    public static class Other {
    }

    public static class Coords {
        @SerializedName("lat")
        public double lat;
        @SerializedName("lng")
        public double lng;
    }

    public static class Locations {
        @SerializedName("_id")
        public String _id;
        @SerializedName("coords")
        public Coords coords;
        @SerializedName("name")
        public String name;
        @SerializedName("placeType")
        public String placeType;
    }

    public static class Photos {
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
    }

    public static class Details {
        @SerializedName("url")
        public String url;
        @SerializedName("title")
        public String title;
        @SerializedName("description")
        public String description;
        @SerializedName("price")
        public String price;
        @SerializedName("other")
        public List<Other> other;
        @SerializedName("locations")
        public List<Locations> locations;
        @SerializedName("photos")
        public List<Photos> photos;
        @SerializedName("photosUrls")
        public List<String> photosUrls;
    }

    public static class Presences {
        @SerializedName("static")
        public String mstatic;
    }

    public static class Status {
        @SerializedName("visible")
        public String visible;
    }

    public static class Timestamp {
        @SerializedName("$date")
        public String $date;
    }

    public static class TimePause {
        @SerializedName("$date")
        public String $date;
    }

    public static class EndDatePost {
        @SerializedName("$date")
        public String $date;
    }

    public static class Stats {
        @SerializedName("seenTotal")
        public int seenTotal;
        @SerializedName("seenToday")
        public int seenToday;
        @SerializedName("seenAll")
        public int seenAll;
    }

    public static class LastEditedTs {
        @SerializedName("$date")
        public String $date;
    }

    public static class TrainingsDetails {
        @SerializedName("sectionLearning")
        public String sectionLearning;
        @SerializedName("typeCategory")
        public String typeCategory;
    }

    public static class CreatedAt {
        @SerializedName("$date")
        public String $date;
    }

    public static class LastLogin {
        @SerializedName("$date")
        public String $date;
    }

    public static class Image {
    }

    public static class ProfileDetails {
    }

    public static class User {
        @SerializedName("_id")
        public String _id;
        @SerializedName("createdAt")
        public CreatedAt createdAt;
        @SerializedName("username")
        public String username;
        @SerializedName("online")
        public boolean online;
        @SerializedName("lastLogin")
        public LastLogin lastLogin;
        @SerializedName("image")
        public Image image;
        @SerializedName("profileDetails")
        public List<ProfileDetails> profileDetails;
    }

    public static class Result {
        @SerializedName("_id")
        public String _id;
        @SerializedName("type")
        public String type;
        @SerializedName("tags")
        public String tags;
        @SerializedName("details")
        public Details details;
        @SerializedName("presences")
        public Presences presences;
        @SerializedName("status")
        public Status status;
        @SerializedName("timestamp")
        public Timestamp timestamp;
        @SerializedName("timePause")
        public TimePause timePause;
        @SerializedName("endDatePost")
        public EndDatePost endDatePost;
        @SerializedName("stats")
        public Stats stats;
        @SerializedName("lastEditedTs")
        public LastEditedTs lastEditedTs;
        @SerializedName("likes")
        public int likes;
        @SerializedName("liked")
        public boolean liked;
        @SerializedName("trainingsDetails")
        public TrainingsDetails trainingsDetails;
        @SerializedName("user")
        public User user;
    }
}
