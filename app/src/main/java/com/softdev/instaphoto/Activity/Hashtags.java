package com.softdev.instaphoto.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.softdev.instaphoto.Adapter.FeedAdapter;
import com.softdev.instaphoto.Adapter.FeedItemAnimator;
import com.softdev.instaphoto.Configuration.AppHandler;
import com.softdev.instaphoto.Configuration.Config;
import com.softdev.instaphoto.Dataset.Feed;
import com.softdev.instaphoto.ItemDivider;
import com.softdev.instaphoto.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Hashtags extends AppCompatActivity implements FeedAdapter.OnFeedItemClickListener {

    Toolbar toolbar;
    RecyclerView recyclerView;
    FeedAdapter feedAdapter;
    ArrayList<Feed> feedItems = new ArrayList<>();
    String strHashtag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hashtags);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        feedAdapter = new FeedAdapter(this, feedItems);
        feedAdapter.setOnFeedItemClickListener(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this) {
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 300;
            }
        };
        recyclerView.setAdapter(feedAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new FeedItemAnimator());

        // Getting parameters
        Bundle param = getIntent().getExtras();
        if (param != null) {
            strHashtag = param.getString("hashtag");
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Search for #" + strHashtag);
            }
        }

        loadFeed();
    }

    public void loadFeed() {
        Log.d("LoadFeed", "load feed");
        StringRequest request = new StringRequest(Request.Method.GET, Config.LOAD_HASHTAG.replace(":hashtag", strHashtag), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        feedItems.clear();
                        JSONArray feeds = obj.getJSONArray("feeds");
                        if (feeds.length() != 0) {
                            for (int i = 0; i < feeds.length(); i++) {
                                JSONObject feed = feeds.getJSONObject(i);
                                Feed f = new Feed();
                                f.setUser_id(feed.getString("user_id"));
                                f.setPostId(feed.getString("postId"));
                                f.setUsername(feed.getString("username"));
                                f.setName(feed.getString("name"));
                                f.setIcon(feed.getString("icon"));
                                f.setLiked(feed.getInt("isLiked") == 1);
                                f.setDescription(feed.getString("description"));
                                f.setCreation(feed.getString("creation"));
                                f.setType(feed.getString("type"));
                                f.setComments(feed.getString("comments"));
                                f.setLikes(feed.getString("likes"));
                                f.setContent(feed.getString("content"));
                                f.setCommentsEnable(feed.getInt("isAllowComments") == 1);
                                f.setPublicCommentsAllowed(feed.getInt("isPublicComments") == 1);
                                f.setFollowing(feed.getInt("isFollowing") == 1);
                                feedItems.add(f);
                            }
                            feedAdapter.updateItems(true);
                        } else {
                            Log.d("Loadfeed", "no items");
                        }

                        Log.d("LoadFeed", "load feed1");
                    } else {
                        Toast.makeText(Hashtags.this, "Couldn't load", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException ex) {
                    Log.e("Hashtags", "Unexpected error: " + ex.getMessage());
                    Toast.makeText(Hashtags.this, "Couldn't load", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Hashtags", "Unexpected error: " + error.getMessage());
                Toast.makeText(Hashtags.this, "Couldn't load", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return AppHandler.getInstance().getAuthorization();
            }
        };
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        request.setRetryPolicy(policy);
        AppHandler.getInstance().addToRequestQueue(request);
    }

    @Override
    public void onCommentsClick(View v, int position) {
        final Intent intent = new Intent(this, Comments.class);
        Feed f = feedItems.get(position);
        intent.putExtra("postId", f.getPostId());
        intent.putExtra("poster", f.getName());
        intent.putExtra("description", f.getDescription());
        intent.putExtra("username", f.getUsername());
        intent.putExtra("creation", f.getCreation());
        intent.putExtra("icon", f.getIcon());
        intent.putExtra("isDisabled", f.isCommentsEnable());
        intent.putExtra("isPublicAllowed", f.isPublicCommentsAllowed());
        intent.putExtra("isFollowing", f.isFollowing());
        int[] startingLocation = new int[2];
        v.getLocationOnScreen(startingLocation);
        intent.putExtra(Config.ARG_DRAWING_START_LOCATION, startingLocation[1]);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public void onMoreClick(View v, int position) {

    }

    @Override
    public void onProfileClick(View v, int position) {
        int[] startingLocation = new int[2];
        v.getLocationOnScreen(startingLocation);
        startingLocation[0] += v.getWidth() / 2;
        UserProfile.startUserProfileFromLocation(startingLocation, this, feedItems.get(position).getUsername(), feedItems.get(position).getName());
        overridePendingTransition(0, 0);
    }

    @Override
    public void onLikeClick(View v, final int position, final int action) {
        StringRequest request = new StringRequest(Request.Method.PUT, Config.UPDATE_LIKE.replace(":id", feedItems.get(position).getPostId()), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        feedItems.get(position).setLiked(action == 1);
                    } else {
                        Log.e("Home", "Server response: " + obj.getString("code"));
                        feedAdapter.notifyItemChanged(position);
                        Toast.makeText(Hashtags.this, "Your request was not proceed", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException ex) {
                    Log.e("Home", "Unexpected error: " + ex.getMessage());
                    feedAdapter.notifyItemChanged(position);
                    Toast.makeText(Hashtags.this, "Your request was not proceed", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Home", "Unexpected error: " + error.getMessage());
                feedAdapter.notifyItemChanged(position);
                Toast.makeText(Hashtags.this, "Your request was not proceed", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("action", String.valueOf(action));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return AppHandler.getInstance().getAuthorization();
            }
        };
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        request.setRetryPolicy(policy);
        AppHandler.getInstance().addToRequestQueue(request);
    }

    @Override
    public void onLikesClick(View v, int position) {
        final Intent intent = new Intent(this, Likes.class);
        Feed f = feedItems.get(position);
        intent.putExtra("postId", f.getPostId());
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public void onHashTagPressed(String hashTag) {
        // Nothing
    }
}
