package com.softdev.instaphoto.Fragment;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.softdev.instaphoto.Activity.UserProfile;
import com.softdev.instaphoto.Adapter.FollowersAdapter;
import com.softdev.instaphoto.Configuration.AppHandler;
import com.softdev.instaphoto.Configuration.Config;
import com.softdev.instaphoto.Dataset.User;
import com.softdev.instaphoto.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Followers extends Fragment implements TabLayout.OnTabSelectedListener, FollowersAdapter.OnFollowersItemClickListener {

    TabLayout tabLayout;
    RecyclerView recyclerView;
    ArrayList<User> followingList = new ArrayList<>();
    ArrayList<User> followersList = new ArrayList<>();
    ProgressBar progressBar;
    TextView txtStatus;
    boolean isFollowingView = false;
    FollowersAdapter fAdapter;

    public Followers() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_followers, container, false);
        tabLayout = (TabLayout) v.findViewById(R.id.tabLayout);
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        txtStatus = (TextView) v.findViewById(R.id.txtStatus);
        txtStatus.setVisibility(View.GONE);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        followersList = new ArrayList<>();
        followingList = new ArrayList<>();

        // Setting up tabs
        tabLayout.addTab(tabLayout.newTab().setText("FOLLOWERS"),true);
        tabLayout.addTab(tabLayout.newTab().setText("FOLLOWING"));
        tabLayout.addOnTabSelectedListener(this);
        showFollowersList();
        return v;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if(tab.getPosition()==0){
            showFollowersList();
        }else if(tab.getPosition()==1){
            showFollowingList();
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {}
    @Override
    public void onTabReselected(TabLayout.Tab tab) {}

    private void showFollowersList(){
        isFollowingView = false;
        fAdapter = new FollowersAdapter(getActivity(), followersList, followingList, isFollowingView);
        fAdapter.setOnFollowersItemClickListener(this);
        recyclerView.setAdapter(fAdapter);
        if (followersList.size() < 1) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            fAdapter.setAnimationsLocked(false);
        } else {
            fAdapter.setAnimationsLocked(true);
        }
        LoadFollowers();
    }

    private void showFollowingList(){
        isFollowingView = true;
        fAdapter = new FollowersAdapter(getActivity(), followersList, followingList, isFollowingView);
        fAdapter.setOnFollowersItemClickListener(this);
        recyclerView.setAdapter(fAdapter);
        if (followingList.size() < 1) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            fAdapter.setAnimationsLocked(false);
        } else {
            fAdapter.setAnimationsLocked(true);
        }
        LoadFollowings();
    }

    @Override
    public void onItemClick(View v, int position) {
        int[] startingLocation = new int[2];
        v.getLocationOnScreen(startingLocation);
        startingLocation[0] += v.getWidth() / 2;
        UserProfile.startUserProfileFromLocation(startingLocation, getActivity(), isFollowingView ? followingList.get(position).getUsername() : followersList.get(position).getUsername(), isFollowingView ? followingList.get(position).getUsername() : followersList.get(position).getName());
        getActivity().overridePendingTransition(0, 0);
    }

    private void LoadFollowers() {
        StringRequest request = new StringRequest(Request.Method.GET, Config.LOAD_FOLLOWERS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        followersList.clear();
                        JSONArray followers = obj.getJSONArray("followers");
                        if (followers.length() != 0) {
                            for (int i = 0; i < followers.length(); i++) {
                                JSONObject follower = followers.getJSONObject(i);
                                User u = new User();
                                u.setId(follower.getString("id"));
                                u.setUsername(follower.getString("username"));
                                u.setName(follower.getString("name"));
                                u.setIcon(follower.getString("icon"));
                                followersList.add(u);
                            }
                            txtStatus.setVisibility(View.GONE);
                            progressBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            fAdapter.updateItems();
                        } else {
                            txtStatus.setText("Aw! You don't have any followers.");
                            txtStatus.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.GONE);
                        }
                    }
                } catch (JSONException ex) {
                    Log.e("Followers", "Unexpected error: " + ex.getMessage());
                    Toast.makeText(getActivity(), "Couldn't refresh", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Followers", "Unexpected error: " + error.getMessage());
                Toast.makeText(getActivity(), "Couldn't refresh", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return AppHandler.getInstance().getAuthorization();
            }
        };
        AppHandler.getInstance().addToRequestQueue(request);
    }

    private void LoadFollowings() {
        StringRequest request = new StringRequest(Request.Method.GET, Config.LOAD_FOLLOWING, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        followingList.clear();
                        JSONArray followings = obj.getJSONArray("following");
                        if (followings.length() != 0) {
                            for (int i = 0; i < followings.length(); i++) {
                                JSONObject following = followings.getJSONObject(i);
                                User u = new User();
                                u.setId(following.getString("id"));
                                u.setUsername(following.getString("username"));
                                u.setName(following.getString("name"));
                                u.setIcon(following.getString("icon"));
                                followingList.add(u);
                            }
                            txtStatus.setVisibility(View.GONE);
                            progressBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            fAdapter.updateItems();
                        } else {
                            txtStatus.setText("You are not following anyone.");
                            txtStatus.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.GONE);
                        }
                    }
                } catch (JSONException ex) {
                    Log.e("Followers", "Unexpected error: " + ex.getMessage());
                    Toast.makeText(getActivity(), "Couldn't refresh", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Followers", "Unexpected error: " + error.getMessage());
                Toast.makeText(getActivity(), "Couldn't refresh", Toast.LENGTH_SHORT).show();
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
