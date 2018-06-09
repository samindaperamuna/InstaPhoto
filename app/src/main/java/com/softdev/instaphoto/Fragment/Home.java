package com.softdev.instaphoto.Fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.softdev.instaphoto.Activity.Comments;
import com.softdev.instaphoto.Activity.Hashtags;
import com.softdev.instaphoto.Activity.Likes;
import com.softdev.instaphoto.Activity.Notifications;
import com.softdev.instaphoto.Activity.UserProfile;
import com.softdev.instaphoto.Adapter.FeedAdapter;
import com.softdev.instaphoto.Adapter.FeedItemAnimator;
import com.softdev.instaphoto.Configuration.AppHandler;
import com.softdev.instaphoto.Configuration.Config;
import com.softdev.instaphoto.Dataset.Feed;
import com.softdev.instaphoto.Dataset.User;
import com.softdev.instaphoto.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Home extends Fragment implements FeedAdapter.OnFeedItemClickListener {

    View v;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    ArrayList<Feed> feedItems;
    FeedAdapter feedAdapter;
    LinearLayout emptyFeedView;
    SwipeRefreshLayout swipeRefreshLayout;
    boolean isRefreshing = false;
    boolean isFinalList = false;
    public Home() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_home, container, false);
        feedItems = new ArrayList<>();

        setHasOptionsMenu(true);
        // Binding Views
        feedAdapter = new FeedAdapter(getContext(), feedItems);
        feedAdapter.setOnFeedItemClickListener(this);
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        emptyFeedView = (LinearLayout) v.findViewById(R.id.empty_Feed);
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipeRefreshLayout);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext()) {
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 300;
            }
        };
        recyclerView.setAdapter(feedAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new FeedItemAnimator());
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int totalItemCount = linearLayoutManager.getItemCount();
                    int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if (totalItemCount <= (lastVisibleItem + 1)) {
                        if (!isRefreshing && !isFinalList) {
                            swipeRefreshLayout.setRefreshing(true);
                            updateFeed(false);
                            isRefreshing = true;
                        }
                        Log.e("Home", "lastScroll isRefreshing: " + isRefreshing + " isFinalList: " + isFinalList);
                    }
                }
            }
        });

        // Loading Feed
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccent, R.color.colorAccentLight);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isRefreshing) {
                    loadFeed();
                }
            }
        });
        //feedItems.addAll(AppHandler.getInstance().getDBHandler().getFeeds());
        loadFeed();
        return v;
    }

    @Override
    public void onCommentsClick(View v, int position) {
        final Intent intent = new Intent(getContext(), Comments.class);
        Feed f = feedItems.get(position);
        intent.putExtra("postId", f.getPostId());
        intent.putExtra("poster", f.getName());
        intent.putExtra("description", f.getDescription());
        intent.putExtra("username", f.getUsername());
        intent.putExtra("creation", f.getCreation());
        intent.putExtra("icon", f.getIcon());
        intent.putExtra("isDisabled", f.isCommentsEnable());
        int[] startingLocation = new int[2];
        v.getLocationOnScreen(startingLocation);
        intent.putExtra(Config.ARG_DRAWING_START_LOCATION, startingLocation[1]);
        startActivity(intent);
        getActivity().overridePendingTransition(0, 0);
    }

    @Override
    public void onMoreClick(View v, int position) {

    }

    @Override
    public void onProfileClick(View v, int position) {
        int[] startingLocation = new int[2];
        v.getLocationOnScreen(startingLocation);
        startingLocation[0] += v.getWidth() / 2;
        UserProfile.startUserProfileFromLocation(startingLocation, getActivity(), feedItems.get(position).getUsername(), feedItems.get(position).getName());
        getActivity().overridePendingTransition(0, 0);
    }

    @Override
    public void onLikesClick(View v, int position) {
        final Intent intent = new Intent(getContext(), Likes.class);
        Feed f = feedItems.get(position);
        intent.putExtra("postId", f.getPostId());
        startActivity(intent);
        getActivity().overridePendingTransition(0, 0);
    }

    @Override
    public void onHashTagPressed(String hashTag) {
        Intent i = new Intent(getActivity(), Hashtags.class);
        i.putExtra("hashtag", hashTag);
        startActivity(i);
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
                        Toast.makeText(getActivity(), "Your request was not proceed", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException ex) {
                    Log.e("Home", "Unexpected error: " + ex.getMessage());
                    feedAdapter.notifyItemChanged(position);
                    Toast.makeText(getActivity(), "Your request was not proceed", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Home", "Unexpected error: " + error.getMessage());
                feedAdapter.notifyItemChanged(position);
                Toast.makeText(getActivity(), "Your request was not proceed", Toast.LENGTH_SHORT).show();
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

    public void loadFeed() {
        if (feedItems.size() > 0) {
            startContentAnimation();
            swipeRefreshLayout.setRefreshing(true);
            updateFeed(true);
            emptyFeedView.setVisibility(feedAdapter.getItemCount() > 0 ? View.INVISIBLE : View.VISIBLE);
        } else {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
            updateFeed(true);
        }
    }

    public void updateFeed(final boolean fromStart) {
        isRefreshing = true;
        StringRequest request = new StringRequest(Request.Method.GET, Config.LOAD_FEED + (!fromStart ? feedItems.size() : 0), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    if (fromStart) {
                        feedItems.clear();
                    }
                    JSONObject obj = new JSONObject(response);
                    swipeRefreshLayout.setRefreshing(false);
                    isRefreshing = false;
                    if (!obj.getBoolean("error")) {
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
                                if (!feedItems.contains(f)) {
                                    feedItems.add(f);
                                }
                            }
                            isFinalList = false;
                            progressBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            feedAdapter.updateItems(true);
                            emptyFeedView.setVisibility(feedAdapter.getItemCount() > 0 ? View.INVISIBLE : View.VISIBLE);
                        } else {
                            isFinalList = true;
                            if (feedAdapter.getItemCount() < 1) {
                                emptyFeedView.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    } else {
                        Toast.makeText(getActivity(), "Couldn't refresh", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException ex) {
                    swipeRefreshLayout.setRefreshing(false);
                    isRefreshing = false;
                    Log.e("Home", "Unexpected error: " + ex.getMessage());
                    Toast.makeText(getActivity(), "Couldn't refresh", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                swipeRefreshLayout.setRefreshing(false);
                isRefreshing = false;
                Log.e("Home", "Unexpected error: " + error.getMessage());
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

    private void startContentAnimation() {
        feedAdapter.updateItems(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_notification) {
            int[] startingLocation = new int[2];
            v.getLocationOnScreen(startingLocation);
            startingLocation[0] += v.getWidth() / 2;
            Notifications.startNotificationsFromLocation(startingLocation, getActivity());
            getActivity().overridePendingTransition(0, 0);
        }
        return super.onOptionsItemSelected(item);
    }
}
