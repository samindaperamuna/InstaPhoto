package com.softdev.instaphoto.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.softdev.instaphoto.Adapter.LikesAdapter;
import com.softdev.instaphoto.Configuration.AppHandler;
import com.softdev.instaphoto.Configuration.Config;
import com.softdev.instaphoto.Dataset.Like;
import com.softdev.instaphoto.ItemDivider;
import com.softdev.instaphoto.R;
import com.softdev.instaphoto.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

public class Likes extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    ArrayList<Like> likesList;
    String postId;
    LinearLayout mainLayout;
    LikesAdapter likesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_likes);

        // Binding Views
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mainLayout = (LinearLayout) findViewById(R.id.layoutMain);
        likesList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        // Getting parameters
        Bundle param = getIntent().getExtras();
        if (param != null) {
            postId = param.getString("postId");
        }

        // Setting up likes and adapter
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this) {
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 300;
            }
        };
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        likesAdapter = new LikesAdapter(this, likesList);
        recyclerView.setAdapter(likesAdapter);
        recyclerView.addItemDecoration(new ItemDivider(this));
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    likesAdapter.setAnimationsLocked(true);
                }
            }
        });

        likesAdapter.updateItems();
        LoadLikes();
    }

    @Override
    public void onBackPressed() {
        ViewCompat.setElevation(toolbar, 0);
        mainLayout.animate()
                .translationY(Utils.getScreenHeight(this))
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Likes.super.onBackPressed();
                        overridePendingTransition(0, 0);
                    }
                })
                .start();
    }

    private void LoadLikes() {
        StringRequest request = new StringRequest(Request.Method.GET, Config.LOAD_LIKES + "0?postId="+postId, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        JSONArray comments = obj.getJSONArray("likes");
                        if (comments.length() != 0) {
                            for (int i = 0; i < comments.length(); i++) {
                                JSONObject like = comments.getJSONObject(i);
                                Like l = new Like();
                                l.setUsername(like.getString("username"));
                                l.setCreation(like.getString("creation"));
                                l.setIcon(like.getString("icon"));
                                l.setName(like.getString("name"));
                                l.setPostid(like.getString("post_id"));
                                l.setFollowed(like.getInt("isFollowed") == 1);
                                likesList.add(l);
                            }
                            likesAdapter.updateItems();
                        }
                    } else {
                        Toast.makeText(Likes.this, "Couldn't load", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException ex) {
                    Log.e("Likes", "Unexpected error: " + ex.getMessage());
                    Toast.makeText(Likes.this, "Couldn't load", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Likes", "Unexpected error: " + error.getMessage());
                Toast.makeText(Likes.this, "Couldn't load", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return AppHandler.getInstance().getAuthorization();
            }
        };
        AppHandler.getInstance().addToRequestQueue(request);
    }
}
