package com.softdev.instaphoto.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.softdev.instaphoto.Adapter.FollowersAdapter;
import com.softdev.instaphoto.Configuration.AppHandler;
import com.softdev.instaphoto.Configuration.Config;
import com.softdev.instaphoto.Dataset.User;
import com.softdev.instaphoto.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Following extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    ArrayList<User> followingList = new ArrayList<>();
    String strUsername;
    View mainLayout, emptyList;
    FollowersAdapter fAdapter;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mainLayout = findViewById(R.id.layoutMain);
        emptyList = findViewById(R.id.emptyList);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        Bundle b = getIntent().getExtras();
        if (b != null) {
            strUsername = b.getString("id");
        } else {
            finish();
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this) {
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 300;
            }
        };
        recyclerView.setLayoutManager(linearLayoutManager);
        fAdapter = new FollowersAdapter(this, followingList, followingList, false);
        recyclerView.setAdapter(fAdapter);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    fAdapter.setAnimationsLocked(true);
                }
            }
        });
        loadFollowing();
        emptyList.setVisibility(View.GONE);
    }

    private void loadFollowing() {
        StringRequest request = new StringRequest(Request.Method.GET, Config.LOAD_FOLLOWING + "/:user".replace(":user", strUsername), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        followingList.clear();
                        JSONArray followers = obj.getJSONArray("following");
                        if (followers.length() != 0) {
                            for (int i = 0; i < followers.length(); i++) {
                                JSONObject follower = followers.getJSONObject(i);
                                User u = new User();
                                u.setId(follower.getString("id"));
                                u.setUsername(follower.getString("username"));
                                u.setName(follower.getString("name"));
                                u.setIcon(follower.getString("icon"));
                                followingList.add(u);
                            }
                            progressBar.setVisibility(View.GONE);
                            emptyList.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            fAdapter.updateItems();
                        } else {
                            recyclerView.setVisibility(View.GONE);
                            progressBar.setVisibility(View.GONE);
                            emptyList.setVisibility(View.VISIBLE);
                        }
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                        emptyList.setVisibility(View.VISIBLE);
                        Toast.makeText(Following.this, "Unexpected error occurred while retrieving followers list.", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException ex) {
                    Log.e("Following", "Unexpected error: " + ex.getMessage());
                    Toast.makeText(Following.this, "Couldn't refresh", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Following", "Unexpected error: " + error.getMessage());
                Toast.makeText(Following.this, "Couldn't refresh", Toast.LENGTH_SHORT).show();
            }
        });
        AppHandler.getInstance().addToRequestQueue(request);
    }
}
