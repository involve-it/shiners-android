package org.buzzar.appPrityazhenie.logic;

import java.util.ArrayList;

/**
 * Created by yury on 1/31/17.
 */

public class Constants {
    public static class Defaults{
        public static final int DEFAULT_MESSAGES_PAGE = 40;
        public static final int DEFAULT_POSTS_PAGE = 40;
        public static final int DEFAULT_DIALOGS_PAGE = 50;
        public static final int DEFAULT_MY_POSTS_PAGE = 5;//40;
    }

    public static class Tags{
        public static final String NEW_POST = "shiners:tags.NEW_POST";
    }

    public static class MethodNames {
        public static final String GET_NEARBY_POSTS = "getNearbyPostsTest";
        public static final String GET_NEARBY_POSTS_BY_CITY = "getNearbyPostsByCityAndLocation";
        public static final String GET_MY_POSTS = "getMyPosts";
        public static final String GET_POST = "getPost";
        public static final String DELETE_POST = "deletePost";
        public static final String ADD_POST = "addPost";
        public static final String GET_CHATS = "getChats";
        public static final String GET_CHAT = "getChat";
        public static final String DELETE_CHATS = "deleteChats";
        public static final String GET_MESSAGES = "getMessages";
        public static final String ADD_MESSAGE = "addMessage";
        public static final String GET_USER = "getUser";
        public static final String EDIT_USER = "editUser";
        public static final String MESSAGES_SET_SEEN = "messagesSetSeen";
        public static final String REGISTER_PUSH_TOKEN = "registerPushToken";
        public static final String REGISTER_PUSH_TOKEN_RAIX = "raix:push-update";
        public static final String GET_POST_ATTENDEE_BY_USER_ID = "getPostAttendeeById";
        public static final String SET_POST_ATTENDEE_BY_USER_ID = "setPostAttendee";
    }

    public static class CollectionNames{
        public static final String MESSAGES = "bz.messages";
        public static final String COMMENTS = "bz.reviews";
    }

    public static class Gcm {
        public static class PayloadType {
            public static final String CHAT = "chat";
            public static final String COMMENT = "comment";
            public static final String POST = "post";
        }
        public static final String EXTRA_ID = "_id";
    }

    public static class Urls{
        public static final String BASE_URL = "http://192.168.0.15:3000/";
        public static final String METEOR_URL = "ws://192.168.0.15:3000/websocket";

//        public static final String BASE_URL = "http://127.0.0.1:3000";
//        public static final String METEOR_URL = "ws://127.0.0.1:3000/websocket";

//        public static final String BASE_URL = "https://shiners.mobi";
//        public static final String METEOR_URL = "wss://shiners.mobi/websocket";
    }

    public static final String[] POST_CATEGORIES = new String[]{"jobs", "trainings", "connect", "trade", "housing", "events", "services", "help"};

    public static class ActivityRequestCodes{
        public static final int LOGIN = 1;
        public static final int NEW_POST_WIZARD = 2;
        public static final int NEARBY_POST_DETAILS = 3;
        public static final int NEW_POST_PHOTO = 4;
        public static final int REGISTER = 5;
        public static final int PROFILE = 6;


    }
    public  static class PostTypes {
        public static final String EVENTS = "events";
        public static final String DATING = "dating";
        public static final String SALES = "sales";
    }
}
