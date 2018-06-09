package com.softdev.instaphoto.Configuration;

public class Config {

    // Response / Error codes
    public static final int UNKNOWN_ERROR = 404;
    public static final int USER_ALREADY_EXISTS = 3;
    public static final int USER_INVALID = 4;
    public static final int EMAIL_INVALID = 5;
    public static final int PASSWORD_INCORRECT = 6;

    // Server Config
    private static final String BASE_URI = "http://192.168.1.103/instaphoto-api/index.php";
    public static final String REGISTER = BASE_URI + "/user/register";
    public static final String LOGIN = BASE_URI + "/user/login";
    public static final String LOAD_MY_FEED = BASE_URI + "/user/data";
    public static final String LOAD_FEED = BASE_URI + "/user/feed/";
    public static final String LOAD_POPULAR_FEED = BASE_URI + "/popular/feed/:from";
    public static final String LOAD_COMMENTS = BASE_URI + "/feed/comments/";
    public static final String LOAD_LIKES = BASE_URI + "/feed/likes/";
    public static final String UPDATE_LIKE = BASE_URI + "/post/like/:id";
    public static final String ADD_COMMENT = BASE_URI + "/post/comment/:id";
    public static final String DELETE_COMMENT = BASE_URI + "/delete/comment/:postId";
    public static final String DELETE_POST = BASE_URI + "/delete/post/:postId";
    public static final String SEARCH_PEOPLE = BASE_URI + "/users/directory/:toFind";
    public static final String LOAD_FOLLOWERS = BASE_URI + "/user/followers";
    public static final String LOAD_FOLLOWING = BASE_URI + "/user/following";
    public static final String GET_USER = BASE_URI + "/user/info/:user";
    public static final String PUBLISH_PHOTO = BASE_URI + "/feed/publish";
    public static final String LOAD_HASHTAG = BASE_URI + "/hashtag/feed/:hashtag";
    public static final String CHECK_HASHTAG = BASE_URI + "/hashtag/:hashtag";
    public static final String FOLLOW_USER = BASE_URI + "/user/follow/:id";
    public static final String UNFOLLOW_USER = BASE_URI + "/user/unfollow/:id";
    public static final String BLOCK_USER = BASE_URI + "/user/block/:id";
    public static final String UNBLOCK_USER = BASE_URI + "/user/unblock/:id";
    public static final String LOAD_BLOCK_LIST = BASE_URI + "/user/blockList";
    public static final String LOAD_NOTIFICATIONS = BASE_URI + "/user/notifications/:from";
    public static final String DELETE_ACCOUNT = BASE_URI + "/account/delete";
    public static final String UPDATE_PROFILE = BASE_URI + "/account/update";

    // In-app Config
    public static final String ARG_DRAWING_START_LOCATION = "arg_drawing_start_location";
    public static final String ACTION_LIKE_BUTTON_CLICKED = "action_like_button_button";
    public static final String ACTION_LIKE_IMAGE_CLICKED = "action_like_image_button";
    public static final int FEED_TYPE_DEFAULT = 1;
    public static final int ANIM_DURATION_TOOLBAR = 300;
}
