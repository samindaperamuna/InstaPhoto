package com.softdev.instaphoto.Configuration;

import android.app.Application;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.softdev.instaphoto.Dataset.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class AppHandler extends Application{
    public static final String TAG = AppHandler.class.getSimpleName();
    static AppHandler mInstance;
    RequestQueue mRequestQueue;
    DataStorage dStorage;
    DatabaseHandler dbHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized AppHandler getInstance() {
        return mInstance;
    }

    public DataStorage getDataManager() {
        if (dStorage == null) {
            dStorage = new DataStorage(this);
        }
        return dStorage;
    }

    public DatabaseHandler getDBHandler() {
        if (dbHandler == null) {
            dbHandler = new DatabaseHandler(this);
        }
        return dbHandler;
    }

    public Map<String, String> getAuthorization() {
        if (dStorage == null) {
            getDataManager();
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", dStorage.getString("api", "null"));
        return headers;
    }

    public void updateProfile(User u) {
        getDataManager().setString("id", u.getId());
        getDataManager().setString("username", u.getUsername());
        getDataManager().setString("name", u.getName());
        getDataManager().setString("email", u.getEmail());
        getDataManager().setString("created_At", u.getCreation());
        getDataManager().setString("icon", u.getIcon());
        getDataManager().setString("totalPosts", u.getPosts());
        getDataManager().setString("followers", u.getFollowers());
        getDataManager().setString("following", u.getFollowing());
        getDataManager().setString("bio", u.getBio());
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public static String getTimestamp(String stamp) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date past = format.parse(stamp);
            Date now = new Date();
            final long cr = now.getTime() - past.getTime();
            if (cr < MINUTE_MILLIS) {
                return "Just a second ago";
            } else if (cr < 2 * MINUTE_MILLIS) {
                return "Just a minute ago";
            } else if (cr < 50 * MINUTE_MILLIS) {
                return cr / MINUTE_MILLIS + " minute ago";
            } else if (cr < 90 * MINUTE_MILLIS) {
                return "1 HOUR AGO";
            } else if (cr < 24 * HOUR_MILLIS) {
                return cr / HOUR_MILLIS + " hour ago";
            } else if (cr < 48 * HOUR_MILLIS) {
                return "1 day ago";
            } else {
                return cr / DAY_MILLIS + " day ago";
            }
        } catch (Exception j) {
            return null;
        }
    }
}
